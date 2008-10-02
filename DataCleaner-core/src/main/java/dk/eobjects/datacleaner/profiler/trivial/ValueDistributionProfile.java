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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import dk.eobjects.datacleaner.profiler.AbstractProfile;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.MatrixBuilder;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.IRowFilter;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.FilterItem;
import dk.eobjects.metamodel.query.OperatorType;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.util.ObjectComparator;

public class ValueDistributionProfile extends AbstractProfile {

	public static final String PROPERTY_TOP_N = "Top n most frequent values";
	public static final String PROPERTY_BOTTOM_N = "Bottom n least frequent values";
	private static final String UNIQUE_VALUES_LABEL = "<Unique values>";

	private Map<Column, Map<String, Long>> _repeatedValues = new HashMap<Column, Map<String, Long>>();
	private Integer _topCount;
	private Integer _bottomCount;

	@Override
	public void setProperties(Map<String, String> properties) {
		super.setProperties(properties);
		_topCount = getPropertyInteger(PROPERTY_TOP_N);
		_bottomCount = getPropertyInteger(PROPERTY_BOTTOM_N);
	}

	@Override
	protected void processValue(Column column, Object value, long valueCount,
			Row row) {
		/**
		 * TODO: Ticket #203: Enable Value Distribution for numeric values (not
		 * only strings)
		 * 
		 * @see http://eobjects.org/trac/ticket/203
		 */
		String repeatedValue = null;
		if (value != null) {
			repeatedValue = value.toString();
		}

		Map<String, Long> valueMap = _repeatedValues.get(column);
		if (valueMap == null) {
			valueMap = new HashMap<String, Long>();
			_repeatedValues.put(column, valueMap);
		}
		Long repeatCount = valueMap.get(repeatedValue);
		if (repeatCount == null) {
			repeatCount = 0l;
		}
		repeatCount += valueCount;
		valueMap.put(repeatedValue, repeatCount);
	}

	@Override
	protected List<IMatrix> getResultMatrices() {
		List<IMatrix> result;
		if (_topCount == null || _bottomCount == null) {
			result = generateResultForEachColumn();
		} else {
			result = generateCollectiveResult(_topCount, _bottomCount);
		}
		return result;
	}

	/**
	 * The preferred result type is one that contains a single matrix that holds
	 * value distributions for all columns that are profiled. This requires
	 * however that the top/bottom property is set, in order to specify the
	 * number of rows in the matrix.
	 * 
	 * @param topBottom
	 * @return
	 */
	private List<IMatrix> generateCollectiveResult(int topCount, int bottomCount) {
		MatrixBuilder mb = new MatrixBuilder();

		if (topCount <= 0) {
			_log.warn("The top property was zero or negative! Resetting to 5.");
			topCount = 5;
		}
		if (bottomCount <= 0) {
			_log
					.warn("The bottom property was zero or negative! Resetting to 5.");
			bottomCount = 5;
		}

		// Create the row headers (ie. for a topBottom value of 2: top 1, top 2,
		// bottom 2, bottom 1)
		for (int i = 0; i < topCount; i++) {
			mb.addRow("top " + (i + 1));
		}
		for (int i = bottomCount; i > 0; i--) {
			mb.addRow("bottom " + i);
		}

		for (final Column column : _columns) {
			String columnName = column.getName();
			Object[] rowValues = new Object[topCount + bottomCount];

			Set<Entry<String, Long>> entries = _repeatedValues.get(column)
					.entrySet();

			// Create two lists: A list of unique values and a list with the
			// entries sorted (highest count first).
			List<Object[]> uniqueValues = new ArrayList<Object[]>();
			List<Entry<String, Long>> sortedEntries = new ArrayList<Entry<String, Long>>(
					entries);
			// Populate the unique values list (and remove these unique values
			// from the sorted list)
			for (Iterator<Entry<String, Long>> it = sortedEntries.iterator(); it
					.hasNext();) {
				Entry<String, Long> entry = it.next();
				if (entry.getValue() == 1l) {
					uniqueValues.add(new Object[] { entry.getKey() });
					it.remove();
				}
			}
			// Sort the sorted list
			Collections.sort(sortedEntries,
					new Comparator<Entry<String, Long>>() {
						public int compare(Entry<String, Long> o1,
								Entry<String, Long> o2) {
							Long o1count = o1.getValue();
							Long o2count = o2.getValue();
							int compareTo = o2count.compareTo(o1count);
							if (compareTo == 0) {
								compareTo = ObjectComparator.getComparator()
										.compare(o1.getKey(), o2.getKey());
							}
							return compareTo;
						}
					});

			int registeredEntries = 0;
			// Take out the top n values from the sorted list and put them in a
			// seperate list
			List<Entry<String, Long>> topValues = new ArrayList<Entry<String, Long>>();
			for (Iterator<Entry<String, Long>> it = sortedEntries.iterator(); it
					.hasNext();) {
				Entry<String, Long> entry = it.next();
				topValues.add(entry);

				// remove the entry from the list so the list will only contain
				// bottom values (or none if it has been emptied out)
				it.remove();

				registeredEntries++;
				if (registeredEntries == topCount) {
					break;
				}
			}

			registeredEntries = 0;
			// Take out the bottom n values from the sorted list and put them in
			// a seperate list
			Collections.reverse(sortedEntries);
			List<Entry<String, Long>> bottomValues = new ArrayList<Entry<String, Long>>();
			boolean hasUniqueValues = !uniqueValues.isEmpty();
			if (hasUniqueValues) {
				// If there are unique values, these will get the bottom 1
				// placement
				rowValues[topCount + bottomCount - 1] = UNIQUE_VALUES_LABEL
						+ " (" + uniqueValues.size() + ")";
				registeredEntries++;
			}
			for (Iterator<Entry<String, Long>> it = sortedEntries.iterator(); it
					.hasNext();) {
				Entry<String, Long> entry = it.next();
				bottomValues.add(entry);

				registeredEntries++;
				if (registeredEntries == bottomCount) {
					break;
				}
			}
			sortedEntries = null;

			String[] detailOperands = new String[topCount + bottomCount];
			for (int i = 0; i < topCount; i++) {
				if (topValues.size() > i) {
					Entry<String, Long> topEntry = topValues.get(i);
					final String key = topEntry.getKey();
					if (key == null) {
						rowValues[i] = "<null> (" + topEntry.getValue() + ")";
					} else {
						rowValues[i] = key + " (" + topEntry.getValue() + ")";
					}
					detailOperands[i] = key;
				}
			}
			for (int i = 0; i < bottomCount; i++) {
				if (bottomValues.size() > i) {
					Entry<String, Long> bottomEntry = bottomValues.get(i);
					int bottomIndex = topCount + bottomCount - 1 - i;
					if (hasUniqueValues) {
						bottomIndex--;
					}
					if (rowValues.length > bottomIndex) {
						String key = bottomEntry.getKey();
						if (key == null) {
							rowValues[bottomIndex] = "<null> ("
									+ bottomEntry.getValue() + ")";
						} else {
							rowValues[bottomIndex] = key + " ("
									+ bottomEntry.getValue() + ")";
						}
						detailOperands[bottomIndex] = key;
					}
				}
			}

			MatrixValue[] matrixValues = mb.addColumn(columnName, rowValues);
			for (int i = 0; i < matrixValues.length; i++) {
				MatrixValue matrixValue = matrixValues[i];
				if (matrixValue.getValue() != null) {
					generateDetailSources(matrixValue, column,
							detailOperands[i]);
				}
			}

			if (hasUniqueValues) {
				Query q = getBaseQuery(column).having(
						new FilterItem(SelectItem.getCountAllItem(),
								OperatorType.EQUALS_TO, 1));
				matrixValues[matrixValues.length - 1].setDetailSource(q);
			}
		}

		ArrayList<IMatrix> result = new ArrayList<IMatrix>(1);
		result.add(mb.getMatrix());
		return result;
	}

	private void generateDetailSources(MatrixValue matrixValue,
			final Column column, final String value) {
		Query query = getBaseQuery();
		if (value != null && value.indexOf('\'') != -1) {
			// We have to approximate with a LIKE filter and
			// then postprocess because the value can't contain
			// single quote (#197)
			String wildcardValue = value.replace('\'', '%');
			query.where(column, OperatorType.LIKE, wildcardValue);
			matrixValue.addDetailRowFilter(new IRowFilter() {
				public boolean accept(Row row) {
					return value.equals(row.getValue(column));
				}
			});
		} else {
			query.where(column, OperatorType.EQUALS_TO, value);
		}
		matrixValue.setDetailSource(query);
	}

	/**
	 * If we don't have a top/bottom property set, we'll have to make a full
	 * value distribution table for each column
	 * 
	 * @return
	 */
	private List<IMatrix> generateResultForEachColumn() {
		ArrayList<IMatrix> result = new ArrayList<IMatrix>();
		for (int i = 0; i < _columns.length; i++) {
			final Column column = _columns[i];
			String columnName = column.getName();

			Map<String, Long> valueMap = _repeatedValues.get(column);

			MatrixBuilder mb = new MatrixBuilder();
			mb.addColumn(columnName + " frequency");
			mb.addColumn("Percentage of total");

			List<Object[]> uniqueValues = new ArrayList<Object[]>();

			for (Entry<String, Long> entry : valueMap.entrySet()) {
				Long repeatCount = entry.getValue();
				final String value = entry.getKey();
				if (repeatCount == 1l) {
					uniqueValues.add(new Object[] { value });
				} else {
					int repeatPercentage = (int) (repeatCount * 100 / _totalCount);
					MatrixValue[] matrixValues = mb.addRow(value, repeatCount,
							repeatPercentage + "%");

					generateDetailSources(matrixValues[0], column, value);
				}
			}

			if (!mb.isEmpty()) {
				mb.sortColumn(0, MatrixBuilder.DESCENDING);
			}

			int uniqueCount = uniqueValues.size();
			if (uniqueCount > 0) {
				int otherPercentage = (int) (uniqueCount * 100 / _totalCount);
				MatrixValue[] matrixValues = mb.addRow(UNIQUE_VALUES_LABEL,
						uniqueCount, otherPercentage + "%");
				matrixValues[0].setDetailSource(new DataSet(
						new SelectItem[] { new SelectItem(UNIQUE_VALUES_LABEL,
								null) }, uniqueValues));
			}

			result.add(mb.getMatrix());
		}
		return result;
	}
}