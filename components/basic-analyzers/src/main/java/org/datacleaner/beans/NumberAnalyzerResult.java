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

@Distributed(reducer = NumberAnalyzerResultReducer.class)
public class NumberAnalyzerResult extends CrosstabResult {

    private static final long serialVersionUID = 1L;

    private final InputColumn<? extends Number>[] _columns;

    public NumberAnalyzerResult(InputColumn<? extends Number>[] columns, Crosstab<?> crosstab) {
        super(crosstab);
        _columns = columns;
    }

    public InputColumn<? extends Number>[] getColumns() {
        return _columns;
    }

    @Metric(order = 1, value = NumberAnalyzer.MEASURE_ROW_COUNT)
    public Number getRowCount(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_ROW_COUNT).get();
    }

    @Metric(order = 2, value = NumberAnalyzer.MEASURE_NULL_COUNT)
    public Number getNullCount(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_NULL_COUNT).get();
    }

    @Metric(order = 3, value = NumberAnalyzer.MEASURE_HIGHEST_VALUE)
    public Number getHighestValue(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_HIGHEST_VALUE).safeGet(null);
    }

    @Metric(order = 4, value = NumberAnalyzer.MEASURE_LOWEST_VALUE)
    public Number getLowestValue(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_LOWEST_VALUE).safeGet(null);
    }

    @Metric(order = 5, value = NumberAnalyzer.MEASURE_SUM)
    public Number getSum(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_SUM).safeGet(null);
    }

    @Metric(order = 6, value = NumberAnalyzer.MEASURE_MEAN)
    public Number getMean(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_MEAN).safeGet(null);
    }

    @Metric(order = 7, value = NumberAnalyzer.MEASURE_GEOMETRIC_MEAN)
    public Number getGeometricMean(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_GEOMETRIC_MEAN).safeGet(null);
    }

    @Metric(order = 8, value = NumberAnalyzer.MEASURE_STANDARD_DEVIATION)
    public Number getStandardDeviation(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_STANDARD_DEVIATION).safeGet(null);
    }

    @Metric(order = 9, value = NumberAnalyzer.MEASURE_VARIANCE)
    public Number getVariance(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_VARIANCE).safeGet(null);
    }

    @Metric(order = 10, value = NumberAnalyzer.MEASURE_SECOND_MOMENT)
    public Number getSecondMoment(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_SECOND_MOMENT).safeGet(null);
    }

    @Metric(order = 11, value = NumberAnalyzer.MEASURE_SUM_OF_SQUARES)
    public Number getSumOfSquares(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_SUM_OF_SQUARES).safeGet(null);
    }

    @Metric(order = 20, value = NumberAnalyzer.MEASURE_MEDIAN)
    public Number getMedian(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_MEDIAN).safeGet(null);
    }

    @Metric(order = 21, value = NumberAnalyzer.MEASURE_PERCENTILE25)
    public Number getPercentile25(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_PERCENTILE25).safeGet(null);
    }

    @Metric(order = 22, value = NumberAnalyzer.MEASURE_PERCENTILE75)
    public Number getPercentile75(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_PERCENTILE75).safeGet(null);
    }

    @Metric(order = 23, value = NumberAnalyzer.MEASURE_KURTOSIS)
    public Number getKurtosis(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_KURTOSIS).safeGet(null);
    }

    @Metric(order = 24, value = NumberAnalyzer.MEASURE_SKEWNESS)
    public Number getSkewness(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_SKEWNESS).safeGet(null);
    }
}
