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
package org.datacleaner.widgets.properties;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AbstractBeanJobBuilder;

import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * Represents a factory for {@link PropertyWidget}s. An implementation is expected to be able to deliver default widgets.
 */
public interface PropertyWidgetFactory {

    /**
     * Convenient type literal that can be used with Guice's binding mechanism
     * to bind to the {@link AbstractBeanJobBuilder} argument of this class's
     * constructor.
     */
    public static final TypeLiteral<AbstractBeanJobBuilder<?, ?, ?>> TYPELITERAL_BEAN_JOB_BUILDER = new TypeLiteral<AbstractBeanJobBuilder<?, ?, ?>>() {
    };
    
    public PropertyWidgetCollection getPropertyWidgetCollection();

    public AbstractBeanJobBuilder<?, ?, ?> getBeanJobBuilder();

    public Injector getInjectorForPropertyWidgets(ConfiguredPropertyDescriptor propertyDescriptor);

    public PropertyWidget<?> create(String propertyName);

    /**
     * Creates (and registers) a widget that fits the specified configured
     * property.
     * 
     * @param propertyDescriptor
     * @return
     */
    public PropertyWidget<?> create(ConfiguredPropertyDescriptor propertyDescriptor);
}
