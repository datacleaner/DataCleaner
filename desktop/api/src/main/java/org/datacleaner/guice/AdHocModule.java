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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Providers;

/**
 * Module with ad hoc variables for limited scoping. Useful module to use as an
 * argument to {@link Injector#createChildInjector(Module...)};
 */
final class AdHocModule implements Module {

    private static final Logger logger = LoggerFactory.getLogger(AdHocModule.class);

    private final Map<TypeLiteral<?>, Object> _bindings;

    public AdHocModule() {
        _bindings = new HashMap<>();
    }

    public <E> void bind(final Class<?> bindingClass, final Object providerOrInstance) {
        bind(Key.get(bindingClass), providerOrInstance);
    }

    public <E> void bind(final Key<?> bindingKey, final Object providerOrInstance) {
        bind(bindingKey.getTypeLiteral(), providerOrInstance);
    }

    public <E> void bind(final TypeLiteral<?> bindingTypeLiteral, Object providerOrInstance) {
        if (providerOrInstance == null) {
            providerOrInstance = Providers.of(null);
        }
        _bindings.put(bindingTypeLiteral, providerOrInstance);
    }

    public boolean hasBindingFor(final TypeLiteral<?> bindingTypeLiteral) {
        return _bindings.containsKey(bindingTypeLiteral);
    }

    public boolean hasBindingFor(final Key<?> bindingKey) {
        return hasBindingFor(bindingKey.getTypeLiteral());
    }

    public boolean hasBindingFor(final Class<?> bindingClass) {
        return hasBindingFor(Key.get(bindingClass));
    }

    @Override
    public void configure(final Binder binder) {
        final Set<Entry<TypeLiteral<?>, Object>> entrySet = _bindings.entrySet();
        for (final Entry<TypeLiteral<?>, Object> entry : entrySet) {
            @SuppressWarnings("unchecked") final TypeLiteral<Object> bindingLiteral =
                    (TypeLiteral<Object>) entry.getKey();

            final Object providerOrInstance = entry.getValue();

            logger.debug("Binding ad-hoc dependency for {}: {}", bindingLiteral, providerOrInstance);

            if (providerOrInstance instanceof Provider) {
                final Provider<?> provider = (Provider<?>) providerOrInstance;
                final com.google.inject.Provider<?> guiceProvider = Providers.guicify(provider);
                binder.bind(bindingLiteral).toProvider(guiceProvider);
            } else if (providerOrInstance instanceof com.google.inject.Provider) {
                final com.google.inject.Provider<?> guiceProvider = (com.google.inject.Provider<?>) providerOrInstance;
                binder.bind(bindingLiteral).toProvider(guiceProvider);
            } else {
                binder.bind(bindingLiteral).toInstance(providerOrInstance);
            }
        }
    }
}
