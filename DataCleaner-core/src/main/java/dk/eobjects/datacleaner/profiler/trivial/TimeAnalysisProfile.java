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
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import dk.eobjects.datacleaner.profiler.AbstractProfile;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.MatrixBuilder;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.metamodel.data.IRowFilter;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.OperatorType;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;

/**
 * Provides profiling information for date and time based columns
 */
public class TimeAnalysisProfile extends AbstractProfile {

	private static final DateTimeFormatter DATE_AND_TIME_PATTERN = DateTimeFormat
			.forPattern("yyyy-MM-dd HH:mm:ss");
	private static final DateTimeFormatter TIME_ONLY_PATTERN = DateTimeFormat
			.forPattern("HH:mm:ss");
	private static final DateTimeFormatter DATE_ONLY_PATTERN = DateTimeFormat
			.forPattern("yyyy-MM-dd");

	private Map<Column, SortedMap<DateTime, Long>> _sortedMaps = new HashMap<Column, SortedMap<DateTime, Long>>();
	private Map<Column, Boolean> _isDateOnly = new HashMap<Column, Boolean>();
	private SortedSet<Integer> _years = new TreeSet<Integer>();

	@Override
	protected void processValue(Column column, Object value, long valueCount,
			Row row) {
		SortedMap<DateTime, Long> sortedMap = _sortedMaps.get(column);
		if (sortedMap == null) {
			sortedMap = new TreeMap<DateTime, Long>();
			_sortedMaps.put(column, sortedMap);
		}

		DateTime dateTime = new DateTime(value);

		Boolean isDateOnly = _isDateOnly.get(column);
		if (isDateOnly == null) {
			isDateOnly = Boolean.TRUE;
			_isDateOnly.put(column, isDateOnly);
		}
		if (isDateOnly == Boolean.TRUE) {
			if (!LocalTime.MIDNIGHT.equals(dateTime.toLocalTime())) {
				isDateOnly = Boolean.FALSE;
				_isDateOnly.put(column, isDateOnly);
			}
		}

		_years.add(dateTime.getYear());

		Long dateTimeCount = sortedMap.get(dateTime);
		if (dateTimeCount == null) {
			dateTimeCount = 0l;
		}
		dateTimeCount += valueCount;
		sortedMap.put(dateTime, dateTimeCount);
	}

	@Override
	protected List<IMatrix> getResultMatrices() {
		List<IMatrix> result = new ArrayList<IMatrix>();
		MatrixBuilder matrixBuilder = new MatrixBuilder();
		matrixBuilder.addRow("Highest value");
		matrixBuilder.addRow("Lowest value");

		for (Integer year : _years) {
			matrixBuilder.addRow("Where [Year=" + year + "]");
		}

		for (final Column column : _columns) {
			SortedMap<DateTime, Long> sortedMap = _sortedMaps.get(column);
			Boolean isDateOnly = _isDateOnly.get(column);

			String lowestValue = null;
			String highestValue = null;
			if (column.getType() == ColumnType.TIME) {
				lowestValue = sortedMap.firstKey().toString(TIME_ONLY_PATTERN);
				highestValue = sortedMap.lastKey().toString(TIME_ONLY_PATTERN);
			} else {
				if (isDateOnly) {
					lowestValue = sortedMap.firstKey().toString(
							DATE_ONLY_PATTERN);
					highestValue = sortedMap.lastKey().toString(
							DATE_ONLY_PATTERN);
				} else {
					lowestValue = sortedMap.firstKey().toString(
							DATE_AND_TIME_PATTERN);
					highestValue = sortedMap.lastKey().toString(
							DATE_AND_TIME_PATTERN);
				}
			}

			int rowCount = matrixBuilder.getRowCount();
			Object[] columnContent = new Object[rowCount];
			columnContent[0] = highestValue;
			columnContent[1] = lowestValue;
			Integer[] yearArray = _years.toArray(new Integer[_years.size()]);
			for (int i = 0; i < yearArray.length; i++) {
				int yearCount = 0;
				DateTime from = new DateTime(yearArray[i], 1, 1, 0, 0, 0, 0);
				DateTime to = new DateTime(yearArray[i] + 1, 1, 1, 0, 0, 0, 0);
				SortedMap<DateTime, Long> subMap = sortedMap.subMap(from, to);
				Set<Entry<DateTime, Long>> entrySet = subMap.entrySet();
				for (Entry<DateTime, Long> entry : entrySet) {
					yearCount += entry.getValue();
				}
				columnContent[2 + i] = yearCount;
			}

			MatrixValue[] matrixValues = matrixBuilder.addColumn(column
					.getName(), columnContent);
			for (int i = 0; i < yearArray.length; i++) {
				final int year = yearArray[i];
				MatrixValue mv = matrixValues[2 + i];
				if (((Integer) mv.getValue()) > 0) {
					Query q = getBaseQuery();
					if (column.getType() == ColumnType.DATE
							|| column.getType() == ColumnType.TIMESTAMP) {
						q.where(column, OperatorType.HIGHER_THAN,
								new LocalDate(year - 1, 12, 31).toDateTimeAtStartOfDay());
						q.where(column, OperatorType.LOWER_THAN, new LocalDate(
								year + 1, 1, 1).toDateTimeAtStartOfDay());
					}
					mv.setDetailSource(q);
					mv.addDetailRowFilter(new IRowFilter() {

						public boolean accept(Row row) {
							Object value = row.getValue(column);
							if (value != null) {
								DateTime dateTime = new DateTime(value);
								if (dateTime.getYear() == year) {
									return true;
								}
							}
							return false;
						}

					});
				}
			}
		}
		if (!matrixBuilder.isEmpty()) {
			result.add(matrixBuilder.getMatrix());
		}
		return result;
	}
}