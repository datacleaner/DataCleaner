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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.stat.descriptive.moment.SecondMoment;
import org.datacleaner.beans.api.Analyzer;
import org.datacleaner.beans.api.Concurrent;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Description;
import org.datacleaner.beans.api.Initialize;
import org.datacleaner.beans.api.Provided;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.CrosstabNavigator;
import org.datacleaner.storage.InMemoryRowAnnotationFactory;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;

/**
 * Number analyzer, which provides statistical information for number values:
 * 
 * <ul>
 * <li>Highest value</li>
 * <li>Lowest value</li>
 * <li>Sum</li>
 * <li>Mean</li>
 * <li>Geometric mean</li>
 * <li>Standard deviation</li>
 * <li>Variance</li>
 * </ul>
 */
@Named("Number analyzer")
@Description("Provides insight into number-column values.")
@Concurrent(true)
public class NumberAnalyzer implements Analyzer<NumberAnalyzerResult> {

    public static final String DIMENSION_COLUMN = "Column";
    public static final String DIMENSION_MEASURE = "Measure";
    public static final String MEASURE_ROW_COUNT = "Row count";
    public static final String MEASURE_NULL_COUNT = "Null count";
    public static final String MEASURE_HIGHEST_VALUE = "Highest value";
    public static final String MEASURE_LOWEST_VALUE = "Lowest value";
    public static final String MEASURE_SUM = "Sum";
    public static final String MEASURE_MEAN = "Mean";
    public static final String MEASURE_GEOMETRIC_MEAN = "Geometric mean";
    public static final String MEASURE_STANDARD_DEVIATION = "Standard deviation";
    public static final String MEASURE_VARIANCE = "Variance";
    public static final String MEASURE_SUM_OF_SQUARES = "Sum of squares";
    public static final String MEASURE_SECOND_MOMENT = "Second moment";

    public static final String MEASURE_MEDIAN = "Median";
    public static final String MEASURE_PERCENTILE25 = "25th percentile";
    public static final String MEASURE_PERCENTILE75 = "75th percentile";
    public static final String MEASURE_KURTOSIS = "Kurtosis";
    public static final String MEASURE_SKEWNESS = "Skewness";

    private Map<InputColumn<? extends Number>, NumberAnalyzerColumnDelegate> _columnDelegates = new HashMap<InputColumn<? extends Number>, NumberAnalyzerColumnDelegate>();

    @Inject
    @Configured
    InputColumn<? extends Number>[] _columns;

    @Inject
    @Configured
    @Description("Gather so-called descriptive statistics, including median, skewness, kurtosis and percentiles, which have a larger memory-footprint.")
    boolean descriptiveStatistics = false;

    @Inject
    @Provided
    RowAnnotationFactory _annotationFactory;

    public NumberAnalyzer() {
    }

    @SafeVarargs
    public NumberAnalyzer(InputColumn<? extends Number>... columns) {
        this();
        _columns = columns;
        _annotationFactory = new InMemoryRowAnnotationFactory();
        init();
    }

    @Initialize
    public void init() {
        for (InputColumn<? extends Number> column : _columns) {
            _columnDelegates.put(column, new NumberAnalyzerColumnDelegate(descriptiveStatistics, _annotationFactory));
        }
    }

    @Override
    public void run(InputRow row, int distinctCount) {
        for (InputColumn<? extends Number> column : _columns) {
            NumberAnalyzerColumnDelegate delegate = _columnDelegates.get(column);
            Number value = row.getValue(column);

            delegate.run(row, value, distinctCount);
        }
    }

    @Override
    public NumberAnalyzerResult getResult() {
        CrosstabDimension measureDimension = new CrosstabDimension(DIMENSION_MEASURE);
        measureDimension.addCategory(MEASURE_ROW_COUNT);
        measureDimension.addCategory(MEASURE_NULL_COUNT);
        measureDimension.addCategory(MEASURE_HIGHEST_VALUE);
        measureDimension.addCategory(MEASURE_LOWEST_VALUE);
        measureDimension.addCategory(MEASURE_SUM);
        measureDimension.addCategory(MEASURE_MEAN);
        measureDimension.addCategory(MEASURE_GEOMETRIC_MEAN);
        measureDimension.addCategory(MEASURE_STANDARD_DEVIATION);
        measureDimension.addCategory(MEASURE_VARIANCE);
        measureDimension.addCategory(MEASURE_SECOND_MOMENT);
        measureDimension.addCategory(MEASURE_SUM_OF_SQUARES);

        if (descriptiveStatistics) {
            measureDimension.addCategory(MEASURE_MEDIAN);
            measureDimension.addCategory(MEASURE_PERCENTILE25);
            measureDimension.addCategory(MEASURE_PERCENTILE75);
            measureDimension.addCategory(MEASURE_SKEWNESS);
            measureDimension.addCategory(MEASURE_KURTOSIS);
        }

        CrosstabDimension columnDimension = new CrosstabDimension(DIMENSION_COLUMN);
        for (InputColumn<? extends Number> column : _columns) {
            columnDimension.addCategory(column.getName());
        }

        Crosstab<Number> crosstab = new Crosstab<Number>(Number.class, columnDimension, measureDimension);
        for (InputColumn<? extends Number> column : _columns) {
            CrosstabNavigator<Number> nav = crosstab.navigate().where(columnDimension, column.getName());
            NumberAnalyzerColumnDelegate delegate = _columnDelegates.get(column);

            StatisticalSummary s = delegate.getStatistics();
            int nullCount = delegate.getNullCount();

            nav.where(measureDimension, MEASURE_NULL_COUNT).put(nullCount);

            if (nullCount > 0) {
                addAttachment(nav, delegate.getNullAnnotation(), column);
            }

            int numRows = delegate.getNumRows();
            nav.where(measureDimension, MEASURE_ROW_COUNT).put(numRows);

            long nonNullCount = s.getN();

            if (nonNullCount > 0) {
                final double highestValue = s.getMax();
                final double lowestValue = s.getMin();
                final double sum = s.getSum();
                final double mean = s.getMean();
                final double standardDeviation = s.getStandardDeviation();
                final double variance = s.getVariance();

                final double geometricMean;
                final double secondMoment;
                final double sumOfSquares;
                if (descriptiveStatistics) {
                    final DescriptiveStatistics descriptiveStats = (DescriptiveStatistics) s;
                    geometricMean = descriptiveStats.getGeometricMean();
                    sumOfSquares = descriptiveStats.getSumsq();
                    secondMoment = new SecondMoment().evaluate(descriptiveStats.getValues());
                } else {
                    final SummaryStatistics summaryStats = (SummaryStatistics) s;
                    geometricMean = summaryStats.getGeometricMean();
                    secondMoment = summaryStats.getSecondMoment();
                    sumOfSquares = summaryStats.getSumsq();
                }

                nav.where(measureDimension, MEASURE_HIGHEST_VALUE).put(highestValue);
                addAttachment(nav, delegate.getMaxAnnotation(), column);

                nav.where(measureDimension, MEASURE_LOWEST_VALUE).put(lowestValue);
                addAttachment(nav, delegate.getMinAnnotation(), column);

                nav.where(measureDimension, MEASURE_SUM).put(sum);
                nav.where(measureDimension, MEASURE_MEAN).put(mean);
                nav.where(measureDimension, MEASURE_GEOMETRIC_MEAN).put(geometricMean);
                nav.where(measureDimension, MEASURE_STANDARD_DEVIATION).put(standardDeviation);
                nav.where(measureDimension, MEASURE_VARIANCE).put(variance);
                nav.where(measureDimension, MEASURE_SUM_OF_SQUARES).put(sumOfSquares);
                nav.where(measureDimension, MEASURE_SECOND_MOMENT).put(secondMoment);

                if (descriptiveStatistics) {
                    final DescriptiveStatistics descriptiveStatistics = (DescriptiveStatistics) s;
                    final double kurtosis = descriptiveStatistics.getKurtosis();
                    final double skewness = descriptiveStatistics.getSkewness();
                    final double median = descriptiveStatistics.getPercentile(50.0);
                    final double percentile25 = descriptiveStatistics.getPercentile(25.0);
                    final double percentile75 = descriptiveStatistics.getPercentile(75.0);

                    nav.where(measureDimension, MEASURE_MEDIAN).put(median);
                    nav.where(measureDimension, MEASURE_PERCENTILE25).put(percentile25);
                    nav.where(measureDimension, MEASURE_PERCENTILE75).put(percentile75);
                    nav.where(measureDimension, MEASURE_SKEWNESS).put(skewness);
                    nav.where(measureDimension, MEASURE_KURTOSIS).put(kurtosis);
                }
            }
        }
        return new NumberAnalyzerResult(_columns, crosstab);
    }

    private void addAttachment(CrosstabNavigator<Number> nav, RowAnnotation annotation, InputColumn<?> column) {
        nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, column));
    }
}
