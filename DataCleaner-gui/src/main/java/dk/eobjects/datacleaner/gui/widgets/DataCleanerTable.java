/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.swingx.JXTable;

import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.profiler.MatrixValue;

/**
 * An extension of JTable that provides a styling consistent with DataCleaner
 * GUI and some functional improvements like right-click menu's etc.
 */
public class DataCleanerTable extends JXTable implements MouseListener {

	private static final TableModel EMPTY_TABLEMODEL = new DefaultTableModel();
	private static final long serialVersionUID = -5376226138423224572L;
	protected Log _log = LogFactory.getLog(getClass());
	protected List<JMenuItem> _rightClickMenuItems;
	protected JPanel _panel;

	public DataCleanerTable(String[] columnNames) {
		super(new Object[0][columnNames.length], columnNames);
		setHighlighters(GuiHelper.LIBERELLO_HIGHLIGHTER);
		getTableHeader().setReorderingAllowed(true);
		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		setRowSelectionAllowed(true);
		setColumnSelectionAllowed(true);
		setColumnControlVisible(true);
		setSortable(true);
		setEditable(false);
		addMouseListener(this);
		_rightClickMenuItems = getCopyMenuItems();
	}

	public DataCleanerTable() {
		this(new String[0]);
	}

	private ActionListener _copySelectItemsActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int rowIndex = DataCleanerTable.this.getSelectedRow();
			int rowCount = DataCleanerTable.this.getSelectedRowCount();

			int colIndex = DataCleanerTable.this.getSelectedColumn();
			int colCount = DataCleanerTable.this.getSelectedColumnCount();

			copyToClipboard(rowIndex, colIndex, colCount, rowCount);
		}
	};

	private ActionListener _copyEntireTableActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			copyToClipboard(0, 0, DataCleanerTable.this.getColumnCount(),
					DataCleanerTable.this.getRowCount());
		}
	};

	@Override
	public void removeNotify() {
		super.removeNotify();
		removeMouseListener(this);
		dataModel.removeTableModelListener(this);
		// Can't set table model to null, so we'll use an empty model to release
		// memory
		dataModel = EMPTY_TABLEMODEL;
	}

	/**
	 * Convenience method to create a panel with this table, including it's
	 * header, correctly layed out.
	 */
	public JPanel toPanel() {
		if (_panel == null) {
			_panel = GuiHelper.createPanel()
					.applyBorderLayout().toComponent();
			_panel.add(getTableHeader(), BorderLayout.NORTH);
			_panel.add(new JScrollPane(this), BorderLayout.CENTER);
			_panel.setPreferredSize(getPanelPreferredSize());
		}
		return _panel;
	}

	public Dimension getPanelPreferredSize() {
		Dimension d = new Dimension();
		Dimension tableSize = getPreferredSize();
		d.width = tableSize.width;
		Dimension headerSize = getTableHeader().getPreferredSize();
		d.height = headerSize.height + tableSize.height;
		
		// Adding a 20 pixel buffer for horisontal scroll bars
		// (Ticket #272)
		d.height = d.height + 20;
		return d;
	}

	protected List<JMenuItem> getCopyMenuItems() {
		ImageIcon icon = GuiHelper.getImageIcon("images/toolbar_copy.png");
		List<JMenuItem> result = new ArrayList<JMenuItem>();

		// JMenuItem for "Copy selected cells to clipboard"
		JMenuItem copySelectedItem = new JMenuItem(
				"Copy selected cells to clipboard", icon);
		copySelectedItem.addActionListener(_copySelectItemsActionListener);
		result.add(copySelectedItem);

		// JMenuItem for "Copy entire table to clipboard"
		JMenuItem copyTableItem = new JMenuItem(
				"Copy entire table to clipboard", icon);
		copyTableItem.addActionListener(_copyEntireTableActionListener);
		result.add(copyTableItem);

		return result;
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getClickCount() == 1) {
			int button = e.getButton();
			if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
				if (_rightClickMenuItems != null
						&& _rightClickMenuItems.size() > 0) {
					JPopupMenu popup = new JPopupMenu();
					for (JMenuItem item : _rightClickMenuItems) {
						popup.add(item);
					}
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
	}

	/**
	 * Copies content from the table to the clipboard. Algorithm is a slight
	 * rewrite of the article below.
	 * 
	 * @see http://www.copy--paste.org/copy-paste-jtables-excel.htm
	 */
	public void copyToClipboard(int rowIndex, int colIndex, int width,
			int height) {
		StringBuilder sb = new StringBuilder();
		if (rowIndex == 0 && colIndex == 0 && width == getColumnCount()
				&& height == getRowCount()) {
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
				if (value instanceof MatrixValue) {
					value = ((MatrixValue) value).getValue();
				}
				if (value == null) {
					value = "";
				}
				sb.append(value);
				sb.append("\t");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("\n");
		}
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stsel = new StringSelection(sb.toString());

		clipboard.setContents(stsel, stsel);
	}

	@Override
	public Object getValueAt(int row, int column) {
		Object value = super.getValueAt(row, column);
		if (value == null) {
			value = GuiHelper.NULL_STRING;
		}
		return value;
	}
}