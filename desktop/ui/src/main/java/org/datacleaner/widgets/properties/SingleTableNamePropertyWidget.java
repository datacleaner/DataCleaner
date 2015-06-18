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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.MutableRef;
import org.apache.metamodel.util.Predicate;
import org.datacleaner.api.InputColumn;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.UpdateableDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.DCComboBox.Listener;
import org.datacleaner.widgets.SchemaStructureComboBoxListRenderer;
import org.datacleaner.windows.CreateTableDialog;

/**
 * Alternative String property widget, specifically built for components that
 * need a {@link Table} name drop down.
 */
public class SingleTableNamePropertyWidget extends AbstractPropertyWidget<String> {

    private final DCComboBox<Table> _comboBox;
    private final MutableRef<Schema> _schemaRef;
    private final MutableRef<Datastore> _datastoreRef;
    private final DCPanel _panelAroundButton;

    /**
     * Creates the property widget
     * 
     * @param componentBuilder
     * @param propertyDescriptor
     * 
     * @deprecated use
     *             {@link #SingleTableNamePropertyWidget(ComponentBuilder, ConfiguredPropertyDescriptor, WindowContext)}
     *             instead.
     */
    @Deprecated
    public SingleTableNamePropertyWidget(final ComponentBuilder componentBuilder,
            final ConfiguredPropertyDescriptor propertyDescriptor) {
        this(componentBuilder, propertyDescriptor, null);
    }

    /**
     * Creates the property widget
     * 
     * @param componentBuilder
     * @param propertyDescriptor
     * @param windowContext
     */
    public SingleTableNamePropertyWidget(final ComponentBuilder componentBuilder,
            final ConfiguredPropertyDescriptor propertyDescriptor, final WindowContext windowContext) {
        super(componentBuilder, propertyDescriptor);

        _schemaRef = new MutableRef<Schema>();
        _datastoreRef = new MutableRef<Datastore>();

        _comboBox = new DCComboBox<Table>();
        _comboBox.setRenderer(new SchemaStructureComboBoxListRenderer(false));
        _comboBox.setEditable(false);
        addComboListener(new Listener<Table>() {
            @Override
            public void onItemSelected(Table item) {
                fireValueChanged();
            }
        });

        final JButton createTableButton = WidgetFactory.createSmallButton(IconUtils.ACTION_CREATE_TABLE);
        createTableButton.setToolTipText("Create table");
        createTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Schema schema = _schemaRef.get();
                final Datastore datastore = _datastoreRef.get();
                if (datastore instanceof UpdateableDatastore) {
                    final UpdateableDatastore updateableDatastore = (UpdateableDatastore) datastore;
                    final CreateTableDialog dialog = new CreateTableDialog(windowContext, updateableDatastore, schema,
                            getCreateTableColumnSuggestions());
                    dialog.addListener(new CreateTableDialog.Listener() {
                        @Override
                        public void onTableCreated(UpdateableDatastore datastore, Schema schema, String tableName) {
                            try (UpdateableDatastoreConnection con = datastore.openConnection()) {
                                con.getDataContext().refreshSchemas();
                                final Schema newSchema = con.getDataContext().getSchemaByName(schema.getName());
                                setSchema(datastore, newSchema);
                                setValue(tableName);
                            }
                        }
                    });
                    dialog.open();
                }
            }
        });

        _panelAroundButton = DCPanel.around(createTableButton);
        _panelAroundButton.setBorder(WidgetUtils.BORDER_EMPTY);
        _panelAroundButton.setVisible(false);

        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(_comboBox, BorderLayout.CENTER);
        panel.add(_panelAroundButton, BorderLayout.EAST);

        add(panel);

        setValue(getCurrentValue());
    }

    protected Collection<InputColumn<?>> getCreateTableColumnSuggestions() {
        final ComponentBuilder componentBuilder = getComponentBuilder();
        List<InputColumn<?>> columns = componentBuilder.getAnalysisJobBuilder().getAvailableInputColumns(
                componentBuilder);
        columns = CollectionUtils.filter(columns, new Predicate<InputColumn<?>>() {
            @Override
            public Boolean eval(InputColumn<?> column) {
                if (column instanceof MutableInputColumn) {
                    return !((MutableInputColumn<?>) column).isHidden();
                }
                return true;
            }
        });
        return columns;
    }

    public void addComboListener(Listener<Table> listener) {
        _comboBox.addListener(listener);
    }

    /**
     * @param schema
     * 
     * @deprecated use {@link #setSchema(Datastore, Schema)} instead
     */
    @Deprecated
    public void setSchema(Schema schema) {
        setSchema(null, schema);
    }

    public void setSchema(Datastore datastore, Schema schema) {
        _panelAroundButton.setVisible(CreateTableDialog.isCreateTableAppropriate(datastore, schema));

        final String previousValue = getValue();
        _schemaRef.set(schema);
        _datastoreRef.set(datastore);

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
