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
import java.util.SortedSet;
import java.util.TreeSet;

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

	// private Map<Column, SortedMap<DateTime, Long>> _sortedMaps = new
	// HashMap<Column, SortedMap<DateTime, Long>>();
	// private SortedSet<Integer> _years = new TreeSet<Integer>();
	private Map<Column, Map<Integer, Long>> _yearCounts = new HashMap<Column, Map<Integer, Long>>();
	private Map<Column, Boolean> _isDateOnly = new HashMap<Column, Boolean>();
	private Map<Column, DateTime> _lowestValues = new HashMap<Column, DateTime>();
	private Map<Column, DateTime> _highestValues = new HashMap<Column, DateTime>();

	@Override
	protected void processValue(Column column, Object value, long valueCount,
			Row row) {
		if (value != null) {
			Map<Integer, Long> yearCountsForColumn = _yearCounts.get(column);
			if (yearCountsForColumn == null) {
				yearCountsForColumn = new HashMap<Integer, Long>();
				_yearCounts.put(column, yearCountsForColumn);
			}

			DateTime dateTime = new DateTime(value);

			// Determines if this value is the lowest value so far
			DateTime lowestValueForColumn = _lowestValues.get(column);
			if (lowestValueForColumn == null) {
				lowestValueForColumn = dateTime;
				_lowestValues.put(column, lowestValueForColumn);
			} else {
				if (lowestValueForColumn.compareTo(dateTime) > 0) {
					lowestValueForColumn = dateTime;
					_lowestValues.put(column, lowestValueForColumn);
				}
			}

			// Determines if this value is the highest value so far
			DateTime highestValueForColumn = _highestValues.get(column);
			if (highestValueForColumn == null) {
				highestValueForColumn = dateTime;
				_highestValues.put(column, highestValueForColumn);
			} else {
				if (highestValueForColumn.compareTo(dateTime) < 0) {
					highestValueForColumn = dateTime;
					_highestValues.put(column, highestValueForColumn);
				}
			}

			// Defaultly sets this column to be "date only", but only untill any
			// a date that has a non-midnight time occurs
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

			// Increments the year count for the specified year
			int year = dateTime.getYear();
			Long yearCount = yearCountsForColumn.get(year);
			if (yearCount == null) {
				yearCount = 0l;
			}
			yearCount += valueCount;
			yearCountsForColumn.put(year, yearCount);
		}
	}

	@Override
	protected List<IMatrix> getResultMatrices() {
		List<IMatrix> result = new ArrayList<IMatrix>();
		MatrixBuilder matrixBuilder = new MatrixBuilder();
		matrixBuilder.addRow("Highest value");
		matrixBuilder.addRow("Lowest value");

		// Create a complete list of all years to make the row labels of the
		// matrix
		SortedSet<Integer> years = new TreeSet<Integer>();
		for (final Column column : _columns) {
			Map<Integer, Long> yearCountForColumn = _yearCounts.get(column);
			if (yearCountForColumn != null) {
				Set<Integer> yearsForColumn = yearCountForColumn.keySet();
				for (Integer year : yearsForColumn) {
					years.add(year);
				}
			}
		}

		for (Integer year : years) {
			matrixBuilder.addRow("Where [Year=" + year + "]");
		}

		for (final Column column : _columns) {
			Boolean isDateOnly = _isDateOnly.get(column);

			String lowestValue = null;
			String highestValue = null;
			DateTime lowestValueForColumn = _lowestValues.get(column);
			DateTime highestValueForColumn = _highestValues.get(column);
			if (lowestValueForColumn != null) {
				if (column.getType() == ColumnType.TIME) {
					lowestValue = lowestValueForColumn
							.toString(TIME_ONLY_PATTERN);
					highestValue = highestValueForColumn
							.toString(TIME_ONLY_PATTERN);
				} else {
					if (isDateOnly) {
						lowestValue = lowestValueForColumn
								.toString(DATE_ONLY_PATTERN);
						highestValue = highestValueForColumn
								.toString(DATE_ONLY_PATTERN);
					} else {
						lowestValue = lowestValueForColumn
								.toString(DATE_AND_TIME_PATTERN);
						highestValue = highestValueForColumn
								.toString(DATE_AND_TIME_PATTERN);
					}
				}
			}

			int rowCount = matrixBuilder.getRowCount();
			Object[] columnContent = new Object[rowCount];
			columnContent[0] = highestValue;
			columnContent[1] = lowestValue;
			Map<Integer, Long> yearCountsForColumn = _yearCounts.get(column);
			if (yearCountsForColumn != null) {
				int i = 0;
				for (Integer year : years) {
					Long yearCount = yearCountsForColumn.get(year);
					if (yearCount == null) {
						yearCount = 0l;
					}
					columnContent[2 + i] = yearCount;
					i++;
				}
			} else {
				for (int i = 0; i < years.size(); i++) {
					columnContent[2 + i] = 0l;
				}
			}

			MatrixValue[] matrixValues = matrixBuilder.addColumn(column
					.getName(), columnContent);

			int i = 0;
			for (final Integer year : years) {
				MatrixValue mv = matrixValues[2 + i];
				Long value = (Long) mv.getValue();
				if (value > 0) {
					Query q = getBaseQuery();
					if (column.getType() == ColumnType.DATE
							|| column.getType() == ColumnType.TIMESTAMP) {
						q.where(column, OperatorType.HIGHER_THAN,
								new LocalDate(year - 1, 12, 31)
										.toDateTimeAtStartOfDay());
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
				i++;
			}
		}
		if (!matrixBuilder.isEmpty()) {
			result.add(matrixBuilder.getMatrix());
		}
		return result;
	}
}