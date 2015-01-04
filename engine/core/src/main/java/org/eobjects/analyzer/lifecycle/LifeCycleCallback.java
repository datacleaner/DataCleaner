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
package org.eobjects.analyzer.lifecycle;

import org.eobjects.analyzer.descriptors.ComponentDescriptor;

/**
 * Represents a callback method that will execute a step in the lifecycle of a
 * component. A step might be to call any initializing methods, inject
 * properties or to close the component.
 * 
 * 
 * 
 * @param <C>
 *            the component type
 * @param <D>
 *            the descriptor type
 */
public interface LifeCycleCallback<C, D extends ComponentDescriptor<?>> {

	public void onEvent(C component, D descriptor);
}
