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

import java.util.Collection;
import java.util.List;

import org.datacleaner.beans.api.ParameterizableMetric;
import org.datacleaner.data.InputColumn;
import org.datacleaner.result.AnalyzerResult;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.Metric;

public class BooleanAnalyzerResult implements AnalyzerResult {

    private static final long serialVersionUID = 1L;

    private final Crosstab<Number> _columnStatisticsCrosstab;
    private final Crosstab<Number> _valueCombinationCrosstab;

    public BooleanAnalyzerResult(Crosstab<Number> columnStatisticsCrosstab, Crosstab<Number> valueCombinationCrosstab) {
        _columnStatisticsCrosstab = columnStatisticsCrosstab;
        _valueCombinationCrosstab = valueCombinationCrosstab;
    }

    public Crosstab<Number> getColumnStatisticsCrosstab() {
        return _columnStatisticsCrosstab;
    }

    public Crosstab<Number> getValueCombinationCrosstab() {
        return _valueCombinationCrosstab;
    }

    /**
     * Because the {@link BooleanAnalyzer} is also wrapped by other analyzers
     * which internally create transformed columns, we need to expose the
     * metrics here based on string names instead of {@link InputColumn}
     * objects. This method is used to provide suggestions for all these string
     * column names.
     * 
     * @return
     */
    private List<String> getColumnNameSuggestions() {
        final CrosstabDimension columnDimension = _columnStatisticsCrosstab
                .getDimension(BooleanAnalyzer.DIMENSION_COLUMN);
        return columnDimension.getCategories();
    }

    @Metric(order = 1, value = "Row count")
    public Number getRowCount() {
        final CrosstabDimension columnDimension = _columnStatisticsCrosstab
                .getDimension(BooleanAnalyzer.DIMENSION_COLUMN);
        return _columnStatisticsCrosstab.where(BooleanAnalyzer.DIMENSION_MEASURE, BooleanAnalyzer.MEASURE_ROW_COUNT)
                .where(columnDimension, columnDimension.getCategories().get(0)).get();
    }

    @Metric(order = 2, value = "Null count")
    public ParameterizableMetric getNullCount() {
        return new ParameterizableMetric() {
            @Override
            public Number getValue(String parameter) {
                return _columnStatisticsCrosstab
                        .where(BooleanAnalyzer.DIMENSION_MEASURE, BooleanAnalyzer.MEASURE_NULL_COUNT)
                        .where(BooleanAnalyzer.DIMENSION_COLUMN, parameter).safeGet(0);
            }

            @Override
            public Collection<String> getParameterSuggestions() {
                return getColumnNameSuggestions();
            }
        };
    }

    @Metric(order = 3, value = "True count")
    public ParameterizableMetric getTrueCount() {
        return new ParameterizableMetric() {
            @Override
            public Number getValue(String parameter) {
                return _columnStatisticsCrosstab
                        .where(BooleanAnalyzer.DIMENSION_MEASURE, BooleanAnalyzer.MEASURE_TRUE_COUNT)
                        .where(BooleanAnalyzer.DIMENSION_COLUMN, parameter).safeGet(0);
            }

            @Override
            public Collection<String> getParameterSuggestions() {
                return getColumnNameSuggestions();
            }
        };
    }

    @Metric(order = 4, value = "False count")
    public ParameterizableMetric getFalseCount() {
        return new ParameterizableMetric() {
            @Override
            public Number getValue(String parameter) {
                return _columnStatisticsCrosstab
                        .where(BooleanAnalyzer.DIMENSION_MEASURE, BooleanAnalyzer.MEASURE_FALSE_COUNT)
                        .where(BooleanAnalyzer.DIMENSION_COLUMN, parameter).safeGet(0);
            }

            @Override
            public Collection<String> getParameterSuggestions() {
                return getColumnNameSuggestions();
            }
        };
    }

    @Metric(order = 5, value = "Total combination count")
    public int getTotalCombinationCount() {
        if (_valueCombinationCrosstab == null) {
            return 0;
        }
        return _valueCombinationCrosstab.getDimension(BooleanAnalyzer.DIMENSION_MEASURE).getCategoryCount();
    }

    @Metric(order = 6, value = "Combination count")
    public ParameterizableMetric getCombinationCount() {
        return new BooleanAnalyzerCombinationMetric(_valueCombinationCrosstab);
    }
}
