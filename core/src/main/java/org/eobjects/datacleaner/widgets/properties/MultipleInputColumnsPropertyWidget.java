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
import java.util.List;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;

import org.eobjects.analyzer.data.DataTypeFamily;
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
import org.eobjects.metamodel.util.CollectionUtils;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Property widget for multiple input columns. Displays these as checkboxes.
 * 
 * @author Kasper Sørensen
 * 
 */
public class MultipleInputColumnsPropertyWidget extends AbstractPropertyWidget<InputColumn<?>[]> implements
		SourceColumnChangeListener, TransformerChangeListener {

	private static final long serialVersionUID = 1L;

	private final ActionListener checkBoxActionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			fireValueChanged();
		}
	};

	private final ActionListener selectAllActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (JCheckBox cb : _checkBoxes) {
				if (cb.isEnabled()) {
					cb.setSelected(true);
				}
			}
			fireValueChanged();
		}
	};

	private final ActionListener selectNoneActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (JCheckBox cb : _checkBoxes) {
				cb.setSelected(false);
			}
			fireValueChanged();
		}
	};

	private final DataTypeFamily _dataTypeFamily;
	private final List<DCCheckBox<InputColumn<?>>> _checkBoxes;
	private final DCPanel _buttonPanel;
	private final JXTextField _searchDatastoreTextField;

	private volatile boolean _firstUpdate;

	@Inject
	public MultipleInputColumnsPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		super(beanJobBuilder, propertyDescriptor);
		_checkBoxes = new ArrayList<DCCheckBox<InputColumn<?>>>();
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
					for (JCheckBox cb : _checkBoxes) {
						cb.setVisible(true);
					}
				} else {
					// do a case insensitive search
					text = text.trim().toLowerCase();
					for (JCheckBox cb : _checkBoxes) {
						String name = cb.getText().toLowerCase();
						cb.setVisible(name.indexOf(text) != -1);
					}
				}
			}
		});

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
				expressionColumnButton.addActionListener(new AddExpressionBasedColumnActionListener(this));
				_buttonPanel.add(expressionColumnButton);
			}

			final JButton reorderColumnsButton = WidgetFactory.createSmallButton(IconUtils.BUTTON_REORDER_COLUMN_IMAGEPATH);
			reorderColumnsButton.setToolTipText("Reorder columns");
			reorderColumnsButton.addActionListener(new ReorderColumnsActionListener(propertyDescriptor, beanJobBuilder));
			_buttonPanel.add(reorderColumnsButton);
		}

		updateComponents();
		_firstUpdate = false;
	}

	private void updateComponents() {
		InputColumn<?>[] currentValue = (InputColumn<?>[]) getBeanJobBuilder()
				.getConfiguredProperty(getPropertyDescriptor());
		updateComponents(currentValue);
	}

	private void updateComponents(InputColumn<?>[] value) {
		removeAll();
		final List<InputColumn<?>> availableColumns = getAnalysisJobBuilder().getAvailableInputColumns(_dataTypeFamily);
		_checkBoxes.clear();

		if (value != null) {
			for (InputColumn<?> col : value) {
				DCCheckBox<InputColumn<?>> cb = new DCCheckBox<InputColumn<?>>(col.getName(), isEnabled(col, value));
				cb.setValue(col);
				_checkBoxes.add(cb);
			}
		}

		if (getBeanJobBuilder() instanceof TransformerJobBuilder) {
			// remove all the columns that are generated by the transformer
			// itself!
			TransformerJobBuilder<?> tjb = (TransformerJobBuilder<?>) getBeanJobBuilder();
			List<MutableInputColumn<?>> outputColumns = tjb.getOutputColumns();
			availableColumns.removeAll(outputColumns);
		}

		for (InputColumn<?> col : availableColumns) {
			boolean found = false;
			for (DCCheckBox<InputColumn<?>> cb : _checkBoxes) {
				if (cb.getValue().equals(col)) {
					found = true;
					break;
				}
			}

			if (!found) {
				DCCheckBox<InputColumn<?>> cb = new DCCheckBox<InputColumn<?>>(col.getName(), isEnabled(col, value));
				cb.setValue(col);
				_checkBoxes.add(cb);
			}
		}

		_searchDatastoreTextField.setVisible(_checkBoxes.size() > 5);

		add(_buttonPanel);
		add(_searchDatastoreTextField);

		if (_checkBoxes.isEmpty()) {
			DCCheckBox<InputColumn<?>> checkBox = new DCCheckBox<InputColumn<?>>("- no columns available -", false);
			checkBox.setEnabled(false);
			_checkBoxes.add(checkBox);
			add(decorateCheckBox(checkBox));
		} else {
			for (DCCheckBox<InputColumn<?>> checkBox : _checkBoxes) {
				checkBox.addActionListener(checkBoxActionListener);
				add(decorateCheckBox(checkBox));
			}
		}

		fireValueChanged();
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
						return true;
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
		for (JCheckBox checkBox : _checkBoxes) {
			if (checkBox.isSelected()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public InputColumn<?>[] getValue() {
		List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
		for (DCCheckBox<InputColumn<?>> cb : _checkBoxes) {
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
			updateComponents();
			updateUI();
		}
	}

	@Override
	public void onRemove(InputColumn<?> sourceColumn) {
		if (_dataTypeFamily == DataTypeFamily.UNDEFINED || _dataTypeFamily == sourceColumn.getDataTypeFamily()) {
			InputColumn<?>[] currentValue = (InputColumn<?>[]) getBeanJobBuilder().getConfiguredProperty(
					getPropertyDescriptor());
			if (currentValue != null) {
				currentValue = CollectionUtils.arrayRemove(currentValue, sourceColumn);
				getBeanJobBuilder().setConfiguredProperty(getPropertyDescriptor(), currentValue);
			}
			updateComponents();
			updateUI();
		}
	}

	@Override
	public void onAdd(TransformerJobBuilder<?> transformerJobBuilder) {
	}

	@Override
	public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder, List<MutableInputColumn<?>> outputColumns) {
		// we need to save the current value before we update the components
		// here. Otherwise any previous selections will be lost.
		getBeanJobBuilder().setConfiguredProperty(getPropertyDescriptor(), getValue());

		updateComponents();
		updateUI();
	}

	@Override
	public void onRemove(TransformerJobBuilder<?> transformerJobBuilder) {
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		getAnalysisJobBuilder().getSourceColumnListeners().remove(this);
	}

	@Override
	public void onConfigurationChanged(TransformerJobBuilder<?> transformerJobBuilder) {
	}

	@Override
	public void onRequirementChanged(TransformerJobBuilder<?> transformerJobBuilder) {
	}

	@Override
	protected void setValue(InputColumn<?>[] value) {
		updateComponents(value);
		updateUI();
	}
}
