package org.eobjects.datacleaner.widgets.properties;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;

public class SingleBooleanPropertyWidget implements PropertyWidget<Boolean> {

	private final ConfiguredPropertyDescriptor _propertyDescriptor;
	private final JCheckBox _checkBox;

	public SingleBooleanPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor, AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		_propertyDescriptor = propertyDescriptor;
		_checkBox = new JCheckBox();
		Boolean currentValue = (Boolean) beanJobBuilder.getConfiguredProperty(_propertyDescriptor);
		_checkBox.setOpaque(false);
		if (currentValue != null) {
			_checkBox.setSelected(currentValue.booleanValue());
		}
	}

	@Override
	public JComponent getWidget() {
		return _checkBox;
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
	public Boolean getValue() {
		return _checkBox.isSelected();
	}

}
