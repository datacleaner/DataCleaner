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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;

import org.apache.commons.lang.ArrayUtils;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.ExpressionBasedInputColumn;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.SourceColumnChangeListener;
import org.eobjects.analyzer.job.builder.TransformerChangeListener;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.actions.AddExpressionBasedColumnActionListener;
import org.eobjects.datacleaner.actions.ReorderColumnsActionListener;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCCheckBox;
import org.eobjects.datacleaner.widgets.DCCheckBox.Listener;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Property widget for multiple input columns. Displays these as checkboxes.
 * 
 * @author Kasper Sørensen
 */
public class MultipleInputColumnsPropertyWidget extends AbstractPropertyWidget<InputColumn<?>[]> implements
		SourceColumnChangeListener, TransformerChangeListener {

	private final Listener<InputColumn<?>> checkBoxListener = new Listener<InputColumn<?>>() {
		@Override
		public void onItemSelected(InputColumn<?> item, boolean selected) {
			fireValueChanged();
		}
	};

	private final ActionListener selectAllActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			selectAll();
		}
	};

	private final ActionListener selectNoneActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			selectNone();
		}
	};

	private final DataTypeFamily _dataTypeFamily;
	private final Map<InputColumn<?>, DCCheckBox<InputColumn<?>>> _checkBoxes;
	private final Map<DCCheckBox<InputColumn<?>>, JComponent> _checkBoxDecorations;
	private final DCPanel _buttonPanel;
	private final JXTextField _searchDatastoreTextField;
	private final DCCheckBox<InputColumn<?>> _notAvailableCheckBox;

	private volatile boolean _firstUpdate;

	@Inject
	public MultipleInputColumnsPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		super(beanJobBuilder, propertyDescriptor);
		_checkBoxes = new LinkedHashMap<InputColumn<?>, DCCheckBox<InputColumn<?>>>();
		_checkBoxDecorations = new IdentityHashMap<DCCheckBox<InputColumn<?>>, JComponent>();
		_firstUpdate = true;
		_dataTypeFamily = propertyDescriptor.getInputColumnDataTypeFamily();
		getAnalysisJobBuilder().getSourceColumnListeners().add(this);
		getAnalysisJobBuilder().getTransformerChangeListeners().add(this);
		setLayout(new VerticalLayout(2));

		_searchDatastoreTextField = WidgetFactory.createTextField("Search/filter columns");
		_searchDatastoreTextField.setBorder(WidgetUtils.BORDER_THIN);
		_searchDatastoreTextField.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent event) {
				String text = _searchDatastoreTextField.getText();
				if (StringUtils.isNullOrEmpty(text)) {
					// when there is no search query, set all datastores visible
					for (JCheckBox cb : _checkBoxes.values()) {
						cb.setVisible(true);
					}
				} else {
					// do a case insensitive search
					text = text.trim().toLowerCase();
					for (JCheckBox cb : _checkBoxes.values()) {
						String name = cb.getText().toLowerCase();
						cb.setVisible(name.indexOf(text) != -1);
					}
				}
			}
		});

		_notAvailableCheckBox = new DCCheckBox<InputColumn<?>>("- no columns available -", false);
		_notAvailableCheckBox.setEnabled(false);

		_buttonPanel = new DCPanel();
		_buttonPanel.setLayout(new HorizontalLayout(2));

		JButton selectAllButton = new JButton("Select all");
		selectAllButton.addActionListener(selectAllActionListener);
		_buttonPanel.add(selectAllButton);

		JButton selectNoneButton = new JButton("Select none");
		selectNoneButton.addActionListener(selectNoneActionListener);
		_buttonPanel.add(selectNoneButton);

		if (propertyDescriptor.isArray()) {
			if (_dataTypeFamily == DataTypeFamily.STRING || _dataTypeFamily == DataTypeFamily.UNDEFINED) {
				final JButton expressionColumnButton = WidgetFactory
						.createSmallButton(IconUtils.BUTTON_EXPRESSION_COLUMN_IMAGEPATH);
				expressionColumnButton.setToolTipText("Create expression/value based column");
				expressionColumnButton.addActionListener(AddExpressionBasedColumnActionListener.forMultipleColumns(this));
				_buttonPanel.add(expressionColumnButton);
			}

			final JButton reorderColumnsButton = WidgetFactory.createSmallButton(IconUtils.BUTTON_REORDER_COLUMN_IMAGEPATH);
			reorderColumnsButton.setToolTipText("Reorder columns");
			reorderColumnsButton.addActionListener(new ReorderColumnsActionListener(this));
			_buttonPanel.add(reorderColumnsButton);
		}

		add(_buttonPanel);
		add(_searchDatastoreTextField);
	}

	@Override
	public void initialize(InputColumn<?>[] value) {
		updateComponents(value);
		_firstUpdate = false;
		if (value != null && value.length > 0) {
			reorderValue(value);
		}
	}

	protected boolean isAllInputColumnsSelectedIfNoValueExist() {
		return true;
	}

	private void updateComponents(final InputColumn<?>[] value) {
		// fetch available input columns
		Class<?> typeArgument = getPropertyDescriptor().getTypeArgument(0);
		final List<InputColumn<?>> availableColumns = getAnalysisJobBuilder().getAvailableInputColumns(_dataTypeFamily,
				typeArgument);

		if (getBeanJobBuilder() instanceof TransformerJobBuilder) {
			// remove all the columns that are generated by the transformer
			// itself!
			TransformerJobBuilder<?> tjb = (TransformerJobBuilder<?>) getBeanJobBuilder();
			List<MutableInputColumn<?>> outputColumns = tjb.getOutputColumns();
			availableColumns.removeAll(outputColumns);
		}

		Set<InputColumn<?>> inputColumnsToBeRemoved = new HashSet<InputColumn<?>>();
		inputColumnsToBeRemoved.addAll(_checkBoxes.keySet());

		if (value != null) {
			// retain selected expression based input columns
			for (InputColumn<?> col : value) {
				if (col instanceof ExpressionBasedInputColumn) {
					inputColumnsToBeRemoved.remove(col);
					availableColumns.add(col);
				}
			}
		}

		for (InputColumn<?> col : availableColumns) {
			inputColumnsToBeRemoved.remove(col);
			DCCheckBox<InputColumn<?>> checkBox = _checkBoxes.get(col);
			if (checkBox == null) {
				addAvailableInputColumn(col, isEnabled(col, value));
			} else {
				// handle updated names from transformed columns.
				checkBox.setText(col.getName());
			}
		}

		for (InputColumn<?> col : inputColumnsToBeRemoved) {
			removeAvailableInputColumn(col);
		}

		updateVisibility();
		fireValueChanged();
	}

	private void updateVisibility() {
		_searchDatastoreTextField.setVisible(_checkBoxes.size() > 5);
		if (_checkBoxes.isEmpty()) {
			add(_notAvailableCheckBox);
		} else {
			remove(_notAvailableCheckBox);
		}
	}

	/**
	 * Method that allows decorating a checkbox for an input column. Subclasses
	 * can eg. wrap the checkbox in a panel, in order to make additional widgets
	 * available.
	 * 
	 * @param checkBox
	 * @return a {@link JComponent} to add to the widget's parent.
	 */
	protected JComponent decorateCheckBox(DCCheckBox<InputColumn<?>> checkBox) {
		return checkBox;
	}

	private boolean isEnabled(InputColumn<?> inputColumn, InputColumn<?>[] currentValue) {
		if (_firstUpdate) {
			if (currentValue == null || currentValue.length == 0) {
				// set all to true if this is the only required inputcolumn
				// property
				ComponentDescriptor<?> componentDescriptor = getPropertyDescriptor().getComponentDescriptor();
				if (componentDescriptor instanceof BeanDescriptor<?>) {
					if (((BeanDescriptor<?>) componentDescriptor).getConfiguredPropertiesForInput(false).size() == 1) {
						return isAllInputColumnsSelectedIfNoValueExist();
					}
				}
				return false;
			}
		}
		for (InputColumn<?> col : currentValue) {
			if (inputColumn == col) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSet() {
		for (JCheckBox checkBox : _checkBoxes.values()) {
			if (checkBox.isSelected()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public InputColumn<?>[] getValue() {
		List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
		for (DCCheckBox<InputColumn<?>> cb : _checkBoxes.values()) {
			if (cb.isSelected()) {
				InputColumn<?> value = cb.getValue();
				if (value != null) {
					result.add(value);
				}
			}
		}
		return result.toArray(new InputColumn<?>[result.size()]);
	}

	@Override
	public void onAdd(InputColumn<?> sourceColumn) {
		if (_dataTypeFamily == DataTypeFamily.UNDEFINED || _dataTypeFamily == sourceColumn.getDataTypeFamily()) {
			addAvailableInputColumn(sourceColumn);
			updateVisibility();
		}
	}

	@Override
	public void onRemove(InputColumn<?> sourceColumn) {
		removeAvailableInputColumn(sourceColumn);
		updateVisibility();
	}

	@Override
	public void onAdd(TransformerJobBuilder<?> transformerJobBuilder) {
	}

	@Override
	public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder, List<MutableInputColumn<?>> outputColumns) {
		// we need to save the current value before we update the components
		// here. Otherwise any previous selections will be lost.
		final InputColumn<?>[] value = getValue();
		getBeanJobBuilder().setConfiguredProperty(getPropertyDescriptor(), value);

		updateComponents(value);
	}

	@Override
	public void onRemove(TransformerJobBuilder<?> transformerJobBuilder) {
	}

	@Override
	public void onPanelRemove() {
		super.onPanelRemove();
		getAnalysisJobBuilder().getSourceColumnListeners().remove(this);
	}

	@Override
	public void onConfigurationChanged(TransformerJobBuilder<?> transformerJobBuilder) {
	}

	@Override
	public void onRequirementChanged(TransformerJobBuilder<?> transformerJobBuilder) {
	}

	@Override
	protected void setValue(InputColumn<?>[] values) {
		// if checkBoxes is empty it means that the value is being set before
		// initializing the widget. This can occur in subclasses and automatic
		// creating of checkboxes should be done.
		if (_checkBoxes.isEmpty()) {
			for (InputColumn<?> value : values) {
				addAvailableInputColumn(value, true);
			}
		}

		// add expression based input columns if needed.
		for (InputColumn<?> inputColumn : values) {
			if (inputColumn instanceof ExpressionBasedInputColumn && !_checkBoxes.containsKey(inputColumn)) {
				addAvailableInputColumn(inputColumn, true);
			}
		}

		// update selections in checkboxes
		for (DCCheckBox<InputColumn<?>> cb : _checkBoxes.values()) {
			if (ArrayUtils.contains(values, cb.getValue())) {
				cb.setSelected(true);
			} else {
				cb.setSelected(false);
			}
		}

		updateUI();
	}

	protected void selectAll() {
		for (DCCheckBox<InputColumn<?>> cb : _checkBoxes.values()) {
			if (cb.isEnabled()) {
				cb.setSelected(true);
			}
		}
		fireValueChanged();
	}

	protected void selectNone() {
		fireValueChanged();
		for (DCCheckBox<InputColumn<?>> cb : _checkBoxes.values()) {
			cb.setSelected(false);
		}
		fireValueChanged();
	}

	private void addAvailableInputColumn(InputColumn<?> col) {
		addAvailableInputColumn(col, false);
	}

	private void addAvailableInputColumn(InputColumn<?> col, boolean selected) {
		final String name = col.getName();
		final DCCheckBox<InputColumn<?>> cb = new DCCheckBox<InputColumn<?>>(name, selected);
		cb.addListener(checkBoxListener);
		cb.setValue(col);
		_checkBoxes.put(col, cb);
		JComponent decoration = decorateCheckBox(cb);
		_checkBoxDecorations.put(cb, decoration);
		add(decoration);
	}

	private void removeAvailableInputColumn(InputColumn<?> col) {
		boolean valueChanged = false;
		DCCheckBox<InputColumn<?>> checkBox = _checkBoxes.remove(col);
		if (checkBox != null) {
			if (checkBox.isSelected()) {
				valueChanged = true;
			}
			final JComponent decoration = _checkBoxDecorations.remove(checkBox);
			remove(decoration);
		}

		if (valueChanged) {
			fireValueChanged();
		}
	}

	/**
	 * Reorders the values
	 * 
	 * @param sortedValue
	 */
	public void reorderValue(final InputColumn<?>[] sortedValue) {
		// the offset represents the search textfield and the button panel
		final int offset = 2;

		// reorder the visual components
		for (int i = 0; i < sortedValue.length; i++) {
			InputColumn<?> inputColumn = sortedValue[i];
			DCCheckBox<InputColumn<?>> checkBox = _checkBoxes.get(inputColumn);
			JComponent decoration = _checkBoxDecorations.get(checkBox);
			add(decoration, i + offset);
		}
		updateUI();

		// recreate the _checkBoxes map
		final TreeMap<InputColumn<?>, DCCheckBox<InputColumn<?>>> checkBoxesCopy = new TreeMap<InputColumn<?>, DCCheckBox<InputColumn<?>>>(
				_checkBoxes);
		_checkBoxes.clear();
		for (InputColumn<?> inputColumn : sortedValue) {
			_checkBoxes.put(inputColumn, checkBoxesCopy.get(inputColumn));
		}
		_checkBoxes.putAll(checkBoxesCopy);
	}

	public Map<InputColumn<?>, DCCheckBox<InputColumn<?>>> getCheckBoxes() {
		return Collections.unmodifiableMap(_checkBoxes);
	}

	public Map<DCCheckBox<InputColumn<?>>, JComponent> getCheckBoxDecorations() {
		return Collections.unmodifiableMap(_checkBoxDecorations);
	}
}
