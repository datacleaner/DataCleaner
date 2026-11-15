/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.WidgetUtils;

/**
 * Base class for DataCleaner tables, extending the plain Swing {@link JTable}
 * with DataCleaner styling (alternating row striping), sorting, editability
 * control and a few convenience methods. Previously this was built on SwingX
 * {@code JXTable}.
 */
public class DCBaseTable extends JTable {

    private static final long serialVersionUID = 1L;

    /**
     * Comparator that sorts by string representation, to keep sorting usable
     * for object columns (which may contain non-{@link Comparable} values).
     */
    private static final Comparator<Object> STRINGIFYING_COMPARATOR = new Comparator<Object>() {
        @Override
        public int compare(final Object o1, final Object o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            if (o1 instanceof Comparable && o2 instanceof Comparable) {
                try {
                    return ((Comparable<Object>) o1).compareTo(o2);
                } catch (final ClassCastException | NullPointerException e) {
                    // fall through to string comparison
                }
            }
            return o1.toString().compareTo(o2.toString());
        }
    };

    protected transient DCPanel _panel;

    private boolean _editable = true;
    private boolean _sortable = true;

    public DCBaseTable(final String... columnNames) {
        super(new Object[0][columnNames.length], columnNames);
        getTableHeader().setReorderingAllowed(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setOpaque(false);
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(true);
        setSortable(true);
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
        // rebind the row sorter to the new model
        if (_sortable) {
            setSortable(true);
        }
    }

    @Override
    public boolean isCellEditable(final int row, final int column) {
        return _editable && super.isCellEditable(row, column);
    }

    /**
     * Sets whether the table is editable at all, regardless of the model's
     * editability. Mirrors the former JXTable behavior.
     *
     * @param editable true if the table can be edited
     */
    public void setEditable(final boolean editable) {
        _editable = editable;
    }

    public boolean isEditable() {
        return _editable;
    }

    /**
     * Enables/disables sorting by clicking the column headers. Sorting uses a
     * {@link TableRowSorter} that falls back to string comparison for
     * non-{@link Comparable} values.
     *
     * @param sortable true to enable sorting, false to disable it.
     */
    public void setSortable(final boolean sortable) {
        _sortable = sortable;
        if (sortable) {
            final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(getModel()) {
                private static final long serialVersionUID = 1L;

                @Override
                public Comparator<?> getComparator(final int column) {
                    final Comparator<?> comparator = super.getComparator(column);
                    if (comparator != null) {
                        return comparator;
                    }
                    // fall back to string comparison for non-Comparable values
                    return STRINGIFYING_COMPARATOR;
                }
            };
            setRowSorter(sorter);
        } else {
            setRowSorter(null);
        }
    }

    public boolean isSortable() {
        return _sortable;
    }

    /**
     * Sets whether the table prefers horizontal scrolling over shrinking
     * columns when there is not enough room.
     *
     * @param enabled true to allow horizontal scrolling, false to resize columns
     */
    public void setHorizontalScrollEnabled(final boolean enabled) {
        setAutoResizeMode(enabled ? JTable.AUTO_RESIZE_OFF : JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    public boolean isHorizontalScrollEnabled() {
        return getAutoResizeMode() == JTable.AUTO_RESIZE_OFF;
    }

    /**
     * Formerly toggled the SwingX "column control" button in the table
     * header's top-right corner. The feature is retired; the method is kept
     * as a no-op for API compatibility.
     *
     * @param visible ignored
     */
    public void setColumnControlVisible(final boolean visible) {
        // no-op
    }

    /**
     * Gets the {@link TableColumn} at the given view index (convenience
     * replacement for the former JXTable method; equivalent to
     * {@code getColumnModel().getColumn(viewIndex)}).
     *
     * @param viewIndex the view index of the column
     * @return the column
     */
    public TableColumn getColumn(final int viewIndex) {
        return getColumnModel().getColumn(viewIndex);
    }

    public void setVisibleColumns(final int min, final int max) {
        final TableColumnModel columnModel = getColumnModel();

        // remove all columns, then add back the visible range in order
        final List<TableColumn> allColumns = new ArrayList<>();
        for (int i = columnModel.getColumnCount() - 1; i >= 0; i--) {
            allColumns.add(0, columnModel.getColumn(i));
        }
        for (final TableColumn column : allColumns) {
            columnModel.removeColumn(column);
        }
        for (int i = min - 1; i < max && i < allColumns.size(); i++) {
            columnModel.addColumn(allColumns.get(i));
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
        setHorizontalScrollEnabled(getColumnCount() >= 9);
    }

    /**
     * Resizes a column to fit its contents (replaces the former JXTable
     * method).
     *
     * @param columnIndex the view index of the column to pack
     * @param margin the margin (in pixels) to add on both sides
     */
    public void packColumn(final int columnIndex, final int margin) {
        if (columnIndex < 0 || columnIndex >= getColumnCount()) {
            return;
        }
        final TableColumn column = getColumnModel().getColumn(columnIndex);

        int width = 0;

        // header width
        final TableCellRenderer headerRenderer = column.getHeaderRenderer() != null ? column.getHeaderRenderer()
                : getTableHeader().getDefaultRenderer();
        final Component headerComponent =
                headerRenderer.getTableCellRendererComponent(this, column.getHeaderValue(), false, false, -1,
                        columnIndex);
        width = Math.max(width, headerComponent.getPreferredSize().width);

        // cell widths
        for (int row = 0; row < getRowCount(); row++) {
            final TableCellRenderer renderer = getCellRenderer(row, columnIndex);
            final Component component =
                    prepareRenderer(renderer, row, columnIndex);
            width = Math.max(width, component.getPreferredSize().width);
        }

        column.setPreferredWidth(width + 2 * margin);
    }

    @Override
    public Component prepareRenderer(final TableCellRenderer renderer, final int row, final int column) {
        final Component component = super.prepareRenderer(renderer, row, column);
        if (!isCellSelected(row, column)) {
            // alternating row striping, replacing the former SwingX highlighter
            final Color stripedBackground = getStrippedBackgroundColor(row);
            if (stripedBackground != null && component instanceof java.awt.Container) {
                component.setBackground(stripedBackground);
            }
        }
        return component;
    }

    protected Color getStrippedBackgroundColor(final int row) {
        final Color defaultBackground = WidgetUtils.getThemeDefaultBackground();
        if (row % 2 == 1) {
            return WidgetUtils.colorBetween(defaultBackground, WidgetUtils.slightlyDarker(defaultBackground));
        }
        return defaultBackground;
    }
}
