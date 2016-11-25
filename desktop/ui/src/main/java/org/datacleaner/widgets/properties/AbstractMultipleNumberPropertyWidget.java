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

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;

import org.apache.metamodel.util.EqualsBuilder;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

/**
 * {@link PropertyWidget} for Number arrays. Displays number arrays as a set of
 * text boxes and plus/minus buttons to grow/shrink the array.
 */
public abstract class AbstractMultipleNumberPropertyWidget<N> extends AbstractPropertyWidget<N> {

    private final NumberFormat _numberFormat = NumberFormat.getInstance();
    private final DCPanel _textFieldPanel;

    protected AbstractMultipleNumberPropertyWidget(final ComponentBuilder componentBuilder,
            final ConfiguredPropertyDescriptor propertyDescriptor) {
        super(componentBuilder, propertyDescriptor);

        _textFieldPanel = new DCPanel();
        _textFieldPanel.setLayout(new VerticalLayout(2));

        final N currentValue = getCurrentValue();
        updateComponents(currentValue);

        final JButton addButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD_DARK);
        addButton.addActionListener(e -> {
            addTextField(null, true);
            fireValueChanged();
        });

        final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE_DARK);
        removeButton.addActionListener(e -> {
            final int componentCount = _textFieldPanel.getComponentCount();
            if (componentCount > 0) {
                _textFieldPanel.remove(componentCount - 1);
                _textFieldPanel.updateUI();
                fireValueChanged();
            }
        });

        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setBorder(new EmptyBorder(0, 4, 0, 0));
        buttonPanel.setLayout(new VerticalLayout(2));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);

        final DCPanel outerPanel = new DCPanel();
        outerPanel.setLayout(new BorderLayout());

        outerPanel.add(_textFieldPanel, BorderLayout.CENTER);
        outerPanel.add(buttonPanel, BorderLayout.EAST);

        add(outerPanel);
    }

    @SuppressWarnings("unchecked")
    private N createArray(final int length) {
        return (N) Array.newInstance(getPropertyDescriptor().getBaseType(), length);
    }

    public void updateComponents(N values) {
        if (values == null) {
            values = createArray(2);
        }
        final N previousValues = getValue();
        if (!EqualsBuilder.equals(values, previousValues)) {
            for (int i = 0; i < Math.min(Array.getLength(previousValues), Array.getLength(values)); i++) {
                // modify text boxes
                if (!EqualsBuilder.equals(Array.get(previousValues, i), Array.get(values, i))) {
                    final JTextComponent component = (JTextComponent) _textFieldPanel.getComponent(i);
                    component.setText(_numberFormat.format(Array.get(values, i)));
                }
            }

            while (_textFieldPanel.getComponentCount() < Array.getLength(values)) {
                // add text boxes if there are too few
                final Object nextValue = Array.get(values, _textFieldPanel.getComponentCount());
                addTextField(nextValue, false);
            }

            while (_textFieldPanel.getComponentCount() > Array.getLength(values)) {
                // remove text boxes if there are too many
                _textFieldPanel.remove(_textFieldPanel.getComponentCount() - 1);
            }
            _textFieldPanel.updateUI();
        }
    }

    private void addTextField(final Object value, final boolean updateUI) {
        final JXTextField textField = WidgetFactory.createTextField();
        if (value != null) {
            textField.setText(_numberFormat.format(value));
        }
        textField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(final DocumentEvent e) {
                fireValueChanged();
            }
        });
        _textFieldPanel.add(textField);
        if (updateUI) {
            _textFieldPanel.updateUI();
        }
    }

    @Override
    public N getValue() {
        final Component[] components = _textFieldPanel.getComponents();

        final N result = createArray(components.length);
        for (int i = 0; i < components.length; i++) {
            final JXTextField textField = (JXTextField) components[i];
            final String stringValue = textField.getText();
            setInArray(result, i, convertToNumber(stringValue));
        }
        return result;
    }

    @Override
    protected void setValue(final N value) {
        updateComponents(value);
    }

    private void setInArray(final Object array, final int index, Number number) {
        final Class<?> baseType = getPropertyDescriptor().getBaseType();
        if (baseType.isPrimitive()) {
            if (number == null) {
                number = 0;
            }
            if (baseType == byte.class) {
                Array.setByte(array, index, number.byteValue());
            } else if (baseType == short.class) {
                Array.setShort(array, index, number.shortValue());
            } else if (baseType == int.class) {
                Array.setInt(array, index, number.intValue());
            } else if (baseType == long.class) {
                Array.setLong(array, index, number.longValue());
            } else if (baseType == float.class) {
                Array.setFloat(array, index, number.floatValue());
            } else if (baseType == double.class) {
                Array.setDouble(array, index, number.doubleValue());
            }
        } else {
            Array.set(array, index, number);
        }
    }

    private Number convertToNumber(final String stringValue) {
        if (StringUtils.isNullOrEmpty(stringValue)) {
            return null;
        }
        final Class<?> type = getPropertyDescriptor().getBaseType();
        try {
            final Number number = _numberFormat.parse(stringValue);
            if (type != Number.class) {
                if (ReflectionUtils.isInteger(type)) {
                    return Integer.valueOf(number.intValue());
                } else if (ReflectionUtils.isLong(type)) {
                    return Long.valueOf(number.longValue());
                } else if (ReflectionUtils.isDouble(type)) {
                    return Double.valueOf(number.doubleValue());
                } else if (ReflectionUtils.isShort(type)) {
                    return Short.valueOf(number.shortValue());
                } else if (ReflectionUtils.isByte(type)) {
                    return Byte.valueOf(number.byteValue());
                } else if (ReflectionUtils.isFloat(type)) {
                    return Float.valueOf(number.floatValue());
                }
            }
            return number;
        } catch (final ParseException e) {
            throw new IllegalStateException("Cannot parse to number: " + stringValue);
        }
    }

}
