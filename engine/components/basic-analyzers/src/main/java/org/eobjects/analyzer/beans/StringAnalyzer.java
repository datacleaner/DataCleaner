/**
 * AnalyzerBeans
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.beans;

import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.ColumnProperty;
import org.eobjects.analyzer.beans.api.Concurrent;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.eobjects.analyzer.util.AverageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An analyzer for various typical String measures.
 * 
 * 
 */
@AnalyzerBean("String analyzer")
@Description("The String analyzer is used to collect a variety of typical metrics on string values.\nMetrics include statistics on character case, words, diacritics, white-spaces and more...")
@Concurrent(true)
public class StringAnalyzer implements Analyzer<StringAnalyzerResult> {

	public static final String DIMENSION_MEASURES = "Measures";
	public static final String DIMENSION_COLUMN = "Column";
	
	public static final String MEASURE_MIN_WORDS = "Min words";
	public static final String MEASURE_MAX_WORDS = "Max words";
	public static final String MEASURE_WORD_COUNT = "Word count";
	public static final String MEASURE_NON_LETTER_CHARS = "Non-letter chars";
	public static final String MEASURE_DIACRITIC_CHARS = "Diacritic chars";
	public static final String MEASURE_DIGIT_CHARS = "Digit chars";
	public static final String MEASURE_LOWERCASE_CHARS = "Lowercase chars";
	public static final String MEASURE_UPPERCASE_CHARS_EXCL_FIRST_LETTERS = "Uppercase chars (excl. first letters)";
	public static final String MEASURE_UPPERCASE_CHARS = "Uppercase chars";
	public static final String MEASURE_AVG_WHITE_SPACES = "Avg white spaces";
	public static final String MEASURE_MIN_WHITE_SPACES = "Min white spaces";
	public static final String MEASURE_MAX_WHITE_SPACES = "Max white spaces";
	public static final String MEASURE_AVG_CHARS = "Avg chars";
	public static final String MEASURE_MIN_CHARS = "Min chars";
	public static final String MEASURE_MAX_CHARS = "Max chars";
	public static final String MEASURE_TOTAL_CHAR_COUNT = "Total char count";
	public static final String MEASURE_ENTIRELY_LOWERCASE_COUNT = "Entirely lowercase count";
	public static final String MEASURE_ENTIRELY_UPPERCASE_COUNT = "Entirely uppercase count";
	public static final String MEASURE_BLANK_COUNT = "Blank count";
	public static final String MEASURE_NULL_COUNT = "Null count";
	public static final String MEASURE_ROW_COUNT = "Row count";

	private static final Logger logger = LoggerFactory.getLogger(StringAnalyzer.class);

	private final Map<InputColumn<String>, StringAnalyzerColumnDelegate> _columnDelegates = new HashMap<InputColumn<String>, StringAnalyzerColumnDelegate>();

	@Configured
	@ColumnProperty(escalateToMultipleJobs=true)
	InputColumn<String>[] _columns;

	@Provided
	RowAnnotationFactory _annotationFactory;

	public StringAnalyzer() {
	}

	@SafeVarargs
	public StringAnalyzer(InputColumn<String>... columns) {
		_columns = columns;
		_annotationFactory = new InMemoryRowAnnotationFactory();
		init();
	}

	@Initialize
	public void init() {
		for (InputColumn<String> column : _columns) {
			_columnDelegates.put(column, new StringAnalyzerColumnDelegate(_annotationFactory));
		}
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		for (InputColumn<String> column : _columns) {
			String value = row.getValue(column);

			StringAnalyzerColumnDelegate delegate = _columnDelegates.get(column);
			delegate.run(row, value, distinctCount);
		}
	}

	@Override
	public StringAnalyzerResult getResult() {
		logger.info("getResult()");
		CrosstabDimension measureDimension = new CrosstabDimension(DIMENSION_MEASURES);
		measureDimension.addCategory(MEASURE_ROW_COUNT);
		measureDimension.addCategory(MEASURE_NULL_COUNT);
		measureDimension.addCategory(MEASURE_BLANK_COUNT);
		measureDimension.addCategory(MEASURE_ENTIRELY_UPPERCASE_COUNT);
		measureDimension.addCategory(MEASURE_ENTIRELY_LOWERCASE_COUNT);
		measureDimension.addCategory(MEASURE_TOTAL_CHAR_COUNT);
		measureDimension.addCategory(MEASURE_MAX_CHARS);
		measureDimension.addCategory(MEASURE_MIN_CHARS);
		measureDimension.addCategory(MEASURE_AVG_CHARS);
		measureDimension.addCategory(MEASURE_MAX_WHITE_SPACES);
		measureDimension.addCategory(MEASURE_MIN_WHITE_SPACES);
		measureDimension.addCategory(MEASURE_AVG_WHITE_SPACES);
		measureDimension.addCategory(MEASURE_UPPERCASE_CHARS);
		measureDimension.addCategory(MEASURE_UPPERCASE_CHARS_EXCL_FIRST_LETTERS);
		measureDimension.addCategory(MEASURE_LOWERCASE_CHARS);
		measureDimension.addCategory(MEASURE_DIGIT_CHARS);
		measureDimension.addCategory(MEASURE_DIACRITIC_CHARS);
		measureDimension.addCategory(MEASURE_NON_LETTER_CHARS);
		measureDimension.addCategory(MEASURE_WORD_COUNT);
		measureDimension.addCategory(MEASURE_MAX_WORDS);
		measureDimension.addCategory(MEASURE_MIN_WORDS);

		CrosstabDimension columnDimension = new CrosstabDimension(DIMENSION_COLUMN);

		Crosstab<Number> crosstab = new Crosstab<Number>(Number.class, columnDimension, measureDimension);

		for (InputColumn<String> column : _columns) {
			String columnName = column.getName();

			StringAnalyzerColumnDelegate delegate = _columnDelegates.get(column);

			columnDimension.addCategory(columnName);

			final Integer numRows = delegate.getNumRows();
			final Integer numNull = delegate.getNumNull();
			final Integer numBlank = delegate.getNumBlank();
			final Integer numEntirelyUppercase = delegate.getNumEntirelyUppercase();
			final Integer numEntirelyLowercase = delegate.getNumEntirelyLowercase();
			final Integer numChars = delegate.getNumChars();
			final Integer maxChars = delegate.getMaxChars();
			final Integer minChars = delegate.getMinChars();
			final Integer numWords = delegate.getNumWords();
			final Integer maxWords = delegate.getMaxWords();
			final Integer minWords = delegate.getMinWords();
			final Integer maxWhitespace = delegate.getMaxWhitespace();
			final Integer minWhitespace = delegate.getMinWhitespace();
			final Integer numUppercase = delegate.getNumUppercase();
			final Integer numUppercaseExclFirstLetter = delegate.getNumUppercaseExclFirstLetter();
			final Integer numLowercase = delegate.getNumLowercase();
			final Integer numDigits = delegate.getNumDigit();
			final Integer numDiacritics = delegate.getNumDiacritics();
			final Integer numNonLetter = delegate.getNumNonLetter();
			final AverageBuilder charAverageBuilder = delegate.getCharAverageBuilder();
			final AverageBuilder blanksAverageBuilder = delegate.getWhitespaceAverageBuilder();

			Double avgChars = null;
			if (charAverageBuilder.getNumValues() > 0) {
				avgChars = charAverageBuilder.getAverage();
			}
			Double avgBlanks = null;
			if (blanksAverageBuilder.getNumValues() > 0) {
				avgBlanks = blanksAverageBuilder.getAverage();
			}

			// begin entering numbers into the crosstab
			CrosstabNavigator<Number> nav = crosstab.where(columnDimension, columnName);

			nav.where(measureDimension, MEASURE_ROW_COUNT).put(numRows);

			nav.where(measureDimension, MEASURE_NULL_COUNT).put(numNull);
			if (numNull > 0) {
				addAttachment(nav, delegate.getNullAnnotation(), column);
			}
			
			nav.where(measureDimension, MEASURE_BLANK_COUNT).put(numBlank);
            if (numBlank > 0) {
                addAttachment(nav, delegate.getBlankAnnotation(), column);
            }

			nav.where(measureDimension, MEASURE_ENTIRELY_UPPERCASE_COUNT).put(numEntirelyUppercase);
			if (numEntirelyUppercase > 0) {
				addAttachment(nav, delegate.getEntirelyUppercaseAnnotation(), column);
			}

			nav.where(measureDimension, MEASURE_ENTIRELY_LOWERCASE_COUNT).put(numEntirelyLowercase);
			if (numEntirelyLowercase > 0) {
				addAttachment(nav, delegate.getEntirelyLowercaseAnnotation(), column);
			}

			nav.where(measureDimension, MEASURE_TOTAL_CHAR_COUNT).put(numChars);

			nav.where(measureDimension, MEASURE_MAX_CHARS).put(maxChars);
			if (maxChars != null) {
				addAttachment(nav, delegate.getMaxCharsAnnotation(), column);
			}

			nav.where(measureDimension, MEASURE_MIN_CHARS).put(minChars);
			if (minChars != null) {
				addAttachment(nav, delegate.getMinCharsAnnotation(), column);
			}

			nav.where(measureDimension, MEASURE_AVG_CHARS).put(avgChars);
			nav.where(measureDimension, MEASURE_MAX_WHITE_SPACES).put(maxWhitespace);
			if (maxWhitespace != null) {
				addAttachment(nav, delegate.getMaxWhitespaceAnnotation(), column);
			}

			nav.where(measureDimension, MEASURE_MIN_WHITE_SPACES).put(minWhitespace);
			if (minWhitespace != null) {
				addAttachment(nav, delegate.getMinWhitespaceAnnotation(), column);
			}

			nav.where(measureDimension, MEASURE_AVG_WHITE_SPACES).put(avgBlanks);
			nav.where(measureDimension, MEASURE_UPPERCASE_CHARS).put(numUppercase);
			nav.where(measureDimension, MEASURE_UPPERCASE_CHARS_EXCL_FIRST_LETTERS).put(numUppercaseExclFirstLetter);
			if (numUppercaseExclFirstLetter > 0) {
				addAttachment(nav, delegate.getUppercaseExclFirstLetterAnnotation(), column);
			}

			nav.where(measureDimension, MEASURE_LOWERCASE_CHARS).put(numLowercase);
			nav.where(measureDimension, MEASURE_DIGIT_CHARS).put(numDigits);
			if (numDigits > 0) {
				addAttachment(nav, delegate.getDigitAnnotation(), column);
			}

			nav.where(measureDimension, MEASURE_DIACRITIC_CHARS).put(numDiacritics);
			if (numDiacritics > 0) {
				addAttachment(nav, delegate.getDiacriticAnnotation(), column);
			}

			nav.where(measureDimension, MEASURE_NON_LETTER_CHARS).put(numNonLetter);
			nav.where(measureDimension, MEASURE_WORD_COUNT).put(numWords);

			nav.where(measureDimension, MEASURE_MAX_WORDS).put(maxWords);
			if (maxWords != null) {
				addAttachment(nav, delegate.getMaxWordsAnnotation(), column);
			}

			nav.where(measureDimension, MEASURE_MIN_WORDS).put(minWords);
			if (minWords != null) {
				addAttachment(nav, delegate.getMinWordsAnnotation(), column);
			}
		}

		return new StringAnalyzerResult(_columns, crosstab);
	}

	private void addAttachment(CrosstabNavigator<Number> nav, RowAnnotation annotation, InputColumn<?> column) {
		nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, column));
	}
}
