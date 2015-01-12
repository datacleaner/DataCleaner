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

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AbstractBeanJobBuilder;
import org.datacleaner.job.builder.TransformerJobBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.widgets.DCCheckBox;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.DCComboBox.Listener;
import org.datacleaner.widgets.SchemaStructureComboBoxListRenderer;
import org.datacleaner.widgets.SourceColumnComboBox;

/**
 * A specialized property widget for multiple input columns that are mapped to
 * another set of input columns. This widget looks like the
 * {@link MultipleInputColumnsPropertyWidget}, but is enhanced with input column
 * combo boxes.
 */
public class MultipleMappedInputColumnsPropertyWidget extends MultipleInputColumnsPropertyWidget {

    public class MappedInputColumnsPropertyWidget extends MinimalPropertyWidget<InputColumn<?>[]> {

        public MappedInputColumnsPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
                ConfiguredPropertyDescriptor propertyDescriptor) {
            super(beanJobBuilder, propertyDescriptor);
        }

        @Override
        public JComponent getWidget() {
            // do not return a visual widget
            return null;
        }

        @Override
        public boolean isSet() {
            return MultipleMappedInputColumnsPropertyWidget.this.isSet();
        }

        @Override
        public InputColumn<?>[] getValue() {
            return getMappedInputColumns();
        }

        @Override
        protected void setValue(InputColumn<?>[] value) {
            if (MultipleMappedInputColumnsPropertyWidget.this.isUpdating()) {
                return;
            }
            setMappedInputColumns(value);
        }
    }

    private final WeakHashMap<InputColumn<?>, DCComboBox<InputColumn<?>>> _mappedInputColumnComboBoxes;
    private final ConfiguredPropertyDescriptor _mappedInputColumnsProperty;
    private final MappedInputColumnsPropertyWidget _mappedInputColumnsPropertyWidget;

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

        _mappedInputColumnsPropertyWidget = new MappedInputColumnsPropertyWidget(beanJobBuilder, mappedColumnsProperty);

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
                getBeanJobBuilder(), Object.class);

        final Collection<DCComboBox<InputColumn<?>>> comboBoxes = _mappedInputColumnComboBoxes.values();
        for (DCComboBox<InputColumn<?>> comboBox : comboBoxes) {
            comboBox.setModel(new DefaultComboBoxModel<InputColumn<?>>(
                    new Vector<InputColumn<?>>(availableInputColumns)));
        }
    }

    @Override
    protected boolean isAllInputColumnsSelectedIfNoValueExist() {
        return false;
    }

    private DCComboBox<InputColumn<?>> createComboBox(InputColumn<?> inputColumn, InputColumn<?> mappedColumn) {
        final List<InputColumn<?>> availableInputColumns = getAnalysisJobBuilder().getAvailableInputColumns(
                getBeanJobBuilder(), Object.class);

        final DCComboBox<InputColumn<?>> comboBox = new DCComboBox<InputColumn<?>>(availableInputColumns);
        if (mappedColumn != null) {
            comboBox.setSelectedItem(mappedColumn);
        }
        comboBox.setRenderer(new SchemaStructureComboBoxListRenderer());
        _mappedInputColumnComboBoxes.put(inputColumn, comboBox);

        comboBox.addListener(new Listener<InputColumn<?>>() {
            @Override
            public void onItemSelected(InputColumn<?> item) {
                if (isBatchUpdating()) {
                    return;
                }
                fireValueChanged();
                _mappedInputColumnsPropertyWidget.fireValueChanged();
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

    public MappedInputColumnsPropertyWidget getMappedInputColumnsPropertyWidget() {
        return _mappedInputColumnsPropertyWidget;
    }

    public ConfiguredPropertyDescriptor getMappedInputColumnsProperty() {
        return _mappedInputColumnsProperty;
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

    public void setMappedInputColumns(InputColumn<?>[] value) {
        final List<InputColumn<?>> inputColumns = MultipleMappedInputColumnsPropertyWidget.this
                .getSelectedInputColumns();
        for (int i = 0; i < inputColumns.size(); i++) {
            final InputColumn<?> inputColumn = inputColumns.get(i);
            final InputColumn<?> mappedColumn;
            if (value == null) {
                mappedColumn = null;
            } else if (i < value.length) {
                mappedColumn = value[i];
            } else {
                mappedColumn = null;
            }
            final DCComboBox<InputColumn<?>> comboBox = _mappedInputColumnComboBoxes.get(inputColumn);
            comboBox.setVisible(true);
            comboBox.setEditable(true);
            comboBox.setSelectedItem(mappedColumn);
            comboBox.setEditable(false);
        }
    }

    public InputColumn<?>[] getMappedInputColumns() {
        final List<InputColumn<?>> inputColumns = MultipleMappedInputColumnsPropertyWidget.this
                .getSelectedInputColumns();
        final List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
        for (InputColumn<?> inputColumn : inputColumns) {
            final DCComboBox<InputColumn<?>> comboBox = _mappedInputColumnComboBoxes.get(inputColumn);
            if (comboBox == null) {
                result.add(null);
            } else {
                final InputColumn<?> column = comboBox.getSelectedItem();
                result.add(column);
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
