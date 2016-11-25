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

import java.util.Date;

import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Metric;
import org.datacleaner.components.convert.ConvertToDateTransformer;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabResult;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the result of a Date and Time Analyzer.
 *
 *
 */
public class DateAndTimeAnalyzerResult extends CrosstabResult {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(DateAndTimeAnalyzerResult.class);

    public DateAndTimeAnalyzerResult(final Crosstab<?> crosstab) {
        super(crosstab);
    }

    protected static Number convertToDaysSinceEpoch(final String str) {
        if (str == null) {
            return null;
        }

        final LocalDate epoch = new LocalDate(1970, 1, 1);

        final Date date = ConvertToDateTransformer.getInternalInstance().transformValue(str);
        if (date == null) {
            logger.warn("Could not parse date string: '{}', returning null metric value.", str);
            return null;
        }

        return Days.daysBetween(epoch, new LocalDate(date)).getDays();
    }

    @Metric(order = 1, value = DateAndTimeAnalyzer.MEASURE_ROW_COUNT)
    public int getRowCount(final InputColumn<?> col) {
        final Number n = (Number) getCrosstab().where(DateAndTimeAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(DateAndTimeAnalyzer.DIMENSION_MEASURE, DateAndTimeAnalyzer.MEASURE_ROW_COUNT).get();
        return n.intValue();
    }

    @Metric(order = 2, value = DateAndTimeAnalyzer.MEASURE_NULL_COUNT)
    public int getNullCount(final InputColumn<?> col) {
        final Number n = (Number) getCrosstab().where(DateAndTimeAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(DateAndTimeAnalyzer.DIMENSION_MEASURE, DateAndTimeAnalyzer.MEASURE_NULL_COUNT).get();
        return n.intValue();
    }

    @Metric(order = 3, value = DateAndTimeAnalyzer.MEASURE_HIGHEST_DATE)
    @Description(
            "The highest date value for the given column. The value is measured in number of days since 1970-01-01.")
    public Number getHighestDate(final InputColumn<?> col) {
        final String s = (String) getCrosstab().where(DateAndTimeAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(DateAndTimeAnalyzer.DIMENSION_MEASURE, DateAndTimeAnalyzer.MEASURE_HIGHEST_DATE).safeGet(null);
        return convertToDaysSinceEpoch(s);
    }

    @Metric(order = 3, value = DateAndTimeAnalyzer.MEASURE_LOWEST_DATE)
    @Description(
            "The lowest date value for the given column. The value is measured in number of days since 1970-01-01.")
    public Number getLowestDate(final InputColumn<?> col) {
        final String s = (String) getCrosstab().where(DateAndTimeAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(DateAndTimeAnalyzer.DIMENSION_MEASURE, DateAndTimeAnalyzer.MEASURE_LOWEST_DATE).safeGet(null);
        return convertToDaysSinceEpoch(s);
    }

    @Metric(order = 4, value = DateAndTimeAnalyzer.MEASURE_MEAN)
    @Description("The mean value for the given column. The value is measured in number of days since 1970-01-01.")
    public Number getMean(final InputColumn<?> col) {
        final String s = (String) getCrosstab().where(DateAndTimeAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(DateAndTimeAnalyzer.DIMENSION_MEASURE, DateAndTimeAnalyzer.MEASURE_MEAN).safeGet(null);
        return convertToDaysSinceEpoch(s);
    }

    @Metric(order = 5, value = DateAndTimeAnalyzer.MEASURE_MEDIAN)
    @Description("The median value for the given column. The value is measured in number of days since 1970-01-01.")
    public Number getMedian(final InputColumn<?> col) {
        final String s = (String) getCrosstab().where(DateAndTimeAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(DateAndTimeAnalyzer.DIMENSION_MEASURE, DateAndTimeAnalyzer.MEASURE_MEDIAN).safeGet(null);
        return convertToDaysSinceEpoch(s);
    }

    @Metric(order = 6, value = DateAndTimeAnalyzer.MEASURE_PERCENTILE25)
    @Description(
            "The 25th percentile value for the given column. The value is measured in number of days since 1970-01-01.")
    public Number getPercentile25(final InputColumn<?> col) {
        final String s = (String) getCrosstab().where(DateAndTimeAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(DateAndTimeAnalyzer.DIMENSION_MEASURE, DateAndTimeAnalyzer.MEASURE_PERCENTILE25).safeGet(null);
        return convertToDaysSinceEpoch(s);
    }

    @Metric(order = 7, value = DateAndTimeAnalyzer.MEASURE_PERCENTILE75)
    @Description(
            "The 75th percentile value for the given column. The value is measured in number of days since 1970-01-01.")
    public Number getPercentile75(final InputColumn<?> col) {
        final String s = (String) getCrosstab().where(DateAndTimeAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(DateAndTimeAnalyzer.DIMENSION_MEASURE, DateAndTimeAnalyzer.MEASURE_PERCENTILE75).safeGet(null);
        return convertToDaysSinceEpoch(s);
    }

    @Metric(order = 8, value = DateAndTimeAnalyzer.MEASURE_KURTOSIS)
    public Number getKurtosis(final InputColumn<?> col) {
        return (Number) getCrosstab().where(DateAndTimeAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(DateAndTimeAnalyzer.DIMENSION_MEASURE, DateAndTimeAnalyzer.MEASURE_KURTOSIS).safeGet(null);
    }

    @Metric(order = 9, value = DateAndTimeAnalyzer.MEASURE_SKEWNESS)
    public Number getSkewness(final InputColumn<?> col) {
        return (Number) getCrosstab().where(DateAndTimeAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(DateAndTimeAnalyzer.DIMENSION_MEASURE, DateAndTimeAnalyzer.MEASURE_SKEWNESS).safeGet(null);
    }
}
