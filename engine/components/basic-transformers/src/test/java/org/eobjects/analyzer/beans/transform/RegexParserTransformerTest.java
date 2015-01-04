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
package org.eobjects.analyzer.beans.transform;

import static org.junit.Assert.assertArrayEquals;

import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

public class RegexParserTransformerTest extends TestCase {

    public void testTransform() throws Exception {
        MockInputColumn<String> col = new MockInputColumn<String>("foobar", String.class);

        RegexParserTransformer t = new RegexParserTransformer();
        t.column = col;
        t.pattern = Pattern.compile("(a+)(b+)|(c+)");

        OutputColumns outputColumns = t.getOutputColumns();
        assertEquals(4, outputColumns.getColumnCount());
        assertEquals("foobar (matched part)", outputColumns.getColumnName(0));
        assertEquals("foobar (group 1)", outputColumns.getColumnName(1));
        assertEquals("foobar (group 2)", outputColumns.getColumnName(2));
        assertEquals("foobar (group 3)", outputColumns.getColumnName(3));

        assertArrayEquals(new String[] { "aabb", "aa", "bb", null }, t.transform(new MockInputRow().put(col, "aabb")));
        assertArrayEquals(new String[] { "cccc", null, null, "cccc" }, t.transform(new MockInputRow().put(col, "cccc")));
        assertArrayEquals(new String[] { null, null, null, null }, t.transform(new MockInputRow().put(col, "dddd")));
    }
}
