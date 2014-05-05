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

import javax.inject.Inject;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.SourceColumnComboBox;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.MutableRef;

/**
 * Property widget for a {@link String} that represents a {@link Column} name.
 */
public final class ColumnNamePropertyWidget extends AbstractPropertyWidget<String> {

    private final SourceColumnComboBox _comboBox;
    private final MutableRef<Table> _tableRef;

    @Inject
    public ColumnNamePropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
            AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
        super(beanJobBuilder, propertyDescriptor);

        _tableRef = new MutableRef<Table>();

        Enum<?>[] enumConstants = (Enum<?>[]) propertyDescriptor.getType().getEnumConstants();

        if (!propertyDescriptor.isRequired()) {
            enumConstants = CollectionUtils.array(new Enum<?>[] { null }, enumConstants);
        }

        _comboBox = new SourceColumnComboBox();

        String currentValue = getCurrentValue();
        setValue(currentValue);

        addComboListener(new Listener<Column>() {
            @Override
            public void onItemSelected(Column item) {
                fireValueChanged();
            }
        });
        add(_comboBox);
    }

    /**
     * Sets the table to use as a source for the available columns in the
     * combobox.
     * 
     * @param table
     */
    public void setTable(Table table) {
        if (table != _tableRef.get()) {
            _tableRef.set(table);

            if (table == null) {
                _comboBox.setEmptyModel();
            } else {
                _comboBox.setModel(table);
            }
        }
    }

    public void addComboListener(Listener<Column> listener) {
        _comboBox.addListener(listener);
    }

    @Override
    public String getValue() {
        Column column = _comboBox.getSelectedItem();
        if (column == null) {
            return null;
        }
        return column.getName();
    }

    @Override
    protected void setValue(String value) {
        if (value == null) {
            _comboBox.setSelectedItem(null);
        }
        _comboBox.setSelectedItem(value);
    }

}
