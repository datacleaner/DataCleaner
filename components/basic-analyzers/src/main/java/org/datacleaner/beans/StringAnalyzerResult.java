/**
 * DataCleaner (community edition)
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
package org.datacleaner.beans;

import org.datacleaner.api.Distributed;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Metric;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabResult;

/**
 * Result type of the StringAnalyzer
 *
 *
 */
@Distributed(reducer = StringAnalyzerResultReducer.class)
public class StringAnalyzerResult extends CrosstabResult {

    private static final long serialVersionUID = 1L;

    private final InputColumn<String>[] _columns;

    public StringAnalyzerResult(final InputColumn<String>[] columns, final Crosstab<?> crosstab) {
        super(crosstab);
        _columns = columns;
    }

    public InputColumn<String>[] getColumns() {
        return _columns;
    }

    @Metric(StringAnalyzer.MEASURE_AVG_CHARS)
    public double getAvgChars(final InputColumn<?> col) {
        return (Double) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_AVG_CHARS).get();
    }

    @Metric(StringAnalyzer.MEASURE_AVG_WHITE_SPACES)
    public double getAvgWhitespaces(final InputColumn<?> col) {
        return (Double) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_AVG_WHITE_SPACES).get();
    }

    @Metric(StringAnalyzer.MEASURE_DIACRITIC_CHARS)
    public int getDiacritiChars(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_DIACRITIC_CHARS).get();
    }

    @Metric(StringAnalyzer.MEASURE_DIGIT_CHARS)
    public int getDigitChars(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_DIGIT_CHARS).get();
    }

    @Metric(StringAnalyzer.MEASURE_ENTIRELY_LOWERCASE_COUNT)
    public int getEntirelyLowerCaseCount(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_ENTIRELY_LOWERCASE_COUNT).get();
    }

    @Metric(StringAnalyzer.MEASURE_ENTIRELY_UPPERCASE_COUNT)
    public int getEntirelyUpperCaseCount(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_ENTIRELY_UPPERCASE_COUNT).get();
    }

    @Metric(StringAnalyzer.MEASURE_LOWERCASE_CHARS)
    public int getLowerCaseChars(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_LOWERCASE_CHARS).get();
    }

    @Metric(StringAnalyzer.MEASURE_MAX_CHARS)
    public int getMaxChars(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_MAX_CHARS).get();
    }

    @Metric(StringAnalyzer.MEASURE_MAX_WHITE_SPACES)
    public int getMaxWhitespaces(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_MAX_WHITE_SPACES).get();
    }

    @Metric(StringAnalyzer.MEASURE_MAX_WORDS)
    public int getMaxWords(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_MAX_WORDS).get();
    }

    @Metric(StringAnalyzer.MEASURE_MIN_CHARS)
    public int getMinChars(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_MIN_CHARS).get();
    }

    @Metric(StringAnalyzer.MEASURE_MIN_WHITE_SPACES)
    public int getMinWhitespaces(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_MIN_WHITE_SPACES).get();
    }

    @Metric(StringAnalyzer.MEASURE_MIN_WORDS)
    public int getMinWords(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_MIN_WORDS).get();
    }

    @Metric(StringAnalyzer.MEASURE_NON_LETTER_CHARS)
    public int getNonLetterChars(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_NON_LETTER_CHARS).get();
    }

    @Metric(StringAnalyzer.MEASURE_NULL_COUNT)
    public int getNullCount(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_NULL_COUNT).get();
    }

    @Metric(StringAnalyzer.MEASURE_BLANK_COUNT)
    public Integer getBlankCount(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_BLANK_COUNT).safeGet(null);
    }

    @Metric(StringAnalyzer.MEASURE_ROW_COUNT)
    public int getRowCount(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_ROW_COUNT).get();
    }

    @Metric(StringAnalyzer.MEASURE_TOTAL_CHAR_COUNT)
    public int getTotalCharCount(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_TOTAL_CHAR_COUNT).get();
    }

    @Metric(StringAnalyzer.MEASURE_UPPERCASE_CHARS)
    public int getUpperCaseChars(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_UPPERCASE_CHARS).get();
    }

    @Metric(StringAnalyzer.MEASURE_UPPERCASE_CHARS_EXCL_FIRST_LETTERS)
    public int getUpperCaseCharsExcludingFirstLetters(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_UPPERCASE_CHARS_EXCL_FIRST_LETTERS)
                .get();
    }

    @Metric(StringAnalyzer.MEASURE_WORD_COUNT)
    public int getWordCount(final InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_WORD_COUNT).get();
    }

}
