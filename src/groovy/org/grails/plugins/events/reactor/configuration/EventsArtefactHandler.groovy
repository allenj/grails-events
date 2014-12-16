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
package org.grails.plugins.events.reactor.configuration

import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.AbstractGrailsClass
import org.codehaus.groovy.grails.commons.ArtefactHandlerAdapter
import org.codehaus.groovy.grails.commons.GrailsClass

/**
 * @author Stephane Maldini
 */
@CompileStatic
class EventsArtefactHandler extends ArtefactHandlerAdapter {

	public static final String TYPE = "Events";
	public static final String SUFFIX = "Events";

	public EventsArtefactHandler() {
		super(TYPE, EventsClass.class, DefaultEventsClass.class, SUFFIX, true);
	}

	@Override
	public String getPluginName() {
		return "events";
	}
}

@CompileStatic
interface EventsClass extends GrailsClass {}

@CompileStatic
class DefaultEventsClass extends AbstractGrailsClass implements EventsClass {
	DefaultEventsClass(Class<?> clazz) {
		super(clazz, EventsArtefactHandler.SUFFIX)
	}
}