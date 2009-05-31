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
import java.util.Map.Entry;

import dk.eobjects.datacleaner.catalog.IDictionary;
import dk.eobjects.datacleaner.profiler.AbstractProfile;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.MatrixBuilder;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.datacleaner.util.ReflectionHelper;
import dk.eobjects.datacleaner.util.SimpleEntry;
import dk.eobjects.datacleaner.validator.dictionary.DictionaryManager;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.IRowFilter;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;

/**
 * Profile that matches values against several dictionaries, 'Dictionary
 * Matcher'.
 * 
 * The profile buffers requests to a dictionary in order to decrease number of
 * dictionary-requests/queries.
 */
public class DictionaryProfile extends AbstractProfile {

	public static final String PROPERTY_BUFFER_SIZE = "Buffer size";
	public static final String PREFIX_PROPERTY_DICTIONARY = "dictionary_";
	private static final String NO_MATCHES = "No Matches";
	private static final String MULTIPLE_MATCHES = "Multiple Matches";
	private int _queryBufferSize = 50;
	private List<Entry<Row, Long>> _bufferEntries = new ArrayList<Entry<Row, Long>>();

	private List<IDictionary> _dictionaryList;
	private Map<Column, Map<IDictionary, Long>> _matches = new HashMap<Column, Map<IDictionary, Long>>();
	private Map<Column, List<String>> _multipleMatches = new HashMap<Column, List<String>>();
	private Map<Column, List<String>> _noMatches = new HashMap<Column, List<String>>();

	public int getQueryBufferSize() {
		return _queryBufferSize;
	}

	public void setQueryBufferSize(int i) {
		_queryBufferSize = i;
	}
	
	public void setDictionaryList(List<IDictionary> dictionaryList) {
		_dictionaryList = dictionaryList;
	}
	

	@Override
	public void initialize(Column... columns) {
		super.initialize(columns);
		if (_properties != null) {
			String bufferSizeStr = _properties.get(PROPERTY_BUFFER_SIZE);
			if (bufferSizeStr != null) {
				try {
					_queryBufferSize = Integer.parseInt(bufferSizeStr);
				} catch (NumberFormatException e) {
					_log.info(e);
				}
			}
			
			List<String> dictionaryNames = ReflectionHelper.getIteratedProperties(
					PREFIX_PROPERTY_DICTIONARY, _properties);
			if (dictionaryNames.isEmpty()) {
				throw new IllegalArgumentException("No dictionaries specified");
			}
			_dictionaryList = new ArrayList<IDictionary>(dictionaryNames.size());
			for (String name : dictionaryNames) {
				IDictionary dictionary = DictionaryManager
				.getDictionaryByName(name);
				if (dictionary == null) {
					throw new IllegalArgumentException("No such dictionary, '"
							+ name + "'");
				}
				_dictionaryList.add(dictionary);
			}
		}
	}

	@Override
	protected List<IMatrix> getResultMatrices() {
		processBuffer();
		List<IMatrix> resultMatrices = new ArrayList<IMatrix>();
		MatrixBuilder mb = new MatrixBuilder();
		for (IDictionary dic : _dictionaryList) {
			mb.addRow(dic.getName());
		}
		mb.addRow(NO_MATCHES);
		mb.addRow(MULTIPLE_MATCHES);

		for (final Column column : _columns) {
			Map<IDictionary, Long> dictionaryMatches = _matches.get(column);
			if (dictionaryMatches == null) {
				dictionaryMatches = new HashMap<IDictionary, Long>();
			}
			List<String> colValues = new ArrayList<String>();

			// calculate matches by column against all the dictionaries.
			for (IDictionary dictionary : _dictionaryList) {
				Long matchCount = 0l;
				if (dictionaryMatches != null) {
					matchCount = dictionaryMatches.get(dictionary);
					if (matchCount == null) {
						matchCount = 0l;
					}
				}
				colValues.add("" + matchCount);
			}

			// find count for no matches
			List<String> noMatchesList = _noMatches.get(column);
			int noMatchesCount;
			if (noMatchesList == null) {
				noMatchesCount = 0;
			} else {
				noMatchesCount = noMatchesList.size();
			}
			colValues.add("" + noMatchesCount);

			// find count for multiple matches
			List<String> multiMatchesList = _multipleMatches.get(column);
			int multiMatchesCount;
			if (multiMatchesList == null) {
				multiMatchesCount = 0;
			} else {
				multiMatchesCount = multiMatchesList.size();
			}
			colValues.add("" + multiMatchesCount);

			MatrixValue[] matrixValues = mb.addColumn(column.getName(),
					colValues.toArray());

			if (isDetailsEnabled()) {
				// Create detail data for dictionary matches
				for (int i = 0; i < _dictionaryList.size(); i++) {
					final IDictionary dictionary = _dictionaryList.get(i);
					MatrixValue matrixValue = matrixValues[i];
					if (!"0".equals(matrixValue.getValue())) {
						matrixValue.setDetailSource(getBaseQuery(column))
								.addDetailRowFilter(new IRowFilter() {
									public boolean accept(Row row) {
										Object value = row.getValue(column);
										if (value != null
												&& dictionary.isValid(value
														.toString())[0]) {
											return true;
										}
										return false;
									}
								});
					}
				}

				// Create detail data for "no matches" and "multiple matches"
				if (noMatchesCount > 0) {
					MatrixValue matrixValue = matrixValues[matrixValues.length - 2];
					matrixValue.setDetailSource(createDataSet(column,
							noMatchesList));
				}
				if (multiMatchesCount > 0) {
					MatrixValue matrixValue = matrixValues[matrixValues.length - 1];
					matrixValue.setDetailSource(createDataSet(column,
							multiMatchesList));
				}
			}
		}
		resultMatrices.add(mb.getMatrix());
		return resultMatrices;
	}

	private DataSet createDataSet(Column column, List<String> noMatchesList) {
		SelectItem[] selectItems = new SelectItem[] { new SelectItem(column) };
		List<Object[]> data = new ArrayList<Object[]>(noMatchesList.size());
		for (String string : noMatchesList) {
			data.add(new Object[] { string });
		}
		return new DataSet(selectItems, data);
	}

	@Override
	public void process(Row row, long distinctRowCount) {
		super.process(row, distinctRowCount);
		_bufferEntries.add(new SimpleEntry<Row, Long>(row, distinctRowCount));
		int numValues = _columns.length * _bufferEntries.size();
		if (numValues >= _queryBufferSize) {
			processBuffer();
		}
	}

	@Override
	protected void processValue(Column column, Object value, long valueCount,
			Row row) {
		// Do nothing
	}

	private void processBuffer() {
		for (Column column : _columns) {
			processColumn(column);
		}
		_bufferEntries.clear();
	}

	private void processColumn(Column column) {
		String[] queryValues = new String[_bufferEntries.size()];

		for (int i = 0; i < _bufferEntries.size(); i++) {
			Entry<Row, Long> entry = _bufferEntries.get(i);
			Row row = entry.getKey();
			Object value = row.getValue(column);
			String queryValue = null;
			if (value != null) {
				queryValue = value.toString();
			}
			queryValues[i] = queryValue;
		}

		// First index = dictionary index, second index = entry/row index
		boolean[][] foundInDictionary = new boolean[_dictionaryList.size()][];
		for (int i = 0; i < _dictionaryList.size(); i++) {
			IDictionary dictionary = _dictionaryList.get(i);
			foundInDictionary[i] = dictionary.isValid(queryValues);
		}

		// Find "no matches" and "multiple matches"
		for (int j = 0; j < _bufferEntries.size(); j++) {
			int numMatches = 0;
			for (int i = 0; i < _dictionaryList.size(); i++) {
				if (foundInDictionary[i][j]) {
					numMatches++;
				}
			}
			if (numMatches == 0 || numMatches > 1) {
				Object value = _bufferEntries.get(j).getKey().getValue(column);
				String queryValue = null;
				if (value != null) {
					queryValue = value.toString();
				}
				if (numMatches == 0) {
					List<String> noMatchesList = _noMatches.get(column);
					if (noMatchesList == null) {
						noMatchesList = new ArrayList<String>();
						_noMatches.put(column, noMatchesList);
					}
					noMatchesList.add(queryValue);
				}
				if (numMatches > 1) {
					List<String> multipleMatchesList = _multipleMatches
							.get(column);
					if (multipleMatchesList == null) {
						multipleMatchesList = new ArrayList<String>();
						_multipleMatches.put(column, multipleMatchesList);
					}
					multipleMatchesList.add(queryValue);
				}
			}
		}

		for (int i = 0; i < _dictionaryList.size(); i++) {
			int resultIndex = 0;

			for (int j = 0; j < _bufferEntries.size(); j++) {
				Entry<Row, Long> entry = _bufferEntries.get(j);
				if (foundInDictionary[i][resultIndex]) {
					IDictionary dictionary = _dictionaryList.get(i);
					Map<IDictionary, Long> dictionaryMatches = _matches
							.get(column);

					if (dictionaryMatches == null) {
						dictionaryMatches = new HashMap<IDictionary, Long>();
						_matches.put(column, dictionaryMatches);
					}
					Long matchCount = dictionaryMatches.get(dictionary);
					if (matchCount == null) {
						matchCount = 0l;
					}
					Long valueCount = entry.getValue();
					matchCount += valueCount;

					dictionaryMatches.put(dictionary, matchCount);
				}
				resultIndex++;
			}
		}
	}
}
