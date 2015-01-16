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
package org.datacleaner.job.builder;

import org.datacleaner.api.Configured;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;

/**
 * Exception thrown when a required {@link Configured} property of a component
 * is not set.
 */
public class UnconfiguredConfiguredPropertyException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    private final ComponentBuilder _componentBuilder;
    private final ConfiguredPropertyDescriptor _configuredProperty;

    public UnconfiguredConfiguredPropertyException(ComponentBuilder componentBuilder,
            ConfiguredPropertyDescriptor configuredProperty) {
        _componentBuilder = componentBuilder;
        _configuredProperty = configuredProperty;
    }

    public ConfiguredPropertyDescriptor getConfiguredProperty() {
        return _configuredProperty;
    }

    public ComponentBuilder getComponentBuilder() {
        return _componentBuilder;
    }

    @Override
    public String getMessage() {
        return "Property '" + getConfiguredProperty().getName() + "' is not properly configured (" + _componentBuilder
                + ")";
    }
}
