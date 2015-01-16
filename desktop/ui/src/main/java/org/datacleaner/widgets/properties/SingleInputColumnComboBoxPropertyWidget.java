/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.widgets.properties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;
import javax.swing.DefaultComboBoxModel;

import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.SourceColumnChangeListener;
import org.datacleaner.job.builder.TransformerChangeListener;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.DCComboBox.Listener;
import org.datacleaner.widgets.SchemaStructureComboBoxListRenderer;

/**
 * {@link PropertyWidget} for single {@link InputColumn}s. Displays the
 * selection as a ComboBox, used for optional input columns.
 * 
 * @author Kasper Sørensen
 */
public class SingleInputColumnComboBoxPropertyWidget extends AbstractPropertyWidget<InputColumn<?>> implements
        SourceColumnChangeListener, TransformerChangeListener, MutableInputColumn.Listener {

    private final DCComboBox<InputColumn<?>> _comboBox;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final Class<?> _dataType;
    private volatile List<InputColumn<?>> _inputColumns;

    @Inject
    public SingleInputColumnComboBoxPropertyWidget(AnalysisJobBuilder analysisJobBuilder,
            ComponentBuilder componentBuilder, ConfiguredPropertyDescriptor propertyDescriptor) {
        super(componentBuilder, propertyDescriptor);
        _comboBox = new DCComboBox<InputColumn<?>>();
        _comboBox.setRenderer(new SchemaStructureComboBoxListRenderer());
        _comboBox.addListener(new Listener<InputColumn<?>>() {
            @Override
            public void onItemSelected(InputColumn<?> item) {
                fireValueChanged();
            }
        });
        _analysisJobBuilder = analysisJobBuilder;
        _analysisJobBuilder.getSourceColumnListeners().add(this);
        _analysisJobBuilder.getTransformerChangeListeners().add(this);
        _dataType = propertyDescriptor.getTypeArgument(0);

        updateComponents();
        add(_comboBox);

    }

    private void updateComponents() {
        InputColumn<?> currentValue = getCurrentValue();
        updateComponents(currentValue);
    }

    private void updateComponents(InputColumn<?> currentValue) {
        _inputColumns = _analysisJobBuilder.getAvailableInputColumns(getComponentBuilder(), _dataType);

        if (currentValue != null) {
            if (!_inputColumns.contains(currentValue)) {
                _inputColumns.add(currentValue);
            }
        }

        if (!getPropertyDescriptor().isRequired()) {
            _inputColumns = new ArrayList<InputColumn<?>>(_inputColumns);
            _inputColumns.add(0, null);
        }

        for (Iterator<InputColumn<?>> it = _inputColumns.iterator(); it.hasNext();) {
            InputColumn<?> inputColumn = it.next();
            if (inputColumn instanceof MutableInputColumn) {
                MutableInputColumn<?> mutableInputColumn = (MutableInputColumn<?>) inputColumn;
                mutableInputColumn.addListener(this);
                if (mutableInputColumn.isHidden()) {
                    it.remove();
                }
            }
        }

        _comboBox.setModel(new DefaultComboBoxModel<InputColumn<?>>(new Vector<InputColumn<?>>(_inputColumns)));
        _comboBox.setSelectedItem(currentValue);
    }

    @Override
    public void onAdd(InputColumn<?> sourceColumn) {
        if (isColumnApplicable(sourceColumn)) {
            updateComponents();
            updateUI();
        }
    }

    @Override
    public void onRemove(InputColumn<?> sourceColumn) {
        handleRemovedColumn(sourceColumn);
    }

    private void handleRemovedColumn(InputColumn<?> column) {
        if (isColumnApplicable(column)) {
            if (column instanceof MutableInputColumn) {
                ((MutableInputColumn<?>) column).removeListener(this);
            }

            final ConfiguredPropertyDescriptor propertyDescriptor = getPropertyDescriptor();
            final ComponentBuilder componentBuilder = getComponentBuilder();

            final InputColumn<?> currentValue = (InputColumn<?>) componentBuilder
                    .getConfiguredProperty(propertyDescriptor);
            if (currentValue != null) {
                if (currentValue.equals(column)) {
                    componentBuilder.setConfiguredProperty(propertyDescriptor, null);
                }
            }
            updateComponents();
            updateUI();
        }
    }

    private boolean isColumnApplicable(InputColumn<?> column) {
        return _dataType == Object.class || ReflectionUtils.is(column.getDataType(), _dataType);
    }

    @Override
    public void onAdd(TransformerComponentBuilder<?> transformerJobBuilder) {
    }

    @Override
    public void onOutputChanged(TransformerComponentBuilder<?> transformerJobBuilder,
            List<MutableInputColumn<?>> outputColumns) {
        updateComponents();
        updateUI();
    }

    @Override
    public void onRemove(TransformerComponentBuilder<?> transformerJobBuilder) {
        List<MutableInputColumn<?>> outputColumns = transformerJobBuilder.getOutputColumns();
        for (MutableInputColumn<?> column : outputColumns) {
            handleRemovedColumn(column);
        }
    }

    @Override
    public void onPanelRemove() {
        super.onPanelRemove();
        _analysisJobBuilder.getSourceColumnListeners().remove(this);
        _analysisJobBuilder.getTransformerChangeListeners().remove(this);

        for (InputColumn<?> column : _inputColumns) {
            if (column instanceof MutableInputColumn) {
                ((MutableInputColumn<?>) column).removeListener(this);
            }
        }
    }

    @Override
    public InputColumn<?> getValue() {
        return (InputColumn<?>) _comboBox.getSelectedItem();
    }

    @Override
    public void onConfigurationChanged(TransformerComponentBuilder<?> transformerJobBuilder) {
        if (transformerJobBuilder == getComponentBuilder()) {
            return;
        }
        updateComponents();
        updateUI();
    }

    @Override
    public void onRequirementChanged(TransformerComponentBuilder<?> transformerJobBuilder) {
    }

    @Override
    protected void setValue(InputColumn<?> value) {
        updateComponents(value);
        updateUI();
    }

    @Override
    public void onNameChanged(MutableInputColumn<?> inputColumn, String oldName, String newName) {
    }

    @Override
    public void onVisibilityChanged(MutableInputColumn<?> inputColumn, boolean hidden) {
        if (!isColumnApplicable(inputColumn)) {
            return;
        }
        if (inputColumn.equals(_comboBox.getSelectedItem())) {
            // don't hide columns that are selected.
            return;
        }
        if (hidden) {
            _comboBox.removeItem(inputColumn);
        } else {
            if (!_comboBox.containsItem(inputColumn)) {
                _comboBox.addItem(inputColumn);
            }
        }
    }
}
