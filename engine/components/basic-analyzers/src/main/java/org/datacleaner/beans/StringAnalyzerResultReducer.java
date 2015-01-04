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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.datacleaner.data.InputColumn;
import org.datacleaner.result.AbstractCrosstabResultReducer;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabNavigator;
import org.datacleaner.util.AverageBuilder;

/**
 * Result reducer for {@link StringAnalyzerResult}s
 */
public class StringAnalyzerResultReducer extends AbstractCrosstabResultReducer<StringAnalyzerResult> {

    private static final Set<String> AVG_MEASURES = new HashSet<String>(Arrays.asList(StringAnalyzer.MEASURE_AVG_CHARS,
            StringAnalyzer.MEASURE_AVG_WHITE_SPACES));

    private static final Set<String> MIN_MEASURES = new HashSet<String>(Arrays.asList(StringAnalyzer.MEASURE_MIN_CHARS,
            StringAnalyzer.MEASURE_MIN_WHITE_SPACES, StringAnalyzer.MEASURE_MIN_WORDS));

    private static final Set<String> MAX_MEASURES = new HashSet<String>(Arrays.asList(StringAnalyzer.MEASURE_MAX_CHARS,
            StringAnalyzer.MEASURE_MAX_WHITE_SPACES, StringAnalyzer.MEASURE_MAX_WORDS));

    @Override
    protected Serializable reduceValues(List<Object> slaveValues, String category1, String category2,
            Collection<? extends StringAnalyzerResult> results, Class<?> valueClass) {

        // category2 = measure

        if (AVG_MEASURES.contains(category2)) {
            return weightedAverage(slaveValues, results, category1, category2);
        } else if (MIN_MEASURES.contains(category2)) {
            return minimum(slaveValues);
        } else if (MAX_MEASURES.contains(category2)) {
            return maximum(slaveValues);
        } else {
            return sumAsInteger(slaveValues);
        }
    }

    private Serializable weightedAverage(List<Object> slaveValues, Collection<? extends StringAnalyzerResult> results,
            String columnName, String measureName) {
        final AverageBuilder averageBuilder = new AverageBuilder();

        for (StringAnalyzerResult analyzerResult : results) {
            final Crosstab<?> crosstab = analyzerResult.getCrosstab();
            final CrosstabNavigator<?> nav = crosstab.where(StringAnalyzer.DIMENSION_COLUMN, columnName);

            final Number rowCount = (Number) nav.where(StringAnalyzer.DIMENSION_MEASURES,
                    StringAnalyzer.MEASURE_ROW_COUNT).get();
            final Number averageMeasureValue = (Number) nav.where(StringAnalyzer.DIMENSION_MEASURES, measureName).get();

            averageBuilder.addValue(averageMeasureValue, rowCount.intValue());
        }

        return averageBuilder.getAverage();
    }

    @Override
    protected StringAnalyzerResult buildResult(Crosstab<?> crosstab, Collection<? extends StringAnalyzerResult> results) {
        final StringAnalyzerResult firstResult = results.iterator().next();
        final InputColumn<String>[] columns = firstResult.getColumns();
        return new StringAnalyzerResult(columns, crosstab);
    }

}
