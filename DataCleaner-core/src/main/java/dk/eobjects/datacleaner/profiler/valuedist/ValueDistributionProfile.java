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
package dk.eobjects.datacleaner.profiler.valuedist;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import dk.eobjects.datacleaner.LabelConstants;
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
import dk.eobjects.metamodel.util.FileHelper;

public class ValueDistributionProfile extends AbstractProfile {

	public static final String PROPERTY_TOP_N = "Top n most frequent values";
	public static final String PROPERTY_BOTTOM_N = "Bottom n least frequent values";

	private Map<Column, StoredMap> _repeatedValues = new HashMap<Column, StoredMap>();
	private Map<Column, Long> _nullValues = new HashMap<Column, Long>();
	private List<Database> _databases = new LinkedList<Database>();
	private Environment _environment;
	private Integer _topCount;
	private Integer _bottomCount;

	@Override
	public void initialize(Column... columns) {
		super.initialize(columns);
		EnvironmentConfig environmentConfig = new EnvironmentConfig();
		environmentConfig.setAllowCreate(true);

		try {
			File tempDir = FileHelper.getTempDir();
			_environment = new Environment(tempDir, environmentConfig);
		} catch (DatabaseException e) {
			throw new IllegalStateException(e);
		}
	}

	private long incrementNullCount(Column column, long amount) {
		Long count = _nullValues.get(column);
		if (count == null) {
			count = amount;
		} else {
			count += amount;
		}
		_nullValues.put(column, count);
		return count;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Long> getRepeatedValues(Column column) {
		StoredMap map = _repeatedValues.get(column);
		if (map == null) {
			synchronized (_repeatedValues) {
				map = _repeatedValues.get(column);
				if (map == null) {
					DatabaseConfig databaseConfig = new DatabaseConfig();
					databaseConfig.setAllowCreate(true);
					try {
						String databaseName = column.getQualifiedLabel()
								+ System.currentTimeMillis();
						Database database = _environment.openDatabase(null,
								databaseName, databaseConfig);
						_databases.add(database);
						EntryBinding keyBinding = new StringBinding();
						EntryBinding valueBinding = new LongBinding();
						map = new StoredMap(database, keyBinding, valueBinding,
								true);

						_repeatedValues.put(column, map);

						_log
								.info("Created temporary database: "
										+ databaseName);
					} catch (DatabaseException e) {
						throw new IllegalStateException(e);
					}
				}
			}
		}
		return map;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		super.setProperties(properties);
		_topCount = getPropertyInteger(PROPERTY_TOP_N);
		_bottomCount = getPropertyInteger(PROPERTY_BOTTOM_N);
	}

	@Override
	protected void processValue(Column column, Object value, long valueCount,
			Row row) {

		if (value == null) {
			incrementNullCount(column, valueCount);
		} else {
			String repeatedValue = value.toString();
			Map<String, Long> valueMap = getRepeatedValues(column);
			Long repeatCount = valueMap.get(repeatedValue);
			if (repeatCount == null) {
				repeatCount = valueCount;
			} else {
				repeatCount += valueCount;
			}
			valueMap.put(repeatedValue, repeatCount);
		}
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

			Set<Entry<String, Long>> entries = getRepeatedValues(column)
					.entrySet();

			ScoredList topEntries = new ScoredList(true, _topCount);
			ScoredList bottomEntries = new ScoredList(false, _bottomCount);
			long uniqueValuesCount = 0l;

			// add null values first
			Long nullCount = _nullValues.get(column);
			if (nullCount != null) {
				topEntries.register(null, nullCount);
				bottomEntries.register(null, nullCount);
			}

			for (Iterator<Entry<String, Long>> it = entries.iterator(); it
					.hasNext();) {
				Entry<String, Long> entry = it.next();
				Long count = entry.getValue();

				// Add to the uniqueValues list
				if (count == 1l) {
					uniqueValuesCount++;
					if (uniqueValuesCount == 1l) {
						bottomEntries.decrementCapacity();
					}
				} else {
					topEntries.register(entry);
					bottomEntries.register(entry);
				}

			}

			// Remove any duplicate entries
			Long lowestScore = topEntries.getLowestScore();
			if (lowestScore != null) {
				bottomEntries.removeAbove(lowestScore - 1);
			}

			String[] detailOperands = new String[topCount + bottomCount];
			int numTopEntries = topEntries.size();
			Iterator<Entry<String, Long>> entryIterator = topEntries
					.iterateLowToHigh();
			int i = 0;
			while (entryIterator.hasNext()) {
				int index = numTopEntries - 1 - i;
				Entry<String, Long> topEntry = entryIterator.next();
				String value = topEntry.getKey();
				rowValues[index] = value + " (" + topEntry.getValue() + ")";
				detailOperands[index] = value;
				i++;
			}

			i = rowValues.length - 1;
			if (uniqueValuesCount > 0l) {
				i--;
			}
			entryIterator = bottomEntries.iterateLowToHigh();
			while (entryIterator.hasNext()) {
				Entry<String, Long> bottomEntry = entryIterator.next();
				String value = bottomEntry.getKey();
				if (value == null) {
					rowValues[i] = LabelConstants.NULL_LABEL + " ("
							+ bottomEntry.getValue() + ")";
				} else {
					rowValues[i] = value + " (" + bottomEntry.getValue() + ")";
				}
				detailOperands[i] = value;
				i--;
			}
			entryIterator = null;

			MatrixValue[] matrixValues = mb.addColumn(columnName, rowValues);
			if (isDetailsEnabled()) {
				for (i = 0; i < matrixValues.length; i++) {
					MatrixValue matrixValue = matrixValues[i];
					if (matrixValue.getValue() != null) {
						generateDetailSources(matrixValue, column,
								detailOperands[i]);
					}
				}

			}

			if (uniqueValuesCount > 0l) {
				if (isDetailsEnabled()) {
					Query q = getBaseQuery(column).having(
							new FilterItem(SelectItem.getCountAllItem(),
									OperatorType.EQUALS_TO, 1));
					matrixValues[matrixValues.length - 1].setDetailSource(q);

				}
				matrixValues[matrixValues.length - 1]
						.setValue(LabelConstants.UNIQUE_VALUES_LABEL + " ("
								+ uniqueValuesCount + ")");
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

			Map<String, Long> valueMap = getRepeatedValues(column);

			MatrixBuilder mb = new MatrixBuilder();
			mb.addColumn(columnName + " frequency");
			mb.addColumn("Percentage of total");

			List<Object[]> uniqueValues = new ArrayList<Object[]>();

			// add null values first
			Long nullValues = _nullValues.get(column);
			if (nullValues != null) {
				if (nullValues.longValue() == 1l) {
					uniqueValues.add(new Object[] { null });
				} else if (nullValues.longValue() > 0l) {
					int repeatPercentage = (int) (nullValues * 100 / _totalCount);
					MatrixValue[] matrixValues = mb.addRow(
							LabelConstants.NULL_LABEL, nullValues,
							repeatPercentage + "%");
					generateDetailSources(matrixValues[0], column, null);
				}
			}

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
				MatrixValue[] matrixValues = mb.addRow(
						LabelConstants.UNIQUE_VALUES_LABEL, uniqueCount,
						otherPercentage + "%");
				matrixValues[0].setDetailSource(new DataSet(
						new SelectItem[] { new SelectItem(
								LabelConstants.UNIQUE_VALUES_LABEL, null) },
						uniqueValues));
			}

			result.add(mb.getMatrix());
		}
		return result;
	}

	@Override
	public void close() {
		super.close();
		try {
			for (Database database : _databases) {
				String databaseName = database.getDatabaseName();
				database.close();
				_environment.removeDatabase(null, databaseName);
				_log.info("Removed temporary database: " + databaseName);
			}
			_environment.compress();
			_environment.cleanLog();
			_environment.sync();
			File home = _environment.getHome();
			_environment.close();
			File[] databaseFiles = home.listFiles(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					return name.endsWith(".jdb");
				}
			});
			for (File file : databaseFiles) {
				file.deleteOnExit();
			}
		} catch (DatabaseException e) {
			_log.error(e);
		}
	}
}