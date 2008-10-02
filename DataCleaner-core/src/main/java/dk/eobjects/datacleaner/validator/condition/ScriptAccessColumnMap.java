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
package dk.eobjects.datacleaner.validator.condition;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

/**
 * Records which columns are being accessed by a script
 */
public class ScriptAccessColumnMap implements Map<String, Object> {

	private static final long serialVersionUID = -8578674203580125758L;
	private Set<Column> _evaluatedColumns;
	private Column[] _columns;
	private Row _row;

	public ScriptAccessColumnMap(Column[] columns, Row row,
			Set<Column> evaluatedColumns) {
		_columns = columns;
		_row = row;
		_evaluatedColumns = evaluatedColumns;
	}

	public Object get(Object key) {
		if (!(key instanceof String)) {
			throw new IllegalArgumentException();
		}
		for (Column column : _columns) {
			if (((String) key).equalsIgnoreCase(column.getName())) {
				_evaluatedColumns.add(column);
				Object value = _row.getValue(column);
				return value;
			}
		}
		throw new IllegalArgumentException("No such column '" + key + "'");
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public boolean containsKey(Object key) {
		throw new UnsupportedOperationException();
	}

	public boolean containsValue(Object value) {
		return get(value) != null;
	}

	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		throw new UnsupportedOperationException();
	}

	public boolean isEmpty() {
		return false;
	}

	public Set<String> keySet() {
		throw new UnsupportedOperationException();
	}

	public Object put(String key, Object value) {
		throw new UnsupportedOperationException();
	}

	public void putAll(Map<? extends String, ? extends Object> t) {
		throw new UnsupportedOperationException();
	}

	public Object remove(Object key) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		return _columns.length;
	}

	public Collection<Object> values() {
		throw new UnsupportedOperationException();
	}
}