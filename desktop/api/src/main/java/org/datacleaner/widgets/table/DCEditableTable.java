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
package org.datacleaner.widgets.table;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;

/**
 * Editable table which allows for convenient adding of new records by clicking enter and deleting of empty records by
 * clicking delete or backspace.
 */
public class DCEditableTable extends DCBaseTable {

    private static final String ACTION_ENTER = "FixedWidth.ColumnsTable.Enter";
    private static final String ACTION_DELETE = "FixedWidth.ColumnsTable.Delete";

    private static final long serialVersionUID = 1L;
    private final Class<?>[] columnClasses;

    public DCEditableTable(String[] columnNames, Class<?>[] columnClasses) {
        super(new DefaultTableModel(columnNames, 1) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        });
        this.columnClasses = columnClasses;

        if (columnNames.length != columnClasses.length) {
            throw new IllegalArgumentException("Column names and types arrays not of equal length!");
        }

        setColumnControlVisible(false);
        setFillsViewportHeight(true);
        setEditable(true);
        setSortable(false);

        final InputMap inputMap = getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ACTION_ENTER);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), ACTION_DELETE);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), ACTION_DELETE);

        getActionMap().put(ACTION_ENTER, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                final boolean editing = isEditing();
                if (editing) {
                    getCellEditor().stopCellEditing();
                } else {
                    final int selectedRow = getSelectedRow();
                    if (selectedRow == -1) {
                        return;
                    }

                    if (selectedRow == getRowCount() - 1) {
                        // when clicking enter on the last row, add another row
                        final Object[] rowData = createNewRow(getRowCount());
                        final DefaultTableModel model = (DefaultTableModel) getModel();
                        model.addRow(rowData);
                    } else {
                        // shift focus to cell below
                        editCellAt(selectedRow + 1, getSelectedColumn());
                    }
                }
            }
        });

        getActionMap().put(ACTION_DELETE, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (getRowCount() > 1 && !isEditing()) {
                    final int selectedRow = getSelectedRow();
                    if (selectedRow != -1 && isEmptyRow(selectedRow)) {
                        // delete empty row
                        final DefaultTableModel model = (DefaultTableModel) getModel();
                        model.removeRow(selectedRow);
                    }
                }
            }
        });
    }

    /**
     * Creates the values for a new row
     * 
     * @param rowIndex
     * @return
     */
    protected Object[] createNewRow(final int rowIndex) {
        return new Object[getColumnCount()];
    }

    @Override
    public Class<?> getColumnClass(final int column) {
        return columnClasses[column];
    }

    private boolean isEmptyRow(final int selectedRow) {
        for (int i = 0; i < getColumnCount(); i++) {
            if (!isNullOrEmpty(getValueAt(selectedRow, i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNullOrEmpty(final Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String && "".equals(value)) {
            return true;
        }
        return false;
    }
}
