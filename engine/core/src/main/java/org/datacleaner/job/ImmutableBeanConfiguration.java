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
package org.datacleaner.job;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.metamodel.util.EqualsBuilder;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.PropertyDescriptor;

import com.google.common.collect.ImmutableMap;

/**
 * Default (immutable) implementation of {@link BeanConfiguration}.
 */
public final class ImmutableBeanConfiguration implements BeanConfiguration {

    private static final long serialVersionUID = 1L;

    private final Map<PropertyDescriptor, Object> _properties;
    private final transient Map<PropertyDescriptor, Object> _transientProperties;

    public ImmutableBeanConfiguration(Map<? extends PropertyDescriptor, Object> properties) {
        if (properties == null) {
            _properties = ImmutableMap.of();
            _transientProperties = null;
        } else {
            // separate transient and serializable properties to make sure we
            // can serialize later on
            final Map<PropertyDescriptor, Object> serializableProperties = new HashMap<>();
            final Map<PropertyDescriptor, Object> transientProperties = new HashMap<>();

            // validate contents
            for (final Map.Entry<? extends PropertyDescriptor, Object> entry : properties.entrySet()) {
                final PropertyDescriptor key = entry.getKey();
                final Object value = entry.getValue();
                if (value instanceof Collection) {
                    throw new IllegalArgumentException(
                            "Collection values are not allowed in BeanConfigurations. Violating entry: " + key + " -> "
                                    + value);
                }
                if (value instanceof Serializable) {
                    serializableProperties.put(key, value);
                } else {
                    transientProperties.put(key, value);
                }
            }
            _properties = ImmutableMap.copyOf(serializableProperties);
            _transientProperties = ImmutableMap.copyOf(transientProperties);
        }

    }

    @Override
    public Object getProperty(ConfiguredPropertyDescriptor propertyDescriptor) {
        final Object result = _properties.get(propertyDescriptor);
        if (result == null && _transientProperties != null) {
            return _transientProperties.get(propertyDescriptor);
        }
        return result;
    }

    @Override
    public String toString() {
        return "ImmutableBeanConfiguration[" + _properties + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime + _properties.size() + _properties.keySet().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final ImmutableBeanConfiguration other = (ImmutableBeanConfiguration) obj;

        // since map comparison does not use deep equals for arrays, we need to
        // do this ourselves!

        final Map<PropertyDescriptor, Object> otherProperties = other._properties;
        final Set<PropertyDescriptor> configredProperties = _properties.keySet();
        if (!configredProperties.equals(otherProperties.keySet())) {
            return false;
        }

        for (final PropertyDescriptor propertyDescriptor : configredProperties) {
            final Object value1 = _properties.get(propertyDescriptor);
            final Object value2 = otherProperties.get(propertyDescriptor);
            final boolean equals = EqualsBuilder.equals(value1, value2);
            if (!equals) {
                return false;
            }
        }

        return true;
    }
}
