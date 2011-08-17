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
import org.eobjects.datacleaner.actions.AddExpressionBasedColumnActionListener;
import org.eobjects.datacleaner.actions.ReorderColumnsActionListener;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.metamodel.util.CollectionUtils;
import org.jdesktop.swingx.HorizontalLayout;
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
	private final List<InputColumn<?>> _inputColumns;
	private final DCPanel _buttonPanel;

	private volatile JCheckBox[] _checkBoxes;
	private volatile boolean _firstUpdate;

	@Inject
	public MultipleInputColumnsPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		super(beanJobBuilder, propertyDescriptor);
		_inputColumns = new ArrayList<InputColumn<?>>();
		_firstUpdate = true;
		_dataTypeFamily = propertyDescriptor.getInputColumnDataTypeFamily();
		getAnalysisJobBuilder().getSourceColumnListeners().add(this);
		getAnalysisJobBuilder().getTransformerChangeListeners().add(this);
		setLayout(new VerticalLayout(2));

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
		_inputColumns.clear();

		if (value != null) {
			for (InputColumn<?> inputColumn : value) {
				if (!_inputColumns.contains(inputColumn)) {
					_inputColumns.add(inputColumn);
				}
			}
		}

		List<InputColumn<?>> availableColumns = getAnalysisJobBuilder().getAvailableInputColumns(_dataTypeFamily);
		for (InputColumn<?> inputColumn : availableColumns) {
			if (!_inputColumns.contains(inputColumn)) {
				_inputColumns.add(inputColumn);
			}
		}

		add(_buttonPanel);

		if (getBeanJobBuilder() instanceof TransformerJobBuilder) {
			// remove all the columns that are generated by the transformer
			// itself!
			TransformerJobBuilder<?> tjb = (TransformerJobBuilder<?>) getBeanJobBuilder();
			List<MutableInputColumn<?>> outputColumns = tjb.getOutputColumns();
			_inputColumns.removeAll(outputColumns);
		}

		if (_inputColumns.isEmpty()) {
			_checkBoxes = new JCheckBox[1];
			_checkBoxes[0] = new JCheckBox("- no columns available -");
			_checkBoxes[0].setOpaque(false);
			_checkBoxes[0].setEnabled(false);
			add(_checkBoxes[0]);
		} else {
			_checkBoxes = new JCheckBox[_inputColumns.size()];
			int i = 0;
			for (InputColumn<?> inputColumn : _inputColumns) {
				JCheckBox checkBox = new JCheckBox(inputColumn.getName(), isEnabled(inputColumn, value));
				checkBox.setOpaque(false);
				checkBox.addActionListener(checkBoxActionListener);
				_checkBoxes[i] = checkBox;
				add(checkBox);
				i++;
			}
		}

		fireValueChanged();
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
		for (int i = 0; i < _checkBoxes.length; i++) {
			if (_checkBoxes[i].isSelected()) {
				result.add(_inputColumns.get(i));
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
