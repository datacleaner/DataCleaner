package org.eobjects.datacleaner.widgets.properties;

import java.util.Date;

import javax.swing.JComponent;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.jdesktop.swingx.JXDatePicker;

public class SingleDatePropertyWidget implements PropertyWidget<Date> {

	private final ConfiguredPropertyDescriptor _propertyDescriptor;
	private final JXDatePicker _datePicker;

	public SingleDatePropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		_propertyDescriptor = propertyDescriptor;
		_datePicker = new JXDatePicker();
		Date currentValue = (Date) beanJobBuilder.getConfiguredProperty(_propertyDescriptor);
		if (currentValue != null) {
			_datePicker.setDate(currentValue);
		}
	}

	@Override
	public JComponent getWidget() {
		return _datePicker;
	}

	@Override
	public ConfiguredPropertyDescriptor getPropertyDescriptor() {
		return _propertyDescriptor;
	}

	@Override
	public boolean isSet() {
		return _datePicker.getDate() != null;
	}

	@Override
	public Date getValue() {
		return _datePicker.getDate();
	}

}
