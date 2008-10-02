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
package dk.eobjects.datacleaner.data;

import java.util.ArrayList;
import java.util.List;

import dk.eobjects.datacleaner.util.WeakObservable;
import dk.eobjects.datacleaner.util.WeakObserver;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

/**
 * A users selection of columns (typically for a profile or validation rule).
 */
public class ColumnSelection extends WeakObservable implements WeakObserver {

	private List<Column> _columns = new ArrayList<Column>();
	
	public ColumnSelection() {
		super();
	}

	public ColumnSelection(DataContextSelection dataContextSelection) {
		if (dataContextSelection != null) {
			dataContextSelection.addObserver(this);
		}
	}

	public List<Column> getColumns() {
		return _columns;
	}

	public void toggleTable(Table table) {
		Column[] columns = table.getColumns();
		for (int i = 0; i < columns.length; i++) {
			if (_columns.contains(columns[i])) {
				_columns.remove(columns[i]);
			} else {
				_columns.add(columns[i]);
			}
		}
		setChanged();
		notifyObservers();
	}

	public void toggleColumn(Column column) {
		if (_columns.contains(column)) {
			_columns.remove(column);
		} else {
			_columns.add(column);
		}
		setChanged();
		notifyObservers();
	}

	public void clearSelection() {
		_columns.clear();
		setChanged();
		notifyObservers();
	}

	public boolean isSelected(Column column) {
		return _columns.contains(column);
	}

	public void update(WeakObservable observable) {
		clearSelection();
	}
}