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

import org.apache.metamodel.util.CollectionUtils;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.EnumerationProvider;
import org.datacleaner.descriptors.EnumerationValue;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.EnumComboBoxListRenderer;

/**
 * @Since 9/15/15
 */
public class SingleRemoteEnumPropertyWidget extends AbstractPropertyWidget<EnumerationValue> {

    private final DCComboBox<EnumerationValue> _comboBox;

    @Inject
    public SingleRemoteEnumPropertyWidget(final ConfiguredPropertyDescriptor propertyDescriptor,
            final ComponentBuilder componentBuilder) {
        super(componentBuilder, propertyDescriptor);

        EnumerationValue[] enumConstants = ((EnumerationProvider) propertyDescriptor).values();

        if (!propertyDescriptor.isRequired()) {
            enumConstants = CollectionUtils.array(new EnumerationValue[] { null }, enumConstants);
        }

        _comboBox = new DCComboBox<>(enumConstants);
        _comboBox.setRenderer(new EnumComboBoxListRenderer());

        final EnumerationValue currentValue = getCurrentValue();
        _comboBox.setSelectedItem(currentValue);

        addComboListener(item -> fireValueChanged());
        add(_comboBox);
    }

    public void addComboListener(final DCComboBox.Listener<EnumerationValue> listener) {
        _comboBox.addListener(listener);
    }

    @Override
    public EnumerationValue getValue() {
        return (EnumerationValue) _comboBox.getSelectedItem();
    }

    @Override
    protected void setValue(final EnumerationValue value) {
        _comboBox.setSelectedItem(value);
    }

}
