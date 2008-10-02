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
package dk.eobjects.datacleaner.profiler.trivial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.eobjects.datacleaner.profiler.AbstractProfile;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.MatrixBuilder;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.FilterItem;
import dk.eobjects.metamodel.query.OperatorType;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.util.ObjectComparator;

/**
 * A profile with standard measures for all kinds of columns. This profile is
 * intended to provide a general overview of data. The measures contained are:
 * 
 * <li>Row count</li> <li>Null values</li> <li>Empty values</li> <li>Highest
 * value</li> <li>Lowest value</li>
 */
public class StandardMeasuresProfile extends AbstractProfile {

	private static final short INDEX_NULL_COUNT = 0;
	private static final short INDEX_EMPTY_COUNT = 1;
	private static final short INDEX_HIGHEST_VALUE = 2;
	private static final short INDEX_LOWEST_VALUE = 3;
	private Map<Column, Object[]> _counts = new HashMap<Column, Object[]>();

	@Override
	protected void processValue(Column column, Object value, long valueCount,
			Row row) {
		Object[] counters = _counts.get(column);
		if (counters == null) {
			counters = new Object[4];
			counters[INDEX_NULL_COUNT] = 0l;
			counters[INDEX_EMPTY_COUNT] = 0l;
			counters[INDEX_HIGHEST_VALUE] = null;
			counters[INDEX_LOWEST_VALUE] = null;
			_counts.put(column, counters);
		}
		if (value == null) {
			counters[INDEX_NULL_COUNT] = ((Long) counters[INDEX_NULL_COUNT])
					+ valueCount;
		} else {
			checkHighestValue(value, counters);
			checkLowestValue(value, counters);
			if ("".equals(value.toString().trim())) {
				counters[INDEX_EMPTY_COUNT] = ((Long) counters[INDEX_EMPTY_COUNT])
						+ valueCount;
			}
		}
	}

	private void checkLowestValue(Object value, Object[] counters) {
		if (value != null) {
			Object lowestValueBefore = counters[INDEX_LOWEST_VALUE];
			if (lowestValueBefore != null) {
				Object highestValue = getHighestObject(value, lowestValueBefore);
				if (highestValue == lowestValueBefore) {
					counters[INDEX_LOWEST_VALUE] = value;
				}
			} else {
				counters[INDEX_LOWEST_VALUE] = value;
			}
		}
	}

	private void checkHighestValue(Object value, Object[] counters) {
		Object highestValueBefore = counters[INDEX_HIGHEST_VALUE];
		Object highestValue = getHighestObject(value, highestValueBefore);
		if (highestValue == value) {
			counters[INDEX_HIGHEST_VALUE] = value;
		}
	}

	/**
	 * @param obj1
	 * @param obj2
	 * @return the highest of the two objects. can be null if the objects cant
	 *         be compared or are null.
	 */
	public static Object getHighestObject(Object obj1, Object obj2) {
		if (obj1 == null && obj2 == null) {
			return null;
		}
		int compareResult = ObjectComparator.getComparator()
				.compare(obj1, obj2);
		if (compareResult > 0) {
			return obj1;
		}
		return obj2;
	}

	@Override
	protected List<IMatrix> getResultMatrices() {
		MatrixBuilder mb = new MatrixBuilder();
		mb.addRow("Row count");
		mb.addRow("Null values");
		mb.addRow("Empty values");
		mb.addRow("Highest value");
		mb.addRow("Lowest value");

		for (int i = 0; i < _columns.length; i++) {
			Column column = _columns[i];
			String columnName = column.getName();
			Object[] counts = _counts.get(column);
			Object nullCount = counts[INDEX_NULL_COUNT];
			Object emptyCount = counts[INDEX_EMPTY_COUNT];
			Object highestValue = counts[INDEX_HIGHEST_VALUE];
			Object lowestValue = counts[INDEX_LOWEST_VALUE];
			MatrixValue[] values = mb.addColumn(columnName, _totalCount,
					nullCount, emptyCount, highestValue, lowestValue);

			if (((Long) nullCount) > 0) {
				values[1].setDetailSource(new Query().from(column.getTable())
						.select(_columns).where(
								new FilterItem(new SelectItem(column),
										OperatorType.EQUALS_TO, null)));
			}

			if (((Long) emptyCount) > 0) {
				if (column.getType() == null || column.getType().isLiteral()) {
					values[2].setDetailSource(new Query().from(
							column.getTable()).select(_columns).where(
							new FilterItem(new SelectItem(column),
									OperatorType.EQUALS_TO, "")));
				}
			}
		}
		List<IMatrix> result = new ArrayList<IMatrix>();
		if (!mb.isEmpty()) {
			result.add(mb.getMatrix());
		}
		return result;
	}
}