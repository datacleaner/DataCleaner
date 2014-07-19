/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
import java.util.WeakHashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.widgets.DCCheckBox;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.SchemaStructureComboBoxListRenderer;
import org.eobjects.datacleaner.widgets.SourceColumnComboBox;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.EqualsBuilder;

/**
 * A specialized property widget for multiple input columns that are mapped to
 * another set of input columns. This widget looks like the
 * {@link MultipleInputColumnsPropertyWidget}, but is enhanced with input column
 * combo boxes.
 * 
 * @author Kasper Sørensen
 */
public class MultipleMappedInputColumnsPropertyWidget extends MultipleInputColumnsPropertyWidget {

    private final WeakHashMap<InputColumn<?>, DCComboBox<InputColumn<?>>> _mappedInputColumnComboBoxes;
    private final ConfiguredPropertyDescriptor _mappedInputColumnsProperty;
    private final MinimalPropertyWidget<InputColumn<?>[]> _mappedInputColumnsPropertyWidget;

    // indicates whether there is currently undergoing a input column listener
    // action
    private volatile boolean _mappedInputColumnUpdating;

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
    public MultipleMappedInputColumnsPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor inputColumnsProperty, ConfiguredPropertyDescriptor mappedColumnsProperty) {
        super(beanJobBuilder, inputColumnsProperty);
        _mappedInputColumnComboBoxes = new WeakHashMap<InputColumn<?>, DCComboBox<InputColumn<?>>>();
        _mappedInputColumnsProperty = mappedColumnsProperty;

        _mappedInputColumnsPropertyWidget = createMappedColumnNamesPropertyWidget();
        _mappedInputColumnUpdating = false;

        final InputColumn<?>[] currentValue = getCurrentValue();
        final InputColumn<?>[] currentMappedColumnsValue = (InputColumn<?>[]) beanJobBuilder
                .getConfiguredProperty(mappedColumnsProperty);

        if (currentValue != null && currentMappedColumnsValue != null) {
            _mappedInputColumnsPropertyWidget.setValue(currentMappedColumnsValue);

            // first create combo's, then set value (so combo is ready before it
            // is requested)
            final int minLength = Math.min(currentValue.length, currentMappedColumnsValue.length);
            for (int i = 0; i < minLength; i++) {
                final InputColumn<?> inputColumn = currentValue[i];
                final InputColumn<?> mappedColumn = currentMappedColumnsValue[i];
                createComboBox(inputColumn, mappedColumn);
            }

            setValue(currentValue);
        }
    }

    @Override
    public void onAdd(InputColumn<?> column) {
        super.onAdd(column);

        final Collection<DCComboBox<InputColumn<?>>> comboBoxes = _mappedInputColumnComboBoxes.values();
        for (DCComboBox<InputColumn<?>> comboBox : comboBoxes) {
            comboBox.addItem(column);
        }
    }

    @Override
    public void onRemove(InputColumn<?> column) {
        super.onRemove(column);

        final Collection<DCComboBox<InputColumn<?>>> comboBoxes = _mappedInputColumnComboBoxes.values();
        for (DCComboBox<InputColumn<?>> comboBox : comboBoxes) {
            comboBox.removeItem(column);
        }
    }

    @Override
    public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder,
            List<MutableInputColumn<?>> outputColumns) {
        super.onOutputChanged(transformerJobBuilder, outputColumns);

        final List<InputColumn<?>> availableInputColumns = getAnalysisJobBuilder().getAvailableInputColumns(
                Object.class);
        if (getBeanJobBuilder() instanceof TransformerJobBuilder) {
            // remove all the columns that are generated by the transformer
            // itself!
            TransformerJobBuilder<?> tjb = (TransformerJobBuilder<?>) getBeanJobBuilder();
            List<MutableInputColumn<?>> ownOutputColumns = tjb.getOutputColumns();
            availableInputColumns.removeAll(ownOutputColumns);
        }

        final Collection<DCComboBox<InputColumn<?>>> comboBoxes = _mappedInputColumnComboBoxes.values();
        final Object[] availableInputColumnsArray = availableInputColumns.toArray();
        for (DCComboBox<InputColumn<?>> comboBox : comboBoxes) {
            comboBox.setModel(new DefaultComboBoxModel(availableInputColumnsArray));
        }
    }

    @Override
    protected boolean isAllInputColumnsSelectedIfNoValueExist() {
        return false;
    }

    private DCComboBox<InputColumn<?>> createComboBox(InputColumn<?> inputColumn, InputColumn<?> mappedColumn) {
        final List<InputColumn<?>> availableInputColumns = getAnalysisJobBuilder().getAvailableInputColumns(
                Object.class);

        final DCComboBox<InputColumn<?>> comboBox = new DCComboBox<InputColumn<?>>(availableInputColumns);
        comboBox.setRenderer(new SchemaStructureComboBoxListRenderer());
        _mappedInputColumnComboBoxes.put(inputColumn, comboBox);

        comboBox.addListener(new Listener<InputColumn<?>>() {
            @Override
            public void onItemSelected(InputColumn<?> item) {
                if (isBatchUpdating()) {
                    return;
                }
                _mappedInputColumnUpdating = true;
                fireValueChanged();
                _mappedInputColumnsPropertyWidget.fireValueChanged();
                _mappedInputColumnUpdating = false;
            }
        });
        return comboBox;
    }

    protected Column getDefaultMappedColumn(InputColumn<?> inputColumn, Table table) {
        // automatically select a column by name, if it exists
        return table.getColumnByName(inputColumn.getName());
    }

    @Override
    protected JComponent decorateCheckBox(final DCCheckBox<InputColumn<?>> checkBox) {
        final DCComboBox<InputColumn<?>> sourceColumnComboBox;
        final InputColumn<?> inputColumn = checkBox.getValue();
        if (_mappedInputColumnComboBoxes.containsKey(inputColumn)) {
            sourceColumnComboBox = _mappedInputColumnComboBoxes.get(inputColumn);
        } else {
            sourceColumnComboBox = createComboBox(inputColumn, null);
        }

        final JComponent decoratedSourceColumnComboBox = decorateSourceColumnComboBox(inputColumn, sourceColumnComboBox);

        checkBox.addListenerToHead(new DCCheckBox.Listener<InputColumn<?>>() {
            @Override
            public void onItemSelected(InputColumn<?> item, boolean selected) {
                _mappedInputColumnUpdating = true;
                decoratedSourceColumnComboBox.setVisible(selected);
            }
        });
        checkBox.addListener(new DCCheckBox.Listener<InputColumn<?>>() {
            @Override
            public void onItemSelected(InputColumn<?> item, boolean selected) {
                if (isBatchUpdating()) {
                    return;
                }
                _mappedInputColumnsPropertyWidget.fireValueChanged();
                _mappedInputColumnUpdating = false;
            }
        });

        decoratedSourceColumnComboBox.setVisible(checkBox.isSelected());

        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(checkBox, BorderLayout.CENTER);
        panel.add(decoratedSourceColumnComboBox, BorderLayout.EAST);
        return panel;
    }

    /**
     * Method which decorates the UI component of an inserted
     * {@link SourceColumnComboBox}. Subclasses can override this method if eg.
     * additional widgets should be added.
     * 
     * @param sourceColumnComboBox
     * @return
     */
    protected JComponent decorateSourceColumnComboBox(InputColumn<?> inputColumn,
            DCComboBox<InputColumn<?>> sourceColumnComboBox) {
        return sourceColumnComboBox;
    }

    public PropertyWidget<InputColumn<?>[]> getMappedInputColumnsPropertyWidget() {
        return _mappedInputColumnsPropertyWidget;
    }

    private MinimalPropertyWidget<InputColumn<?>[]> createMappedColumnNamesPropertyWidget() {
        MinimalPropertyWidget<InputColumn<?>[]> propertyWidget = new MinimalPropertyWidget<InputColumn<?>[]>(
                getBeanJobBuilder(), _mappedInputColumnsProperty) {

            @Override
            public JComponent getWidget() {
                // do not return a visual widget
                return null;
            }

            @Override
            public boolean isSet() {
                final InputColumn<?>[] inputColumns = MultipleMappedInputColumnsPropertyWidget.this.getValue();
                for (InputColumn<?> inputColumn : inputColumns) {
                    DCComboBox<InputColumn<?>> comboBox = _mappedInputColumnComboBoxes.get(inputColumn);
                    if (comboBox.getSelectedItem() == null) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public InputColumn<?>[] getValue() {
                return getMappedColumns();
            }

            @Override
            protected void setValue(InputColumn<?>[] value) {
                if (_mappedInputColumnUpdating) {
                    // setValue of the mapped columns will be called prematurely
                    // (with previous value) by change notifications of the
                    // input columns property.
                    return;
                }
                if (EqualsBuilder.equals(value, getValue())) {
                    return;
                }
                final InputColumn<?>[] inputColumns = MultipleMappedInputColumnsPropertyWidget.this.getValue();
                for (int i = 0; i < inputColumns.length; i++) {
                    final InputColumn<?> inputColumn = inputColumns[i];
                    final InputColumn<?> mappedColumn;
                    if (value == null) {
                        mappedColumn = null;
                    } else if (i < value.length) {
                        mappedColumn = value[i];
                    } else {
                        mappedColumn = null;
                    }
                    final DCComboBox<InputColumn<?>> comboBox = _mappedInputColumnComboBoxes.get(inputColumn);
                    comboBox.setEditable(true);
                    comboBox.setSelectedItem(mappedColumn);
                    comboBox.setEditable(false);
                }
            }
        };

        return propertyWidget;
    }

    @Override
    public InputColumn<?>[] getValue() {
        final InputColumn<?>[] checkedInputColumns = super.getValue();
        final List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
        for (InputColumn<?> inputColumn : checkedInputColumns) {
            // exclude input columns that have not been mapped yet
            final DCComboBox<InputColumn<?>> comboBox = _mappedInputColumnComboBoxes.get(inputColumn);
            if (comboBox != null) {
                if (comboBox.getSelectedItem() != null) {
                    result.add(inputColumn);
                }
            }
        }
        return result.toArray(new InputColumn[result.size()]);
    }

    private InputColumn<?>[] getMappedColumns() {
        final InputColumn<?>[] inputColumns = MultipleMappedInputColumnsPropertyWidget.this.getValue();
        final List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
        for (InputColumn<?> inputColumn : inputColumns) {
            DCComboBox<InputColumn<?>> comboBox = _mappedInputColumnComboBoxes.get(inputColumn);
            if (comboBox != null) {
                InputColumn<?> column = comboBox.getSelectedItem();
                if (column != null) {
                    result.add(column);
                }
            }
        }
        return result.toArray(new InputColumn[result.size()]);
    }

    public Map<InputColumn<?>, DCComboBox<InputColumn<?>>> getMappedColumnComboBoxes() {
        return Collections.unmodifiableMap(_mappedInputColumnComboBoxes);
    }

    @Override
    protected void selectAll() {
        batchUpdateWidget(new Runnable() {
            @Override
            public void run() {
                Collection<DCComboBox<InputColumn<?>>> comboBoxes = _mappedInputColumnComboBoxes.values();
                for (DCComboBox<InputColumn<?>> comboBox : comboBoxes) {
                    comboBox.setVisible(true);
                }
                MultipleMappedInputColumnsPropertyWidget.super.selectAll();
            }
        });
    }

    @Override
    protected void selectNone() {
        for (DCComboBox<InputColumn<?>> comboBox : _mappedInputColumnComboBoxes.values()) {
            comboBox.setVisible(false);
        }
        super.selectNone();
    }

    @Override
    protected void onBatchFinished() {
        super.onBatchFinished();
        _mappedInputColumnsPropertyWidget.fireValueChanged();
    }
}