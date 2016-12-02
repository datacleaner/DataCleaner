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

import org.datacleaner.api.Configured;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Validate;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.configuration.InjectionPoint;
import org.datacleaner.descriptors.CloseMethodDescriptor;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.InitializeMethodDescriptor;
import org.datacleaner.descriptors.ProvidedPropertyDescriptor;
import org.datacleaner.descriptors.ValidateMethodDescriptor;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility/convenience class for doing simple lifecycle management and/or
 * mimicing the lifecycle of components lifecycle in a job execution.
 */
public final class LifeCycleHelper {

    private static final Logger logger = LoggerFactory.getLogger(LifeCycleHelper.class);

    private final InjectionManager _injectionManager;
    private final boolean _includeNonDistributedTasks;

    /**
     * @param injectionManager
     *
     * @deprecated use {@link #LifeCycleHelper(InjectionManager, boolean)}
     *             instead
     */
    @Deprecated
    public LifeCycleHelper(final InjectionManager injectionManager) {
        this(injectionManager, true);
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
    public LifeCycleHelper(final InjectionManager injectionManager, final boolean includeNonDistributedTasks) {
        _injectionManager = injectionManager;
        _includeNonDistributedTasks = includeNonDistributedTasks;
    }

    /**
     *
     * @param configuration
     * @param job
     * @param referenceDataActivationManager
     * @param includeNonDistributedTasks
     *            whether or not non-distributed methods (such as
     *            {@link Initialize} or {@link Cloneable} methods that are
     *            marked with distributed=false) should be included or not. On
     *            single-node executions, this will typically be true, on slave
     *            nodes in a cluster, this will typically be false.
     */
    public LifeCycleHelper(final DataCleanerConfiguration configuration, final AnalysisJob job,
            final boolean includeNonDistributedTasks) {
        if (configuration == null) {
            _injectionManager = null;
        } else {
            final InjectionManagerFactory injectionManagerFactory =
                    configuration.getEnvironment().getInjectionManagerFactory();
            if (job == null) {
                _injectionManager = injectionManagerFactory.getInjectionManager(configuration);
            } else {
                _injectionManager = injectionManagerFactory.getInjectionManager(configuration, job);
            }
        }
        _includeNonDistributedTasks = includeNonDistributedTasks;
    }

    public boolean isIncludeNonDistributedTasks() {
        return _includeNonDistributedTasks;
    }

    public InjectionManager getInjectionManager() {
        return _injectionManager;
    }

    /**
     * Assigns/injects {@link Configured} property values to a component.
     *
     * @param descriptor
     * @param component
     * @param componentConfiguration
     */
    public void assignConfiguredProperties(final ComponentDescriptor<?> descriptor, final Object component,
            final ComponentConfiguration componentConfiguration) {
        final AssignConfiguredPropertiesHelper helper = new AssignConfiguredPropertiesHelper();
        helper.assignProperties(component, descriptor, componentConfiguration);
    }

    /**
     * Assigns/injects {@link Provided} property values to a component.
     *
     * @param descriptor
     * @param component
     */
    public void assignProvidedProperties(final ComponentDescriptor<?> descriptor, final Object component) {
        final Set<ProvidedPropertyDescriptor> providedDescriptors = descriptor.getProvidedProperties();
        for (final ProvidedPropertyDescriptor providedDescriptor : providedDescriptors) {

            final InjectionPoint<Object> injectionPoint = new PropertyInjectionPoint(providedDescriptor, component);
            final Object value = _injectionManager.getInstance(injectionPoint);
            providedDescriptor.setValue(component, value);

        }
    }

    /**
     * Validates a component using any {@link Validate} methods. This is
     * typically done after
     * {@link #assignProvidedProperties(ComponentDescriptor, Object)} and
     * {@link #assignConfiguredProperties(ComponentDescriptor, Object, ComponentConfiguration)}
     *
     * Usually validation is light-weight, idempotent and quick, as compared to
     * {@link #initialize(ComponentDescriptor, Object, boolean)}.
     *
     * @param descriptor
     * @param component
     */
    public void validate(final ComponentDescriptor<?> descriptor, final Object component) {
        final Set<ValidateMethodDescriptor> validateDescriptors = descriptor.getValidateMethods();
        for (final ValidateMethodDescriptor validateDescriptor : validateDescriptors) {
            validateDescriptor.validate(component);
        }
    }

    /**
     * Initializes a component before use. This is typically done after
     * {@link #assignProvidedProperties(ComponentDescriptor, Object)} and
     * {@link #assignConfiguredProperties(ComponentDescriptor, Object, ComponentConfiguration)}
     * .
     *
     * This initialization also includes a validation, see
     * {@link #validate(ComponentDescriptor, Object)}.
     *
     * @param descriptor
     * @param component
     */
    public void initialize(final ComponentDescriptor<?> descriptor, final Object component) {
        final Set<InitializeMethodDescriptor> initializeDescriptors = descriptor.getInitializeMethods();
        for (final InitializeMethodDescriptor initializeDescriptor : initializeDescriptors) {
            if (_includeNonDistributedTasks || initializeDescriptor.isDistributed()) {
                initializeDescriptor.initialize(component);
            }
        }
    }

    /**
     * Closes a component after use.
     *
     * @param descriptor
     * @param component
     */
    public void close(final ComponentDescriptor<?> descriptor, final Object component, final boolean success) {
        final Set<CloseMethodDescriptor> closeMethods = descriptor.getCloseMethods();
        for (final CloseMethodDescriptor closeDescriptor : closeMethods) {
            if (_includeNonDistributedTasks || closeDescriptor.isDistributed()) {
                if (success && closeDescriptor.isEnabledOnSuccess()) {
                    closeDescriptor.close(component);
                } else if (!success && closeDescriptor.isEnabledOnFailure()) {
                    closeDescriptor.close(component);
                } else {
                    logger.debug("Omitting close method {} since success={}", closeDescriptor, success);
                }
            }
        }
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
    public void close(final ComponentDescriptor<?> descriptor, final Object component) {
        close(descriptor, component, true);
    }
}
