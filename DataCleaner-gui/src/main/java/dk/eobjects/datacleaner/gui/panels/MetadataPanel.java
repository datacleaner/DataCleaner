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
package dk.eobjects.datacleaner.gui.panels;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.gui.GuiBuilder;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.widgets.DataCleanerTable;
import dk.eobjects.datacleaner.util.WeakObservable;
import dk.eobjects.datacleaner.util.WeakObserver;
import dk.eobjects.metamodel.schema.Column;

public class MetadataPanel extends JPanel implements WeakObserver {

	private static final long serialVersionUID = -8700916792678228132L;
	protected final Log _log = LogFactory.getLog(getClass());
	public static final int COLUMN_TABLE = 0;
	public static final int COLUMN_COLUMN = 1;
	public static final int COLUMN_TYPE = 2;
	public static final int COLUMN_NATIVE_TYPE = 3;
	public static final int COLUMN_SIZE = 4;
	public static final int COLUMN_NULLABLE = 5;
	public static final int COLUMN_INDEXED = 6;
	public static final int COLUMN_REMARKS = 7;
	private DataCleanerTable _table;
	private ColumnSelection _columnSelection;

	@Override
	public void removeNotify() {
		super.removeNotify();
		_log.debug("removeNotify()");
		_columnSelection.deleteObserver(this);
		_columnSelection = null;
	}

	public MetadataPanel(ColumnSelection columnSelection) {
		new GuiBuilder<JPanel>(this).applyLightBackground().applyBorderLayout();
		_columnSelection = columnSelection;
		_columnSelection.addObserver(this);
		updatePanel();
	}

	private void updatePanel() {
		List<Column> columns = _columnSelection.getColumns();
		int rowCount = columns.size();
		int columnCount = 7;

		DefaultTableModel tableModel = new DefaultTableModel(rowCount,
				columnCount);
		tableModel.setColumnIdentifiers(new String[] { "Table", "Column",
				"Type", "Native type", "Size", "Nullable", "Indexed?",
				"Remarks" });
		for (int i = 0; i < columns.size(); i++) {
			Column column = columns.get(i);
			tableModel.setValueAt(column.getTable().getName(), i, COLUMN_TABLE);
			tableModel.setValueAt(column.getName(), i, COLUMN_COLUMN);
			tableModel.setValueAt(column.getType(), i, COLUMN_TYPE);
			tableModel
					.setValueAt(column.getNativeType(), i, COLUMN_NATIVE_TYPE);
			tableModel.setValueAt(column.getColumnSize(), i, COLUMN_SIZE);
			tableModel.setValueAt(column.isNullable(), i, COLUMN_NULLABLE);
			tableModel.setValueAt(column.isIndexed(), i, COLUMN_INDEXED);
			tableModel.setValueAt(column.getRemarks(), i, COLUMN_REMARKS);
		}

		_table = new DataCleanerTable();
		_table.setModel(tableModel);
		_table.addHighlighter(GuiHelper.LIBERELLO_HIGHLIGHTER);
		removeAll();
		add(_table.toPanel());
	}

	public void update(WeakObservable o) {
		if (o instanceof ColumnSelection) {
			updatePanel();
		}
	}

	public DataCleanerTable getTable() {
		return _table;
	}

}
