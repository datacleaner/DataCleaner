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
package org.eobjects.datacleaner.widgets.properties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;
import javax.swing.DefaultComboBoxModel;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.SourceColumnChangeListener;
import org.eobjects.analyzer.job.builder.TransformerChangeListener;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.SchemaStructureComboBoxListRenderer;

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
            AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder, ConfiguredPropertyDescriptor propertyDescriptor) {
        super(beanJobBuilder, propertyDescriptor);
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
        _inputColumns = _analysisJobBuilder.getAvailableInputColumns(getBeanJobBuilder(), _dataType);

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
            final AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder = getBeanJobBuilder();

            final InputColumn<?> currentValue = (InputColumn<?>) beanJobBuilder
                    .getConfiguredProperty(propertyDescriptor);
            if (currentValue != null) {
                if (currentValue.equals(column)) {
                    beanJobBuilder.setConfiguredProperty(propertyDescriptor, null);
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
    public void onAdd(TransformerJobBuilder<?> transformerJobBuilder) {
    }

    @Override
    public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder,
            List<MutableInputColumn<?>> outputColumns) {
        updateComponents();
        updateUI();
    }

    @Override
    public void onRemove(TransformerJobBuilder<?> transformerJobBuilder) {
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
    public void onConfigurationChanged(TransformerJobBuilder<?> transformerJobBuilder) {
        if (transformerJobBuilder == getBeanJobBuilder()) {
            return;
        }
        updateComponents();
        updateUI();
    }

    @Override
    public void onRequirementChanged(TransformerJobBuilder<?> transformerJobBuilder) {
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
