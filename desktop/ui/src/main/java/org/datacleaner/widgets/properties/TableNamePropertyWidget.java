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

import javax.swing.DefaultComboBoxModel;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AbstractBeanJobBuilder;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.DCComboBox.Listener;
import org.datacleaner.widgets.SchemaStructureComboBoxListRenderer;
import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.MutableRef;

/**
 * Alternative String property widget, specifically built for components that
 * need a {@link Table} name drop down.
 */
public class TableNamePropertyWidget extends AbstractPropertyWidget<String> {

    private final DCComboBox<Table> _comboBox;
    private final MutableRef<Schema> _schemaRef;

    public TableNamePropertyWidget(final AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            final ConfiguredPropertyDescriptor propertyDescriptor) {
        super(beanJobBuilder, propertyDescriptor);
        _comboBox = new DCComboBox<Table>();
        _comboBox.setRenderer(new SchemaStructureComboBoxListRenderer(false));
        _comboBox.setEditable(false);
        addComboListener(new Listener<Table>() {
            @Override
            public void onItemSelected(Table item) {
                fireValueChanged();
            }
        });
        add(_comboBox);
        _schemaRef = new MutableRef<Schema>();
        
        setValue(getCurrentValue());
    }

    public void addComboListener(Listener<Table> listener) {
        _comboBox.addListener(listener);
    }

    public void setSchema(Schema schema) {
        String previousValue = getValue();
        _schemaRef.set(schema);
        if (schema == null) {
            _comboBox.setModel(new DefaultComboBoxModel<Table>(new Table[1]));
        } else {
            Table[] tables = schema.getTables();
            tables = CollectionUtils.array(new Table[1], tables);
            _comboBox.setModel(new DefaultComboBoxModel<Table>(tables));

            if (previousValue == null) {
                if (schema.getTableCount() == 1) {
                    // if there is only 1 table, select that
                    Table table = schema.getTables()[0];
                    _comboBox.setSelectedItem(table);
                }
            } else {
                // select table by name
                Table table = schema.getTableByName(previousValue);
                _comboBox.setSelectedItem(table);
            }
        }
    }

    /**
     * Gets the combo box containing the available {@link Table}s
     * 
     * @return
     */
    public DCComboBox<Table> getComboBox() {
        return _comboBox;
    }

    @Override
    public String getValue() {
        final Table table = getTable();
        if (table == null) {
            return null;
        }
        return table.getName();
    }

    public Table getTable() {
        return (Table) _comboBox.getSelectedItem();
    }

    @Override
    protected void setValue(final String value) {
        if (getValue() == value) {
            return;
        }

        final Schema schema = _schemaRef.get();
        final Table table;
        if (value == null) {
            table = null;
        } else if (schema == null) {
            table = new MutableTable(value);
        } else {
            table = schema.getTableByName(value);
        }

        _comboBox.setEditable(true);
        _comboBox.setSelectedItem(table);
        _comboBox.setEditable(false);
        
        fireValueChanged();
    }
}
