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
package org.grails.plugins.events.reactor.promise

import grails.async.Promise
import groovy.transform.CompileStatic
import reactor.Environment
import reactor.rx.Promise as P

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
/**
 * Implementation of {@link P} interface for Reactor
 *
 * @author Stephane Maldini
 * @since 2.3
 */
@CompileStatic
class ReactorPromise<T> implements Promise<T> {

		final static String PROMISE_DISPATCHER = 'grailsPromiseDispatcher'

    P<T> internalPromise

    ReactorPromise(P internalPromise) {
        this.internalPromise = internalPromise
    }

    ReactorPromise(Closure<T> callable, Environment environment = null) {
        internalPromise = reactor.core.composable.Promise.<T> from(callable).env(environment).dispatcher(PROMISE_DISPATCHER)
		        .get().flush()
    }

    T get() {
        internalPromise.await()
    }

    T get(long timeout, TimeUnit units) throws Throwable {
        T res = internalPromise.await(timeout, units)
        if (!internalPromise.success) {
            throw new TimeoutException()
        } else {
            res
        }
    }

    Promise leftShift(Closure callable) {
        then callable
    }

    @SuppressWarnings("unchecked")
    Promise<T> onComplete(Closure callable) {
        internalPromise.onSuccess(callable)
        this
    }

    @SuppressWarnings("unchecked")
    Promise<T> onError(Closure callable) {
        internalPromise.onError(callable)
        this
    }

    @SuppressWarnings("unchecked")
    Promise then(Closure callable) {
        new ReactorPromise(internalPromise.then(callable))
    }
}