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
package org.eobjects.analyzer.lifecycle;

import java.util.Collection;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.beans.api.Validate;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.job.BeanConfiguration;
import org.eobjects.analyzer.job.runner.ReferenceDataActivationManager;

/**
 * Utility/convenience class for doing simple lifecycle management and/or
 * mimicing the lifecycle of components lifecycle in a job execution.
 * 
 * 
 */
public final class LifeCycleHelper {

    private final InjectionManager _injectionManager;
    private final ReferenceDataActivationManager _referenceDataActivationManager;
    private boolean _includeNonDistributedTasks;

    /**
     * @param injectionManager
     * 
     * @deprecated use {@link #LifeCycleHelper(InjectionManager, boolean)}
     *             instead
     */
    @Deprecated
    public LifeCycleHelper(InjectionManager injectionManager) {
        this(injectionManager, null, true);
    }

    /**
     * @param injectionManager
     * 
     * @deprecated use
     *             {@link #LifeCycleHelper(InjectionManager, ReferenceDataActivationManager, boolean)}
     *             instead
     */
    @Deprecated
    public LifeCycleHelper(InjectionManager injectionManager,
            ReferenceDataActivationManager referenceDataActivationManager) {
        this(injectionManager, referenceDataActivationManager, true);
    }

    /**
     * 
     * @param injectionManager
     * @param includeNonDistributedTasks
     *            whether or not non-distributed methods (such as
     *            {@link Initialize} or {@link Cloneable} methods that are
     *            marked with distributed=false) should be included or not. On
     *            single-node executions, this will typically be true, on slave
     *            nodes in a cluster, this will typically be false.
     */
    public LifeCycleHelper(InjectionManager injectionManager, boolean includeNonDistributedTasks) {
        this(injectionManager, null, includeNonDistributedTasks);
    }

    /**
     * 
     * @param injectionManager
     * @param referenceDataActivationManager
     * @param includeNonDistributedTasks
     *            whether or not non-distributed methods (such as
     *            {@link Initialize} or {@link Cloneable} methods that are
     *            marked with distributed=false) should be included or not. On
     *            single-node executions, this will typically be true, on slave
     *            nodes in a cluster, this will typically be false.
     */
    public LifeCycleHelper(InjectionManager injectionManager,
            ReferenceDataActivationManager referenceDataActivationManager, boolean includeNonDistributedTasks) {
        _injectionManager = injectionManager;
        _referenceDataActivationManager = referenceDataActivationManager;
        _includeNonDistributedTasks = includeNonDistributedTasks;
    }
    
    public boolean isIncludeNonDistributedTasks() {
        return _includeNonDistributedTasks;
    }
    
    public InjectionManager getInjectionManager() {
        return _injectionManager;
    }
    
    public ReferenceDataActivationManager getReferenceDataActivationManager() {
        return _referenceDataActivationManager;
    }

    /**
     * Assigns/injects {@link Configured} property values to a component.
     * 
     * @param descriptor
     * @param component
     * @param beanConfiguration
     */
    public void assignConfiguredProperties(ComponentDescriptor<?> descriptor, Object component,
            BeanConfiguration beanConfiguration) {
        AssignConfiguredCallback callback = new AssignConfiguredCallback(beanConfiguration,
                _referenceDataActivationManager);
        callback.onEvent(component, descriptor);
    }

    /**
     * Assigns/injects {@link Provided} property values to a component.
     * 
     * @param descriptor
     * @param component
     */
    public void assignProvidedProperties(ComponentDescriptor<?> descriptor, Object component) {
        AssignProvidedCallback callback = new AssignProvidedCallback(_injectionManager);
        callback.onEvent(component, descriptor);
    }

    /**
     * Validates a component using any {@link Validate} methods. This is
     * typically done after
     * {@link #assignProvidedProperties(ComponentDescriptor, Object)} and
     * {@link #assignConfiguredProperties(ComponentDescriptor, Object, BeanConfiguration)}
     * 
     * Usually validation is light-weight, idempotent and quick, as compared to
     * {@link #initialize(ComponentDescriptor, Object, boolean)}.
     * 
     * @param descriptor
     * @param component
     */
    public void validate(ComponentDescriptor<?> descriptor, Object component) {
        InitializeCallback callback = new InitializeCallback(true, false, _includeNonDistributedTasks);
        callback.onEvent(component, descriptor);
    }

    /**
     * Initializes a component before use. This is typically done after
     * {@link #assignProvidedProperties(ComponentDescriptor, Object)} and
     * {@link #assignConfiguredProperties(ComponentDescriptor, Object, BeanConfiguration)}
     * .
     * 
     * This initialization also includes a validation, see
     * {@link #validate(ComponentDescriptor, Object)}.
     * 
     * @param descriptor
     * @param component
     */
    public void initialize(ComponentDescriptor<?> descriptor, Object component) {
        InitializeCallback callback = new InitializeCallback(true, true, _includeNonDistributedTasks);
        callback.onEvent(component, descriptor);
    }

    /**
     * Closes a component after use.
     * 
     * @param descriptor
     * @param component
     */
    public void close(ComponentDescriptor<?> descriptor, Object component, boolean success) {
        CloseCallback callback = new CloseCallback(_includeNonDistributedTasks, success);
        callback.onEvent(component, descriptor);
    }

    /**
     * Closes a component after user.
     * 
     * @param descriptor
     * @param component
     * 
     * @deprecated use {@link #close(ComponentDescriptor, Object, boolean)}
     *             instead.
     */
    @Deprecated
    public void close(ComponentDescriptor<?> descriptor, Object component) {
        close(descriptor, component, true);
    }

    /**
     * Closes all reference data used in this life cycle helper
     */
    public void closeReferenceData() {
        if (_referenceDataActivationManager == null) {
            return;
        }
        final Collection<Object> referenceData = _referenceDataActivationManager.getAllReferenceData();
        for (Object object : referenceData) {
            ComponentDescriptor<? extends Object> descriptor = Descriptors.ofComponent(object.getClass());
            close(descriptor, object, true);
        }
    }

    /**
     * Initializes all reference data used in this life cycle helper
     */
    public void initializeReferenceData() {
        if (_referenceDataActivationManager == null) {
            return;
        }
        final Collection<Object> referenceDataCollection = _referenceDataActivationManager.getAllReferenceData();
        for (Object referenceData : referenceDataCollection) {
            ComponentDescriptor<? extends Object> descriptor = Descriptors.ofComponent(referenceData.getClass());

            assignProvidedProperties(descriptor, referenceData);
            initialize(descriptor, referenceData);
        }
    }
}
