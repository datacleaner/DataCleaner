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
        ValueCountList topValues = ValueCountListImpl.emptyList();
        Collection<String> uniqueValues = Collections.emptyList();
        final Map<String, RowAnnotation> annotations = Collections.emptyMap();
        final InMemoryRowAnnotationFactory annotationFactory = new InMemoryRowAnnotationFactory();
        InputColumn<?>[] highlightedColumns = null;

        SingleValueDistributionResult reducedResult = new SingleValueDistributionResult("", topValues, uniqueValues, 0,
                0, 0, annotations, new RowAnnotationImpl(), annotationFactory, highlightedColumns);

        for (ValueDistributionAnalyzerResult partialResult : analyzerResults) {
            if (partialResult instanceof SingleValueDistributionResult) {
                reducedResult = new SingleValueDistributionResult(reducedResult.getName(),
                        reducedResult.getTopValues(), reducedResult.getUniqueValues(), reducedResult.getUniqueCount(),
                        reducedResult.getDistinctCount(),
                        reducedResult.getTotalCount() + partialResult.getTotalCount(), annotations,
                        new RowAnnotationImpl(), annotationFactory, highlightedColumns);
            } else {
                // TODO: Disregard grouped results for now
            }
        }

        return reducedResult;
    }

}
