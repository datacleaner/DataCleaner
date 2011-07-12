/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.widgets.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.metamodel.util.CollectionUtils;

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
