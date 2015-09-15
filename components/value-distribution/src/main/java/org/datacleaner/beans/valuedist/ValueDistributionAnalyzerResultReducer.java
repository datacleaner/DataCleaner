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

        ValueDistributionAnalyzerResult reducedResult = null;

        for (ValueDistributionAnalyzerResult partialResult : analyzerResults) {
            if (partialResult instanceof SingleValueDistributionResult) {
                final SingleValueDistributionResult singleReducedResult = (SingleValueDistributionResult) reducedResult;
                final SingleValueDistributionResult singlePartialResult = (SingleValueDistributionResult) partialResult;
                reducedResult = reduceSingleResult(annotations, singlePartialResult.getHighlightedColumns(), singleReducedResult,
                        singlePartialResult);
            } else if (partialResult instanceof GroupedValueDistributionResult) {
                final GroupedValueDistributionResult groupedReducedResult = (GroupedValueDistributionResult) reducedResult;
                final GroupedValueDistributionResult groupedPartialResult = (GroupedValueDistributionResult) partialResult;
                
                reducedResult = reduceGroupedResult(annotations, groupedReducedResult, groupedPartialResult);
            } else {
                throw new IllegalStateException("Unsupported type of "
                        + ValueDistributionAnalyzerResult.class.getSimpleName() + ": "
                        + partialResult.getClass().getSimpleName());
            }
        }

        return reducedResult;
    }

    private SingleValueDistributionResult reduceSingleResult(final Map<String, RowAnnotation> annotations,
            final InputColumn<?>[] highlightedColumns, final SingleValueDistributionResult reducedResult,
            final SingleValueDistributionResult singleResult) {
        if (reducedResult == null) {
            return singleResult;
        }
        final ValueCountList reducedTopValues = reduceTopValues(reducedResult.getTopValues(),
                singleResult.getTopValues());
        final int reducedDistinctCount = reduceDistinctCount(reducedResult, singleResult);
        final int reducedTotalCount = reducedResult.getTotalCount() + singleResult.getTotalCount();
        final Collection<String> reducedUniqueValues = reduceUniqueValues(reducedResult.getUniqueValues(),
                singleResult.getUniqueValues());

        return new SingleValueDistributionResult(reducedResult.getName(), reducedTopValues, reducedUniqueValues,
                reducedUniqueValues.size(), reducedDistinctCount, reducedTotalCount, annotations,
                new RowAnnotationImpl(), _rowAnnotationFactory, highlightedColumns);
    }

    @SuppressWarnings("unchecked")
    private GroupedValueDistributionResult reduceGroupedResult(final Map<String, RowAnnotation> annotations, final GroupedValueDistributionResult reducedResult,
            final GroupedValueDistributionResult groupedResult) {
        if (reducedResult == null) {
            return groupedResult;
        }
        
        InputColumn<?>[] highlightedColumns = null;

        final Collection<ValueCountingAnalyzerResult> reducedChildResults = new ArrayList<ValueCountingAnalyzerResult>();
        for (ValueCountingAnalyzerResult singleValueCountingResult : groupedResult.getGroupResults()) {
            boolean groupFound = false;
            SingleValueDistributionResult singleResult = (SingleValueDistributionResult) singleValueCountingResult;
            for (ValueCountingAnalyzerResult singleValueCountingReducedResult : reducedResult.getGroupResults()) {
                SingleValueDistributionResult singleReducedResult = (SingleValueDistributionResult) singleValueCountingReducedResult;

                // TODO: Not only name but also grouping column
                if (singleReducedResult.getName().equals(singleResult.getName())) {
                    SingleValueDistributionResult reducedSingleResult = reduceSingleResult(annotations,
                            singleResult.getHighlightedColumns(), singleReducedResult, singleResult);
                    reducedChildResults.add(reducedSingleResult);
                    highlightedColumns = reducedSingleResult.getHighlightedColumns();
                    groupFound = true;
                    break;
                }
            }
            
            if (!groupFound) {
                reducedChildResults.addAll(reducedResult.getGroupResults());
                reducedChildResults.add(singleResult);
                highlightedColumns = singleResult.getHighlightedColumns();
            }
        }
        
        return new GroupedValueDistributionResult(highlightedColumns[0], (InputColumn<String>) highlightedColumns[1],
                reducedChildResults);
    }

    private ValueCountList reduceTopValues(ValueCountList topValues1, ValueCountList topValues2) {
        if (topValues1.getValueCounts().isEmpty()) {
            return topValues2;
        }

        ValueCountListImpl topValuesImpl1 = (ValueCountListImpl) topValues1;
        ValueCountListImpl topValuesImpl2 = (ValueCountListImpl) topValues2;

        // TODO: Check if introduced a bug here (leaving out values from the
        // first set)
        for (ValueFrequency valueFrequency2 : topValuesImpl2.getValueCounts()) {
            for (ValueFrequency valueFrequency1 : topValuesImpl1.getValueCounts()) {
                if (valueFrequency1.getName().equals(valueFrequency2.getName())) {
                    topValuesImpl1.getValueCounts().remove(valueFrequency1);
                    topValuesImpl1.register(new SingleValueFrequency(valueFrequency2.getValue(), valueFrequency2
                            .getCount() + valueFrequency1.getCount()));
                    break;
                }
            }
        }

        return topValuesImpl1;
    }

    private int reduceDistinctCount(ValueDistributionAnalyzerResult reducedResult,
            ValueDistributionAnalyzerResult partialResult) {
        Set<String> distinctValues = new HashSet<>();

        for (ValueFrequency valueFrequency : reducedResult.getValueCounts()) {
            if (valueFrequency instanceof CompositeValueFrequency) {
                CompositeValueFrequency compositeValueFrequency = (CompositeValueFrequency) valueFrequency;

                for (ValueFrequency childValueFrequency : compositeValueFrequency.getChildren()) {
                    distinctValues.add(childValueFrequency.getValue());
                }
            } else {
                distinctValues.add(valueFrequency.getValue());
            }
        }

        for (ValueFrequency valueFrequency : partialResult.getValueCounts()) {
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

    private Collection<String> reduceUniqueValues(Collection<String> uniqueValues1, Collection<String> uniqueValues2) {
        Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
        
        if (uniqueValues1 != null) {
            for (String value : uniqueValues1) {
                if (frequencyMap.containsKey(value)) {
                    frequencyMap.put(value, frequencyMap.get(value) + 1);
                } else {
                    frequencyMap.put(value, 1);
                }
            }
        }
        
        if (uniqueValues2 != null) {
            for (String value : uniqueValues2) {
                if (frequencyMap.containsKey(value)) {
                    frequencyMap.put(value, frequencyMap.get(value) + 1);
                } else {
                    frequencyMap.put(value, 1);
                }
            }
        }
        
        List<String> uniqueList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry: frequencyMap.entrySet()) {
            if (entry.getValue() == 1) {
                uniqueList.add(entry.getKey());
            }
        }
        
        return uniqueList;
    }

}
