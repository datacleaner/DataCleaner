package org.eobjects.datacleaner.widgets.properties;

import javax.swing.JComponent;
import javax.swing.JTextField;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.util.Percentage;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.util.NumberDocument;

public class SingleNumberPropertyWidget implements PropertyWidget<Number> {

	private final ConfiguredPropertyDescriptor _propertyDescriptor;
	private final JTextField _textField;

	public SingleNumberPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		_propertyDescriptor = propertyDescriptor;
		_textField = new JTextField(5);
		_textField.setDocument(new NumberDocument());
		Number currentValue = (Number) beanJobBuilder.getConfiguredProperty(_propertyDescriptor);
		if (currentValue != null) {
			_textField.setText(currentValue.toString());
		}
	}

	@Override
	public JComponent getWidget() {
		return _textField;
	}

	@Override
	public ConfiguredPropertyDescriptor getPropertyDescriptor() {
		return _propertyDescriptor;
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
		Class<?> type = _propertyDescriptor.getType();
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
		throw new IllegalStateException("Unsupported number-property type: " + type);
	}

}
