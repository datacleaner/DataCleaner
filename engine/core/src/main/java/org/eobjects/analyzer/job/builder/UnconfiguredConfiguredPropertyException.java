/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.job.builder;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;

/**
 * Exception thrown when a required {@link Configured} property of a component
 * is not set.
 */
public class UnconfiguredConfiguredPropertyException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    private final AbstractBeanJobBuilder<?, ?, ?> _beanJobBuilder;
    private final ConfiguredPropertyDescriptor _configuredProperty;

    public UnconfiguredConfiguredPropertyException(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor configuredProperty) {
        _beanJobBuilder = beanJobBuilder;
        _configuredProperty = configuredProperty;
    }

    public ConfiguredPropertyDescriptor getConfiguredProperty() {
        return _configuredProperty;
    }

    public AbstractBeanJobBuilder<?, ?, ?> getBeanJobBuilder() {
        return _beanJobBuilder;
    }

    @Override
    public String getMessage() {
        return "Property '" + getConfiguredProperty().getName() + "' is not properly configured (" + _beanJobBuilder
                + ")";
    }
}
