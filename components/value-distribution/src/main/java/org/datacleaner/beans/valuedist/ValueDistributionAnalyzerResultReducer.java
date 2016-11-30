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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.datacleaner.api.AnalyzerResultReducer;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Provided;
import org.datacleaner.result.ReducedSingleValueDistributionResult;
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
    public ValueDistributionAnalyzerResult reduce(
            final Collection<? extends ValueDistributionAnalyzerResult> analyzerResults) {
        if (hasGroupedResults(analyzerResults)) {
            return reduceGroupedResults(analyzerResults);
        } else {
            return reduceSingleResults(analyzerResults);
        }
    }

    private ValueDistributionAnalyzerResult reduceSingleResults(
            final Collection<? extends ValueDistributionAnalyzerResult> analyzerResults) {
        final Map<String, Integer> reducedValueCounts = new HashMap<>();
        Integer nullCount = 0;

        final ValueDistributionAnalyzerResult first = analyzerResults.iterator().next();

        for (final ValueDistributionAnalyzerResult partialResult : analyzerResults) {
            if ((partialResult instanceof SingleValueDistributionResult)
                    || (partialResult instanceof ReducedSingleValueDistributionResult)) {
                nullCount = reduceValueCounts(reducedValueCounts, nullCount, partialResult);
            } else {
                throw new IllegalStateException(
                        "Unsupported type of " + ValueDistributionAnalyzerResult.class.getSimpleName() + ": "
                                + partialResult.getClass().getSimpleName());
            }
        }

        return new ReducedSingleValueDistributionResult(first.getName(), reducedValueCounts, nullCount);
    }

    private boolean hasGroupedResults(final Collection<? extends ValueDistributionAnalyzerResult> analyzerResults) {
        for (final ValueDistributionAnalyzerResult valueDistributionAnalyzerResult : analyzerResults) {
            if (valueDistributionAnalyzerResult instanceof GroupedValueDistributionResult) {
                return true;
            }
        }
        return false;
    }

    private ValueDistributionAnalyzerResult reduceGroupedResults(
            final Collection<? extends ValueDistributionAnalyzerResult> analyzerResults) {

        final Map<String, List<ValueDistributionAnalyzerResult>> groupedMap = new HashMap<>();

        final ValueDistributionAnalyzerResult first = analyzerResults.iterator().next();

        for (final ValueDistributionAnalyzerResult partialResult : analyzerResults) {
            if (partialResult instanceof GroupedValueDistributionResult) {
                final GroupedValueDistributionResult groupedPartialResult =
                        (GroupedValueDistributionResult) partialResult;

                for (final ValueCountingAnalyzerResult childValueCountingResult : groupedPartialResult
                        .getGroupResults()) {
                    final ValueDistributionAnalyzerResult childValueDistributionResult =
                            (ValueDistributionAnalyzerResult) childValueCountingResult;
                    final String groupName = childValueCountingResult.getName();
                    if (groupedMap.containsKey(groupName)) {
                        final List<ValueDistributionAnalyzerResult> list = groupedMap.get(groupName);
                        list.add(childValueDistributionResult);
                    } else {
                        final List<ValueDistributionAnalyzerResult> list = new ArrayList<>();
                        list.add(childValueDistributionResult);
                        groupedMap.put(groupName, list);
                    }
                }
            } else {
                throw new IllegalStateException(
                        "Unsupported type of " + ValueDistributionAnalyzerResult.class.getSimpleName() + ": "
                                + partialResult.getClass().getSimpleName());
            }
        }

        final List<ValueDistributionAnalyzerResult> reducedChildResults = new ArrayList<>();
        final Collection<List<ValueDistributionAnalyzerResult>> groupedLists = groupedMap.values();
        for (final List<ValueDistributionAnalyzerResult> list : groupedLists) {
            final ValueDistributionAnalyzerResult reducedChildResult = reduce(list);
            reducedChildResults.add(reducedChildResult);
        }

        final InputColumn<?> inputColumn = ((GroupedValueDistributionResult) first).getColumn();
        final InputColumn<String> groupColumn = ((GroupedValueDistributionResult) first).getGroupColumn();
        return new GroupedValueDistributionResult(inputColumn, groupColumn, reducedChildResults);
    }

    private Integer reduceValueCounts(final Map<String, Integer> reducedValueCounts, Integer nullCount,
            final ValueDistributionAnalyzerResult partialResult) {
        final Collection<ValueFrequency> valueCounts = partialResult.getValueCounts();
        for (final ValueFrequency valueFrequency : valueCounts) {
            if (!valueFrequency.isComposite()) {
                nullCount = recordNonCompositeValueFrequency(reducedValueCounts, nullCount, valueFrequency);
            } else {
                for (final ValueFrequency childValueFrequency : valueFrequency.getChildren()) {
                    nullCount = recordNonCompositeValueFrequency(reducedValueCounts, nullCount, childValueFrequency);
                }
            }

        }
        return nullCount;
    }

    private Integer recordNonCompositeValueFrequency(final Map<String, Integer> reducedValueCounts, Integer nullCount,
            final ValueFrequency valueFrequency) {
        final String value = valueFrequency.getValue();
        final int count = valueFrequency.getCount();

        if (value != null) {
            if (reducedValueCounts.containsKey(value)) {
                final Integer oldCount = reducedValueCounts.get(value);
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
