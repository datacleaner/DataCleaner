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

import javax.inject.Inject;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;

public class DummyPropertyWidget implements PropertyWidget<Object> {

    private final ConfiguredPropertyDescriptor _propertyDescriptor;
    private Object _value;

    @Inject
    protected DummyPropertyWidget(final ConfiguredPropertyDescriptor propertyDescriptor) {
        _propertyDescriptor = propertyDescriptor;
    }

    @Override
    public JComponent getWidget() {
        return new JLabel("Not yet implemented");
    }

    @Override
    public boolean isSet() {
        return _value != null;
    }

    @Override
    public Object getValue() {
        return _value;
    }

    @Override
    public ConfiguredPropertyDescriptor getPropertyDescriptor() {
        return _propertyDescriptor;
    }

    @Override
    public void onValueTouched(final Object value) {
        _value = value;
    }

    @Override
    public void initialize(final Object value) {
    }
}
