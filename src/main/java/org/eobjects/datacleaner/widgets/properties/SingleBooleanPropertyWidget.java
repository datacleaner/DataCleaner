package org.eobjects.datacleaner.widgets.properties;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;

public class SingleBooleanPropertyWidget extends AbstractPropertyWidget<Boolean> {

	private static final long serialVersionUID = 1L;

	private final JCheckBox _checkBox;

	public SingleBooleanPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(propertyDescriptor);
		_checkBox = new JCheckBox();
		Boolean currentValue = (Boolean) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		_checkBox.setOpaque(false);
		if (currentValue != null) {
			_checkBox.setSelected(currentValue.booleanValue());
		}
		_checkBox.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				fireValueChanged();
			}
		});
		add(_checkBox);
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
