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
package org.datacleaner.guice;

import org.datacleaner.api.Component;
import org.datacleaner.job.builder.AbstractBeanJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.datacleaner.widgets.properties.PropertyWidgetFactoryImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;

/**
 * Guice {@link Module} that has specific bindings for a single
 * {@link Component}
 */
final class ComponentBuilderModule extends AbstractModule {

    private final ComponentBuilder _componentBuilder;

    public ComponentBuilderModule(ComponentBuilder componentBuilder) {
        _componentBuilder = componentBuilder;
    }

    @Override
    protected void configure() {
        bind(PropertyWidgetFactory.class).to(PropertyWidgetFactoryImpl.class);
    }

    @Provides
    public ComponentBuilder getComponentBuilder() {
        return _componentBuilder;
    }

    @Provides
    public AbstractBeanJobBuilder<?, ?, ?> getAbstractBeanJobBuilder() {
        return (AbstractBeanJobBuilder<?, ?, ?>) _componentBuilder;
    }
}
