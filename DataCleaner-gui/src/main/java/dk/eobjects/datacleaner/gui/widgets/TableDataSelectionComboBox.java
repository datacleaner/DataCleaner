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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.util.WeakObservable;
import dk.eobjects.datacleaner.util.WeakObserver;
import dk.eobjects.metamodel.MetaModelHelper;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

/**
 * A combo box that lists the tables in a data selection
 */
public class TableDataSelectionComboBox extends JComboBox implements
		WeakObserver {

	private static final long serialVersionUID = -1710922243398760674L;
	private static final Log _log = LogFactory.getLog(SchemaTree.class);
	private ColumnSelection _columnSelection;
	private Map<String, Table> _tables;

	@Override
	public void removeNotify() {
		super.removeNotify();
		_log.debug("removeNotify()");
		_columnSelection.deleteObserver(this);
		_columnSelection = null;
		_tables = null;
	}

	public TableDataSelectionComboBox(ColumnSelection columnSelection) {
		super();
		_columnSelection = columnSelection;
		_columnSelection.addObserver(this);
		setEditable(false);
		updateTables();
	}

	public void update(WeakObservable o) {
		if (o instanceof ColumnSelection) {
			updateTables();
		}
	}

	private void updateTables() {
		removeAllItems();
		_tables = new HashMap<String, Table>();
		List<Column> columns = _columnSelection.getColumns();
		Table[] tables = MetaModelHelper.getTables(columns);
		for (int i = 0; i < tables.length; i++) {
			Table table = tables[i];
			String tableName = table.getName();
			_tables.put(tableName, table);
			addItem(tableName);
		}
	}

	public void setSelectedTable(Table table) {
		String tableName = table.getName();
		if (_tables.containsKey(tableName)) {
			setSelectedItem(tableName);
		}
	}

	public Table getSelectedTable() {
		String tableName = (String) getSelectedItem();
		return _tables.get(tableName);
	}
}