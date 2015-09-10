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
import java.util.HashSet;
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
        final ValueCountList topValues = ValueCountListImpl.emptyList();
        final Collection<String> uniqueValues = Collections.emptyList();
        final Map<String, RowAnnotation> annotations = Collections.emptyMap();
        final InputColumn<?>[] highlightedColumns = null;

        SingleValueDistributionResult reducedResult = new SingleValueDistributionResult(null, topValues, uniqueValues,
                0, 0, 0, annotations, new RowAnnotationImpl(), _rowAnnotationFactory, highlightedColumns);

        for (ValueDistributionAnalyzerResult partialResult : analyzerResults) {
            if (partialResult instanceof SingleValueDistributionResult) {
                final SingleValueDistributionResult singleResult = (SingleValueDistributionResult) partialResult;
                reducedResult = reduceSingleResult(annotations, highlightedColumns, reducedResult, singleResult);
            } else if (partialResult instanceof GroupedValueDistributionResult) {
                GroupedValueDistributionResult groupedResult = (GroupedValueDistributionResult) partialResult;

                for (ValueCountingAnalyzerResult valueCountingResult : groupedResult.getGroupResults()) {
                    if (valueCountingResult instanceof SingleValueDistributionResult) {
                        SingleValueDistributionResult singleResult = (SingleValueDistributionResult) valueCountingResult;
                        reducedResult = reduceSingleResult(annotations, highlightedColumns, reducedResult, singleResult);
                    } else {
                        throw new IllegalStateException("Expected "
                                + SingleValueDistributionResult.class.getSimpleName() + ", but encountered "
                                + valueCountingResult.getClass().getSimpleName());
                    }
                }

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

    private ValueCountList reduceTopValues(ValueCountList topValues1, ValueCountList topValues2) {
        if (topValues1.getValueCounts().isEmpty()) {
            return topValues2;
        }

        ValueCountListImpl topValuesImpl1 = (ValueCountListImpl) topValues1;
        ValueCountListImpl topValuesImpl2 = (ValueCountListImpl) topValues2;

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
        if ((uniqueValues1 == null) || uniqueValues1.isEmpty()) {
            return uniqueValues2;
        }

        Collection<String> reducedUniqueValues = new ArrayList<>();

        if (uniqueValues1.size() >= uniqueValues2.size()) {
            for (String value : uniqueValues1) {
                if (!uniqueValues2.contains(value)) {
                    reducedUniqueValues.add(value);
                }
            }
        } else {
            for (String value : uniqueValues2) {
                if (!uniqueValues1.contains(value)) {
                    reducedUniqueValues.add(value);
                }
            }
        }

        return reducedUniqueValues;
    }

}
