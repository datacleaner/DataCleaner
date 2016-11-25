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
package org.datacleaner.data;

import org.apache.metamodel.data.DataSetHeader;
import org.apache.metamodel.data.DefaultRow;
import org.apache.metamodel.data.SimpleDataSetHeader;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.MutableColumn;
import org.datacleaner.api.InputColumn;

import junit.framework.TestCase;

public class TransformedInputRowTest extends TestCase {

    public void testConstaints() throws Exception {
        try {
            new TransformedInputRow(null, 1);
            fail("Exception expected");
        } catch (final IllegalArgumentException e) {
            assertEquals("Delegate cannot be null", e.getMessage());
        }

        final Column col1 = new MutableColumn("foo");
        final InputColumn<?> inputColumn1 = new MetaModelInputColumn(col1);

        final TransformedInputRow row = new TransformedInputRow(new MockInputRow(), 1);

        try {
            row.addValue(inputColumn1, "bar");
            fail("Exception expected");
        } catch (final IllegalArgumentException e) {
            assertEquals("Cannot add physical column values to transformed InputRow.", e.getMessage());
        }
    }

    public void testDelegateOnPhysicalColumn() throws Exception {
        final Column col1 = new MutableColumn("foo");
        final InputColumn<?> inputColumn1 = new MetaModelInputColumn(col1);

        final Column col2 = new MutableColumn("bar");
        final InputColumn<?> inputColumn2 = new MetaModelInputColumn(col2);

        final SelectItem[] selectItems = new SelectItem[] { new SelectItem(col1), new SelectItem(col2) };
        final Object[] values = new Object[] { 1234, 4567 };
        final DataSetHeader header = new SimpleDataSetHeader(selectItems);
        final TransformedInputRow row =
                new TransformedInputRow(new MetaModelInputRow(0, new DefaultRow(header, values)), 1);

        assertEquals(1234, row.getValue(inputColumn1));
        assertEquals(4567, row.getValue(inputColumn2));
    }

    public void testGetValue() throws Exception {
        final InputColumn<String> inputColumn1 = new MockInputColumn<>("foo", String.class);
        final InputColumn<String> inputColumn2 = new MockInputColumn<>("bar", String.class);
        final InputColumn<String> inputColumn3 = new MockInputColumn<>("bar", String.class);

        final TransformedInputRow row1 = new TransformedInputRow(new MockInputRow(), 1);
        row1.addValue(inputColumn1, "f");
        row1.addValue(inputColumn2, "b");
        assertEquals("f", row1.getValue(inputColumn1));
        assertEquals("b", row1.getValue(inputColumn2));
        assertNull(row1.getValue(inputColumn3));
        assertNull(row1.getValue(null));

        final TransformedInputRow row2 = new TransformedInputRow(row1, 1);
        assertEquals("f", row2.getValue(inputColumn1));
        assertEquals("b", row2.getValue(inputColumn2));

        row2.addValue(inputColumn3, "w");

        assertNull(row1.getValue(inputColumn3));
        assertEquals("w", row2.getValue(inputColumn3));
    }
}
