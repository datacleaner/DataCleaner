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
package org.eobjects.datacleaner.panels.datastructures;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.eobjects.analyzer.beans.datastructures.SelectFromMapTransformer;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.properties.MinimalPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.MultipleStringPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.SingleClassPropertyWidget;
import org.jdesktop.swingx.JXTextField;

/**
 * Specialized {@link PropertyWidget} for the key/type mapping of
 * {@link SelectFromMapTransformer}.
 */
public class KeysAndTypesPropertyWidget extends MultipleStringPropertyWidget {

	private final ConfiguredPropertyDescriptor _typesProperty;
	private final List<DCComboBox<Class<?>>> _comboBoxes;
	private final MinimalPropertyWidget<Class<?>[]> _typesPropertyWidget;

	public KeysAndTypesPropertyWidget(ConfiguredPropertyDescriptor keysProperty, ConfiguredPropertyDescriptor typesProperty,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(keysProperty, beanJobBuilder);
		_comboBoxes = new ArrayList<DCComboBox<Class<?>>>();
		_typesProperty = typesProperty;
		_typesPropertyWidget = new MinimalPropertyWidget<Class<?>[]>(getBeanJobBuilder(), _typesProperty) {

			@Override
			public JComponent getWidget() {
				return null;
			}

			@Override
			public Class<?>[] getValue() {
				final Class<?>[] result = new Class<?>[_comboBoxes.size()];
				for (int i = 0; i < result.length; i++) {
					result[i] = _comboBoxes.get(i).getSelectedItem();
				}
				return result;
			}

			@Override
			protected void setValue(Class<?>[] value) {
				// TODO Auto-generated method stub
			}
		};

		// TODO: Initialize value
	}

	@Override
	protected JComponent decorateTextField(JXTextField textField) {
		final DCPanel panel = new DCPanel();
		panel.setLayout(new BorderLayout());
		panel.add(textField, BorderLayout.CENTER);

		final DCComboBox<Class<?>> comboBox = SingleClassPropertyWidget.createClassComboBox(true);
		comboBox.addListener(new Listener<Class<?>>() {
			@Override
			public void onItemSelected(Class<?> item) {
				_typesPropertyWidget.fireValueChanged();
			}
		});
		_comboBoxes.add(comboBox);

		panel.add(comboBox, BorderLayout.EAST);

		return panel;
	}

	public PropertyWidget<?> getTypesPropertyWidget() {
		return _typesPropertyWidget;
	}

}
