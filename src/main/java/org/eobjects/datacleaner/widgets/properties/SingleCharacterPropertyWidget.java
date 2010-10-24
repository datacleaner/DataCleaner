package org.eobjects.datacleaner.widgets.properties;

import javax.swing.JComponent;
import javax.swing.JTextField;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.util.SingleCharacterDocument;

public class SingleCharacterPropertyWidget implements PropertyWidget<Character> {

	private final ConfiguredPropertyDescriptor _propertyDescriptor;
	private final JTextField _textField;

	public SingleCharacterPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor, AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		_propertyDescriptor = propertyDescriptor;
		_textField = new JTextField(1);
		_textField.setDocument(new SingleCharacterDocument());
		Character currentValue = (Character) beanJobBuilder.getConfiguredProperty(_propertyDescriptor);
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
		String text = _textField.getText();
		return (text != null && text.length() == 1);
	}

	@Override
	public Character getValue() {
		String text = _textField.getText();
		if (text == null || text.length() == 0) {
			return null;
		}
		return text.charAt(0);
	}

}
