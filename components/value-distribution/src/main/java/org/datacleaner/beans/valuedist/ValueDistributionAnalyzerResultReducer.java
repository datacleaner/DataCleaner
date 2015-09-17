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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.AnalyzerResultReducer;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Provided;
import org.datacleaner.result.CompositeValueFrequency;
import org.datacleaner.result.SingleValueFrequency;
import org.datacleaner.result.ValueCountList;
import org.datacleaner.result.ValueCountListImpl;
import org.datacleaner.result.ValueCountingAnalyzerResult;
import org.datacleaner.result.ValueFrequency;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.storage.RowAnnotationImpl;

/**
 * A reducer of {@link ValueDistributionAnalyzerResult}s.
 */
public class ValueDistributionAnalyzerResultReducer implements AnalyzerResultReducer<ValueDistributionAnalyzerResult> {

    @Inject
    @Provided
    RowAnnotationFactory _rowAnnotationFactory;

    @Override
    public ValueDistributionAnalyzerResult reduce(Collection<? extends ValueDistributionAnalyzerResult> analyzerResults) {
        final Map<String, RowAnnotation> annotations = Collections.emptyMap();

        Map<String, Collection<ValueFrequency>> flattenedTopValuesMap = new HashMap<>();
        Map<String, Collection<String>> flattenedUniqueValuesMap = new HashMap<>();
        Map<String, Collection<ValueFrequency>> flattenedDistinctValuesMap = new HashMap<>();

        Map<String, Integer> reducedTotalCountMap = new HashMap<>();

        AnalyzerResult first = analyzerResults.iterator().next();

        for (ValueDistributionAnalyzerResult partialResult : analyzerResults) {
            if (partialResult instanceof SingleValueDistributionResult) {
                final SingleValueDistributionResult singlePartialResult = (SingleValueDistributionResult) partialResult;

                flattenAggregates(flattenedTopValuesMap, flattenedUniqueValuesMap, flattenedDistinctValuesMap,
                        reducedTotalCountMap, singlePartialResult);
            } else if (partialResult instanceof GroupedValueDistributionResult) {
                final GroupedValueDistributionResult groupedPartialResult = (GroupedValueDistributionResult) partialResult;

                for (ValueCountingAnalyzerResult childValueCountingResult : groupedPartialResult.getGroupResults()) {
                    SingleValueDistributionResult childResult = (SingleValueDistributionResult) childValueCountingResult;

                    flattenAggregates(flattenedTopValuesMap, flattenedUniqueValuesMap, flattenedDistinctValuesMap,
                            reducedTotalCountMap, childResult);
                }

            } else {
                throw new IllegalStateException("Unsupported type of "
                        + ValueDistributionAnalyzerResult.class.getSimpleName() + ": "
                        + partialResult.getClass().getSimpleName());
            }
        }

        Map<String, Collection<String>> reducedUniqueValuesMap = reduceUniqueValuesMap(flattenedUniqueValuesMap);
        Map<String, Collection<ValueFrequency>> reducedTopValuesMap = reduceTopValuesMap(flattenedTopValuesMap);
        Map<String, Integer> reducedDistinctValuesMap = reduceDistinctValuesMap(flattenedDistinctValuesMap);

        ValueDistributionAnalyzerResult reducedResult;
        // Assumption: all the elements in the collection are supposed to be the
        // same type. Either SingleValueDistributionResult or
        // GroupedValueDistributionResult. Checking the first one, then...
        if (first instanceof GroupedValueDistributionResult) {
            final InputColumn<?> inputColumn = ((GroupedValueDistributionResult) first).getColumn();
            final InputColumn<String> groupColumn = ((GroupedValueDistributionResult) first).getGroupColumn();
            final InputColumn<?>[] highlightedColumns = new InputColumn<?>[2];
            highlightedColumns[0] = inputColumn;
            highlightedColumns[0] = groupColumn;
            List<SingleValueDistributionResult> childResults = new ArrayList<>();
            for (String groupName : flattenedDistinctValuesMap.keySet()) {
                final ValueCountList reducedValueCountList = createValueCountList(reducedTopValuesMap.get(groupName));
                SingleValueDistributionResult childValueDistributionResult = new SingleValueDistributionResult(
                        groupName, reducedValueCountList, reducedUniqueValuesMap.get(groupName), reducedUniqueValuesMap
                                .get(groupName).size(), reducedDistinctValuesMap.get(groupName),
                        reducedTotalCountMap.get(groupName), annotations, new RowAnnotationImpl(),
                        _rowAnnotationFactory, highlightedColumns);
                childResults.add(childValueDistributionResult);
            }
            reducedResult = new GroupedValueDistributionResult(inputColumn, groupColumn, childResults);
        } else {
            final InputColumn<?>[] highlightedColumns = ((SingleValueDistributionResult) first).getHighlightedColumns();
            final Collection<String> reducedUniqueValues = reducedUniqueValuesMap.values().iterator().next();
            final ValueCountList reducedValueCountList = createValueCountList(reducedTopValuesMap.values().iterator()
                    .next());
            reducedResult = new SingleValueDistributionResult(((SingleValueDistributionResult) first).getName(),
                    reducedValueCountList, reducedUniqueValues, reducedUniqueValues.size(), reducedDistinctValuesMap
                            .values().iterator().next(), reducedTotalCountMap.values().iterator().next(), annotations,
                    new RowAnnotationImpl(), _rowAnnotationFactory, highlightedColumns);
        }

        return reducedResult;
    }

    private void flattenAggregates(Map<String, Collection<ValueFrequency>> flattenedTopValuesMap,
            Map<String, Collection<String>> flattenedUniqueValuesMap,
            Map<String, Collection<ValueFrequency>> flattenedDistinctValuesMap,
            Map<String, Integer> reducedTotalCountMap, final SingleValueDistributionResult singlePartialResult) {
        Collection<ValueFrequency> flattenedTopValues = getOrInitializeFlattenedTopValues(flattenedTopValuesMap,
                singlePartialResult);
        Collection<String> flattenedUniqueValues = getOrInitializeFlattenedUniqueValues(flattenedUniqueValuesMap,
                singlePartialResult);
        Collection<ValueFrequency> flattenedDistinctValues = getOrInitializeFlattenedTopValues(
                flattenedDistinctValuesMap, singlePartialResult);

        flattenedUniqueValues.addAll(singlePartialResult.getUniqueValues());
        flattenedTopValues.addAll(singlePartialResult.getTopValues().getValueCounts());
        flattenedDistinctValues.addAll(singlePartialResult.getValueCounts());

        Integer reducedTotalCount = reducedTotalCountMap.get(singlePartialResult.getName());
        if (reducedTotalCount == null) {
            reducedTotalCountMap.put(singlePartialResult.getName(), singlePartialResult.getTotalCount());
            reducedTotalCount = reducedTotalCountMap.get(singlePartialResult.getName());
        } else {
            reducedTotalCountMap.remove(singlePartialResult.getName());
            reducedTotalCountMap.put(singlePartialResult.getName(),
                    reducedTotalCount + singlePartialResult.getTotalCount());
        }
    }

    private ValueCountList createValueCountList(Collection<ValueFrequency> valueFrequencies) {
        ValueCountListImpl valueCountListImpl = ValueCountListImpl.createFullList();
        for (ValueFrequency valueFrequency : valueFrequencies) {
            valueCountListImpl.register(valueFrequency);
        }
        return valueCountListImpl;
    }

    private Collection<String> getOrInitializeFlattenedUniqueValues(
            Map<String, Collection<String>> flattenedUniqueValuesMap,
            final SingleValueDistributionResult singlePartialResult) {
        Collection<String> flattenedUniqueValues = flattenedUniqueValuesMap.get(singlePartialResult.getName());
        if (flattenedUniqueValues == null) {
            flattenedUniqueValuesMap.put(singlePartialResult.getName(), new ArrayList<String>());
            flattenedUniqueValues = flattenedUniqueValuesMap.get(singlePartialResult.getName());
        }
        return flattenedUniqueValues;
    }

    private Collection<ValueFrequency> getOrInitializeFlattenedTopValues(
            Map<String, Collection<ValueFrequency>> flattenedTopValuesMap,
            final SingleValueDistributionResult singlePartialResult) {
        Collection<ValueFrequency> flattenedTopValues = flattenedTopValuesMap.get(singlePartialResult.getName());
        if (flattenedTopValues == null) {
            flattenedTopValuesMap.put(singlePartialResult.getName(), new ArrayList<ValueFrequency>());
            flattenedTopValues = flattenedTopValuesMap.get(singlePartialResult.getName());
        }
        return flattenedTopValues;
    }

    private Map<String, Collection<ValueFrequency>> reduceTopValuesMap(
            Map<String, Collection<ValueFrequency>> flattenedTopValuesMap) {
        Map<String, Collection<ValueFrequency>> reducedValuesMap = new HashMap<>();

        for (Map.Entry<String, Collection<ValueFrequency>> entry : flattenedTopValuesMap.entrySet()) {
            String groupName = entry.getKey();
            Collection<ValueFrequency> reducedValues = reduceTopValues(entry.getValue()).getValueCounts();
            reducedValuesMap.put(groupName, reducedValues);
        }

        return reducedValuesMap;
    }

    private ValueCountList reduceTopValues(Collection<ValueFrequency> flattenedTopValues) {
        List<ValueFrequency> reducedValueFrequencies = new ArrayList<>();

        for (ValueFrequency valueFrequency : flattenedTopValues) {

            boolean found = false;
            for (ValueFrequency reducedValueFrequency : reducedValueFrequencies) {
                if (reducedValueFrequency.getName().equals(valueFrequency.getName())) {
                    found = true;
                    reducedValueFrequencies.remove(reducedValueFrequency);
                    reducedValueFrequencies.add(new SingleValueFrequency(reducedValueFrequency.getName(),
                            reducedValueFrequency.getCount() + valueFrequency.getCount()));
                    break;
                }
            }

            if (!found) {
                reducedValueFrequencies.add(valueFrequency);
            }
        }

        ValueCountList reducedValueCountList = createValueCountList(reducedValueFrequencies);
        return reducedValueCountList;
    }

    private Map<String, Integer> reduceDistinctValuesMap(
            Map<String, Collection<ValueFrequency>> flattenedDistinctValuesMap) {
        Map<String, Integer> reducedValuesMap = new HashMap<>();

        for (Map.Entry<String, Collection<ValueFrequency>> entry : flattenedDistinctValuesMap.entrySet()) {
            String groupName = entry.getKey();
            Integer reducedCount = reduceDistinctCount(entry.getValue());
            reducedValuesMap.put(groupName, reducedCount);
        }

        return reducedValuesMap;
    }

    private int reduceDistinctCount(Collection<ValueFrequency> valueFrequencies) {
        Set<String> distinctValues = new HashSet<>();

        for (ValueFrequency valueFrequency : valueFrequencies) {
            if (valueFrequency instanceof CompositeValueFrequency) {
                CompositeValueFrequency compositeValueFrequency = (CompositeValueFrequency) valueFrequency;

                for (ValueFrequency childValueFrequency : compositeValueFrequency.getChildren()) {
                    distinctValues.add(childValueFrequency.getValue());
                }
            } else {
                distinctValues.add(valueFrequency.getValue());
            }
        }

        return distinctValues.size();
    }

    private Map<String, Collection<String>> reduceUniqueValuesMap(
            Map<String, Collection<String>> flattenedUniqueValuesMap) {
        Map<String, Collection<String>> reducedUniqueValuesMap = new HashMap<>();

        for (Map.Entry<String, Collection<String>> entry : flattenedUniqueValuesMap.entrySet()) {
            String groupName = entry.getKey();
            Collection<String> reducedUniqueValues = reduceUniqueValues(entry.getValue());
            reducedUniqueValuesMap.put(groupName, reducedUniqueValues);
        }

        return reducedUniqueValuesMap;
    }

    private Collection<String> reduceUniqueValues(Collection<String> flattenedUniqueValues) {
        Map<String, Integer> frequencyMap = new HashMap<String, Integer>();

        if (flattenedUniqueValues != null) {
            for (String value : flattenedUniqueValues) {
                if (frequencyMap.containsKey(value)) {
                    frequencyMap.put(value, frequencyMap.get(value) + 1);
                } else {
                    frequencyMap.put(value, 1);
                }
            }
        }

        List<String> uniqueList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() == 1) {
                uniqueList.add(entry.getKey());
            }
        }

        return uniqueList;
    }

}
