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
package org.datacleaner.sample;

import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;

import junit.framework.TestCase;

/**
 * TestCase for the {@link HelloWorldTransformer} class.
 * 
 * Testing a transformer is pretty much straight-forward. You will typically
 * need to use {@link MockInputColumn} and {@link MockInputRow} to represent the
 * data that you feed to the transformer.
 * 
 * @author Kasper Sørensen
 * 
 */
public class HelloWorldTransformerTest extends TestCase {

	public void testSingleGreeting() throws Exception {
		MockInputColumn<String> col = new MockInputColumn<String>("Name", String.class);

		HelloWorldTransformer transformer = new HelloWorldTransformer();
		transformer.nameColumn = col;
		transformer.greetings = new String[] { "Hello" };

		assertEquals("Name (greeting)", transformer.getOutputColumns().getColumnName(0));

		assertEquals("Hello Tom", transformer.transform(new MockInputRow().put(col, "Tom"))[0]);
		assertEquals("Hello Martin", transformer.transform(new MockInputRow().put(col, "Martin"))[0]);
		assertEquals("Hello Jesse", transformer.transform(new MockInputRow().put(col, "Jesse"))[0]);
	}

	public void testRandomGreeting() throws Exception {
		MockInputColumn<String> col = new MockInputColumn<String>("Name", String.class);

		HelloWorldTransformer transformer = new HelloWorldTransformer();
		transformer.nameColumn = col;
		transformer.greetings = new String[] { "Hello", "Hi" };

		assertEquals("Name (greeting)", transformer.getOutputColumns().getColumnName(0));

		String greetingLine = transformer.transform(new MockInputRow().put(col, "Tom"))[0];
		assertTrue(greetingLine.endsWith(" Tom"));
		assertTrue(greetingLine.startsWith("Hello") || greetingLine.startsWith("Hi"));
	}
}
