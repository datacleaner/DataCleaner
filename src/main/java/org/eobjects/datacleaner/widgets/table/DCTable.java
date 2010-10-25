package org.eobjects.datacleaner.widgets.table;

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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.LabelConstants;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of JTable that provides a styling consistent with DataCleaner
 * GUI and some functional improvements like right-click menu's etc.
 */
public class DCTable extends JXTable implements MouseListener {

	private static final Logger logger = LoggerFactory.getLogger(DCTable.class);

	private static final long serialVersionUID = -5376226138423224572L;
	protected List<JMenuItem> _rightClickMenuItems;
	protected DCPanel _panel;
	private JScrollPane _scrollPane;

	public DCTable(String... columnNames) {
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
		_rightClickMenuItems = getCopyMenuItems();
	}

	public DCTable() {
		this(new String[0]);
	}

	public DCTable(TableModel tableModel) {
		this();
		setModel(tableModel);
	}

	private ActionListener _copySelectItemsActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int rowIndex = DCTable.this.getSelectedRow();
			int rowCount = DCTable.this.getSelectedRowCount();

			int colIndex = DCTable.this.getSelectedColumn();
			int colCount = DCTable.this.getSelectedColumnCount();

			copyToClipboard(rowIndex, colIndex, colCount, rowCount);
		}
	};

	private ActionListener _copyEntireTableActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			copyToClipboard(0, 0, DCTable.this.getColumnCount(), DCTable.this.getRowCount());
		}
	};

	/**
	 * Convenience method to create a panel with this table, including it's
	 * header, correctly layed out.
	 */
	public DCPanel toPanel() {
		if (_panel == null) {
			_panel = new DCPanel();
			_panel.setLayout(new BorderLayout());
			_panel.add(getTableHeader(), BorderLayout.NORTH);
			_scrollPane = WidgetUtils.scrolleable(this);
			_panel.add(_scrollPane, BorderLayout.CENTER);
			Dimension ps = getPanelPreferredSize();
			_panel.setPreferredSize(ps);
		}
		return _panel;
	}

	private Dimension getPanelPreferredSize() {
		Dimension d = new Dimension();
		Dimension tableSize = getPreferredSize();
		d.width = tableSize.width;
		Dimension headerSize = getTableHeader().getPreferredSize();

		// TODO: This was reverse-engineered but I'm currently not sure why 10
		// pixels has to be added
		d.height = 10 + headerSize.height + (getRowHeight() * getRowCount());

		if (_scrollPane != null) {
			JScrollBar scrollBar = _scrollPane.getHorizontalScrollBar();
			if (scrollBar.isShowing()) {
				d.height = d.height + scrollBar.getHeight();
			}
		}

		logger.debug("Preferred size is: {}", d);

		return d;
	}

	/**
	 * Since setModel(...) is used to update the contents and repaint the
	 * widget, we will also make this happen if a panel is presenting the table.
	 */
	@Override
	public void setModel(TableModel dataModel) {
		super.setModel(dataModel);
		if (_panel != null) {
			_panel.setPreferredSize(getPanelPreferredSize());
		}
	}

	protected List<JMenuItem> getCopyMenuItems() {
		ImageIcon icon = ImageManager.getInstance().getImageIcon("images/actions/copy.png");
		List<JMenuItem> result = new ArrayList<JMenuItem>();

		// JMenuItem for "Copy selected cells to clipboard"
		JMenuItem copySelectedItem = new JMenuItem("Copy selected cells to clipboard", icon);
		copySelectedItem.addActionListener(_copySelectItemsActionListener);
		result.add(copySelectedItem);

		// JMenuItem for "Copy entire table to clipboard"
		JMenuItem copyTableItem = new JMenuItem("Copy entire table to clipboard", icon);
		copyTableItem.addActionListener(_copyEntireTableActionListener);
		result.add(copyTableItem);

		return result;
	}

	public void mouseClicked(MouseEvent e) {
		forwardMouseEvent(e);
	}

	public void mouseEntered(MouseEvent e) {
		// forwardMouseEvent(e);
	}

	public void mouseExited(MouseEvent e) {
		// forwardMouseEvent(e);
	}

	public void mousePressed(MouseEvent e) {
		forwardMouseEvent(e);
	}

	public void mouseReleased(MouseEvent e) {
		boolean forwarded = forwardMouseEvent(e);
		if (!forwarded) {
			// handle right click

			if (e.getClickCount() == 1) {
				int button = e.getButton();
				if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
					if (_rightClickMenuItems != null && _rightClickMenuItems.size() > 0) {
						JPopupMenu popup = new JPopupMenu();
						for (JMenuItem item : _rightClickMenuItems) {
							popup.add(item);
						}
						popup.show(e.getComponent(), e.getX(), e.getY());
						return;
					}
				}
			}
		}
	}

	private boolean forwardMouseEvent(MouseEvent e) {
		logger.debug("forwardMouseEvent({})", e);
		int x = e.getX();
		int y = e.getY();
		int col = getColumnModel().getColumnIndexAtX(x);
		int row = y / getRowHeight();

		if (row >= getRowCount()) {
			row = -1;
		}

		if (row == -1 || col == -1) {
			logger.info("Disregarding mouse event at {},{} (row={},col={})", new Object[] { x, y, row, col });
			return false;
		}

		Object value = getValueAt(row, col);
		if (value instanceof JComponent) {
			JComponent component = (JComponent) value;
			MouseEvent newEvent = SwingUtilities.convertMouseEvent(this, e, component);

			component.dispatchEvent(newEvent);
			repaint();
			return true;
		}
		return false;
	}

	/**
	 * Copies content from the table to the clipboard. Algorithm is a slight
	 * rewrite of the article below.
	 * 
	 * @see http://www.copy--paste.org/copy-paste-jtables-excel.htm
	 */
	public void copyToClipboard(int rowIndex, int colIndex, int width, int height) {
		StringBuilder sb = new StringBuilder();
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
			value = LabelConstants.NULL_LABEL;
		}
		return value;
	}

	public void setVisibleColumns(int min, int max) {
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

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		logger.debug("getCellRenderer({},{})", row, column);
		Object value = getValueAt(row, column);
		if (value instanceof JComponent) {
			return JComponentCellRenderer.getInstance();
		}
		if (value instanceof Icon) {
			return IconCellRenderer.getInstance();
		}
		return super.getCellRenderer(row, column);
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		logger.debug("getCellEditor({},{})", row, column);
		Object value = getValueAt(row, column);

		if (value instanceof JComponent) {
			return JComponentCellEditor.forComponent((JComponent) value);
		}

		return JComponentCellEditor.forComponent(null);
	}
}