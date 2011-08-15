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

import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.configuration.InjectionPoint;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.user.UserPreferences;

/**
 * Wraps a standard {@link InjectionManager} and adds support for certain types
 * of DataCleaner that should be available to eg. Output writing analyzers.
 * 
 * @author Kasper Sørensen
 * 
 */
final class DCInjectionManager implements InjectionManager {

	private final InjectionManager _delegate;
	private final DCModule _module;

	public DCInjectionManager(InjectionManager delegate, DCModule module) {
		_delegate = delegate;
		_module = module;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E getInstance(InjectionPoint<E> injectionPoint) {
		E instance = _delegate.getInstance(injectionPoint);
		if (instance != null) {
			return instance;
		}
		Class<E> baseType = injectionPoint.getBaseType();
		if (baseType == UserPreferences.class) {
			return (E) _module.getUserPreferences();
		} else if (baseType == WindowContext.class) {
			return (E) _module.getWindowContext();
		}
		return null;
	}

}
