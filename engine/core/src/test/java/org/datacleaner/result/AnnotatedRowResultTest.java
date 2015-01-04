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

import java.util.Arrays;

import javax.swing.table.TableModel;

import org.apache.commons.lang.SerializationUtils;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.storage.InMemoryRowAnnotationFactory;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;

import junit.framework.TestCase;

public class AnnotatedRowResultTest extends TestCase {

	public void testSerializeAndDeserialize() throws Exception {
		RowAnnotationFactory annotationFactory = new InMemoryRowAnnotationFactory();
		RowAnnotation annotation = annotationFactory.createAnnotation();
		InputColumn<String> col1 = new MockInputColumn<String>("foo", String.class);
		InputColumn<String> col2 = new MockInputColumn<String>("bar", String.class);

		annotationFactory.annotate(new MockInputRow().put(col1, "1").put(col2, "2"), 1, annotation);
		annotationFactory.annotate(new MockInputRow().put(col1, "3").put(col2, "4"), 1, annotation);

		AnnotatedRowsResult result1 = new AnnotatedRowsResult(annotation, annotationFactory, col1);
		performAssertions(result1);

		AnnotatedRowsResult result2 = (AnnotatedRowsResult) SerializationUtils.deserialize(SerializationUtils
				.serialize(result1));
		performAssertions(result2);
	}

	private void performAssertions(AnnotatedRowsResult result) {
		assertEquals(2, result.getAnnotatedRowCount());
		assertEquals("[MockInputColumn[name=foo], MockInputColumn[name=bar]]", result.getInputColumns().toString());
		assertEquals("[MockInputColumn[name=foo]]", Arrays.toString(result.getHighlightedColumns()));
		assertNotNull(result.getRows());
		TableModel tableModel = result.toTableModel();
		assertNotNull(tableModel);
		assertEquals(2, tableModel.getColumnCount());
		assertEquals("foo", tableModel.getColumnName(0));
		assertEquals("bar", tableModel.getColumnName(1));

		tableModel = result.toDistinctValuesTableModel(result.getHighlightedColumns()[0]);
		assertNotNull(tableModel);
		assertEquals(2, tableModel.getColumnCount());
		assertEquals("foo", tableModel.getColumnName(0));
		assertEquals("Count in dataset", tableModel.getColumnName(1));
	}
}
