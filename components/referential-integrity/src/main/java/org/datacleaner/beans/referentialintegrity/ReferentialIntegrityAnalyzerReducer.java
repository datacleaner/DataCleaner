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
package org.datacleaner.beans.referentialintegrity;

import java.util.Collection;
import java.util.List;

import org.datacleaner.api.AnalyzerResultReducer;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.storage.InMemoryRowAnnotationFactory2;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationImpl;

public class ReferentialIntegrityAnalyzerReducer implements AnalyzerResultReducer<ReferentialIntegrityAnalyzerResult> {

    @Override
    public ReferentialIntegrityAnalyzerResult reduce(final Collection<? extends ReferentialIntegrityAnalyzerResult> partialResults) {
        if (partialResults.isEmpty()) {
            return null;
        }
        
        final RowAnnotation reducerAnnotation = new RowAnnotationImpl();
        final InMemoryRowAnnotationFactory2 reducerAnnotationFactory = new InMemoryRowAnnotationFactory2();
        InputColumn<?>[] highlightedColumns = null;
        
        for (ReferentialIntegrityAnalyzerResult partialResult : partialResults) {
            final List<InputRow> partialRows = partialResult.getSampleRows();
            for (InputRow partialRow : partialRows) {
                reducerAnnotationFactory.annotate(partialRow, reducerAnnotation);
            }
            highlightedColumns = partialResult.getHighlightedColumns();
        }
        
        return new ReferentialIntegrityAnalyzerResult(reducerAnnotation, reducerAnnotationFactory, highlightedColumns);
    }

}
