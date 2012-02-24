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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.JComponent;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.widgets.DCCheckBox;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.SourceColumnComboBox;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.MutableColumn;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.EqualsBuilder;
import org.eobjects.metamodel.util.MutableRef;

/**
 * A specialized property widget for multiple input columns that are mapped to
 * physical columns. This widget looks like the
 * {@link MultipleInputColumnsPropertyWidget}, but is enhanced with source
 * column combo boxes and awareness of changes to selected table.
 * 
 * @author Kasper Sørensen
 */
public class MultipleMappedColumnsPropertyWidget extends
		MultipleInputColumnsPropertyWidget {

	private final WeakHashMap<InputColumn<?>, SourceColumnComboBox> _mappedColumnComboBoxes;
	private final MutableRef<Table> _tableRef;
	private final ConfiguredPropertyDescriptor _mappedColumnsProperty;
	private final MinimalPropertyWidget<String[]> _mappedColumnNamesPropertyWidget;

	// indicates whether there is currently undergoing a source column listener
	// action
	private volatile boolean _sourceColumnUpdating;

	/**
	 * Constructs the property widget
	 * 
	 * @param beanJobBuilder
	 *            the transformer job builder for the table lookup
	 * @param inputColumnsProperty
	 *            the property represeting the columns to use for settig up
	 *            conditional lookup (InputColumn[])
	 * @param mappedColumnsProperty
	 *            the property representing the mapped columns in the datastore
	 *            (String[])
	 */
	public MultipleMappedColumnsPropertyWidget(
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor inputColumnsProperty,
			ConfiguredPropertyDescriptor mappedColumnsProperty) {
		super(beanJobBuilder, inputColumnsProperty);
		_mappedColumnComboBoxes = new WeakHashMap<InputColumn<?>, SourceColumnComboBox>();
		_mappedColumnsProperty = mappedColumnsProperty;

		_tableRef = new MutableRef<Table>();
		_mappedColumnNamesPropertyWidget = createMappedColumnNamesPropertyWidget();
		_sourceColumnUpdating = false;

		final InputColumn<?>[] currentValue = getCurrentValue();
		final String[] currentMappedColumnsValue = (String[]) beanJobBuilder
				.getConfiguredProperty(mappedColumnsProperty);
		if (currentValue != null && currentMappedColumnsValue != null) {
			// first create combo's, then set value (so combo is ready before it
			// is requested)

			_mappedColumnNamesPropertyWidget
					.setValue(currentMappedColumnsValue);
			final int minLength = Math.min(currentValue.length,
					currentMappedColumnsValue.length);
			for (int i = 0; i < minLength; i++) {
				final InputColumn<?> inputColumn = currentValue[i];
				final String mappedColumnName = currentMappedColumnsValue[i];
				createComboBox(inputColumn, new MutableColumn(mappedColumnName));
			}

			setValue(currentValue);
		}
	}

	public void setTable(Table table) {
		if (table != _tableRef.get()) {
			_tableRef.set(table);
			updateMappedColumns();
		}
	}

	private void updateMappedColumns() {
		final Table table = _tableRef.get();
		final Set<Entry<InputColumn<?>, SourceColumnComboBox>> entrySet = _mappedColumnComboBoxes
				.entrySet();

		for (Entry<InputColumn<?>, SourceColumnComboBox> entry : entrySet) {
			InputColumn<?> inputColumn = entry.getKey();
			SourceColumnComboBox comboBox = entry.getValue();

			if (table == null) {
				comboBox.setEmptyModel();
			} else {
				comboBox.setModel(table);
				if (comboBox.getSelectedItem() == null) {
					Column column = getDefaultMappedColumn(inputColumn, table);
					if (column != null) {
						comboBox.setEditable(true);
						comboBox.setSelectedItem(column);
						comboBox.setEditable(false);
					}
				}
			}
		}
	}

	@Override
	protected boolean isAllInputColumnsSelectedIfNoValueExist() {
		return false;
	}

	private SourceColumnComboBox createComboBox(InputColumn<?> inputColumn,
			Column mappedColumn) {
		final SourceColumnComboBox sourceColumnComboBox = new SourceColumnComboBox();
		_mappedColumnComboBoxes.put(inputColumn, sourceColumnComboBox);

		Table table = _tableRef.get();
		if (mappedColumn == null && table != null) {
			mappedColumn = getDefaultMappedColumn(inputColumn, table);
		}

		if (mappedColumn != null) {
			sourceColumnComboBox.setEditable(true);
			sourceColumnComboBox.setSelectedItem(mappedColumn);
			sourceColumnComboBox.setEditable(false);
		}
		sourceColumnComboBox.addListener(new Listener<Column>() {
			@Override
			public void onItemSelected(Column item) {
				if (isBatchUpdating()) {
					return;
				}
				_sourceColumnUpdating = true;
				fireValueChanged();
				_mappedColumnNamesPropertyWidget.fireValueChanged();
				_sourceColumnUpdating = false;
			}
		});
		return sourceColumnComboBox;
	}

	protected Column getDefaultMappedColumn(InputColumn<?> inputColumn,
			Table table) {
		// automatically select a column by name, if it exists
		return table.getColumnByName(inputColumn.getName());
	}

	@Override
	protected JComponent decorateCheckBox(
			final DCCheckBox<InputColumn<?>> checkBox) {
		final SourceColumnComboBox sourceColumnComboBox;
		if (_mappedColumnComboBoxes.containsKey(checkBox.getValue())) {
			sourceColumnComboBox = _mappedColumnComboBoxes.get(checkBox
					.getValue());
		} else {
			sourceColumnComboBox = createComboBox(checkBox.getValue(), null);
		}
		checkBox.addListenerToHead(new DCCheckBox.Listener<InputColumn<?>>() {
			@Override
			public void onItemSelected(InputColumn<?> item, boolean selected) {
				_sourceColumnUpdating = true;
				sourceColumnComboBox.setVisible(selected);
			}
		});
		checkBox.addListener(new DCCheckBox.Listener<InputColumn<?>>() {
			@Override
			public void onItemSelected(InputColumn<?> item, boolean selected) {
				if (isBatchUpdating()) {
					return;
				}
				_mappedColumnNamesPropertyWidget.fireValueChanged();
				_sourceColumnUpdating=false;
			}
		});

		Table table = _tableRef.get();
		if (table != null) {
			sourceColumnComboBox.setModel(table);
		}

		sourceColumnComboBox.setVisible(checkBox.isSelected());

		final DCPanel panel = new DCPanel();
		panel.setLayout(new BorderLayout());
		panel.add(checkBox, BorderLayout.CENTER);
		panel.add(sourceColumnComboBox, BorderLayout.EAST);
		return panel;
	}

	public PropertyWidget<String[]> getMappedColumnNamesPropertyWidget() {
		return _mappedColumnNamesPropertyWidget;
	}

	private MinimalPropertyWidget<String[]> createMappedColumnNamesPropertyWidget() {
		return new MinimalPropertyWidget<String[]>(getBeanJobBuilder(),
				_mappedColumnsProperty) {

			@Override
			public JComponent getWidget() {
				// do not return a visual widget
				return null;
			}

			@Override
			public boolean isSet() {
				final InputColumn<?>[] inputColumns = MultipleMappedColumnsPropertyWidget.this
						.getValue();
				for (InputColumn<?> inputColumn : inputColumns) {
					SourceColumnComboBox comboBox = _mappedColumnComboBoxes
							.get(inputColumn);
					if (comboBox.getSelectedItem() == null) {
						return false;
					}
				}
				return true;
			}

			@Override
			public String[] getValue() {
				return getMappedColumnNames();
			}

			@Override
			protected void setValue(String[] value) {
				if (_sourceColumnUpdating) {
					// setValue of the mapped columns will be called prematurely
					// (with previous value) by change notifications of the
					// input columns property.
					return;
				}
				if (EqualsBuilder.equals(value, getValue())) {
					return;
				}
				final InputColumn<?>[] inputColumns = MultipleMappedColumnsPropertyWidget.this
						.getValue();
				for (int i = 0; i < inputColumns.length; i++) {
					final InputColumn<?> inputColumn = inputColumns[i];
					final String mappedColumnName;
					if (value == null) {
						mappedColumnName = null;
					} else if (i < value.length) {
						mappedColumnName = value[i];
					} else {
						mappedColumnName = null;
					}
					final SourceColumnComboBox comboBox = _mappedColumnComboBoxes
							.get(inputColumn);
					comboBox.setEditable(true);
					comboBox.setSelectedItem(mappedColumnName);
					comboBox.setEditable(false);
				}
			}
		};
	}

	@Override
	public InputColumn<?>[] getValue() {
		final InputColumn<?>[] checkedInputColumns = super.getValue();
		final List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
		for (InputColumn<?> inputColumn : checkedInputColumns) {
			// exclude input columns that have not been mapped yet
			final SourceColumnComboBox comboBox = _mappedColumnComboBoxes
					.get(inputColumn);
			if (comboBox != null) {
				if (comboBox.getSelectedItem() != null) {
					result.add(inputColumn);
				}
			}
		}
		return result.toArray(new InputColumn[result.size()]);
	}

	private String[] getMappedColumnNames() {
		final InputColumn<?>[] inputColumns = MultipleMappedColumnsPropertyWidget.this
				.getValue();
		final List<String> result = new ArrayList<String>();
		for (InputColumn<?> inputColumn : inputColumns) {
			SourceColumnComboBox comboBox = _mappedColumnComboBoxes
					.get(inputColumn);
			if (comboBox != null) {
				Column column = comboBox.getSelectedItem();
				if (column != null) {
					result.add(column.getName());
				}
			}
		}
		return result.toArray(new String[result.size()]);
	}

	public Map<InputColumn<?>, SourceColumnComboBox> getMappedColumnComboBoxes() {
		return Collections.unmodifiableMap(_mappedColumnComboBoxes);
	}

	@Override
	protected void selectAll() {
		batchUpdateWidget(new Runnable() {
			@Override
			public void run() {
				Collection<SourceColumnComboBox> comboBoxes = _mappedColumnComboBoxes
						.values();
				for (SourceColumnComboBox comboBox : comboBoxes) {
					comboBox.setVisible(true);
				}
				MultipleMappedColumnsPropertyWidget.super.selectAll();
			}
		});
	}

	@Override
	protected void selectNone() {
		for (SourceColumnComboBox sourceColumnComboBox : _mappedColumnComboBoxes
				.values()) {
			sourceColumnComboBox.setVisible(false);
		}
		super.selectNone();
	}

	@Override
	protected void onBatchFinished() {
		super.onBatchFinished();
		_mappedColumnNamesPropertyWidget.fireValueChanged();
	}
}