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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.storage.InMemoryRowAnnotationFactory2;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.junit.Test;

public class MarkRowsAnalyzerResultReducerTest {

    private final MarkRowsAnalyzerResultReducer reducer = new MarkRowsAnalyzerResultReducer();

    @Test
    public void testReduceNone() throws Exception {
        final MarkRowsAnalyzerResult result = reducer.reduce(Collections.emptyList());
        assertEquals(0, result.getTotalRowCount());
        assertTrue(result.getSampleRows().isEmpty());
    }

    @Test
    public void testReduceSums() throws Exception {
        final MarkRowsAnalyzerResult result1 = createResult();
        final MarkRowsAnalyzerResult result2 = createResult();

        final MarkRowsAnalyzerResult result = reducer.reduce(Arrays.asList(result1, result2));
        assertEquals(2, result.getAnnotatedRowCount());
        assertTrue(result.getSampleRows().isEmpty());
    }

    private MarkRowsAnalyzerResult createResult() {
        final RowAnnotationFactory annotationFactory = new InMemoryRowAnnotationFactory2();
        final RowAnnotation annotation = annotationFactory.createAnnotation();
        final InputColumn<?> col1 = new MockInputColumn<String>("foo");
        final InputColumn<?> col2 = new MockInputColumn<Number>("bar");
        final InputColumn<?>[] highlightedColumns = new InputColumn[] { col1, col2 };

        annotationFactory.annotate(new MockInputRow().put(col1, "hello world").put(col2, 42), annotation);

        return new MarkRowsAnalyzerResult(annotation, annotationFactory, highlightedColumns);
    }
}
