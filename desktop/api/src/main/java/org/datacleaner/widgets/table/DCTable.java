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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.jdesktop.swingx.JXTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of JTable that provides a styling consistent with DataCleaner
 * GUI and some functional improvements like right-click menu's etc.
 */
public class DCTable extends JXTable implements MouseListener {

    public static final int EDITABLE_TABLE_ROW_HEIGHT = 30;
    private static final Logger logger = LoggerFactory.getLogger(DCTable.class);
    private static final long serialVersionUID = -5376226138423224572L;
    private final transient DCTableCellRenderer _tableCellRenderer;
    protected transient List<JMenuItem> _rightClickMenuItems;
    protected transient DCPanel _panel;
    private ActionListener _copySelectItemsActionListener = e -> {
        final int rowIndex = DCTable.this.getSelectedRow();
        final int rowCount = DCTable.this.getSelectedRowCount();

        final int colIndex = DCTable.this.getSelectedColumn();
        final int colCount = DCTable.this.getSelectedColumnCount();

        copyToClipboard(rowIndex, colIndex, colCount, rowCount);
    };
    private ActionListener _copyEntireTableActionListener =
            e -> copyToClipboard(0, 0, DCTable.this.getColumnCount(), DCTable.this.getRowCount());

    public DCTable(final String... columnNames) {
        super(new Object[0][columnNames.length], columnNames);
        addHighlighter(WidgetUtils.LIBERELLO_HIGHLIGHTER);
        getTableHeader().setReorderingAllowed(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setOpaque(false);
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(true);
        setColumnControlVisible(true);
        setSortable(true);

        // currently (because of the implementation of the editor) the enabled
        // "editable" property is only to enable clicking on buttons etc. in
        // tables.
        setEditable(true);

        addMouseListener(this);
        _tableCellRenderer = new DCTableCellRenderer(this);
    }

    public DCTable() {
        this(new String[0]);
    }

    public DCTable(final TableModel tableModel) {
        this();
        setModel(tableModel);
    }

    /**
     * Convenience method to create a panel with this table, including it's
     * header, correctly layed out.
     *
     * @param scrolleable
     *            whether or not the table panel should feature scrolleable
     *            table contents.
     * @return
     */
    public DCPanel toPanel(final boolean scrolleable) {
        if (_panel == null) {
            _panel = new DCTablePanel(this, scrolleable);
        }
        return _panel;
    }

    /**
     * Convenience method to create a panel with this table, including it's
     * header, correctly layed out.
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
     * Since setModel(...) is used to update the contents and repaint the
     * widget, we will also make this happen if a panel is presenting the table.
     */
    @Override
    public void setModel(final TableModel dataModel) {
        super.setModel(dataModel);
        if (_panel != null) {
            _panel.updateUI();
        }
    }

    protected List<JMenuItem> getCopyMenuItems() {
        final Icon icon = ImageManager.get().getImageIcon(IconUtils.ACTION_COPY, IconUtils.ICON_SIZE_MENU_ITEM);
        final List<JMenuItem> result = new ArrayList<>();

        // JMenuItem for "Copy selected cells to clipboard"
        final JMenuItem copySelectedItem = new JMenuItem("Copy selected cells to clipboard", icon);
        copySelectedItem.addActionListener(_copySelectItemsActionListener);
        result.add(copySelectedItem);

        // JMenuItem for "Copy entire table to clipboard"
        final JMenuItem copyTableItem = new JMenuItem("Copy entire table to clipboard", icon);
        copyTableItem.addActionListener(_copyEntireTableActionListener);
        result.add(copyTableItem);

        return result;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        forwardMouseEvent(e);
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        // forwardMouseEvent(e);
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        // forwardMouseEvent(e);
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        forwardMouseEvent(e);
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        final boolean forwarded = forwardMouseEvent(e);
        if (!forwarded) {
            // handle right click
            consumeMouseClick(e);
        }
    }

    protected void consumeMouseClick(final MouseEvent e) {
        logger.debug("consumeMouseClick({})", e);
        if (e.getClickCount() == 1) {
            final int button = e.getButton();
            if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
                if (initializeRightClickMenuItems()) {
                    final JPopupMenu popup = new JPopupMenu();
                    for (final JMenuItem item : _rightClickMenuItems) {
                        popup.add(item);
                    }
                    popup.show(e.getComponent(), e.getX(), e.getY());
                    return;
                }
            }
        }
    }

    private boolean initializeRightClickMenuItems() {
        if (_rightClickMenuItems == null) {
            _rightClickMenuItems = getCopyMenuItems();
            if (_rightClickMenuItems == null) {
                _rightClickMenuItems = new ArrayList<>();
            }
        }
        return !_rightClickMenuItems.isEmpty();
    }

    private boolean forwardMouseEvent(final MouseEvent e) {
        logger.debug("forwardMouseEvent({})", e);
        final int x = e.getX();
        final int y = e.getY();
        final int col = getColumnModel().getColumnIndexAtX(x);
        int row = y / getRowHeight();

        if (row >= getRowCount()) {
            row = -1;
        }

        if (row == -1 || col == -1) {
            logger.debug("Disregarding mouse event at {},{} (row={},col={})", new Object[] { x, y, row, col });
            return false;
        }

        try {
            final Object value = getValueAt(row, col);
            if (value instanceof JComponent) {
                final JComponent component = (JComponent) value;
                final MouseEvent newEvent = SwingUtilities.convertMouseEvent(this, e, component);

                component.dispatchEvent(newEvent);
                repaint();
                return true;
            }
            return false;
        } catch (final IndexOutOfBoundsException exception) {
            // on some machines this may occur if x/y coordinates are dragged
            // outside of the table
            logger.debug("Failed to dispatch event for component because of IndexOutOfBoundsException", exception);
            return false;
        }
    }

    /**
     * Copies content from the table to the clipboard. Algorithm is a slight
     * rewrite of the article below.
     *
     * @see <a href="http://www.copy--paste.org/copy-paste-jtables-excel.htm">http://www.copy--paste.org/copy-paste-jtables-excel.htm</a>
     */
    public void copyToClipboard(final int rowIndex, final int colIndex, final int width, final int height) {
        final StringBuilder sb = new StringBuilder();
        if (rowIndex == 0 && colIndex == 0 && width == getColumnCount() && height == getRowCount()) {
            for (int i = 0; i < width; i++) {
                sb.append(getColumnName(i));
                if (i < height - 1) {
                    sb.append("\t");
                }
            }
            sb.append("\n");
        }

        for (int row = rowIndex; row < rowIndex + height; row++) {
            for (int col = colIndex; col < colIndex + width; col++) {
                Object value = getValueAt(row, col);
                if (value == null) {
                    value = "";
                } else if (value instanceof JComponent) {
                    value = WidgetUtils.extractText((JComponent) value);
                }
                sb.append(value);
                sb.append("\t");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("\n");
        }
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final StringSelection stsel = new StringSelection(sb.toString());

        clipboard.setContents(stsel, stsel);
    }

    public String getTextValueAt(final int row, final int column) {
        Object value = getValueAt(row, column);
        if (value == null) {
            value = "";
        } else if (value instanceof JComponent) {
            value = WidgetUtils.extractText((JComponent) value);
        }
        return value.toString();
    }

    @Override
    public Object getValueAt(final int row, final int column) {
        Object value = super.getValueAt(row, column);
        if (value == null) {
            value = LabelUtils.NULL_LABEL;
        }
        return value;
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

    public DCTableCellRenderer getDCTableCellRenderer() {
        if (_tableCellRenderer == null) {
            // should only occur in deserialized instances
            return new DCTableCellRenderer(this);
        }
        return _tableCellRenderer;
    }

    @Override
    public TableCellRenderer getCellRenderer(final int row, final int column) {
        return getDCTableCellRenderer();
    }

    @Override
    public TableCellEditor getCellEditor(final int row, final int column) {
        logger.debug("getCellEditor({},{})", row, column);
        final Object value = getValueAt(row, column);

        if (value instanceof JComponent) {
            return JComponentCellEditor.forComponent((JComponent) value);
        }

        return JComponentCellEditor.forComponent(null);
    }

    public void setAlignment(final int column, final Alignment alignment) {
        getDCTableCellRenderer().setAlignment(column, alignment);
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
