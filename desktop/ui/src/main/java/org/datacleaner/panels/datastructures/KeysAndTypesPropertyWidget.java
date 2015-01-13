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
package org.datacleaner.panels.datastructures;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;

import org.datacleaner.beans.datastructures.SelectFromMapTransformer;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AbstractBeanJobBuilder;
import org.datacleaner.util.StringUtils;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.DCComboBox.Listener;
import org.datacleaner.widgets.properties.MinimalPropertyWidget;
import org.datacleaner.widgets.properties.MultipleStringPropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidget;
import org.datacleaner.widgets.properties.SingleClassPropertyWidget;
import org.apache.metamodel.util.EqualsBuilder;
import org.jdesktop.swingx.JXTextField;

/**
 * Specialized {@link PropertyWidget} for the key/type mapping of
 * {@link SelectFromMapTransformer}.
 */
public class KeysAndTypesPropertyWidget extends MultipleStringPropertyWidget {

    private final ConfiguredPropertyDescriptor _typesProperty;
    private final List<DCComboBox<Class<?>>> _comboBoxes;

    @SuppressWarnings("rawtypes")
    private final MinimalPropertyWidget<Class[]> _typesPropertyWidget;

    @SuppressWarnings("rawtypes")
    public KeysAndTypesPropertyWidget(ConfiguredPropertyDescriptor keysProperty,
            ConfiguredPropertyDescriptor typesProperty, AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
        super(keysProperty, beanJobBuilder);
        _comboBoxes = new ArrayList<DCComboBox<Class<?>>>();
        _typesProperty = typesProperty;
        _typesPropertyWidget = new MinimalPropertyWidget<Class[]>(getComponentBuilder(), _typesProperty) {

            @Override
            public JComponent getWidget() {
                return null;
            }

            @Override
            public Class[] getValue() {
                final String[] keys = KeysAndTypesPropertyWidget.this.getValue();
                final List<Class<?>> result = new ArrayList<Class<?>>();
                for (int i = 0; i < keys.length; i++) {
                    if (!StringUtils.isNullOrEmpty(keys[i])) {
                        final DCComboBox<Class<?>> comboBox = _comboBoxes.get(i);
                        result.add(comboBox.getSelectedItem());
                    }
                }
                return result.toArray(new Class[result.size()]);
            }

            @Override
            public boolean isSet() {
                if (_comboBoxes.isEmpty()) {
                    return false;
                }
                for (DCComboBox<Class<?>> comboBox : _comboBoxes) {
                    if (comboBox.getSelectedItem() == null) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            protected void setValue(Class[] value) {
                if (EqualsBuilder.equals(value, getValue())) {
                    return;
                }

                if (value == null) {
                    value = new Class[0];
                }

                final String[] keys = KeysAndTypesPropertyWidget.this.getValue();
                if (keys.length != value.length) {
                    // disregard this invalid value update
                    return;
                }

                for (int i = 0; i < keys.length; i++) {
                    DCComboBox<Class<?>> comboBox = _comboBoxes.get(i);
                    Class<?> selectedClass = value[i];
                    comboBox.setSelectedItem(selectedClass);
                }
            }
        };

        final String[] currentKeysValue = getCurrentValue();
        final Class[] currentTypesValue = (Class[]) beanJobBuilder.getConfiguredProperty(typesProperty);
        if (currentTypesValue != null) {
            // first create textfields, then set keys value

            for (int i = 0; i < currentTypesValue.length; i++) {
                final Class<?> type = currentTypesValue[i];
                createComboBox(type);
            }

            setValue(currentKeysValue);
            _typesPropertyWidget.onValueTouched(currentTypesValue);
        }
    }

    private DCComboBox<Class<?>> createComboBox(Class<?> type) {
        final DCComboBox<Class<?>> comboBox = SingleClassPropertyWidget.createClassComboBox(true);
        if (type != null) {
            comboBox.setSelectedItem(type);
        }
        _comboBoxes.add(comboBox);
        return comboBox;
    }

    @Override
    protected JComponent decorateTextField(JXTextField textField, int index) {
        final DCComboBox<Class<?>> comboBox;

        if (index < _comboBoxes.size()) {
            comboBox = _comboBoxes.get(index);
        } else {
            comboBox = createComboBox(null);
        }

        comboBox.addListener(new Listener<Class<?>>() {
            @Override
            public void onItemSelected(Class<?> item) {
                _typesPropertyWidget.fireValueChanged();
            }
        });

        textField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                // invoke later, because document events are fired before the
                // textfield.getText() returns the new value
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setUpdating(true);
                        _typesPropertyWidget.fireValueChanged();
                        setUpdating(false);
                    }
                });
            }
        });

        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(textField, BorderLayout.CENTER);
        panel.add(comboBox, BorderLayout.EAST);

        return panel;
    }

    public PropertyWidget<?> getTypesPropertyWidget() {
        return _typesPropertyWidget;
    }

    @Override
    protected boolean isEmptyStringValid() {
        return false;
    }
}
