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

import java.util.Collection;

import javax.swing.JComponent;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;

/**
 * A panel which presents multiple property widgets and their labels in a
 * form-like view.
 */
public abstract class PropertyWidgetPanel extends FormPanel {

    private static final long serialVersionUID = 1L;

    public void addProperties(Collection<ConfiguredPropertyDescriptor> properties) {
        for (ConfiguredPropertyDescriptor propertyDescriptor : properties) {
            final PropertyWidget<?> propertyWidget = getPropertyWidget(propertyDescriptor);

            // some properties may not have a PropertyWidget
            if (propertyWidget != null) {
                JComponent component = propertyWidget.getWidget();

                // some properties may have a PropertyWidget implementation that
                // is "invisible", ie. the JComponent is not returned
                if (component != null) {
                    final String propertyName = propertyDescriptor.getName();
                    final String description = propertyDescriptor.getDescription();

                    addFormEntry(propertyName, description, component);
                }
            }
        }
    }


    protected abstract PropertyWidget<?> getPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor);
}
