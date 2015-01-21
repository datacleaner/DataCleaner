/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.job.tasks;

import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.job.BeanConfiguration;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InitializeTask implements Task {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final LifeCycleHelper _lifeCycleHelper;
	private final ComponentDescriptor<?> _componentDescriptor;
	private final Object _component;
	private final BeanConfiguration _beanConfiguration;

	public InitializeTask(LifeCycleHelper lifeCycleHelper, ComponentDescriptor<?> componentDescriptor, Object component,
			BeanConfiguration beanConfiguration) {
		_lifeCycleHelper = lifeCycleHelper;
		_componentDescriptor = componentDescriptor;
		_component = component;
		_beanConfiguration = beanConfiguration;
	}

	@Override
	public void execute() throws Exception {
		logger.debug("execute()");

		_lifeCycleHelper.assignConfiguredProperties(_componentDescriptor, _component, _beanConfiguration);
		_lifeCycleHelper.assignProvidedProperties(_componentDescriptor, _component);
		_lifeCycleHelper.initialize(_componentDescriptor, _component);
	}

	@Override
	public String toString() {
		return "AssignCallbacksAndInitializeTasks[" + _component + "]";
	}
}
