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
package org.eobjects.datacleaner.widgets.properties;

import javax.swing.JComponent;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;

/**
 * Defines an interface for (wrappers of) widgets that are used to edit
 * configurable properties of analyzers, transformers and filters.
 * 
 * @param <E>
 */
public interface PropertyWidget<E> {

	public void initialize(E value);

	public JComponent getWidget();

	public ConfiguredPropertyDescriptor getPropertyDescriptor();

	/**
	 * Called on a widget if the value it contains is prone to have been changed
	 * by a another party (typically some sort of shortcut in the UI to populate
	 * values or similar).
	 * 
	 * Note that this method will sometimes also be invoked at when the
	 * surrounding environment is not able to determine if it has changed or
	 * not. The property widget should therefore investigate if the incoming
	 * value does in deed differ from the existing.
	 */
	public void onValueTouched(E value);

	public boolean isSet();

	public E getValue();
}
