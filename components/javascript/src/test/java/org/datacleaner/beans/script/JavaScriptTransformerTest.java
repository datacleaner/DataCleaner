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
package org.datacleaner.beans.script;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;

import junit.framework.TestCase;

public class JavaScriptTransformerTest extends TestCase {

    public void testReturnNull() throws Exception {
        final JavaScriptTransformer t = new JavaScriptTransformer();
        t.setSourceCode("function eval() {return null;}; eval();");
        t.setColumns(new InputColumn[0]);
        t.init();
        final Object object = t.transform(null)[0];
        assertNull(object);
    }

    public void testSimpleScriptExecution() throws Exception {
        final JavaScriptTransformer t = new JavaScriptTransformer();
        t.setSourceCode("function eval() {return 1+1;}; eval();");
        t.setColumns(new InputColumn[0]);
        t.init();
        Object object = t.transform(null)[0];
        assertEquals("2", object.toString());
        assertEquals(String.class, object.getClass());

        t.returnType = JavaScriptTransformer.ReturnType.NUMBER;
        object = t.transform(null)[0];
        assertEquals("2.0", object.toString());
        assertEquals(Double.class, object.getClass());

        assertEquals(Number.class, t.getOutputColumns().getColumnType(0));
    }

    /**
     * Tests that you can use the 'out' variable in JS to print to the console
     *
     * @throws Exception
     */
    public void testSharedScopedVariables() throws Exception {
        final JavaScriptTransformer t = new JavaScriptTransformer();
        t.setSourceCode("function eval() {out.print(\"hello world\"); return 1+1;}; eval();");
        t.setColumns(new InputColumn[0]);

        final PrintStream oldOut = System.out;

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final PrintStream newOut = new PrintStream(byteArrayOutputStream);
        System.setOut(newOut);
        t.init();
        t.transform(null);
        System.setOut(oldOut);

        newOut.flush();
        newOut.close();
        final byte[] byteArray = byteArrayOutputStream.toByteArray();
        assertEquals("hello world", new String(byteArray));
    }

    /**
     * Tests that adding two numbers will mathematicall add them and not concat
     * them as strings
     *
     * @throws Exception
     */
    public void testAddNumberTypes() throws Exception {
        final JavaScriptTransformer t = new JavaScriptTransformer();
        t.setSourceCode("function eval() {return values[0] + 2;}; eval();");
        final InputColumn<Number> col = new MockInputColumn<>("my number", Number.class);
        t.setColumns(new InputColumn[] { col });

        t.init();

        assertEquals("125", t.transform(new MockInputRow().put(col, 123))[0]);

        assertEquals("3", t.transform(new MockInputRow().put(col, 1.0))[0]);
        assertEquals("3.5", t.transform(new MockInputRow().put(col, 1.5))[0]);
    }

    public void testSimpleScriptParseIntExecution() throws Exception {
        final JavaScriptTransformer t = new JavaScriptTransformer();
        t.setSourceCode("function eval() {return parseInt(values[0], 10)}; eval();");
        final InputColumn<String> col = new MockInputColumn<>("my number", String.class);
        t.setColumns(new InputColumn[] { col });

        t.init();

        assertEquals("123", t.transform(new MockInputRow().put(col, 123))[0]);

        assertEquals("1", t.transform(new MockInputRow().put(col, 1.0))[0]);
        assertEquals("1", t.transform(new MockInputRow().put(col, 1.5))[0]);
        assertEquals("10", t.transform(new MockInputRow().put(col, "010"))[0]);
        //the Number cannot be parsed because it starts with letter 'O'
        assertEquals("NaN", t.transform(new MockInputRow().put(col, "O10"))[0]);
    }
}
