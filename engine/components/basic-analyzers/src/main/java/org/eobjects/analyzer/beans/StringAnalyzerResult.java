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
package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.beans.api.Distributed;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.Metric;

/**
 * Result type of the StringAnalyzer
 * 
 * 
 */
@Distributed(reducer = StringAnalyzerResultReducer.class)
public class StringAnalyzerResult extends CrosstabResult {

    private static final long serialVersionUID = 1L;

    private final InputColumn<String>[] _columns;

    public StringAnalyzerResult(InputColumn<String>[] columns, Crosstab<?> crosstab) {
        super(crosstab);
        _columns = columns;
    }

    public InputColumn<String>[] getColumns() {
        return _columns;
    }

    @Metric(StringAnalyzer.MEASURE_AVG_CHARS)
    public double getAvgChars(InputColumn<?> col) {
        return (Double) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_AVG_CHARS).get();
    }

    @Metric(StringAnalyzer.MEASURE_AVG_WHITE_SPACES)
    public double getAvgWhitespaces(InputColumn<?> col) {
        return (Double) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_AVG_WHITE_SPACES).get();
    }

    @Metric(StringAnalyzer.MEASURE_DIACRITIC_CHARS)
    public int getDiacritiChars(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_DIACRITIC_CHARS).get();
    }

    @Metric(StringAnalyzer.MEASURE_DIGIT_CHARS)
    public int getDigitChars(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_DIGIT_CHARS).get();
    }

    @Metric(StringAnalyzer.MEASURE_ENTIRELY_LOWERCASE_COUNT)
    public int getEntirelyLowerCaseCount(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_ENTIRELY_LOWERCASE_COUNT).get();
    }

    @Metric(StringAnalyzer.MEASURE_ENTIRELY_UPPERCASE_COUNT)
    public int getEntirelyUpperCaseCount(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_ENTIRELY_UPPERCASE_COUNT).get();
    }

    @Metric(StringAnalyzer.MEASURE_LOWERCASE_CHARS)
    public int getLowerCaseChars(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_LOWERCASE_CHARS).get();
    }

    @Metric(StringAnalyzer.MEASURE_MAX_CHARS)
    public int getMaxChars(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_MAX_CHARS).get();
    }

    @Metric(StringAnalyzer.MEASURE_MAX_WHITE_SPACES)
    public int getMaxWhitespaces(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_MAX_WHITE_SPACES).get();
    }

    @Metric(StringAnalyzer.MEASURE_MAX_WORDS)
    public int getMaxWords(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_MAX_WORDS).get();
    }

    @Metric(StringAnalyzer.MEASURE_MIN_CHARS)
    public int getMinChars(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_MIN_CHARS).get();
    }

    @Metric(StringAnalyzer.MEASURE_MIN_WHITE_SPACES)
    public int getMinWhitespaces(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_MIN_WHITE_SPACES).get();
    }

    @Metric(StringAnalyzer.MEASURE_MIN_WORDS)
    public int getMinWords(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_MIN_WORDS).get();
    }

    @Metric(StringAnalyzer.MEASURE_NON_LETTER_CHARS)
    public int getNonLetterChars(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_NON_LETTER_CHARS).get();
    }

    @Metric(StringAnalyzer.MEASURE_NULL_COUNT)
    public int getNullCount(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_NULL_COUNT).get();
    }

    @Metric(StringAnalyzer.MEASURE_BLANK_COUNT)
    public Integer getBlankCount(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_BLANK_COUNT).safeGet(null);
    }

    @Metric(StringAnalyzer.MEASURE_ROW_COUNT)
    public int getRowCount(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_ROW_COUNT).get();
    }

    @Metric(StringAnalyzer.MEASURE_TOTAL_CHAR_COUNT)
    public int getTotalCharCount(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_TOTAL_CHAR_COUNT).get();
    }

    @Metric(StringAnalyzer.MEASURE_UPPERCASE_CHARS)
    public int getUpperCaseChars(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_UPPERCASE_CHARS).get();
    }

    @Metric(StringAnalyzer.MEASURE_UPPERCASE_CHARS_EXCL_FIRST_LETTERS)
    public int getUpperCaseCharsExcludingFirstLetters(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_UPPERCASE_CHARS_EXCL_FIRST_LETTERS)
                .get();
    }

    @Metric(StringAnalyzer.MEASURE_WORD_COUNT)
    public int getWordCount(InputColumn<?> col) {
        return (Integer) getCrosstab().where(StringAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(StringAnalyzer.DIMENSION_MEASURES, StringAnalyzer.MEASURE_WORD_COUNT).get();
    }

}
