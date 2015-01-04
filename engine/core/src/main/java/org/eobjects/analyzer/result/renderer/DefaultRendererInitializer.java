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
package org.eobjects.analyzer.result.renderer;

import java.util.Set;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.configuration.InjectionPoint;
import org.eobjects.analyzer.descriptors.ProvidedPropertyDescriptor;
import org.eobjects.analyzer.descriptors.RendererBeanDescriptor;
import org.eobjects.analyzer.lifecycle.PropertyInjectionPoint;

/**
 * Default renderer initializer which uses an {@link InjectionManager} to
 * initialize renderers.
 */
public class DefaultRendererInitializer implements RendererInitializer {

    private final InjectionManager _injectionManager;

    public DefaultRendererInitializer(AnalyzerBeansConfiguration configuration) {
        this(configuration.getInjectionManager(null));
    }

    public DefaultRendererInitializer(InjectionManager injectionManager) {
        _injectionManager = injectionManager;
    }

    @Override
    public void initialize(RendererBeanDescriptor<?> descriptor, Renderer<?, ?> renderer) {
        Set<ProvidedPropertyDescriptor> providedProperties = descriptor.getProvidedProperties();
        for (ProvidedPropertyDescriptor providedPropertyDescriptor : providedProperties) {
            InjectionPoint<?> injectionPoint = new PropertyInjectionPoint(providedPropertyDescriptor, renderer);
            Object value = _injectionManager.getInstance(injectionPoint);
            providedPropertyDescriptor.setValue(renderer, value);
        }
    }

}
