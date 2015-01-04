/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.beans.script;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

public class JavaScriptTransformerTest extends TestCase {
	
	public void testReturnNull() throws Exception {
		JavaScriptTransformer t = new JavaScriptTransformer();
		t.setSourceCode("function eval() {return null;}; eval();");
		t.setColumns(new InputColumn[0]);
		t.init();
		Object object = t.transform(null)[0];	
		assertNull(object);
	}

	public void testSimpleScriptExecution() throws Exception {
		JavaScriptTransformer t = new JavaScriptTransformer();
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
		JavaScriptTransformer t = new JavaScriptTransformer();
		t.setSourceCode("function eval() {out.print(\"hello world\"); return 1+1;}; eval();");
		t.setColumns(new InputColumn[0]);

		PrintStream oldOut = System.out;

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		PrintStream newOut = new PrintStream(byteArrayOutputStream);
		System.setOut(newOut);
		t.init();
		t.transform(null);
		System.setOut(oldOut);

		newOut.flush();
		newOut.close();
		byte[] byteArray = byteArrayOutputStream.toByteArray();
		assertEquals("hello world", new String(byteArray));
	}

	/**
	 * Tests that adding two numbers will mathematicall add them and not concat
	 * them as strings
	 * 
	 * @throws Exception
	 */
	public void testAddNumberTypes() throws Exception {
		JavaScriptTransformer t = new JavaScriptTransformer();
		t.setSourceCode("function eval() {return values[0] + 2;}; eval();");
		InputColumn<Number> col = new MockInputColumn<Number>("my number",
				Number.class);
		t.setColumns(new InputColumn[] { col });

		t.init();

		assertEquals("125", t.transform(new MockInputRow().put(col, 123))[0]);

		assertEquals("3", t.transform(new MockInputRow().put(col, 1.0))[0]);
		assertEquals("3.5", t.transform(new MockInputRow().put(col, 1.5))[0]);
	}
}
