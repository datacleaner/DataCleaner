package org.eobjects.datacleaner.widgets.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.jdesktop.swingx.JXDatePicker;

public class SingleDatePropertyWidget extends AbstractPropertyWidget<Date> {

	private static final long serialVersionUID = 1L;

	private final JXDatePicker _datePicker;

	public SingleDatePropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(propertyDescriptor);
		_datePicker = new JXDatePicker();
		_datePicker.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				fireValueChanged();
			}
		});
		Date currentValue = (Date) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		if (currentValue != null) {
			_datePicker.setDate(currentValue);
		}
		add(_datePicker);
	}

	@Override
	public Date getValue() {
		return _datePicker.getDate();
	}

}
