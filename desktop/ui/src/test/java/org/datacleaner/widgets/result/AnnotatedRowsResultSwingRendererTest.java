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
package org.datacleaner.widgets.result;

import org.datacleaner.api.InputColumn;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.storage.RowAnnotations;
import org.datacleaner.user.UserPreferencesImpl;
import org.datacleaner.widgets.result.AnnotatedRowsResultSwingRenderer.AnnotatedRowResultPanel;
import org.datacleaner.widgets.table.DCTable;

import junit.framework.TestCase;

public class AnnotatedRowsResultSwingRendererTest extends TestCase {

    public void testInitialViewState() throws Exception {
        final AnnotatedRowsResultSwingRenderer renderer = new AnnotatedRowsResultSwingRenderer();
        renderer.userPreferences = new UserPreferencesImpl(null);
        renderer.datastoreCatalog = new DatastoreCatalogImpl();

        final RowAnnotationFactory annotationFactory = RowAnnotations.getDefaultFactory();
        final RowAnnotation annotation = annotationFactory.createAnnotation();

        final MockInputColumn<String> colFoo = new MockInputColumn<>("foo", String.class);
        final MockInputColumn<String> colBar = new MockInputColumn<>("bar", String.class);
        final MockInputColumn<String> colBaz = new MockInputColumn<>("baz", String.class);

        final MockInputRow row1 = new MockInputRow().put(colFoo, "1").put(colBar, "2").put(colBaz, "3");
        annotationFactory.annotate(row1, 1, annotation);

        final MockInputRow row2 = new MockInputRow().put(colFoo, "4").put(colBar, "5").put(colBaz, "6");
        annotationFactory.annotate(row2, 1, annotation);

        // test multiple highlighted columns
        InputColumn<?>[] highlightedColumns = new InputColumn[] { colFoo, colBaz };

        AnnotatedRowResultPanel panel =
                renderer.render(new AnnotatedRowsResult(annotation, annotationFactory, highlightedColumns));

        DCTable table = panel.getTable();
        assertEquals(2, table.getRowCount());
        assertEquals(3, table.getColumnCount());
        assertEquals("1", table.getValueAt(0, 0));
        assertEquals("2", table.getValueAt(0, 1));
        assertEquals("3", table.getValueAt(0, 2));

        // test single highlighted column
        highlightedColumns = new InputColumn[] { colFoo };

        panel = renderer.render(new AnnotatedRowsResult(annotation, annotationFactory, highlightedColumns));

        table = panel.getTable();
        assertEquals(2, table.getRowCount());
        assertEquals(3, table.getColumnCount());
        assertEquals("1", table.getValueAt(0, 0));
        assertEquals("2", table.getValueAt(0, 1));
        assertEquals("3", table.getValueAt(0, 2));
    }
}
