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
package org.datacleaner.result.renderer;

import java.util.Set;

import org.datacleaner.api.Renderer;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.configuration.InjectionPoint;
import org.datacleaner.descriptors.ProvidedPropertyDescriptor;
import org.datacleaner.descriptors.RendererBeanDescriptor;
import org.datacleaner.lifecycle.PropertyInjectionPoint;

/**
 * Default renderer initializer which uses an {@link InjectionManager} to
 * initialize renderers.
 */
public class DefaultRendererInitializer implements RendererInitializer {

    private final InjectionManager _injectionManager;

    public DefaultRendererInitializer(final InjectionManager injectionManager) {
        _injectionManager = injectionManager;
    }

    @Override
    public void initialize(final RendererBeanDescriptor<?> descriptor, final Renderer<?, ?> renderer) {
        final Set<ProvidedPropertyDescriptor> providedProperties = descriptor.getProvidedProperties();
        for (final ProvidedPropertyDescriptor providedPropertyDescriptor : providedProperties) {
            final InjectionPoint<?> injectionPoint = new PropertyInjectionPoint(providedPropertyDescriptor, renderer);
            final Object value = _injectionManager.getInstance(injectionPoint);
            providedPropertyDescriptor.setValue(renderer, value);
        }
    }

}
