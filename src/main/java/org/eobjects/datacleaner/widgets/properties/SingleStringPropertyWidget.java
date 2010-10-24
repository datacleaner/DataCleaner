package org.eobjects.datacleaner.widgets.properties;

import javax.swing.JComponent;
import javax.swing.JTextField;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.jdesktop.swingx.JXTextField;

public class SingleStringPropertyWidget implements PropertyWidget<String> {

	private final JTextField _textField;
	private final ConfiguredPropertyDescriptor _propertyDescriptor;

	public SingleStringPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		_propertyDescriptor = propertyDescriptor;
		_textField = new JXTextField(propertyDescriptor.getName());
		String currentValue = (String) beanJobBuilder.getConfiguredProperty(_propertyDescriptor);
		if (currentValue != null) {
			_textField.setText(currentValue);
		}
	}

	@Override
	public JComponent getWidget() {
		return _textField;
	}

	@Override
	public boolean isSet() {
		return _textField.getText() != null && _textField.getText().length() > 0;
	}

	@Override
	public String getValue() {
		return _textField.getText();
	}

	@Override
	public ConfiguredPropertyDescriptor getPropertyDescriptor() {
		return _propertyDescriptor;
	}
}
