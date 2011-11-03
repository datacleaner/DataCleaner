/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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

import javax.inject.Inject;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import org.eobjects.analyzer.beans.convert.ConvertToNumberTransformer;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.util.Percentage;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.NumberDocument;

public class SingleNumberPropertyWidget extends AbstractPropertyWidget<Number> {

	private static final long serialVersionUID = 1L;

	private final boolean _primitive;
	private final JTextField _textField;

	@Inject
	public SingleNumberPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(beanJobBuilder, propertyDescriptor);
		_textField = new JTextField(5);

		_textField.setDocument(new NumberDocument(isDecimalAllowed()));
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
		return true;
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
