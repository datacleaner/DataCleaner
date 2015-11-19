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

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.datacleaner.api.OutputColumns;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.test.MockOutputRowCollector;

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
    
    public void testExpressionForDimensions() throws Exception {
        MockInputColumn<String> col = new MockInputColumn<String>("foobar", String.class);

        RegexParserTransformer t = new RegexParserTransformer();
        t.column = col;
        t.pattern = Pattern.compile("(\\d+\\,?\\d+?)(x|X)([0-9]+\\,?\\d+?)");
        t.mode = RegexParserTransformer.Mode.FIND_FIRST;

        assertEquals("[12x34, 12, x, 34]", Arrays.toString(t.transform(new MockInputRow().put(col, "foo 12x34 bar"))));
        assertEquals("[12X34, 12, X, 34]", Arrays.toString(t.transform(new MockInputRow().put(col, "foo 12X34 bar"))));
        assertEquals("[1,2x3,4, 1,2, x, 3,4]", Arrays.toString(t.transform(new MockInputRow().put(col, "foo 1,2x3,4 bar"))));
    }
    
    public void testFindAllMultiMatch() throws Exception {
        final MockOutputRowCollector outputRowCollector = new MockOutputRowCollector();
        final MockInputColumn<String> col = new MockInputColumn<String>("foobar", String.class);

        final RegexParserTransformer t = new RegexParserTransformer();
        t.column = col;
        t.pattern = Pattern.compile("(\\d+\\,?\\d*)(x|X)(\\d+\\,?\\d*)");
        t.mode = RegexParserTransformer.Mode.FIND_ALL;
        t.outputRowCollector = outputRowCollector;

        final String[] transformResult1 = t.transform(new MockInputRow().put(col, "foo 12x34 bar 56x78 baz 9x10 "));
        assertEquals("[12x34, 12, x, 34]", Arrays.toString(transformResult1));
        
        final List<Object[]> output1 = outputRowCollector.getOutput();
        assertEquals(2, output1.size());
        
        assertEquals("[56x78, 56, x, 78]", Arrays.toString(output1.get(0)));
        assertEquals("[9x10, 9, x, 10]", Arrays.toString(output1.get(1)));
    }
    
    public void testFindAllNoMatch() throws Exception {
        final MockOutputRowCollector outputRowCollector = new MockOutputRowCollector();
        final MockInputColumn<String> col = new MockInputColumn<String>("foobar", String.class);

        final RegexParserTransformer t = new RegexParserTransformer();
        t.column = col;
        t.pattern = Pattern.compile("(\\d+\\,?\\d*)(x|X)(\\d+\\,?\\d*)");
        t.mode = RegexParserTransformer.Mode.FIND_ALL;
        t.outputRowCollector = outputRowCollector;

        final String[] transformResult1 = t.transform(new MockInputRow().put(col, "foo"));
        assertEquals("[null, null, null, null]", Arrays.toString(transformResult1));
        
        final List<Object[]> output1 = outputRowCollector.getOutput();
        assertEquals(0, output1.size());
    }
}
