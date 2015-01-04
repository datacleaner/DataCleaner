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
package org.eobjects.analyzer.data;

import junit.framework.TestCase;

import org.apache.metamodel.data.DataSetHeader;
import org.apache.metamodel.data.DefaultRow;
import org.apache.metamodel.data.SimpleDataSetHeader;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.MutableColumn;

public class TransformedInputRowTest extends TestCase {

	public void testConstaints() throws Exception {
		try {
			new TransformedInputRow(null);
			fail("Exception expected");
		} catch (IllegalArgumentException e) {
			assertEquals("Delegate cannot be null", e.getMessage());
		}

		Column col1 = new MutableColumn("foo");
		InputColumn<?> inputColumn1 = new MetaModelInputColumn(col1);

		TransformedInputRow row = new TransformedInputRow(new MockInputRow());

		try {
			row.addValue(inputColumn1, "bar");
			fail("Exception expected");
		} catch (IllegalArgumentException e) {
			assertEquals("Cannot add physical column values to transformed InputRow.", e.getMessage());
		}
	}

	public void testDelegateOnPhysicalColumn() throws Exception {
		Column col1 = new MutableColumn("foo");
		InputColumn<?> inputColumn1 = new MetaModelInputColumn(col1);

		Column col2 = new MutableColumn("bar");
		InputColumn<?> inputColumn2 = new MetaModelInputColumn(col2);

		SelectItem[] selectItems = new SelectItem[] { new SelectItem(col1), new SelectItem(col2) };
		Object[] values = new Object[] { 1234, 4567 };
		DataSetHeader header = new SimpleDataSetHeader(selectItems);
		TransformedInputRow row = new TransformedInputRow(new MetaModelInputRow(0, new DefaultRow(header, values)));

		assertEquals(1234, row.getValue(inputColumn1));
		assertEquals(4567, row.getValue(inputColumn2));
	}

	public void testGetValue() throws Exception {
		InputColumn<String> inputColumn1 = new MockInputColumn<String>("foo", String.class);
		InputColumn<String> inputColumn2 = new MockInputColumn<String>("bar", String.class);
		InputColumn<String> inputColumn3 = new MockInputColumn<String>("bar", String.class);

		TransformedInputRow row1 = new TransformedInputRow(new MockInputRow());
		row1.addValue(inputColumn1, "f");
		row1.addValue(inputColumn2, "b");
		assertEquals("f", row1.getValue(inputColumn1));
		assertEquals("b", row1.getValue(inputColumn2));
		assertNull(row1.getValue(inputColumn3));
		assertNull(row1.getValue(null));

		TransformedInputRow row2 = new TransformedInputRow(row1);
		assertEquals("f", row2.getValue(inputColumn1));
		assertEquals("b", row2.getValue(inputColumn2));

		row2.addValue(inputColumn3, "w");

		assertNull(row1.getValue(inputColumn3));
		assertEquals("w", row2.getValue(inputColumn3));
	}
}
