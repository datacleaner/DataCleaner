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

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import dk.eobjects.datacleaner.profiler.AbstractProfile;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.MatrixBuilder;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.datacleaner.util.ReflectionHelper;
import dk.eobjects.metamodel.data.IRowFilter;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Column;

/**
 * Date mask matcher profile. This profile is configured with an arbitrary
 * amount of date masks and categorizes/matches incoming string values according
 * to them.
 * 
 * To configure the profile use iterated property names to specify input date
 * masks. For example:
 * 
 * <ul>
 * <li>date_mask_0=yyyy-MM-dd</li>
 * <li>date_mask_1=dd/MM/yyyy</li>
 * <li>date_mask_2=yyMMddHHmmssZ</li>
 * </ul>
 * 
 * The format of the mask is based on the Joda Time API.
 * 
 * @see http://joda-time.sourceforge.net/api-release/org/joda/time/format/DateTimeFormat.html
 */
public class DateMaskProfile extends AbstractProfile {

	public static final String PREFIX_PROPERTY_REGEX = "date_mask_";
	private static final String NO_MATCHES = "No Matches";
	private static final String MULTIPLE_MATCHES = "Multiple Matches";
	private Map<Column, Map<String, Long>> _dateMaskCounts;
	private List<DateTimeFormatter> _formatters;
	private List<String> _dateMasks;

	@Override
	public void initialize(Column... columns) {
		super.initialize(columns);

		// Set up date masks
		_dateMasks = ReflectionHelper.getIteratedProperties(
				PREFIX_PROPERTY_REGEX, _properties);
		if (_dateMasks.isEmpty()) {
			throw new IllegalArgumentException("No date masks specified");
		}

		// Set up formatters
		_formatters = new ArrayList<DateTimeFormatter>(_dateMasks.size());
		for (int i = 0; i < _dateMasks.size(); i++) {
			String dateMask = _dateMasks.get(i);
			_formatters.add(DateTimeFormat.forPattern(dateMask));
		}

		// Set up counter map
		_dateMaskCounts = new HashMap<Column, Map<String, Long>>();
		for (Column column : columns) {
			HashMap<String, Long> matchesForColumn = new HashMap<String, Long>();
			for (int i = 0; i < _dateMasks.size(); i++) {
				String dateMask = _dateMasks.get(i);
				matchesForColumn.put(dateMask, 0l);
			}
			matchesForColumn.put(NO_MATCHES, 0l);
			matchesForColumn.put(MULTIPLE_MATCHES, 0l);
			_dateMaskCounts.put(column, matchesForColumn);
		}
	}

	@Override
	protected void processValue(Column column, Object value, long valueCount,
			Row row) {
		Map<String, Long> matchesForColumn = _dateMaskCounts.get(column);
		if (value == null) {
			Long count = matchesForColumn.get(NO_MATCHES);
			count += valueCount;
			matchesForColumn.put(NO_MATCHES, count);
		} else {
			int numMatches = 0;
			String valueString = value.toString();
			for (int i = 0; i < _formatters.size(); i++) {
				DateTimeFormatter formatter = _formatters.get(i);
				try {
					// If no exception was thrown when parsing, then we have a
					// match
					formatter.parseDateTime(valueString);
					Long count = matchesForColumn.get(_dateMasks.get(i));
					count += valueCount;
					matchesForColumn.put(_dateMasks.get(i), count);

					numMatches++;
				} catch (Exception e) {
					if (_log.isDebugEnabled()) {
						_log.debug("'" + valueString
								+ "' did not match date mask '"
								+ _dateMasks.get(i) + "'", e);
					}
				}
			}
			if (numMatches > 1) {
				Long count = matchesForColumn.get(MULTIPLE_MATCHES);
				count += valueCount;
				matchesForColumn.put(MULTIPLE_MATCHES, count);
			} else if (numMatches == 0) {
				Long count = matchesForColumn.get(NO_MATCHES);
				count += valueCount;
				matchesForColumn.put(NO_MATCHES, count);
			}
		}
	}

	@Override
	protected List<IMatrix> getResultMatrices() {
		MatrixBuilder mb = new MatrixBuilder();
		for (String dateMask : _dateMasks) {
			mb.addRow(dateMask);
		}
		mb.addRow(NO_MATCHES);
		mb.addRow(MULTIPLE_MATCHES);

		for (final Column column : _columns) {
			Object[] columnValues = new Object[_dateMasks.size() + 2];
			for (int i = 0; i < _dateMasks.size(); i++) {
				String dateMask = _dateMasks.get(i);
				columnValues[i] = _dateMaskCounts.get(column).get(dateMask);
			}
			columnValues[columnValues.length - 2] = _dateMaskCounts.get(column)
					.get(NO_MATCHES);
			columnValues[columnValues.length - 1] = _dateMaskCounts.get(column)
					.get(MULTIPLE_MATCHES);

			MatrixValue[] matrixValues = mb.addColumn(column.getName(),
					columnValues);
			if (isDetailsEnabled()) {
				Query query = getBaseQuery();

				for (int i = 0; i < matrixValues.length; i++) {
					if ((Long) columnValues[i] > 0l) {
						matrixValues[i].setDetailSource(query);
						IRowFilter filter = null;
						if (i < _formatters.size()) {
							// Handle date mask matching
							final DateTimeFormatter formatter = _formatters
									.get(i);
							filter = new IRowFilter() {
								public boolean accept(Row row) {
									Object value = row.getValue(column);
									if (value == null) {
										return false;
									}
									try {
										formatter.parseDateTime(value
												.toString());
										return true;
									} catch (Exception e) {
										return false;
									}
								}
							};
						} else if (i == matrixValues.length - 2) {
							// Handle "no matches"
							filter = new IRowFilter() {
								public boolean accept(Row row) {
									Object value = row.getValue(column);
									if (value == null) {
										return true;
									}
									for (DateTimeFormatter formatter : _formatters) {
										try {
											formatter.parseDateTime(value
													.toString());
											return false;
										} catch (Exception e) {
											// Do nothing, continue
										}
									}
									return true;
								}
							};
						} else if (i == matrixValues.length - 1) {
							// Handle "multiple matches"
							filter = new IRowFilter() {
								public boolean accept(Row row) {
									Object value = row.getValue(column);
									if (value == null) {
										return false;
									}
									int numMatches = 0;
									for (Iterator<DateTimeFormatter> it = _formatters
											.iterator(); it.hasNext()
											&& numMatches < 2;) {
										DateTimeFormatter formatter = it.next();
										try {
											formatter.parseDateTime(value
													.toString());
											numMatches++;
										} catch (Exception e) {
											// Do nothing, continue
										}
									}
									return (numMatches > 1);
								}
							};
						}
						matrixValues[i].addDetailRowFilter(filter);
					}
				}
			}
		}

		ArrayList<IMatrix> result = new ArrayList<IMatrix>(1);
		result.add(mb.getMatrix());
		return result;
	}
}