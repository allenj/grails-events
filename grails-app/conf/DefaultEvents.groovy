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

import org.grails.plugins.events.reactor.api.EventsApi
import org.grails.plugins.events.reactor.promise.ReactorPromise
import reactor.core.config.DispatcherType

doWithReactor = {
	environment {
		defaultDispatcher = "grailsDispatcher"

		dispatcher('grailsDispatcher') {
			type = DispatcherType.RING_BUFFER
			backlog = 512
		}

		if (grailsApplication.metadata['app.grails.version'].startsWith('2.3')) {

			dispatcher(ReactorPromise.PROMISE_DISPATCHER) {
				type = DispatcherType.RING_BUFFER
				backlog = 512
			}

		}
	}

	reactor(EventsApi.GRAILS_REACTOR) {
	}
}