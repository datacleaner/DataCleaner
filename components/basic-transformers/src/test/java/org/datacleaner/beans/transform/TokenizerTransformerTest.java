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
package org.datacleaner.beans.transform;

import org.apache.metamodel.schema.MutableColumn;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.beans.transform.TokenizerTransformer.TokenTarget;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MockInputRow;
import org.easymock.EasyMock;

import junit.framework.TestCase;

public class TokenizerTransformerTest extends TestCase {

    public void testGetOutputColumns() throws Exception {
        final InputColumn<?> col = new MetaModelInputColumn(new MutableColumn("name"));

        @SuppressWarnings("unchecked") final InputColumn<String> castColumn = (InputColumn<String>) col;
        TokenizerTransformer transformer = new TokenizerTransformer(castColumn, 2);

        final OutputColumns oc = transformer.getOutputColumns();

        assertEquals(2, oc.getColumnCount());
        assertEquals("name (token 1)", oc.getColumnName(0));
        assertEquals("name (token 2)", oc.getColumnName(1));

        transformer = new TokenizerTransformer(castColumn, 1);
        assertEquals("OutputColumns[name (token 1)]", transformer.getOutputColumns().toString());

        transformer = new TokenizerTransformer(castColumn, 0);
        try {
            transformer.getOutputColumns();
            fail("Exception expected");
        } catch (final IllegalArgumentException e) {
            assertEquals("Column names length must be 1 or greater", e.getMessage());
        }
    }

    public void testTransformToColumns() throws Exception {
        final InputColumn<?> col = new MetaModelInputColumn(new MutableColumn("name"));

        @SuppressWarnings("unchecked") final TokenizerTransformer transformer =
                new TokenizerTransformer((InputColumn<String>) col, 2);

        assertEquals(2, transformer.getOutputColumns().getColumnCount());

        MockInputRow row = new MockInputRow();
        row.put(col, "Kasper Sorensen");
        String[] values = transformer.transform(row);
        assertEquals(2, values.length);
        assertEquals("Kasper", values[0]);
        assertEquals("Sorensen", values[1]);

        row = new MockInputRow();
        row.put(col, "Kasper ");
        values = transformer.transform(row);
        assertEquals(2, values.length);
        assertEquals("Kasper", values[0]);
        assertNull(values[1]);
    }

    public void testTransformNull() throws Exception {
        final InputColumn<?> col = new MetaModelInputColumn(new MutableColumn("name"));

        @SuppressWarnings("unchecked") final TokenizerTransformer transformer =
                new TokenizerTransformer((InputColumn<String>) col, 2);

        assertEquals(2, transformer.getOutputColumns().getColumnCount());

        final MockInputRow row = new MockInputRow();
        row.put(col, null);
        final String[] values = transformer.transform(row);
        assertEquals(2, values.length);
        assertEquals(null, values[0]);
        assertEquals(null, values[1]);
    }

    public void testTransformToRows() throws Exception {
        final InputColumn<?> col = new MetaModelInputColumn(new MutableColumn("name"));

        @SuppressWarnings("unchecked") final TokenizerTransformer transformer =
                new TokenizerTransformer((InputColumn<String>) col, 1);
        transformer.tokenTarget = TokenTarget.ROWS;
        final OutputRowCollector collectorMock = EasyMock.createMock(OutputRowCollector.class);
        transformer.outputRowCollector = collectorMock;

        assertEquals(1, transformer.getOutputColumns().getColumnCount());
        assertEquals("name (token)", transformer.getOutputColumns().getColumnName(0));

        collectorMock.putValues("Hello");
        collectorMock.putValues("world");

        EasyMock.replay(collectorMock);

        final String[] result = transformer.transform(new MockInputRow().put(col, "Hello world"));
        assertNull(result);

        EasyMock.verify(collectorMock);
    }
}
