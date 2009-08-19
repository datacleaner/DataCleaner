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
package dk.eobjects.datacleaner.comparator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.LabelConstants;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.MatrixBuilder;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;

public class ColumnComparator {

	private static final Log _log = LogFactory.getLog(ColumnComparator.class);
	private Map<Object, Map<Column, Long>> _valueCounts = new HashMap<Object, Map<Column, Long>>();
	private Column[] _columns;
	
	public Column[] getColumns() {
		return _columns;
	}

	public void initialize(Column... columns) {
		_columns = columns;
		ColumnType type = null;
		for (Column column : columns) {
			if (type == null) {
				type = column.getType();
			} else {
				if (type != column.getType()) {
					_log
							.warn("Columns are of different types, which may make them incomparable: "
									+ ArrayUtils.toString(columns));
				}
			}
		}
	}

	public void processValue(Column column, Object value, long count) {
		Map<Column, Long> valueCount = _valueCounts.get(value);
		if (valueCount == null) {
			valueCount = new HashMap<Column, Long>();
			_valueCounts.put(value, valueCount);
		}
		valueCount.put(column, count);
		if (cleanValueCount(valueCount)) {
			_valueCounts.remove(value);
		}
	}

	public IMatrix getResult() {
		MatrixBuilder mb = new MatrixBuilder();
		for (int i = 0; i < _columns.length; i++) {
			mb.addColumn(_columns[i].getName());
		}
		Set<Entry<Object, Map<Column, Long>>> entries = _valueCounts.entrySet();
		for (Entry<Object, Map<Column, Long>> entry : entries) {
			Object value = entry.getKey();
			Map<Column, Long> valueCount = entry.getValue();
			String valueName = LabelConstants.NULL_LABEL;
			if (value != null) {
				valueName = value.toString();
			}
			Object[] counts = new Long[_columns.length];
			for (int i = 0; i < _columns.length; i++) {
				Long count = valueCount.get(_columns[i]);
				if (count == null) {
					count = 0l;
				}
				counts[i] = count;
			}
			mb.addRow(valueName, counts);
		}
		mb.sortColumn(0, MatrixBuilder.DESCENDING);
		return mb.getMatrix();
	}

	/**
	 * Gets a partial result containing only the values that where found in the
	 * given column (metaphorically somewhat similar to a left join result,
	 * whereas the full result will be an outer join of results).
	 */
	public IMatrix getResultForColumn(Column column) {
		MatrixBuilder mb = new MatrixBuilder();
		int columnIndex = 0;
		for (int i = 0; i < _columns.length; i++) {
			Column col = _columns[i];
			if (col.equals(column)) {
				columnIndex = i;
			}
			mb.addColumn(col.getName());
		}
		Set<Entry<Object, Map<Column, Long>>> entries = _valueCounts.entrySet();
		for (Entry<Object, Map<Column, Long>> entry : entries) {
			Map<Column, Long> valueCount = entry.getValue();
			if (valueCount.containsKey(column)) {
				Object value = entry.getKey();

				String valueName = LabelConstants.NULL_LABEL;
				if (value != null) {
					valueName = value.toString();
				}
				Object[] counts = new Long[_columns.length];
				for (int i = 0; i < _columns.length; i++) {
					Long count = valueCount.get(_columns[i]);
					if (count == null) {
						count = 0l;
					}
					counts[i] = count;
				}
				mb.addRow(valueName, counts);
			}
		}
		mb.sortColumn(columnIndex, MatrixBuilder.DESCENDING);
		return mb.getMatrix();
	}

	/**
	 * Removes a value count from this ColumnComparator's state if all columns
	 * are registered in the value count and the count's are equal
	 */
	private boolean cleanValueCount(Map<Column, Long> valueCount) {
		if (valueCount.size() == _columns.length) {
			Long globalCount = null;
			for (Column column : _columns) {
				Long count = valueCount.get(column);
				if (count == null) {
					return false;
				}
				if (globalCount == null) {
					globalCount = count;
				} else {
					if (!globalCount.equals(count)) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}
}