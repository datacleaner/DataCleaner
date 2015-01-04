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

public class ELInputColumnTest extends TestCase {

	public void testPhysicalColumn() throws Exception {
		ELInputColumn elCol = new ELInputColumn("Hello #{foo}");
		assertFalse(elCol.isPhysicalColumn());
	}

	public void testSimpleExpression() throws Exception {
		ELInputColumn elCol = new ELInputColumn("Hello #{foo}");

		MockInputColumn<String> fooCol = new MockInputColumn<String>("foo", String.class);
		assertEquals("Hello World", elCol.evaluate(new MockInputRow().put(fooCol, "World")));
	}

	public void testVariableWithWhitespace() throws Exception {
		ELInputColumn elCol = new ELInputColumn("Hello #{foo_bar}");

		MockInputColumn<String> fooCol = new MockInputColumn<String>("foo bar", String.class);
		assertEquals("Hello World", elCol.evaluate(new MockInputRow().put(fooCol, "World")));
	}
}
