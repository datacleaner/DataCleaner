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
import java.util.Map;

import javax.inject.Inject;

import org.datacleaner.api.AnalyzerResultReducer;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Provided;
import org.datacleaner.result.ValueCountList;
import org.datacleaner.result.ValueCountListImpl;
import org.datacleaner.storage.InMemoryRowAnnotationFactory;
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
        final RowAnnotationFactory annotationFactory = new InMemoryRowAnnotationFactory();
        final InputColumn<?>[] highlightedColumns = null;

        SingleValueDistributionResult reducedResult = new SingleValueDistributionResult("", topValues, uniqueValues, 0,
                0, 0, annotations, new RowAnnotationImpl(), annotationFactory, highlightedColumns);

        for (ValueDistributionAnalyzerResult partialResult : analyzerResults) {
            if (partialResult instanceof SingleValueDistributionResult) {
                int reducedTotalCount = reducedResult.getTotalCount() + partialResult.getTotalCount();
                Collection<String> reducedUniqueValues = reduceUniqueValues(reducedResult.getUniqueValues(),
                        partialResult.getUniqueValues());

                reducedResult = new SingleValueDistributionResult(reducedResult.getName(),
                        reducedResult.getTopValues(), reducedUniqueValues, reducedUniqueValues.size(),
                        reducedResult.getDistinctCount(), reducedTotalCount, annotations, new RowAnnotationImpl(),
                        annotationFactory, highlightedColumns);
            } else {
                // TODO: Disregard grouped results for now
            }
        }

        return reducedResult;
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
