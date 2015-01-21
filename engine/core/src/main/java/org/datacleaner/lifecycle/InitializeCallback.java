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
package org.datacleaner.lifecycle;

import java.util.Set;

import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.InitializeMethodDescriptor;
import org.datacleaner.descriptors.ValidateMethodDescriptor;

/**
 * Life cycle callback for the initialize phase.
 * 
 * 
 */
final class InitializeCallback implements LifeCycleCallback<Object, ComponentDescriptor<?>> {
	
	private final boolean _validate;
	private final boolean _initialize;
    private final boolean _initializeNonDistributed;

	public InitializeCallback(boolean validate, boolean initialize, boolean initializeNonDistributed) {
		_validate = validate;
		_initialize = initialize;
		_initializeNonDistributed = initializeNonDistributed;
	}

	@Override
	public void onEvent(Object component, ComponentDescriptor<?> descriptor) {
		if (_validate) {
			Set<ValidateMethodDescriptor> validateDescriptors = descriptor.getValidateMethods();
			for (ValidateMethodDescriptor validateDescriptor : validateDescriptors) {
				validateDescriptor.validate(component);
			}
		}

		if (_initialize) {
			Set<InitializeMethodDescriptor> initializeDescriptors = descriptor.getInitializeMethods();
			for (InitializeMethodDescriptor initializeDescriptor : initializeDescriptors) {
			    if (_initializeNonDistributed || initializeDescriptor.isDistributed()) {
			        initializeDescriptor.initialize(component);
			    }
			}
		}
	}

}
