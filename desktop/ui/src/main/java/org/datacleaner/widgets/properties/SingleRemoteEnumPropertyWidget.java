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
import org.datacleaner.descriptors.JsonSchemaConfiguredPropertyDescriptorImpl;
import org.datacleaner.descriptors.RemoteEnumerationValue;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.EnumComboBoxListRenderer;

/**
 * @Since 9/15/15
 */
public class SingleRemoteEnumPropertyWidget extends AbstractPropertyWidget<RemoteEnumerationValue> {

    private final DCComboBox<RemoteEnumerationValue> _comboBox;

    @Inject
    public SingleRemoteEnumPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
                                    ComponentBuilder componentBuilder) {
        super(componentBuilder, propertyDescriptor);

        RemoteEnumerationValue[] enumConstants = ((JsonSchemaConfiguredPropertyDescriptorImpl)propertyDescriptor).getEnumValues();

        if (!propertyDescriptor.isRequired()) {
            enumConstants = CollectionUtils.array(new RemoteEnumerationValue[]{null}, enumConstants);
        }

        _comboBox = new DCComboBox<>(enumConstants);
        _comboBox.setRenderer(new EnumComboBoxListRenderer());

        RemoteEnumerationValue currentValue = getCurrentValue();
        _comboBox.setSelectedItem(currentValue);

        addComboListener(new DCComboBox.Listener<RemoteEnumerationValue>() {
            @Override
            public void onItemSelected(RemoteEnumerationValue item) {
                fireValueChanged();
            }
        });
        add(_comboBox);
    }

    public void addComboListener(DCComboBox.Listener<RemoteEnumerationValue> listener) {
        _comboBox.addListener(listener);
    }

    @Override
    public RemoteEnumerationValue getValue() {
        return (RemoteEnumerationValue) _comboBox.getSelectedItem();
    }

    @Override
    protected void setValue(RemoteEnumerationValue value) {
        _comboBox.setSelectedItem(value);
    }

}
