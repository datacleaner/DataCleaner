package org.eobjects.datacleaner.widgets.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.util.CollectionUtils;

public final class SingleEnumPropertyWidget extends AbstractPropertyWidget<Enum<?>> {

	private static final long serialVersionUID = 1L;
	private final JComboBox _comboBox;

	public SingleEnumPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(beanJobBuilder, propertyDescriptor);

		Enum<?>[] enumConstants = (Enum<?>[]) propertyDescriptor.getType().getEnumConstants();

		if (!propertyDescriptor.isRequired()) {
			enumConstants = CollectionUtils.array(new Enum<?>[] { null }, enumConstants);
		}

		_comboBox = new JComboBox(enumConstants);

		Object currentValue = beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		_comboBox.setSelectedItem(currentValue);

		_comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fireValueChanged();
			}
		});
		add(_comboBox);
	}

	@Override
	public Enum<?> getValue() {
		return (Enum<?>) _comboBox.getSelectedItem();
	}

	@Override
	protected void setValue(Enum<?> value) {
		_comboBox.setSelectedItem(value);
	}

}
