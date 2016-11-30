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
package org.datacleaner.windows;

import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.apache.metamodel.MetaModelHelper;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.create.CreateTable;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.ColumnTypeImpl;
import org.apache.metamodel.schema.Schema;
import org.datacleaner.api.InputColumn;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.UpdateableDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;
import org.datacleaner.panels.CreateTableColumnDefintionPanel;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

/**
 * A dialog which gives the user the ability to create a table in a datastore.
 */
public class CreateTableDialog extends AbstractDialog {

    public interface Listener {
        void onTableCreated(UpdateableDatastore datastore, Schema schema, String tableName);
    }

    private static final long serialVersionUID = 1L;
    private final UpdateableDatastore _datastore;
    private final Schema _schema;
    private final DCPanel _columnsListPanel;
    private final List<CreateTableColumnDefintionPanel> _columnDefinitionPanels;
    private final List<Listener> _listeners;

    public CreateTableDialog(final WindowContext windowContext, final UpdateableDatastore datastore,
            final Schema schema) {
        this(windowContext, datastore, schema, null);
    }

    public CreateTableDialog(final WindowContext windowContext, final UpdateableDatastore datastore,
            final Schema schema, final Collection<InputColumn<?>> columnSuggestions) {
        super(windowContext, ImageManager.get().getImage("images/window/banner-tabledef.png"));

        _datastore = datastore;
        _schema = schema;
        _listeners = new ArrayList<>(1);

        _columnDefinitionPanels = new ArrayList<>();

        _columnsListPanel = new DCPanel();
        _columnsListPanel.setBorder(WidgetUtils.BORDER_EMPTY);
        _columnsListPanel.setLayout(new VerticalLayout());

        if (columnSuggestions != null && !columnSuggestions.isEmpty()) {
            // add columns based on the suggestions
            for (final InputColumn<?> columnSuggestion : columnSuggestions) {
                final String name = columnSuggestion.getName();
                if (columnSuggestion.isPhysicalColumn()) {
                    final Column physicalColumn = columnSuggestion.getPhysicalColumn();
                    final ColumnType columnType = physicalColumn.getType();
                    final boolean isPrimaryKey = physicalColumn.isPrimaryKey();
                    addColumnDefinitionPanel(new CreateTableColumnDefintionPanel(this, name, columnType, isPrimaryKey));
                } else {
                    final ColumnType columnType = ColumnTypeImpl.convertColumnType(columnSuggestion.getDataType());
                    addColumnDefinitionPanel(new CreateTableColumnDefintionPanel(this, name, columnType, false));
                }
            }
        } else {
            // add some columns to begin with
            addColumnDefinitionPanel(new CreateTableColumnDefintionPanel(this, "ID", ColumnType.INTEGER, true));
            addColumnDefinitionPanel();
        }
    }

    /**
     * Determines if it is appropriate/possible to create a table in a
     * particular schema or a particular datastore.
     *
     * @param datastore
     * @param schema
     * @return
     */
    public static boolean isCreateTableAppropriate(final Datastore datastore, final Schema schema) {
        if (datastore == null || schema == null) {
            return false;
        }
        if (!(datastore instanceof UpdateableDatastore)) {
            return false;
        }
        if (datastore instanceof CsvDatastore) {
            // see issue https://issues.apache.org/jira/browse/METAMODEL-31 - as
            // long as this is an issue we do not want to expose "create table"
            // functionality to CSV datastores.
            return false;
        }
        if (MetaModelHelper.isInformationSchema(schema)) {
            return false;
        }
        return true;
    }

    public void addListener(final Listener listener) {
        if (listener == null) {
            return;
        }
        _listeners.add(listener);
    }

    public void removeListener(final Listener listener) {
        _listeners.remove(listener);
    }

    @Override
    public String getWindowTitle() {
        return "Create table";
    }

    @Override
    protected String getBannerTitle() {
        return "Create table\nin schema '" + _schema.getName() + "'";
    }

    @Override
    protected int getDialogWidth() {
        return 700;
    }

    @Override
    protected JComponent getDialogContent() {
        final DCLabel label1 = DCLabel.darkMultiLine(
                "Please fill out the name and describe the columns that should comprise your new table. "
                        + "The table will be created in the schema '<b>" + _schema.getName() + "</b>' of datastore '<b>"
                        + _datastore.getName() + "</b>'");

        final DCLabel label2 = DCLabel.darkMultiLine("Note that the column data types may be adapted/interpreted in "
                + "order to fit the type of datastore, should they not apply to the datastore natively.");

        final JXTextField tableNameTextField = WidgetFactory.createTextField("Table name");

        final JButton createTableButton =
                WidgetFactory.createPrimaryButton("Create table", IconUtils.ACTION_CREATE_TABLE);
        createTableButton.addActionListener(e -> {
            final String tableName = tableNameTextField.getText().trim();
            if (tableName.isEmpty()) {
                WidgetUtils.showErrorMessage("Invalid table name", "Please enter a valid table name.");
                return;
            }

            if (_columnDefinitionPanels.isEmpty()) {
                WidgetUtils.showErrorMessage("No columns defined", "Please add at least one column to the table.");
                return;
            }

            final List<Column> columns = new ArrayList<>(_columnDefinitionPanels.size());
            for (final CreateTableColumnDefintionPanel columnDefinitionPanel : _columnDefinitionPanels) {
                columnDefinitionPanel.highlightIssues();
                if (!columnDefinitionPanel.isColumnDefined()) {
                    return;
                }
                columns.add(columnDefinitionPanel.toColumn());
            }

            doCreateTable(tableName, columns);
            for (final Listener listener : _listeners) {
                listener.onTableCreated(_datastore, _schema, tableName);
            }
            CreateTableDialog.this.close();
        });

        final JButton cancelButton = WidgetFactory.createDefaultButton("Cancel", IconUtils.ACTION_CANCEL);
        cancelButton.addActionListener(e -> CreateTableDialog.this.close());

        final JButton addColumnButton = WidgetFactory.createSmallButton("Add column", IconUtils.ACTION_ADD_DARK);
        addColumnButton.addActionListener(e -> addColumnDefinitionPanel());

        final JButton removeAllColumnsButton =
                WidgetFactory.createSmallButton("Remove all columns", IconUtils.ACTION_REMOVE_DARK);
        removeAllColumnsButton.setForeground(WidgetUtils.ADDITIONAL_COLOR_RED_BRIGHT);
        removeAllColumnsButton.addActionListener(e -> removeAllColumns());

        final DCPanel buttonPanel = DCPanel.flow(addColumnButton, removeAllColumnsButton);
        buttonPanel.setBorder(WidgetUtils.BORDER_EMPTY);

        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_WELL_BACKGROUND);
        int row = 0;
        WidgetUtils.addToGridBag(label1, panel, 0, row, 2, 1);
        row++;
        WidgetUtils.addToGridBag(tableNameTextField, panel, 0, row, 2, 1);
        row++;
        WidgetUtils.addToGridBag(label2, panel, 0, row, 2, 1);
        row++;
        WidgetUtils.addToGridBag(buttonPanel, panel, 0, row, 2, 1);
        row++;
        WidgetUtils.addToGridBag(WidgetUtils.scrolleable(_columnsListPanel), panel, 0, row, 2, 1,
                GridBagConstraints.NORTHWEST, 0, 1.0, 1.0, GridBagConstraints.BOTH);
        row++;
        WidgetUtils.addToGridBag(createTableButton, panel, 0, row, 0.5, 0.1);
        WidgetUtils.addToGridBag(cancelButton, panel, 1, row, 0.5, 0.1);

        panel.setPreferredSize(getDialogWidth(), 400);

        return panel;
    }

    protected boolean isWindowResizable() {
        return true;
    }

    protected void doCreateTable(final String tableName, final List<Column> columns) {
        try (UpdateableDatastoreConnection con = _datastore.openConnection()) {
            final CreateTable createTable = new CreateTable(_schema, tableName);
            for (final Column column : columns) {
                createTable.withColumn(column.getName()).like(column);
            }
            final UpdateableDataContext dataContext = con.getUpdateableDataContext();
            dataContext.executeUpdate(createTable);
        }
    }

    public void removeColumnDefinitionPanel(final CreateTableColumnDefintionPanel columnDefintionPanel) {
        _columnDefinitionPanels.remove(columnDefintionPanel);
        _columnsListPanel.remove(columnDefintionPanel);
        _columnsListPanel.updateUI();
    }

    public void addColumnDefinitionPanel() {
        final CreateTableColumnDefintionPanel columnDefintionPanel = new CreateTableColumnDefintionPanel(this);
        addColumnDefinitionPanel(columnDefintionPanel);
    }


    protected void removeAllColumns() {
        _columnDefinitionPanels.clear();
        _columnsListPanel.removeAll();
        _columnsListPanel.updateUI();
    }

    private void addColumnDefinitionPanel(final CreateTableColumnDefintionPanel columnDefintionPanel) {
        _columnDefinitionPanels.add(columnDefintionPanel);
        _columnsListPanel.add(columnDefintionPanel);
        _columnsListPanel.updateUI();
    }
}
