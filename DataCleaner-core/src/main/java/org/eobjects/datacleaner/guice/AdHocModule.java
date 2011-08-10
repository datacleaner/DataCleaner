/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.guice;

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
import com.google.inject.util.Providers;

/**
 * Module with ad hoc variables for limited scoping. Useful module to use as an
 * argument to {@link Injector#createChildInjector(Module...)};
 * 
 * @author Kasper Sørensen
 */
final class AdHocModule implements Module {

	private static final Logger logger = LoggerFactory.getLogger(AdHocModule.class);

	private final Map<Key<?>, Object> _bindings;

	public AdHocModule() {
		_bindings = new HashMap<Key<?>, Object>();
	}

	public <E> void bind(Class<?> bindingClass, Object providerOrInstance) {
		bind(Key.get(bindingClass), providerOrInstance);
	}

	public <E> void bind(Key<?> bindingKey, Object providerOrInstance) {
		if (providerOrInstance == null) {
			providerOrInstance = Providers.of(null);
		}
		_bindings.put(bindingKey, providerOrInstance);
	}

	public boolean hasBindingFor(Key<?> bindingKey) {
		return _bindings.containsKey(bindingKey);
	}

	public boolean hasBindingFor(Class<?> bindingClass) {
		return hasBindingFor(Key.get(bindingClass));
	}

	@Override
	public void configure(Binder binder) {
		Set<Entry<Key<?>, Object>> entrySet = _bindings.entrySet();
		for (Entry<Key<?>, Object> entry : entrySet) {
			@SuppressWarnings("unchecked")
			Key<Object> bindingKey = (Key<Object>) entry.getKey();

			Object providerOrInstance = entry.getValue();

			logger.info("Binding ad-hoc dependency for {}: {}", bindingKey, providerOrInstance);

			if (providerOrInstance instanceof Provider) {
				Provider<?> provider = (Provider<?>) providerOrInstance;
				com.google.inject.Provider<?> guiceProvider = Providers.guicify(provider);
				binder.bind(bindingKey).toProvider(guiceProvider);
			} else if (providerOrInstance instanceof com.google.inject.Provider) {
				com.google.inject.Provider<?> guiceProvider = (com.google.inject.Provider<?>) providerOrInstance;
				binder.bind(bindingKey).toProvider(guiceProvider);
			} else {
				binder.bind(bindingKey).toInstance(providerOrInstance);
			}
		}
	}
}
