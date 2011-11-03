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
package org.eobjects.datacleaner.panels.tablelookup;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.WeakHashMap;

import javax.swing.JComponent;

import org.eobjects.analyzer.beans.transform.TableLookupTransformer;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.widgets.DCCheckBox;
import org.eobjects.datacleaner.widgets.SourceColumnComboBox;
import org.eobjects.datacleaner.widgets.properties.MinimalPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.MultipleInputColumnsPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.SingleDatastorePropertyWidget;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.MutableRef;

/**
 * A specialized property widget for the multiple input columns property of the
 * {@link TableLookupTransformer}. This widget is enhanced with source column
 * combo boxes and awareness of changes to selected datastore.
 * 
 * @author Kasper Sørensen
 */
public class TableLookupInputColumnsPropertyWidget extends MultipleInputColumnsPropertyWidget {

	private static final long serialVersionUID = 1L;

	private final WeakHashMap<InputColumn<?>, SourceColumnComboBox> _mappedColumnComboBoxes;
	private final SingleDatastorePropertyWidget _datastorePropertyWidget;
	private final ItemListener _datastoreItemListener;
	private final MutableRef<Datastore> _datastoreRef;
	private final MutableRef<Table> _tableRef;
	private final ConfiguredPropertyDescriptor _mappedColumnsProperty;
	private final MinimalPropertyWidget<String[]> _mappedColumnNamesPropertyWidget;

	/**
	 * Constructs the property widget
	 * @param transformerJobBuilder the transformer job builder for the table lookup
	 * @param inputColumnsProperty the property represeting the columns to use for settig up conditional lookup (InputColumn[])
	 * @param mappedColumnsProperty the property representing the mapped columns in the datastore (String[])
	 * @param datastorePropertyWidget the property widget for selecting the datastore
	 */
	public TableLookupInputColumnsPropertyWidget(TransformerJobBuilder<TableLookupTransformer> transformerJobBuilder,
			ConfiguredPropertyDescriptor inputColumnsProperty, ConfiguredPropertyDescriptor mappedColumnsProperty, SingleDatastorePropertyWidget datastorePropertyWidget) {
		super(transformerJobBuilder, inputColumnsProperty);
		_mappedColumnComboBoxes = new WeakHashMap<InputColumn<?>, SourceColumnComboBox>();
		_datastorePropertyWidget = datastorePropertyWidget;
		_mappedColumnsProperty = mappedColumnsProperty;

		_tableRef = new MutableRef<Table>();
		_datastoreRef = new MutableRef<Datastore>(_datastorePropertyWidget.getValue());
		_datastoreItemListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				_datastoreRef.set(_datastorePropertyWidget.getValue());
				updateMappedColumns();
			}
		};
		_datastorePropertyWidget.addComboItemListener(_datastoreItemListener);
		_mappedColumnNamesPropertyWidget = createMappedColumnNamesPropertyWidget();
	}

	protected void updateMappedColumns() {
		Collection<SourceColumnComboBox> values = _mappedColumnComboBoxes.values();
		for (SourceColumnComboBox sourceColumnComboBox : values) {
			Table table = _tableRef.get();
			if (table == null) {
				sourceColumnComboBox.setModel(_datastoreRef.get());
			} else {
				sourceColumnComboBox.setModel(_datastoreRef.get(), table);
			}
		}
	}

	@Override
	protected boolean isAllInputColumnsSelectedIfNoValueExist() {
		return false;
	}

	@Override
	protected JComponent decorateCheckBox(final DCCheckBox<InputColumn<?>> checkBox) {
		final SourceColumnComboBox sourceColumnComboBox = new SourceColumnComboBox();
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sourceColumnComboBox.setVisible(checkBox.isSelected());
			}
		});
		sourceColumnComboBox.setVisible(checkBox.isSelected());
		_mappedColumnComboBoxes.put(checkBox.getValue(), sourceColumnComboBox);
		
		sourceColumnComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				_mappedColumnNamesPropertyWidget.fireValueChanged();
			}
		});

		final DCPanel panel = new DCPanel();
		panel.setLayout(new BorderLayout());
		panel.add(checkBox, BorderLayout.WEST);
		panel.add(sourceColumnComboBox, BorderLayout.EAST);
		return panel;
	}
	
	public PropertyWidget<String[]> getMappedColumnNamesPropertyWidget() {
		return _mappedColumnNamesPropertyWidget;
	}

	private MinimalPropertyWidget<String[]> createMappedColumnNamesPropertyWidget() {
		return new MinimalPropertyWidget<String[]>(getBeanJobBuilder(), _mappedColumnsProperty) {

			@Override
			public JComponent getWidget() {
				// do not return a visual widget
				return null;
			}

			@Override
			public boolean isSet() {
				final InputColumn<?>[] inputColumns = TableLookupInputColumnsPropertyWidget.this.getValue();
				for (InputColumn<?> inputColumn : inputColumns) {
					SourceColumnComboBox comboBox = _mappedColumnComboBoxes.get(inputColumn);
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
				InputColumn<?>[] inputColumns = TableLookupInputColumnsPropertyWidget.this.getCurrentValue();
				assert inputColumns.length == value.length;
				for (int i = 0; i < inputColumns.length; i++) {
					InputColumn<?> inputColumn = inputColumns[i];
					String mappedColumnName = value[i];
					SourceColumnComboBox comboBox = _mappedColumnComboBoxes.get(inputColumn);
					comboBox.setEditable(true);
					comboBox.setSelectedItem(mappedColumnName);
					comboBox.setEditable(false);
				}
			}
		};
	}
	
	private String[] getMappedColumnNames() {
		final InputColumn<?>[] inputColumns = TableLookupInputColumnsPropertyWidget.this.getValue();
		final String[] result = new String[inputColumns.length];
		for (int i = 0; i < result.length; i++) {
			SourceColumnComboBox comboBox = _mappedColumnComboBoxes.get(inputColumns[i]);
			try {
				result[i] = comboBox.getSelectedItem().getName();
			} catch (NullPointerException e) {
				// both comboBox or selected item might be null, ignore it
			}
		}
		return result;
	}
}