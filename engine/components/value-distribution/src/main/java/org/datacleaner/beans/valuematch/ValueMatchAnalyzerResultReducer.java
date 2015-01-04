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
package org.datacleaner.beans.valuematch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.datacleaner.beans.api.Provided;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;
import org.datacleaner.result.AnalyzerResultReducer;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;

/**
 * A reducer of {@link ValueMatchAnalyzerResult}s.
 */
public class ValueMatchAnalyzerResultReducer implements AnalyzerResultReducer<ValueMatchAnalyzerResult> {

    @Inject
    @Provided
    RowAnnotationFactory _rowAnnotationFactory;

    @Override
    public ValueMatchAnalyzerResult reduce(Collection<? extends ValueMatchAnalyzerResult> analyzerResults) {
        final ValueMatchAnalyzerResult firstResult = analyzerResults.iterator().next();

        final InputColumn<?> column = firstResult.getColumn();
        final RowAnnotation nullAnnotation = _rowAnnotationFactory.createAnnotation();
        final RowAnnotation unexpectedValuesAnnotation = _rowAnnotationFactory.createAnnotation();
        final Map<String, RowAnnotation> valueAnnotations = new HashMap<String, RowAnnotation>();

        int totalCount = 0;

        for (ValueMatchAnalyzerResult analyzerResult : analyzerResults) {
            final AnnotatedRowsResult slaveNullAnnotation = analyzerResult.getAnnotatedRowsForNull();
            final AnnotatedRowsResult slaveUnexpectedValuesAnnotation = analyzerResult.getAnnotatedRowsForUnexpectedValues();
            
            totalCount += analyzerResult.getTotalCount();
            reduce(nullAnnotation, slaveNullAnnotation);
            reduce(unexpectedValuesAnnotation, slaveUnexpectedValuesAnnotation);

            final Set<String> expectedValues = analyzerResult.getExpectedValueAnnotations().keySet();
            for (final String expectedValue : expectedValues) {
                final AnnotatedRowsResult annotatedRowsResultForExpectedValue = analyzerResult
                        .getAnnotatedRowsForValue(expectedValue);
                if (annotatedRowsResultForExpectedValue != null) {
                    final int slaveRowCount = annotatedRowsResultForExpectedValue.getAnnotatedRowCount();
                    if (slaveRowCount > 0) {
                        RowAnnotation masterAnnotation = valueAnnotations.get(expectedValue);
                        if (masterAnnotation == null) {
                            masterAnnotation = _rowAnnotationFactory.createAnnotation();
                            valueAnnotations.put(expectedValue, masterAnnotation);
                        }
                        
                        reduce(masterAnnotation, annotatedRowsResultForExpectedValue);
                    }
                }
            }
        }

        final ValueMatchAnalyzerResult result = new ValueMatchAnalyzerResult(column, _rowAnnotationFactory,
                valueAnnotations, nullAnnotation, unexpectedValuesAnnotation, totalCount);
        return result;
    }

    private void reduce(RowAnnotation annotation, AnnotatedRowsResult annotatedRowsResult) {
        if (annotatedRowsResult == null) {
            return;
        }
        final int rowCount = annotatedRowsResult.getAnnotatedRowCount();
        if (rowCount == 0) {
            return;
        }

        final InputRow[] rows = annotatedRowsResult.getRows();
        if (rows.length == rowCount) {
            _rowAnnotationFactory.annotate(rows, annotation);
        } else {
            _rowAnnotationFactory.transferAnnotations(annotatedRowsResult.getAnnotation(), annotation);
        }
    }

}
