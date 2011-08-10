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

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.util.Modules;

/**
 * Helper class for creating sub-injectors to the current injector.
 * 
 * @author Kasper Sørensen
 */
public final class InjectorBuilder {

	private final List<Class<?>> _inheritedClassBindings;
	private final AdHocModule _adHocModule;
	private final DCModule _parentModule;
	private final Injector _parentInjector;

	@Inject
	protected InjectorBuilder(DCModule parentModule, Injector injector) {
		_parentModule = parentModule;
		_parentInjector = injector;
		_adHocModule = new AdHocModule();
		_inheritedClassBindings = Arrays.<Class<?>> asList(AnalysisJobBuilderWindow.class, Datastore.class);
	}

	public InjectorBuilder with(Class<?> bindingClass, Object providerOrInstance) {
		_adHocModule.bind(bindingClass, providerOrInstance);
		return this;
	}

	public Injector createInjector() {
		for (Class<?> inheritedClass : _inheritedClassBindings) {
			Key<?> key = Key.get(inheritedClass);
			Binding<?> binding = _parentInjector.getExistingBinding(key);
			if (binding != null) {
				if (!_adHocModule.hasBindingFor(key)) {
					// Bind entry if not already bound in adhoc module!!!
					_adHocModule.bind(key, binding.getProvider());
				}
			}
		}

		Module module = Modules.override(_parentModule).with(_adHocModule);
		return Guice.createInjector(module);
	}
}
