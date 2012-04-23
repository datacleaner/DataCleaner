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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.swing.JList;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.DCListCellRenderer;

public class SingleClassPropertyWidget extends AbstractPropertyWidget<Class<?>> {

	private final DCComboBox<Class<?>> _comboBox;

	@Inject
	public SingleClassPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(beanJobBuilder, propertyDescriptor);

		_comboBox = createClassComboBox(propertyDescriptor.isRequired());
		Class<?> currentValue = getCurrentValue();
		if (currentValue != null) {
			_comboBox.setSelectedItem(currentValue);
		}

		_comboBox.addListener(new Listener<Class<?>>() {
			@Override
			public void onItemSelected(Class<?> item) {
				fireValueChanged(item);
			}
		});

		add(_comboBox);
	}

	public static DCComboBox<Class<?>> createClassComboBox(boolean required) {
		Collection<Class<?>> items = new ArrayList<Class<?>>();

		if (!required) {
			items.add(null);
		}
		items.add(String.class);
		items.add(Number.class);
		items.add(Date.class);
		items.add(Boolean.class);
		items.add(List.class);
		items.add(Map.class);
		items.add(Object.class);

		DCComboBox<Class<?>> comboBox = new DCComboBox<Class<?>>(items);
		comboBox.setRenderer(new DCListCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				if (value instanceof Class) {
					// render eg. java.lang.String as just "String"
					value = ((Class<?>) value).getSimpleName();
				}
				return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}
		});
		return comboBox;
	}

	@Override
	public Class<?> getValue() {
		return _comboBox.getSelectedItem();
	}

	@Override
	protected void setValue(Class<?> value) {
		_comboBox.setSelectedItem(value);
	}

}
