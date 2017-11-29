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
package org.datacleaner.components.annotate;

import java.util.Collection;

import org.datacleaner.api.AnalyzerResultReducer;
import org.datacleaner.api.InputColumn;
import org.datacleaner.storage.RowAnnotationImpl;

public class MarkRowsAnalyzerResultReducer implements AnalyzerResultReducer<MarkRowsAnalyzerResult> {

    @Override
    public MarkRowsAnalyzerResult reduce(Collection<? extends MarkRowsAnalyzerResult> results) {
        InputColumn<?>[] highlightedColumns = null;
        final RowAnnotationImpl annotation = new RowAnnotationImpl();
        for (MarkRowsAnalyzerResult result : results) {
            final int annotatedRowCount = result.getAnnotatedRowCount();
            annotation.incrementRowCount(annotatedRowCount);
            if (highlightedColumns == null || highlightedColumns.length == 0) {
                highlightedColumns = result.getHighlightedColumns();
            }
        }
        return new MarkRowsAnalyzerResult(annotation, null, highlightedColumns);
    }

}
