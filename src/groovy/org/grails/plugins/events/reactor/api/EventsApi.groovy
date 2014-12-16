/*
 * Copyright (c) 2011-2015 Pivotal Software Inc., Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.plugins.events.reactor.api

import groovy.transform.CompileStatic
import org.apache.log4j.Logger
import reactor.bus.Event
import reactor.bus.EventBus
import reactor.bus.registry.Registration
import reactor.bus.selector.Selector
import reactor.bus.selector.Selectors
import reactor.fn.Consumer
import reactor.groovy.config.GroovyEnvironment
import reactor.groovy.support.ClosureConsumer
import reactor.groovy.support.ClosureEventConsumer
import reactor.rx.Deferred
import reactor.rx.Stream
import reactor.rx.Streams

import java.lang.reflect.UndeclaredThrowableException

/**
 * @author Stephane Maldini
 */
@CompileStatic
class EventsApi {

	private static final log = Logger.getLogger(EventsApi)

	static final String GRAILS_REACTOR = 'grailsEventBus'

	GroovyEnvironment groovyEnvironment
	EventBus appEventBus

	EventBus reactor(String name = null){
		name ? groovyEnvironment[name] : appEventBus
	}

	Stream<?> withStream(Stream<?> s,
	                     @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = WithStream) Closure c) {
		newStream(s, c)
		s.compose()
	}

	Stream<?> withStream(@DelegatesTo(strategy = Closure.DELEGATE_FIRST,
			value = WithStream) Closure c) {

		def deferred = Streams.<?> defer().env(groovyEnvironment.environment()).get()
		def stream = deferred

		newStream(deferred, c)
		stream
	}

	void event(Map args,
	           @DelegatesTo(strategy = Closure.DELEGATE_FIRST,
			           value = ClosureEventConsumer)
	           Closure callback = null) {
		def namespace = args.remove('for') ?: args.remove('namespace')
		def onError = args.remove('onError')
		if(onError && Closure.isAssignableFrom(onError.class)){
			onError = new ClosureConsumer((Closure)onError)
		}
		event(args.remove('key'),
				args.remove('data'),
				(String) namespace,
				(Map) args.remove('params') ?: args,
				callback ? new ClosureEventConsumer(callback) : null,
				(Consumer<Throwable>)onError
		)

	}

	void event(key = null, data = null,
	           @DelegatesTo(strategy = Closure.DELEGATE_FIRST,
			           value = ClosureEventConsumer)
	           Closure callback = null) {
		event(key,
				data,
				null,
				null,
				callback ? new ClosureEventConsumer(callback) : null,
				null
		)
	}


	void event(key, data, String ns, Map params, Consumer<Event> deferred,
	                     Consumer<Throwable> errorConsumer) {

		final Event ev = data && Event.class.isAssignableFrom(data?.class) ? (Event) data :
				new Event(params ? new Event.Headers(params) : null, data, errorConsumer)

		final reactor = ns ? groovyEnvironment[ns] : appEventBus

		if (deferred) {
			final replyTo = Selectors.$()
			ev.setReplyTo(replyTo.t2)

			reactor.on replyTo.t1, deferred
		}

		if (key) {
			if (ev.replyTo)
				reactor.send key, ev
			else
				reactor.notify key, ev
		}else{
				reactor.notify ev
		}
	}

	Registration<Consumer> on(key, @DelegatesTo(strategy = Closure.DELEGATE_FIRST,
			value = ClosureEventConsumer.ReplyDecorator) Closure callback) {
		_on(appEventBus, key, callback)
	}

	Registration<Consumer> on(String namespace, key, @DelegatesTo(strategy = Closure.DELEGATE_FIRST,
			value = ClosureEventConsumer.ReplyDecorator) Closure callback) {
		_on(groovyEnvironment[namespace], key, callback)
	}

	private Registration<Consumer> _on(EventBus reactor, key, Closure callback) {
		if (key instanceof Selector)
			reactor.on key, callback
		else if (key instanceof Class)
			reactor.on Selectors.T((Class)key), callback
		else
			reactor.on Selectors.$(key), callback
	}


	boolean removeConsumers(String ns = null, key) {
		(ns ? groovyEnvironment[ns] : appEventBus).consumerRegistry.unregister(key)
	}

	int countConsumers(String ns = null, key) {
		(ns ? groovyEnvironment[ns] : appEventBus).consumerRegistry.select(key).size()
	}


	private WithStream newStream(Deferred d, Closure c) {
		def withStream = new WithStream(d)
		withStream.appEventBus = appEventBus
		withStream.groovyEnvironment = groovyEnvironment
		c.delegate = withStream
		c.resolveStrategy = Closure.DELEGATE_FIRST
		c.call()
		withStream
	}

	private class WithStream extends EventsApi {
		final Deferred deferred

		WithStream(Stream<?> deferred) {
			this.deferred = deferred
		}

		@Override
		void event(key, data, String ns, Map params, Consumer<Event> _deferred,
		                     Consumer<Throwable> errorConsumer) {
			if(!errorConsumer){
				errorConsumer = new Consumer(){
					@Override
					void accept(throwable) {
						if(UndeclaredThrowableException.class.isAssignableFrom(throwable.class)){
							deferred.broadcastNext(((UndeclaredThrowableException)throwable).cause)
						}else{
							deferred.broadcastNext((Throwable)throwable)
						}
					}
				}
			}
			super.event(key, data, ns, params, new Consumer<Event>() {
				@Override
				void accept(Event o) {
					if (_deferred)
						_deferred << o

					deferred << o.data
				}
			}, errorConsumer)
		}
	}
}
