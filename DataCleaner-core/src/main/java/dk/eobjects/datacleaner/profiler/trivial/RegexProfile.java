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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import dk.eobjects.datacleaner.profiler.AbstractProfile;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.MatrixBuilder;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.datacleaner.util.ReflectionHelper;
import dk.eobjects.metamodel.data.IRowFilter;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Column;

public class RegexProfile extends AbstractProfile {

	public static final String PREFIX_PROPERTY_REGEX = "regex_";
	public static final String PREFIX_PROPERTY_LABEL = "label_";
	private static final String NO_MATCHES = "No Matches";
	private static final String MULTIPLE_MATCHES = "Multiple Matches";
	private Map<Column, Map<String, Long>> _results = new HashMap<Column, Map<String, Long>>();
	private List<Pattern> _patternList;
	private List<String> _labelList;

	@Override
	public void initialize(Column... columns) {
		super.initialize(columns);
		List<String> expressionList = ReflectionHelper.getIteratedProperties(
				PREFIX_PROPERTY_REGEX, _properties);
		if (expressionList.isEmpty()) {
			throw new IllegalArgumentException("No expressions specified");
		}
		_patternList = new ArrayList<Pattern>(expressionList.size());
		for (String expression : expressionList) {
			_patternList.add(Pattern.compile(expression));
		}
		_labelList = ReflectionHelper.getIteratedProperties(
				PREFIX_PROPERTY_LABEL, _properties);
		if (_labelList.isEmpty()) {
			throw new IllegalArgumentException("No labels specified");
		}
		if (_labelList.size() != _patternList.size()) {
			throw new IllegalArgumentException(
					"Patterns and label lists are unequal in size: patterns="
							+ _patternList.size() + ", labels="
							+ _labelList.size());
		}
		for (Column column : columns) {
			_results.put(column, new HashMap<String, Long>());
		}
	}

	@Override
	protected void processValue(Column column, Object value, long valueCount,
			Row row) {
		Map<String, Long> resultsForColumn = _results.get(column);
		if (value == null) {
			Long count = resultsForColumn.get(NO_MATCHES);
			if (count == null) {
				count = 0l;
			}
			count += valueCount;
			resultsForColumn.put(NO_MATCHES, count);
		} else {
			int numPatternMatches = 0;
			for (int i = 0; i < _labelList.size(); i++) {
				Pattern pattern = _patternList.get(i);
				boolean matches = pattern.matcher(value.toString()).matches();
				if (matches) {
					String label = _labelList.get(i);
					Long count = resultsForColumn.get(label);
					if (count == null) {
						count = 0l;
					}
					count += valueCount;
					resultsForColumn.put(label, count);
					numPatternMatches++;
				}
			}
			if (numPatternMatches == 0) {
				Long count = resultsForColumn.get(NO_MATCHES);
				if (count == null) {
					count = 0l;
				}
				count += valueCount;
				resultsForColumn.put(NO_MATCHES, count);
			} else if (numPatternMatches > 1) {
				Long count = resultsForColumn.get(MULTIPLE_MATCHES);
				if (count == null) {
					count = 0l;
				}
				count += valueCount;
				resultsForColumn.put(MULTIPLE_MATCHES, count);
			}
		}
	}

	@Override
	protected List<IMatrix> getResultMatrices() {
		MatrixBuilder mb = new MatrixBuilder();
		String[] labelsForResult = new String[_labelList.size() + 2];
		for (int i = 0; i < _labelList.size(); i++) {
			labelsForResult[i] = _labelList.get(i);
		}
		labelsForResult[labelsForResult.length - 2] = NO_MATCHES;
		labelsForResult[labelsForResult.length - 1] = MULTIPLE_MATCHES;

		for (int i = 0; i < labelsForResult.length; i++) {
			mb.addRow(labelsForResult[i]);
		}

		for (int i = 0; i < _columns.length; i++) {
			final Column column = _columns[i];
			Map<String, Long> resultsForColumn = _results.get(column);
			Object[] values = new Object[labelsForResult.length];
			for (int j = 0; j < labelsForResult.length; j++) {
				Long count = resultsForColumn.get(labelsForResult[j]);
				if (count == null) {
					count = 0l;
				}
				values[j] = count;
			}
			MatrixValue[] matrixValues = mb.addColumn(column.getName(), values);

			if (isDetailsEnabled()) {
				Query query = getBaseQuery();
				for (int j = 0; j < matrixValues.length; j++) {
					Long count = (Long) values[j];
					if (count > 0l) {
						MatrixValue matrixValue = matrixValues[j];
						matrixValue.setDetailSource(query);

						if (j < matrixValues.length - 2) {
							// Set rowfilter for single pattern matches
							final Pattern pattern = _patternList.get(j);
							matrixValue.addDetailRowFilter(new IRowFilter() {
								public boolean accept(Row row) {
									Object value = row.getValue(column);
									if (value != null) {
										if (pattern.matcher(value.toString())
												.matches()) {
											return true;
										}
									}
									return false;
								}
							});
						} else if (j == matrixValues.length - 2) {
							// Set rowfilter for "no matches"
							matrixValue.addDetailRowFilter(new IRowFilter() {
								public boolean accept(Row row) {
									Object value = row.getValue(column);
									if (value == null) {
										return true;
									}

									boolean matched = false;
									String valueString = value.toString();
									for (Iterator<Pattern> it = _patternList
											.iterator(); it.hasNext()
											&& !matched;) {
										Pattern pattern = it.next();
										matched = pattern.matcher(valueString)
												.matches();
									}
									return !matched;
								}
							});
						} else if (j == matrixValues.length - 1) {
							// Set rowfilter for "multiple matches"
							matrixValue.addDetailRowFilter(new IRowFilter() {
								public boolean accept(Row row) {
									int numMatches = 0;
									Object value = row.getValue(column);
									if (value != null) {
										String valueString = value.toString();
										for (Iterator<Pattern> it = _patternList
												.iterator(); it.hasNext()
												&& numMatches < 2;) {
											Pattern pattern = it.next();
											if (pattern.matcher(valueString)
													.matches()) {
												numMatches++;
											}
										}
									}
									return (numMatches > 1);
								}
							});
						}
					}
				}
			}
		}

		ArrayList<IMatrix> result = new ArrayList<IMatrix>(1);
		result.add(mb.getMatrix());
		return result;
	}
}