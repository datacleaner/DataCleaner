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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import dk.eobjects.datacleaner.LabelConstants;
import dk.eobjects.datacleaner.gui.DataCleanerGui;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;
import dk.eobjects.datacleaner.gui.windows.DataSetWindow;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;

public class MatrixTable extends DataCleanerTable {

	private static final long serialVersionUID = 7057950015120409869L;
	private static final Icon HYPERLINK_ICON = GuiHelper
			.getImageIcon("images/matrixtable_goto_details.png");
	private DataContext _dataContext;
	private IMatrix _matrix;
	private boolean _horisontalMatrix;

	@Override
	public void removeNotify() {
		super.removeNotify();
		_log.debug("removeNotify()");
		_dataContext = null;
		_matrix = null;
	}

	public MatrixTable(IMatrix matrix, DataContext dataContext) {
		super();
		setHighlighters(GuiHelper.LIBERELLO_HIGHLIGHTER, new ColumnHighlighter(
				0));
		_dataContext = dataContext;
		_matrix = matrix;

		// Hack to make actions on drill-to-detail hyperlinks
		setEditable(true);

		boolean horisontalMatrix = GuiSettings.getSettings()
				.isHorisontalMatrixTables();
		updateTableModel(horisontalMatrix);

		JMenuItem diceTableMenuItem = new JMenuItem("Swap columns and rows",
				GuiHelper
						.getImageIcon("images/matrixtable_swap_dimensions.png"));
		diceTableMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateTableModel(!_horisontalMatrix);
				if (_panel != null) {
					Dimension d = getPreferredSize();
					d.height = d.height
							+ getTableHeader().getPreferredSize().height;
					_panel.setSize(d);
					_panel.setPreferredSize(d);
				}
			}
		});
		_rightClickMenuItems.add(diceTableMenuItem);
	}

	public void updateTableModel(boolean horisontalMatrix) {
		_horisontalMatrix = horisontalMatrix;
		DefaultTableModel tableModel = new DefaultTableModel();
		String[] rowNames = _matrix.getRowNames();
		String[] columnNames = _matrix.getColumnNames();
		MatrixValue[][] values = _matrix.getValues();
		if (_horisontalMatrix) {
			// Lay out table according to matrix (columns on X axis)
			String[] headers = new String[columnNames.length + 1];
			headers[0] = "";
			for (int i = 0; i < columnNames.length; i++) {
				headers[i + 1] = columnNames[i];
			}

			tableModel.setColumnIdentifiers(headers);
			tableModel.setRowCount(rowNames.length);

			for (int i = 0; i < rowNames.length; i++) {
				String rowName = rowNames[i];
				tableModel.setValueAt(rowName, i, 0);
			}
			for (int i = 0; i < values.length; i++) {
				for (int j = 0; j < values[i].length; j++) {
					tableModel.setValueAt(values[i][j], i, 1 + j);
				}
			}
		} else {
			// Lay out table opposite to matrix (columns on Y axis)
			String[] headers = new String[rowNames.length + 1];
			headers[0] = "";
			for (int i = 0; i < rowNames.length; i++) {
				headers[i + 1] = rowNames[i];
			}

			tableModel.setColumnIdentifiers(headers);
			tableModel.setRowCount(columnNames.length);

			for (int i = 0; i < columnNames.length; i++) {
				String columnName = columnNames[i];
				tableModel.setValueAt(columnName, i, 0);
			}
			for (int i = 0; i < values.length; i++) {
				for (int j = 0; j < values[i].length; j++) {
					tableModel.setValueAt(values[i][j], j, 1 + i);
				}
			}
		}
		setModel(tableModel);
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		if (column == 0) {
			return _labelCellRenderer;
		}
		final Object value = getValueAt(row, column);
		if (value instanceof MatrixValue) {
			MatrixValue mv = (MatrixValue) value;
			if (mv.isDetailed()) {
				return _hyperlinkCellRenderer;
			}
		}
		return _labelCellRenderer;
	}

	/**
	 * Cell renderer that renders a clickable hyperlink, usable for
	 * MatrixValue's with details
	 */
	private TableCellRenderer _hyperlinkCellRenderer = new TableCellRenderer() {

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			final MatrixValue mv = (MatrixValue) value;
			String text = LabelConstants.NULL_LABEL;
			if (mv.getValue() != null) {
				text = mv.getValue().toString();
			}
			JLabel label = new JLabel(text, HYPERLINK_ICON, JLabel.RIGHT);
			label.setOpaque(true);
			label
					.setToolTipText("Click to drill to detail data for this measure.");
			Font font = label.getFont().deriveFont(Font.BOLD);
			label.setFont(font);
			return label;
		}
	};

	/**
	 * Cell renderer that simply renders a label
	 */
	private TableCellRenderer _labelCellRenderer = new TableCellRenderer() {

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			String text = LabelConstants.NULL_LABEL;
			if (value instanceof MatrixValue) {
				MatrixValue mv = (MatrixValue) value;
				if (mv.getValue() != null) {
					text = mv.getValue().toString();
				}
			} else {
				if (value != null) {
					text = value.toString();
				}
			}
			JLabel label = new JLabel(text, JLabel.RIGHT);
			label.setOpaque(true);
			return label;
		}
	};

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		final Object value = getValueAt(row, column);
		if (value instanceof MatrixValue) {
			MatrixValue mv = (MatrixValue) value;
			if (mv.isDetailed()) {
				DataSet detailsData = mv.getDetails(_dataContext);

				String columnName;
				String rowName;
				if (_horisontalMatrix) {
					columnName = _matrix.getColumnNames()[column - 1];
					rowName = _matrix.getRowNames()[row];
				} else {
					columnName = _matrix.getRowNames()[column - 1];
					rowName = _matrix.getColumnNames()[row];
				}
				DataSetWindow frame = new DataSetWindow("Detail data for ["
						+ columnName + ", " + rowName + "]", detailsData);
				DataCleanerGui.getMainWindow().addWindow(frame);
			}
		}
		return _doNothingEditor;
	}

	private TableCellEditor _doNothingEditor = new TableCellEditor() {

		public boolean isCellEditable(EventObject anEvent) {
			return false;
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			return null;
		}

		public void addCellEditorListener(CellEditorListener l) {
		}

		public void cancelCellEditing() {
		}

		public Object getCellEditorValue() {
			return null;
		}

		public void removeCellEditorListener(CellEditorListener l) {
		}

		public boolean shouldSelectCell(EventObject anEvent) {
			return true;
		}

		public boolean stopCellEditing() {
			return true;
		}
	};
}