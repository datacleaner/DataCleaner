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
package org.datacleaner.beans;

import org.datacleaner.beans.api.Description;
import org.datacleaner.beans.api.Distributed;
import org.datacleaner.data.InputColumn;
import org.datacleaner.result.AnalyzerResult;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.Metric;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;

/**
 * {@link AnalyzerResult} class for {@link CompletenessAnalyzer}.
 */
@Description("Incomplete records")
@Distributed(reducer=CompletenessAnalyzerResultReducer.class)
public class CompletenessAnalyzerResult extends AnnotatedRowsResult implements AnalyzerResult {

    private static final long serialVersionUID = 1L;
    private final int _rowCount;

    public CompletenessAnalyzerResult(int rowCount, RowAnnotation annotation, RowAnnotationFactory annotationFactory,
            InputColumn<?>[] highlightedColumns) {
        super(annotation, annotationFactory, highlightedColumns);
        _rowCount = rowCount;
    }

    @Metric(order = 1, value = "Row count")
    public int getTotalRowCount() {
        return _rowCount;
    }

    @Metric(order = 2, value = "Valid row count")
    public int getValidRowCount() {
        return _rowCount - getInvalidRowCount();
    }

    @Metric(order = 3, value = "Invalid row count")
    public int getInvalidRowCount() {
        return getAnnotation().getRowCount();
    }
}
