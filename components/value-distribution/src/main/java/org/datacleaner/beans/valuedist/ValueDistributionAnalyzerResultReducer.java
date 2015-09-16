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
import org.datacleaner.api.Provided;
import org.datacleaner.result.CompositeValueFrequency;
import org.datacleaner.result.SingleValueFrequency;
import org.datacleaner.result.ValueCountList;
import org.datacleaner.result.ValueCountListImpl;
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

        Collection<ValueFrequency> flattenedTopValues = new ArrayList<>();
        Collection<String> flattenedUniqueValues = new ArrayList<>();
        Collection<ValueFrequency> flattenedDistinctValues = new ArrayList<>();
        int reducedTotalCount = 0;

        AnalyzerResult first = analyzerResults.iterator().next();

        for (ValueDistributionAnalyzerResult partialResult : analyzerResults) {
            if (partialResult instanceof SingleValueDistributionResult) {
                final SingleValueDistributionResult singlePartialResult = (SingleValueDistributionResult) partialResult;

                flattenedUniqueValues.addAll(singlePartialResult.getUniqueValues());
                flattenedTopValues.addAll(singlePartialResult.getTopValues().getValueCounts());
                flattenedDistinctValues.addAll(singlePartialResult.getValueCounts());
                reducedTotalCount += singlePartialResult.getTotalCount();

                // } else if (partialResult instanceof
                // GroupedValueDistributionResult) {
                // final GroupedValueDistributionResult groupedReducedResult =
                // (GroupedValueDistributionResult) reducedResult;
                // final GroupedValueDistributionResult groupedPartialResult =
                // (GroupedValueDistributionResult) partialResult;
                //
                // reducedResult = reduceGroupedResult(annotations,
                // groupedReducedResult, groupedPartialResult);
            } else {
                throw new IllegalStateException("Unsupported type of "
                        + ValueDistributionAnalyzerResult.class.getSimpleName() + ": "
                        + partialResult.getClass().getSimpleName());
            }
        }

        Collection<String> reducedUniqueValues = reduceUniqueValues(flattenedUniqueValues);
        ValueCountList reducedTopValues = reduceTopValues(flattenedTopValues);
        int reducedDistinctCount = reduceDistinctCount(flattenedDistinctValues);

        ValueDistributionAnalyzerResult reducedResult = new SingleValueDistributionResult(
                ((SingleValueDistributionResult) first).getName(), reducedTopValues, reducedUniqueValues,
                reducedUniqueValues.size(), reducedDistinctCount, reducedTotalCount, annotations,
                new RowAnnotationImpl(), _rowAnnotationFactory,
                ((SingleValueDistributionResult) first).getHighlightedColumns());

        return reducedResult;
    }

    // @SuppressWarnings("unchecked")
    // private GroupedValueDistributionResult reduceGroupedResult(final
    // Map<String, RowAnnotation> annotations, final
    // GroupedValueDistributionResult reducedResult,
    // final GroupedValueDistributionResult groupedResult) {
    // if (reducedResult == null) {
    // return groupedResult;
    // }
    //
    // InputColumn<?>[] highlightedColumns = null;
    //
    // final Collection<SingleValueDistributionResult> reducedChildResults =
    // (Collection<SingleValueDistributionResult>)
    // reducedResult.getGroupResults();
    // for (SingleValueDistributionResult singleResult :
    // (Collection<SingleValueDistributionResult>)
    // groupedResult.getGroupResults()) {
    // boolean groupFound = false;
    //
    // for (SingleValueDistributionResult singleReducedResult :
    // (Collection<SingleValueDistributionResult>)
    // reducedResult.getGroupResults()) {
    //
    // if (singleReducedResult.getName().equals(singleResult.getName())) {
    // SingleValueDistributionResult reducedSingleResult =
    // reduceSingleResult(annotations,
    // singleResult.getHighlightedColumns(), singleReducedResult, singleResult);
    // reducedChildResults.remove(singleResult);
    // reducedChildResults.add(reducedSingleResult);
    // highlightedColumns = reducedSingleResult.getHighlightedColumns();
    // groupFound = true;
    // break;
    // }
    // }
    //
    // if (!groupFound) {
    // reducedChildResults.add(singleResult);
    // highlightedColumns = singleResult.getHighlightedColumns();
    // }
    // }
    //
    // return new GroupedValueDistributionResult(highlightedColumns[0],
    // (InputColumn<String>) highlightedColumns[1],
    // reducedChildResults);
    // }

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

        ValueCountListImpl reducedValueCountList = ValueCountListImpl.createFullList();
        for (ValueFrequency valueFrequency : reducedValueFrequencies) {
            reducedValueCountList.register(valueFrequency);
        }
        return reducedValueCountList;
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
