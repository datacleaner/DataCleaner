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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.AggregateSummaryStatistics;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.result.AbstractCrosstabResultReducer;
import org.datacleaner.result.Crosstab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Result reducer for {@link NumberAnalyzerResult}s.
 *
 * Note: Some of the result metrics of {@link NumberAnalyzerResult} are NOT
 * reduceable. Since the inclusion of these metrics are anyways optional (based
 * on a configuration property), we take the optimistic approach and reduce what
 * we can.
 *
 * Warnings will be raised if non-reduceable metrics are encountered.
 */
public class NumberAnalyzerResultReducer extends AbstractCrosstabResultReducer<NumberAnalyzerResult> {

    private static final Logger logger = LoggerFactory.getLogger(NumberAnalyzerResultReducer.class);

    private static final Set<String> SUM_MEASURES = new HashSet<>(
            Arrays.asList(NumberAnalyzer.MEASURE_SUM, NumberAnalyzer.MEASURE_ROW_COUNT,
                    NumberAnalyzer.MEASURE_NULL_COUNT));

    @Override
    protected Serializable reduceValues(final List<Object> slaveValues, final String column, final String measure,
            final Collection<? extends NumberAnalyzerResult> results, final Class<?> valueClass) {

        if (SUM_MEASURES.contains(measure)) {
            return sum(slaveValues);
        } else if (NumberAnalyzer.MEASURE_HIGHEST_VALUE.equals(measure)) {
            return maximum(slaveValues);
        } else if (NumberAnalyzer.MEASURE_LOWEST_VALUE.equals(measure)) {
            return minimum(slaveValues);
        } else if (NumberAnalyzer.MEASURE_MEAN.equals(measure)) {
            final StatisticalSummary summary = getSummary(column, results);
            return summary.getMean();
        } else if (NumberAnalyzer.MEASURE_STANDARD_DEVIATION.equals(measure)) {
            final StatisticalSummary summary = getSummary(column, results);
            return summary.getStandardDeviation();
        } else if (NumberAnalyzer.MEASURE_VARIANCE.equals(measure)) {
            final StatisticalSummary summary = getSummary(column, results);
            return summary.getVariance();
        }

        logger.warn("Encountered non-reduceable measure '{}'. Slave values are: {}", measure, slaveValues);
        return null;
    }

    private StatisticalSummary getSummary(final String column,
            final Collection<? extends NumberAnalyzerResult> results) {
        final List<SummaryStatistics> statistics = new ArrayList<>(results.size());
        for (final NumberAnalyzerResult analyzerResult : results) {
            final SummaryStatistics stats = buildStatistics(column, analyzerResult);
            statistics.add(stats);
        }
        return AggregateSummaryStatistics.aggregate(statistics);
    }

    private SummaryStatistics buildStatistics(final String column, final NumberAnalyzerResult analyzerResult) {
        return new SummaryStatistics() {
            private static final long serialVersionUID = 1L;

            private final InputColumn<Number> col = new MockInputColumn<>(column);

            @Override
            public long getN() {
                return analyzerResult.getRowCount(col).longValue();
            }

            @Override
            public double getSum() {
                return analyzerResult.getSum(col).longValue();
            }

            @Override
            public double getVariance() {
                return analyzerResult.getVariance(col).longValue();
            }

            @Override
            public double getStandardDeviation() {
                return analyzerResult.getStandardDeviation(col).longValue();
            }

            @Override
            public double getMean() {
                return analyzerResult.getMean(col).longValue();
            }

            @Override
            public double getMin() {
                return analyzerResult.getLowestValue(col).longValue();
            }

            @Override
            public double getMax() {
                return analyzerResult.getHighestValue(col).longValue();
            }

            @Override
            public double getGeometricMean() {
                return analyzerResult.getGeometricMean(col).doubleValue();
            }

            @Override
            public double getSecondMoment() {
                return analyzerResult.getSecondMoment(col).doubleValue();
            }

            @Override
            public double getSumsq() {
                return analyzerResult.getSumOfSquares(col).doubleValue();
            }
        };
    }

    @Override
    protected NumberAnalyzerResult buildResult(final Crosstab<?> crosstab,
            final Collection<? extends NumberAnalyzerResult> results) {
        final NumberAnalyzerResult firstResult = results.iterator().next();

        final InputColumn<? extends Number>[] columns = firstResult.getColumns();
        return new NumberAnalyzerResult(columns, crosstab);
    }

}
