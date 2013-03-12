/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.widgets.result;

import junit.framework.TestCase;

import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.eobjects.datacleaner.user.UserPreferencesImpl;
import org.eobjects.datacleaner.widgets.result.AnnotatedRowsResultSwingRenderer.AnnotatedRowResultPanel;
import org.eobjects.datacleaner.widgets.table.DCTable;

public class AnnotatedRowsResultSwingRendererTest extends TestCase {

	public void testInitialViewState() throws Exception {
		AnnotatedRowsResultSwingRenderer renderer = new AnnotatedRowsResultSwingRenderer();
		renderer.usageLogger = null;
		renderer.userPreferences = new UserPreferencesImpl(null);
		renderer.datastoreCatalog = new DatastoreCatalogImpl();

		RowAnnotationFactory annotationFactory = new InMemoryRowAnnotationFactory();
		RowAnnotation annotation = annotationFactory.createAnnotation();

		MockInputColumn<String> colFoo = new MockInputColumn<String>("foo", String.class);
		MockInputColumn<String> colBar = new MockInputColumn<String>("bar", String.class);
		MockInputColumn<String> colBaz = new MockInputColumn<String>("baz", String.class);

		MockInputRow row1 = new MockInputRow().put(colFoo, "1").put(colBar, "2").put(colBaz, "3");
		annotationFactory.annotate(row1, 1, annotation);

		MockInputRow row2 = new MockInputRow().put(colFoo, "4").put(colBar, "5").put(colBaz, "6");
		annotationFactory.annotate(row2, 1, annotation);

		// test multiple highlighted columns
		InputColumn<?>[] highlightedColumns = new InputColumn[] { colFoo, colBaz };

		AnnotatedRowResultPanel panel = renderer.render(new AnnotatedRowsResult(annotation, annotationFactory,
				highlightedColumns));

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
