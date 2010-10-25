package org.eobjects.datacleaner.widgets.properties;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.util.Percentage;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.util.NumberDocument;

public class SingleNumberPropertyWidget extends AbstractPropertyWidget<Number> {

	private static final long serialVersionUID = 1L;

	private final JTextField _textField;

	public SingleNumberPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(propertyDescriptor);
		_textField = new JTextField(5);
		_textField.setDocument(new NumberDocument());
		Number currentValue = (Number) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		if (currentValue != null) {
			_textField.setText(currentValue.toString());
		}
		_textField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				fireValueChanged();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				fireValueChanged();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				fireValueChanged();
			}
		});
		add(_textField);
	}

	@Override
	public boolean isSet() {
		return true;
	}

	@Override
	public Number getValue() {
		String text = _textField.getText();
		if (text == null || text.length() == 0) {
			return null;
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
			if (text.indexOf('%') != -1) {
				return Percentage.parsePercentage(text);
			} else if (text.indexOf('.') != -1) {
				return Double.parseDouble(text);
			} else {
				return Integer.parseInt(text);
			}
		}
		throw new IllegalStateException("Unsupported number-property type: " + type);
	}

}
