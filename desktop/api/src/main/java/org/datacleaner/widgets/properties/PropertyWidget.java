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

import javax.swing.JComponent;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;

/**
 * Defines an interface for (wrappers of) widgets that are used to edit
 * configurable properties of analyzers, transformers and filters.
 * 
 * @param <E>
 */
public interface PropertyWidget<E> {

    /**
     * Initializes the property widget with the initial value of the property
     * 
     * @param value
     */
    public void initialize(E value);

    /**
     * Gets the visual widget to show on the UI. This may (in special cases)
     * return null if the widget should not be shown, or if it is represented as
     * part of a different part of the UI.
     * 
     * @return
     */
    public JComponent getWidget();

    /**
     * Gets the {@link ConfiguredPropertyDescriptor} that this
     * {@link PropertyWidget} is modelling.
     * 
     * @return
     */
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

    /**
     * Determines if the property is set given the current state in the UI.
     * 
     * @return
     */
    public boolean isSet();

    /**
     * Gets the current value of the property given the current state in the UI.
     * 
     * @return
     */
    public E getValue();
}
