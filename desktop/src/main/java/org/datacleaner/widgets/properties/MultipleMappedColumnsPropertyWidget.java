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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.JComponent;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.MutableRef;
import org.datacleaner.data.InputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AbstractBeanJobBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.widgets.DCCheckBox;
import org.datacleaner.widgets.DCComboBox.Listener;
import org.datacleaner.widgets.SourceColumnComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A specialized property widget for multiple input columns that are mapped to
 * physical columns. This widget looks like the
 * {@link MultipleInputColumnsPropertyWidget}, but is enhanced with source
 * column combo boxes and awareness of changes to selected table.
 */
public class MultipleMappedColumnsPropertyWidget extends MultipleInputColumnsPropertyWidget {

    public class MappedColumnNamesPropertyWidget extends MinimalPropertyWidget<String[]> {

        public MappedColumnNamesPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
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
            return MultipleMappedColumnsPropertyWidget.this.isSet();
        }

        @Override
        public String[] getValue() {
            return getMappedColumnNames();
        }

        @Override
        protected void setValue(String[] value) {
            if (MultipleMappedColumnsPropertyWidget.this.isUpdating()) {
                return;
            }
            setMappedColumnNames(value);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(MultipleMappedColumnsPropertyWidget.class);

    private final WeakHashMap<InputColumn<?>, SourceColumnComboBox> _mappedColumnComboBoxes;
    private final MutableRef<Table> _tableRef;
    private final ConfiguredPropertyDescriptor _mappedColumnsProperty;
    private final MappedColumnNamesPropertyWidget _mappedColumnNamesPropertyWidget;

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
    public MultipleMappedColumnsPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor inputColumnsProperty, ConfiguredPropertyDescriptor mappedColumnsProperty) {
        super(beanJobBuilder, inputColumnsProperty);
        _mappedColumnComboBoxes = new WeakHashMap<InputColumn<?>, SourceColumnComboBox>();
        _mappedColumnsProperty = mappedColumnsProperty;

        _tableRef = new MutableRef<Table>();
        _mappedColumnNamesPropertyWidget = new MappedColumnNamesPropertyWidget(beanJobBuilder, mappedColumnsProperty);

        final InputColumn<?>[] currentValue = getCurrentValue();
        final String[] currentMappedColumnsValue = (String[]) beanJobBuilder
                .getConfiguredProperty(mappedColumnsProperty);
        if (currentValue != null && currentMappedColumnsValue != null) {
            // first create combo's, then set value (so combo is ready before it
            // is requested)

            _mappedColumnNamesPropertyWidget.setValue(currentMappedColumnsValue);
            final int minLength = Math.min(currentValue.length, currentMappedColumnsValue.length);
            for (int i = 0; i < minLength; i++) {
                final InputColumn<?> inputColumn = currentValue[i];
                final String mappedColumnName = currentMappedColumnsValue[i];
                createComboBox(inputColumn, new MutableColumn(mappedColumnName));
            }

            setValue(currentValue);
        }
    }

    public void setMappedColumnNames(String[] mappedColumnNames) {
        if (logger.isDebugEnabled()) {
            logger.debug("setMappedColumnNames({})", Arrays.toString(mappedColumnNames));
        }

        final List<InputColumn<?>> inputColumns = MultipleMappedColumnsPropertyWidget.this.getSelectedInputColumns();
        for (int i = 0; i < inputColumns.size(); i++) {
            final InputColumn<?> inputColumn = inputColumns.get(i);
            final String mappedColumnName;
            if (mappedColumnNames == null) {
                mappedColumnName = null;
            } else if (i < mappedColumnNames.length) {
                mappedColumnName = mappedColumnNames[i];
            } else {
                mappedColumnName = null;
            }
            final SourceColumnComboBox comboBox = _mappedColumnComboBoxes.get(inputColumn);
            comboBox.setVisible(true);
            comboBox.setEditable(true);
            comboBox.setSelectedItem(mappedColumnName);
            comboBox.setEditable(false);
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
        final Set<Entry<InputColumn<?>, SourceColumnComboBox>> entrySet = _mappedColumnComboBoxes.entrySet();

        batchUpdateWidget(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    private SourceColumnComboBox createComboBox(InputColumn<?> inputColumn, Column mappedColumn) {
        final SourceColumnComboBox sourceColumnComboBox = new SourceColumnComboBox();
        _mappedColumnComboBoxes.put(inputColumn, sourceColumnComboBox);

        Table table = _tableRef.get();
        if (mappedColumn == null && table != null) {
            mappedColumn = getDefaultMappedColumn(inputColumn, table);
        }

        if (mappedColumn == null) {
            logger.info("No default mapping found for column: {}", inputColumn);
        } else {
            sourceColumnComboBox.setEditable(true);
            sourceColumnComboBox.setSelectedItem(mappedColumn);
            sourceColumnComboBox.setEditable(false);
        }
        sourceColumnComboBox.addColumnSelectedListener(new Listener<Column>() {
            @Override
            public void onItemSelected(Column item) {
                if (isBatchUpdating()) {
                    return;
                }
                _mappedColumnNamesPropertyWidget.fireValueChanged();
                fireValueChanged();
            }
        });
        return sourceColumnComboBox;
    }

    protected Column getDefaultMappedColumn(InputColumn<?> inputColumn, Table table) {
        // automatically select a column by name, if it exists
        return table.getColumnByName(inputColumn.getName());
    }

    /**
     * Gets the {@link ConfiguredPropertyDescriptor} of the property that has
     * the column names that are being mapped to.
     * 
     * @return
     */
    public ConfiguredPropertyDescriptor getMappedColumnsProperty() {
        return _mappedColumnsProperty;
    }

    @Override
    protected JComponent decorateCheckBox(final DCCheckBox<InputColumn<?>> checkBox) {
        final SourceColumnComboBox sourceColumnComboBox;
        final InputColumn<?> inputColumn = checkBox.getValue();
        if (_mappedColumnComboBoxes.containsKey(inputColumn)) {
            sourceColumnComboBox = _mappedColumnComboBoxes.get(inputColumn);
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
                _mappedColumnNamesPropertyWidget.fireValueChanged();
            }
        });

        Table table = _tableRef.get();
        if (table != null) {
            sourceColumnComboBox.setModel(table);
        }

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
            SourceColumnComboBox sourceColumnComboBox) {
        return sourceColumnComboBox;
    }

    public MappedColumnNamesPropertyWidget getMappedColumnNamesPropertyWidget() {
        return _mappedColumnNamesPropertyWidget;
    }

    public String[] getMappedColumnNames() {
        final List<InputColumn<?>> selectedInputColumns = getSelectedInputColumns();
        final List<String> result = new ArrayList<String>();
        for (InputColumn<?> inputColumn : selectedInputColumns) {
            final SourceColumnComboBox comboBox = _mappedColumnComboBoxes.get(inputColumn);
            if (comboBox == null) {
                logger.warn("No SourceColumnComboBox found for input column: {}", inputColumn);
                result.add(null);
            } else {
                final Column column = comboBox.getSelectedItem();
                if (column == null) {
                    result.add(null);
                } else {
                    result.add(column.getName());
                }
            }
        }

        logger.debug("getMappedColumnNames() returning: {}", result);

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
                Collection<SourceColumnComboBox> comboBoxes = _mappedColumnComboBoxes.values();
                for (SourceColumnComboBox comboBox : comboBoxes) {
                    comboBox.setVisible(true);
                }
                MultipleMappedColumnsPropertyWidget.super.selectAll();
            }
        });
    }

    @Override
    protected void selectNone() {
        for (SourceColumnComboBox sourceColumnComboBox : _mappedColumnComboBoxes.values()) {
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
