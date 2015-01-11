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
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import org.datacleaner.api.NumberProperty;
import org.datacleaner.beans.convert.ConvertToNumberTransformer;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AbstractBeanJobBuilder;
import org.datacleaner.util.Percentage;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.NumberDocument;

public class SingleNumberPropertyWidget extends AbstractPropertyWidget<Number> {

    private final boolean _primitive;
    private final JTextField _textField;

    @Inject
    public SingleNumberPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
            AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
        super(beanJobBuilder, propertyDescriptor);
        _textField = new JTextField(5);

        _textField.setDocument(new NumberDocument(isDecimalAllowed(), isNegativeAllowed()));
        _primitive = propertyDescriptor.getType().isPrimitive();
        Number currentValue = getCurrentValue();
        if (currentValue != null) {
            _textField.setText(currentValue.toString());
        }
        _textField.getDocument().addDocumentListener(new DCDocumentListener() {

            @Override
            protected void onChange(DocumentEvent e) {
                fireValueChanged();
            }
        });
        add(_textField);
    }

    private boolean isNegativeAllowed() {
        NumberProperty numberProperty = getPropertyDescriptor().getAnnotation(NumberProperty.class);
        if (numberProperty != null) {
            return numberProperty.negative();
        }
        return true;
    }

    private boolean isDecimalAllowed() {
        Class<?> type = getPropertyDescriptor().getBaseType();
        if (ReflectionUtils.isByte(type) || ReflectionUtils.isShort(type) || ReflectionUtils.isInteger(type)
                || ReflectionUtils.isLong(type)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isSet() {
        return getValue() != null;
    }

    @Override
    public Number getValue() {
        String text = _textField.getText();
        if (text == null || text.length() == 0) {
            if (_primitive) {
                text = "0";
            } else {
                return null;
            }
        }
        Class<?> type = getPropertyDescriptor().getType();
        if (ReflectionUtils.isInteger(type)) {
            return Integer.parseInt(text);
        }
        if (ReflectionUtils.isDouble(type)) {
            return Double.parseDouble(text);
        }
        if (ReflectionUtils.isLong(type)) {
            return Long.parseLong(text);
        }
        if (ReflectionUtils.isByte(type)) {
            return Byte.parseByte(text);
        }
        if (ReflectionUtils.isFloat(type)) {
            return Float.parseFloat(text);
        }
        if (ReflectionUtils.isShort(type)) {
            return Short.parseShort(text);
        }
        if (ReflectionUtils.is(type, Percentage.class)) {
            return Percentage.parsePercentage(text);
        }
        if (ReflectionUtils.isNumber(type)) {
            // type is simple "number" - ie. any number
            return ConvertToNumberTransformer.transformValue(text);
        }
        throw new IllegalStateException("Unsupported number-property type: " + type);
    }

    @Override
    protected void setValue(Number value) {
        if (value == null) {
            _textField.setText("");
            return;
        }
        _textField.setText(value.toString());
    }
}
