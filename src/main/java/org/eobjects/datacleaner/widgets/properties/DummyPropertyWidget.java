package org.eobjects.datacleaner.widgets.properties;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;

public class DummyPropertyWidget implements PropertyWidget<Object> {

	private final ConfiguredPropertyDescriptor _propertyDescriptor;
	private Object _value;

	public DummyPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor) {
		_propertyDescriptor = propertyDescriptor;
	}

	@Override
	public JComponent getWidget() {
		return new JLabel("Not yet implemented");
	}

	@Override
	public boolean isSet() {
		return _value != null;
	}

	@Override
	public Object getValue() {
		return _value;
	}
	
	@Override
	public ConfiguredPropertyDescriptor getPropertyDescriptor() {
		return _propertyDescriptor;
	}

	@Override
	public void onValueTouched(Object value) {
		_value = value;
	}
}
