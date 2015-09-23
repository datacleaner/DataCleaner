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

import java.lang.reflect.Array;
import java.util.Set;

import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.ComponentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AssignConfiguredPropertiesHelper {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public void assignProperties(Object component, ComponentDescriptor<?> descriptor,
            ComponentConfiguration configuration) {
        assignProperties(component, descriptor, configuration, false);
    }

    public void assignProperties(Object component, ComponentDescriptor<?> descriptor,
            ComponentConfiguration configuration, boolean replace) {
        final Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor.getConfiguredProperties();
        for (final ConfiguredPropertyDescriptor property : configuredProperties) {
            Object configuredValue = getValue(property, configuration);
            if (configuredValue == null) {
                if (!replace) {
                    setValue(property, component, null);
                }
            } else {
                if (property.isArray()) {
                    setValue(property, component, configuredValue);
                } else {
                    if (configuredValue.getClass().isArray()) {
                        if (Array.getLength(configuredValue) == 1) {
                            configuredValue = Array.get(configuredValue, 0);
                        } else if (Array.getLength(configuredValue) > 1) {
                            throw new IllegalStateException("Cannot assign an array-value (" + configuredValue
                                    + ") to a non-array property (" + property + ")");
                        } else {
                            configuredValue = null;
                        }
                    }
                    setValue(property, component, configuredValue);
                }
            }
        }
    }

    protected void setValue(ConfiguredPropertyDescriptor property, Object component, Object value) {
        property.setValue(component, value);
    }

    protected Object getValue(ConfiguredPropertyDescriptor propertyDescriptor,
            ComponentConfiguration componentConfiguration) {
        final Object value = componentConfiguration.getProperty(propertyDescriptor);
        logger.debug("Property '{}' in configuration: {}", propertyDescriptor.getName(), value);
        return value;
    }
}
