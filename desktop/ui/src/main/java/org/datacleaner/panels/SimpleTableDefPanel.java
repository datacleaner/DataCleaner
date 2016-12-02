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
package org.datacleaner.panels;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.table.DefaultTableModel;

import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.EnumComboBoxListRenderer;
import org.datacleaner.widgets.table.DCTable;
import org.jdesktop.swingx.JXTextField;

public class SimpleTableDefPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final DCTable _table;
    private final DefaultTableModel _tableModel;
    private final String _tableName;

    public SimpleTableDefPanel(final SimpleTableDef tableDef) {
        _tableName = tableDef.getName();
        final String[] names = tableDef.getColumnNames();
        final ColumnType[] types = tableDef.getColumnTypes();

        _tableModel = new DefaultTableModel(new String[] { "Field name", "Type" }, 0);
        for (int i = 0; i < names.length; i++) {
            addField(names[i], types[i]);
        }

        _table = new DCTable(_tableModel);
        _table.setSortable(false);
        _table.setColumnControlVisible(false);
        _table.setRowHeight(DCTable.EDITABLE_TABLE_ROW_HEIGHT);

        setLayout(new BorderLayout());
        add(createButtonPanel(), BorderLayout.NORTH);
        add(_table.toPanel(), BorderLayout.CENTER);
    }

    private DCPanel createButtonPanel() {
        final JButton addButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD_DARK);
        addButton.setText("Add field");
        addButton.addActionListener(e -> addField(null, null));

        final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE_DARK);
        removeButton.setText("Remove field");
        removeButton.addActionListener(e -> _tableModel.removeRow(_tableModel.getRowCount() - 1));

        return DCPanel.flow(Alignment.RIGHT, 10, 10, addButton, removeButton);
    }

    private void addField(String name, ColumnType columnType) {
        if (name == null) {
            name = "";
        }
        final JXTextField nameTextField = WidgetFactory.createTextField();
        nameTextField.setText(name);

        if (columnType == null) {
            columnType = ColumnType.VARCHAR;
        }
        final DCComboBox<ColumnType> typeComboBox = new DCComboBox<>(getAvailableColumnTypes());
        typeComboBox.setRenderer(new EnumComboBoxListRenderer());
        typeComboBox.setSelectedItem(columnType);

        _tableModel.addRow(new Object[] { nameTextField, typeComboBox });
        updateUI();
    }

    private ColumnType[] getAvailableColumnTypes() {
        return new ColumnType[] { ColumnType.VARCHAR, ColumnType.DECIMAL, ColumnType.INTEGER, ColumnType.BOOLEAN,
                ColumnType.DATE, ColumnType.TIME, ColumnType.TIMESTAMP, ColumnType.MAP, ColumnType.LIST,
                ColumnType.BINARY };
    }

    public SimpleTableDef getTableDef() {
        final int rowCount = _tableModel.getRowCount();
        final String[] names = new String[rowCount];
        final ColumnType[] types = new ColumnType[rowCount];
        for (int i = 0; i < types.length; i++) {
            final JXTextField nameTextField = (JXTextField) _tableModel.getValueAt(i, 0);
            names[i] = nameTextField.getText();

            @SuppressWarnings("unchecked") final DCComboBox<ColumnType> typeComboBox =
                    (DCComboBox<ColumnType>) _tableModel.getValueAt(i, 1);
            types[i] = typeComboBox.getSelectedItem();
        }
        return new SimpleTableDef(_tableName, names, types);
    }

}
