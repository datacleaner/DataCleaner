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
package org.datacleaner.result;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import org.datacleaner.api.AnalyzerResultReducer;
import org.datacleaner.api.Provided;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;

/**
 * Result reducer for {@link CategorizationResult}.
 */
public class CategorizationResultReducer implements AnalyzerResultReducer<CategorizationResult> {

    @Inject
    @Provided
    RowAnnotationFactory _rowAnnotationFactory;

    @Override
    public CategorizationResult reduce(Collection<? extends CategorizationResult> results) {
        RowAnnotationFactory annotationFactory = null;
        Map<String, RowAnnotation> reducedCategories = new LinkedHashMap<>();
        for (CategorizationResult result : results) {

            final Collection<String> categoryNames = result.getCategoryNames();
            for (String categoryName : categoryNames) {
                final RowAnnotation partialAnnotation = result.getCategoryRowAnnotation(categoryName);

                final RowAnnotation reducedAnnotation = reducedCategories.get(categoryName);
                if (reducedAnnotation == null) {
                    // adopt the annotation from the partial result
                    final RowAnnotation annotation = _rowAnnotationFactory.createAnnotation();
                    _rowAnnotationFactory.transferAnnotations(partialAnnotation, annotation);

                    reducedCategories.put(categoryName, annotation);
                } else {
                    // add records to the existing annotation
                    _rowAnnotationFactory.transferAnnotations(partialAnnotation, reducedAnnotation);
                }
            }
        }
        return new CategorizationResult(annotationFactory, reducedCategories);
    }

}
