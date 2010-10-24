package org.eobjects.datacleaner.widgets.properties;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;

public class DummyPropertyWidget implements PropertyWidget<Object> {

	private final ConfiguredPropertyDescriptor _propertyDescriptor;

	public DummyPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor) {
		_propertyDescriptor = propertyDescriptor;
	}

	@Override
	public JComponent getWidget() {
		return new JLabel("Not yet implemented");
	}

	@Override
	public boolean isSet() {
		return false;
	}

	@Override
	public Object getValue() {
		return null;
	}

	@Override
	public ConfiguredPropertyDescriptor getPropertyDescriptor() {
		return _propertyDescriptor;
	}

}
