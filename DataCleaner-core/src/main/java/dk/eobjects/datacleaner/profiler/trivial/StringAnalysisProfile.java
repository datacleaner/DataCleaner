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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import dk.eobjects.datacleaner.profiler.AbstractProfile;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.MatrixBuilder;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.datacleaner.util.AverageBuilder;
import dk.eobjects.metamodel.data.IRowFilter;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.util.FormatHelper;

/**
 * Provides profiling information for string-based columns.
 */
public class StringAnalysisProfile extends AbstractProfile {
	
	private NumberFormat _numberFormat = FormatHelper.getUiNumberFormat();

	private static final short INDEX_NUM_CHARS = 0;
	private static final short INDEX_MAX_CHARS = 1;
	private static final short INDEX_MIN_CHARS = 2;
	private static final short INDEX_NUM_UPPERCASE = 3;
	private static final short INDEX_NUM_LOWERCASE = 4;
	private static final short INDEX_NUM_NONLETTER = 5;
	private static final short INDEX_NUM_WORDS = 6;
	private static final short INDEX_MAX_WORDS = 7;
	private static final short INDEX_MIN_WORDS = 8;
	private static final short INDEX_MAX_WHITE_SPACES = 9;
	private static final short INDEX_MIN_WHITE_SPACES = 10;
	private Map<Column, Long[]> _counts = new HashMap<Column, Long[]>();
	private Map<Column, AverageBuilder> _charAverages = new HashMap<Column, AverageBuilder>();

	@Override
	public void initialize(Column... columns) {
		super.initialize(columns);
		for (Column column : columns) {
			if (!column.getType().isLiteral()) {
				IllegalArgumentException e = new IllegalArgumentException("Column is not of literal type: " + column);
				_log.error(e);
				throw e;
			}
		}
	}

	@Override
	protected void processValue(Column column, Object value, long valueCount, Row row) {
		Long[] counters = _counts.get(column);
		AverageBuilder charAverageBuilder = _charAverages.get(column);
		if (counters == null) {
			counters = new Long[11];
			counters[INDEX_MIN_CHARS] = null;
			counters[INDEX_MAX_CHARS] = null;
			counters[INDEX_NUM_CHARS] = 0l;
			counters[INDEX_NUM_UPPERCASE] = 0l;
			counters[INDEX_NUM_LOWERCASE] = 0l;
			counters[INDEX_NUM_NONLETTER] = 0l;
			counters[INDEX_NUM_WORDS] = 0l;
			counters[INDEX_MIN_WORDS] = null;
			counters[INDEX_MAX_WORDS] = null;
			counters[INDEX_MIN_WHITE_SPACES] = null;
			counters[INDEX_MAX_WHITE_SPACES] = null;
			_counts.put(column, counters);
			
			charAverageBuilder = new AverageBuilder();
			_charAverages.put(column, charAverageBuilder);
		}
		if (value != null) {
			String string = value.toString();
			long length = string.length();
			long numWords = new StringTokenizer(string).countTokens();

			if (counters[INDEX_MIN_CHARS] == null) {
				// This is the first time we encounter a non-null value, so we
				// just set all counters
				counters[INDEX_MAX_CHARS] = length;
				counters[INDEX_MIN_CHARS] = length;
				counters[INDEX_MIN_WORDS] = numWords;
				counters[INDEX_MAX_WORDS] = numWords;
			}

			counters[INDEX_NUM_CHARS] = counters[INDEX_NUM_CHARS] + length;
			counters[INDEX_NUM_WORDS] = counters[INDEX_NUM_WORDS] + numWords;

			if (counters[INDEX_MAX_CHARS] < length) {
				counters[INDEX_MAX_CHARS] = length;
			}
			if (counters[INDEX_MIN_CHARS] > length) {
				counters[INDEX_MIN_CHARS] = length;
			}
			if (counters[INDEX_MAX_WORDS] < numWords) {
				counters[INDEX_MAX_WORDS] = numWords;
			}
			if (counters[INDEX_MIN_WORDS] > numWords) {
				counters[INDEX_MIN_WORDS] = numWords;
			}

			for (int i = 0; i < length; i++) {
				char c = string.charAt(i);
				if (Character.isLetter(c)) {
					if (Character.isUpperCase(c)) {
						counters[INDEX_NUM_UPPERCASE] = counters[INDEX_NUM_UPPERCASE] + 1;
					} else {
						counters[INDEX_NUM_LOWERCASE] = counters[INDEX_NUM_LOWERCASE] + 1;
					}
				} else {
					counters[INDEX_NUM_NONLETTER] = counters[INDEX_NUM_NONLETTER] + 1;
				}
			}

			charAverageBuilder.addValue(length);
		}
	}

	@Override
	protected List<IMatrix> getResultMatrices() {
		MatrixBuilder mb = new MatrixBuilder();
		mb.addRow("Char count");
		mb.addRow("Max chars");
		mb.addRow("Min chars");
		mb.addRow("Avg chars");
		mb.addRow("Uppercase chars");
		mb.addRow("Lowercase chars");
		mb.addRow("Non-letter chars");
		mb.addRow("Word count");
		mb.addRow("Max words");
		mb.addRow("Min words");

		for (int i = 0; i < _columns.length; i++) {
			final Column column = _columns[i];
			String columnName = column.getName();
			Long[] counts = _counts.get(column);
			AverageBuilder charAverageBuilder = _charAverages.get(column);

			Long numChars = counts[INDEX_NUM_CHARS];
			final Long maxChars = counts[INDEX_MAX_CHARS];
			final Long minChars = counts[INDEX_MIN_CHARS];
			String avgChars = null;
			if (charAverageBuilder.getNumValues() > 0) {
				avgChars = _numberFormat.format(charAverageBuilder.getAverage());
			}
			String numUppercase = "0%";
			String numLowercase = "0%";
			String numNonletter = "0%";
			if (numChars > 0) {
				numUppercase = (counts[INDEX_NUM_UPPERCASE] * 100 / numChars) + "%";
				numLowercase = (counts[INDEX_NUM_LOWERCASE] * 100 / numChars) + "%";
				numNonletter = (counts[INDEX_NUM_NONLETTER] * 100 / numChars) + "%";
			}
			Long numWords = counts[INDEX_NUM_WORDS];
			final Long maxWords = counts[INDEX_MAX_WORDS];
			final Long minWords = counts[INDEX_MIN_WORDS];
			MatrixValue[] matrixValues = mb.addColumn(columnName, numChars, maxChars, minChars, avgChars, numUppercase,
					numLowercase, numNonletter, numWords, maxWords, minWords);

			if (maxChars != null) {
				MatrixValue mv = matrixValues[1];
				mv.setDetailSource(getBaseQuery(column));
				mv.addDetailRowFilter(getCharFilter(column, maxChars));
			}

			if (minChars != null) {
				MatrixValue mv = matrixValues[2];
				mv.setDetailSource(getBaseQuery(column));
				mv.addDetailRowFilter(getCharFilter(column, minChars));
			}

			if (maxWords != null) {
				MatrixValue mv = matrixValues[8];
				mv.setDetailSource(getBaseQuery(column));
				mv.addDetailRowFilter(getWordFilter(column, maxWords));
			}

			if (minWords != null) {
				MatrixValue mv = matrixValues[9];
				mv.setDetailSource(getBaseQuery(column));
				mv.addDetailRowFilter(getWordFilter(column, minWords));
			}
		}
		List<IMatrix> result = new ArrayList<IMatrix>();
		if (!mb.isEmpty()) {
			result.add(mb.getMatrix());
		}
		return result;
	}

	private IRowFilter getCharFilter(final Column column, final Long numChars) {
		return new IRowFilter() {
			public boolean accept(Row row) {
				Object value = row.getValue(column);
				if (value != null && value.toString().length() == numChars) {
					return true;
				}
				return false;
			}
		};
	}

	private IRowFilter getWordFilter(final Column column, final Long numWords) {
		return new IRowFilter() {
			public boolean accept(Row row) {
				Object value = row.getValue(column);
				if (value != null && new StringTokenizer(value.toString()).countTokens() == numWords) {
					return true;
				}
				return false;
			}
		};
	}
}