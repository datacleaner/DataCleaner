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

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXTable;

public class DCBaseTable extends JXTable {

    private static final long serialVersionUID = 1L;

    protected transient DCPanel _panel;

    public DCBaseTable(final String... columnNames) {
        super(new Object[0][columnNames.length], columnNames);
        addHighlighter(WidgetUtils.LIBERELLO_HIGHLIGHTER);
        getTableHeader().setReorderingAllowed(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setOpaque(false);
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(true);
        setColumnControlVisible(true);
    }

    public DCBaseTable() {
        this(new String[0]);
    }

    public DCBaseTable(final TableModel tableModel) {
        this();
        setModel(tableModel);
    }

    /**
     * Convenience method to create a panel with this table, including it's header, correctly layed out.
     *
     * @param scrolleable whether or not the table panel should feature scrolleable table contents.
     * @return
     */
    public DCPanel toPanel(final boolean scrolleable) {
        if (_panel == null) {
            _panel = new DCTablePanel(this, scrolleable);
        }
        return _panel;
    }

    /**
     * Convenience method to create a panel with this table, including it's header, correctly layed out.
     */
    public DCPanel toPanel() {
        return toPanel(true);
    }

    @Override
    public void setVisible(final boolean visible) {
        super.setVisible(visible);
        if (_panel != null) {
            _panel.updateUI();
        }
    }

    /**
     * Since setModel(...) is used to update the contents and repaint the widget, we will also make this happen if a
     * panel is presenting the table.
     */
    @Override
    public void setModel(final TableModel dataModel) {
        super.setModel(dataModel);
        if (_panel != null) {
            _panel.updateUI();
        }
    }

    public void setVisibleColumns(final int min, final int max) {
        // Note: The loop starts in the top and goes down, this is because the
        // getColumnExt() index is affected directly, when setting a column as
        // invisible!
        for (int i = getColumnCount(); i > 0; i--) {
            if (i >= min && i <= max) {
                getColumnExt(i - 1).setVisible(true);
            } else {
                getColumnExt(i - 1).setVisible(false);
            }
        }
    }

    public void selectRows(final int... rowIndexes) {
        final ListSelectionModel selectionModel = getSelectionModel();
        selectionModel.setValueIsAdjusting(true);
        for (int i = 0; i < rowIndexes.length; i++) {
            final int rowIndex = rowIndexes[i];
            if (i == 0) {
                setRowSelectionInterval(rowIndex, rowIndex);
            } else {
                addRowSelectionInterval(rowIndex, rowIndex);
            }
        }
        selectionModel.setValueIsAdjusting(false);
        getColumnModel().getSelectionModel().setValueIsAdjusting(true);
        setColumnSelectionInterval(0, getColumnCount() - 1);
        getColumnModel().getSelectionModel().setValueIsAdjusting(false);
    }

    public void autoSetHorizontalScrollEnabled() {
        if (getColumnCount() >= 9) {
            setHorizontalScrollEnabled(true);
        } else {
            setHorizontalScrollEnabled(false);
        }
    }

}
