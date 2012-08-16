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

import java.awt.BorderLayout;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.JComponent;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.widgets.DCCheckBox;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.EnumComboBoxListRenderer;
import org.eobjects.metamodel.util.EqualsBuilder;

/**
 * A specialized property widget for multiple input columns that are mapped to
 * enum values. This widget looks like the
 * {@link MultipleInputColumnsPropertyWidget}, but is enhanced with enum combo
 * boxes.
 * 
 * @author Kasper Sørensen
 */
public class MultipleMappedEnumsPropertyWidget<E extends Enum<?>> extends MultipleInputColumnsPropertyWidget {

	private final WeakHashMap<InputColumn<?>, DCComboBox<E>> _mappedEnumComboBoxes;
	private final ConfiguredPropertyDescriptor _mappedEnumsProperty;
	private final MinimalPropertyWidget<E[]> _mappedEnumsPropertyWidget;

	/**
	 * Constructs the property widget
	 * 
	 * @param beanJobBuilder
	 *            the transformer job builder for the table lookup
	 * @param inputColumnsProperty
	 *            the property represeting the columns to use for settig up
	 *            conditional lookup (InputColumn[])
	 * @param mappedEnumsProperty
	 *            the property representing the mapped enums
	 */
	public MultipleMappedEnumsPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor inputColumnsProperty, ConfiguredPropertyDescriptor mappedEnumsProperty) {
		super(beanJobBuilder, inputColumnsProperty);
		_mappedEnumComboBoxes = new WeakHashMap<InputColumn<?>, DCComboBox<E>>();
		_mappedEnumsProperty = mappedEnumsProperty;

		_mappedEnumsPropertyWidget = createMappedEnumsPropertyWidget();

		InputColumn<?>[] currentValue = getCurrentValue();
		if (currentValue != null) {
			setValue(currentValue);
		}

		@SuppressWarnings("unchecked")
		E[] currentMappedEnums = (E[]) beanJobBuilder.getConfiguredProperty(mappedEnumsProperty);
		if (currentValue != null && currentMappedEnums != null) {
			int minLength = Math.min(currentValue.length, currentMappedEnums.length);
			for (int i = 0; i < minLength; i++) {
				InputColumn<?> inputColumn = currentValue[i];
				E mappedEnum = currentMappedEnums[i];
				createComboBox(inputColumn, mappedEnum);
			}
		}
	}

	@Override
	protected boolean isAllInputColumnsSelectedIfNoValueExist() {
		return false;
	}

	private DCComboBox<E> createComboBox(InputColumn<?> inputColumn, E mappedEnum) {
		if (mappedEnum == null && inputColumn != null) {
			mappedEnum = getSuggestedValue(inputColumn);
		}

		@SuppressWarnings("unchecked")
		final E[] enumConstants = (E[]) _mappedEnumsProperty.getBaseType().getEnumConstants();

		final DCComboBox<E> comboBox = new DCComboBox<E>(enumConstants);
		comboBox.setRenderer(new EnumComboBoxListRenderer());
		_mappedEnumComboBoxes.put(inputColumn, comboBox);
		if (mappedEnum != null) {
			comboBox.setSelectedItem(mappedEnum);
		}
		comboBox.addListener(new Listener<E>() {
			@Override
			public void onItemSelected(E item) {
				_mappedEnumsPropertyWidget.fireValueChanged();
			}
		});
		return comboBox;
	}

	protected E getSuggestedValue(InputColumn<?> inputColumn) {
		return null;
	}

	@Override
	protected JComponent decorateCheckBox(final DCCheckBox<InputColumn<?>> checkBox) {
		final DCComboBox<E> comboBox;
		if (_mappedEnumComboBoxes.containsKey(checkBox.getValue())) {
			comboBox = _mappedEnumComboBoxes.get(checkBox.getValue());
		} else {
			comboBox = createComboBox(checkBox.getValue(), null);
		}
		checkBox.addListener(new DCCheckBox.Listener<InputColumn<?>>() {
			@Override
			public void onItemSelected(InputColumn<?> item, boolean selected) {
			    if (isBatchUpdating()) {
			        return;
			    }
				comboBox.setVisible(selected);
				_mappedEnumsPropertyWidget.fireValueChanged();
			}
		});

		comboBox.setVisible(checkBox.isSelected());

		final DCPanel panel = new DCPanel();
		panel.setLayout(new BorderLayout());
		panel.add(checkBox, BorderLayout.CENTER);
		panel.add(comboBox, BorderLayout.EAST);
		return panel;
	}

	public PropertyWidget<E[]> getMappedEnumsPropertyWidget() {
		return _mappedEnumsPropertyWidget;
	}

	private MinimalPropertyWidget<E[]> createMappedEnumsPropertyWidget() {
		return new MinimalPropertyWidget<E[]>(getBeanJobBuilder(), _mappedEnumsProperty) {

			@Override
			public JComponent getWidget() {
				// do not return a visual widget
				return null;
			}

			@Override
			public boolean isSet() {
				final E[] enumValues = getValue();
				for (E enumValue : enumValues) {
					if (enumValue == null) {
						return false;
					}
				}

				final InputColumn<?>[] inputColumns = MultipleMappedEnumsPropertyWidget.this.getValue();
				return enumValues.length == inputColumns.length;
			}

			@Override
			public E[] getValue() {
				return getMappedEnums();
			}

			@Override
			protected void setValue(E[] value) {
				if (EqualsBuilder.equals(value, getValue())) {
					return;
				}
				final InputColumn<?>[] inputColumns = MultipleMappedEnumsPropertyWidget.this.getValue();
				for (int i = 0; i < inputColumns.length; i++) {
					final InputColumn<?> inputColumn = inputColumns[i];
					final E mappedEnum;
					if (value == null) {
						mappedEnum = null;
					} else if (i < value.length) {
						mappedEnum = value[i];
					} else {
						mappedEnum = null;
					}
					final DCComboBox<E> comboBox = _mappedEnumComboBoxes.get(inputColumn);
					if (mappedEnum != null) {
						comboBox.setSelectedItem(mappedEnum);
					}
				}
			}
		};
	}

	private E[] getMappedEnums() {
		final InputColumn<?>[] inputColumns = MultipleMappedEnumsPropertyWidget.this.getValue();
		final List<E> result = new ArrayList<E>();
		for (InputColumn<?> inputColumn : inputColumns) {
			DCComboBox<E> comboBox = _mappedEnumComboBoxes.get(inputColumn);
			if (comboBox == null) {
				result.add(null);
			} else {
				E value = comboBox.getSelectedItem();
				result.add(value);
			}
		}

		@SuppressWarnings("unchecked")
		E[] array = (E[]) Array.newInstance(_mappedEnumsProperty.getBaseType(), result.size());

		return result.toArray(array);
	}

	@Override
	protected void selectAll() {
		for (DCComboBox<E> comboBox : _mappedEnumComboBoxes.values()) {
			comboBox.setVisible(true);
		}
		super.selectAll();
	}

	@Override
	protected void selectNone() {
		for (DCComboBox<E> comboBox : _mappedEnumComboBoxes.values()) {
			comboBox.setVisible(false);
		}
		super.selectNone();
	}
}