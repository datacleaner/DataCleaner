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
package org.datacleaner.beans.valuedist;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.datacleaner.api.AnalyzerResultReducer;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Provided;
import org.datacleaner.result.ReducedValueDistributionResult;
import org.datacleaner.result.ValueCountingAnalyzerResult;
import org.datacleaner.result.ValueFrequency;
import org.datacleaner.storage.RowAnnotationFactory;

/**
 * A reducer of {@link ValueDistributionAnalyzerResult}s.
 */
public class ValueDistributionAnalyzerResultReducer implements AnalyzerResultReducer<ValueDistributionAnalyzerResult> {

    @Inject
    @Provided
    RowAnnotationFactory _rowAnnotationFactory;

    @Override
    public ValueDistributionAnalyzerResult reduce(Collection<? extends ValueDistributionAnalyzerResult> analyzerResults) {
        final Map<String, Integer> reducedValueCounts = new HashMap<>();
        Integer nullCount = 0;

        ValueDistributionAnalyzerResult first = analyzerResults.iterator().next();

        for (ValueDistributionAnalyzerResult partialResult : analyzerResults) {
            if (partialResult instanceof SingleValueDistributionResult) {
                nullCount = reduceValueCounts(reducedValueCounts, nullCount, partialResult);
            } else if (partialResult instanceof GroupedValueDistributionResult) {
                final GroupedValueDistributionResult groupedPartialResult = (GroupedValueDistributionResult) partialResult;

                for (ValueCountingAnalyzerResult childValueCountingResult : groupedPartialResult.getGroupResults()) {
                    SingleValueDistributionResult childResult = (SingleValueDistributionResult) childValueCountingResult;
                    nullCount = reduceValueCounts(reducedValueCounts, nullCount, childResult);
                }
            } else {
                throw new IllegalStateException("Unsupported type of "
                        + ValueDistributionAnalyzerResult.class.getSimpleName() + ": "
                        + partialResult.getClass().getSimpleName());
            }
        }

        
        final InputColumn<?>[] highlightedColumns;
        if (first instanceof GroupedValueDistributionResult) {
            final InputColumn<?> inputColumn = ((GroupedValueDistributionResult) first).getColumn();
            final InputColumn<String> groupColumn = ((GroupedValueDistributionResult) first).getGroupColumn();
            highlightedColumns = new InputColumn<?>[2];
            highlightedColumns[0] = inputColumn;
            highlightedColumns[0] = groupColumn;
        } else {
            highlightedColumns = ((SingleValueDistributionResult) first).getHighlightedColumns();
        }

        return new ReducedValueDistributionResult(first.getName(), reducedValueCounts, nullCount);
    }

    private Integer reduceValueCounts(Map<String, Integer> reducedValueCounts, Integer nullCount,
            ValueDistributionAnalyzerResult partialResult) {
        Collection<ValueFrequency> valueCounts = partialResult.getValueCounts();
        for (ValueFrequency valueFrequency : valueCounts) {
            if (!valueFrequency.isComposite()) {
                nullCount = recordNonCompositeValueFrequency(reducedValueCounts, nullCount, valueFrequency);
            } else {
                for (ValueFrequency childValueFrequency : valueFrequency.getChildren()) {
                    nullCount = recordNonCompositeValueFrequency(reducedValueCounts, nullCount, childValueFrequency);
                }
            }

        }
        return nullCount;
    }

    private Integer recordNonCompositeValueFrequency(Map<String, Integer> reducedValueCounts, Integer nullCount,
            ValueFrequency valueFrequency) {
        String value = valueFrequency.getValue();
        int count = valueFrequency.getCount();
        
        if (value != null) {
            if (reducedValueCounts.containsKey(value)) {
                Integer oldCount = reducedValueCounts.get(value);
                reducedValueCounts.put(value, oldCount + count);
            } else {
                reducedValueCounts.put(value, count);
            }
        } else {
            nullCount = nullCount + count;
        }
        return nullCount;
    }

}
