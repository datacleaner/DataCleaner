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
package org.datacleaner.components.categories;

import junit.framework.TestCase;

public class AbstractComponentCategoryTest extends TestCase {

	public void testGetName() throws Exception {
		assertEquals("Scripting", new ScriptingCategory().getName());
		assertEquals("Text", new TextCategory().getName());
		assertEquals("Text", new TextCategory().toString());
		
		assertEquals("Conversion", new ConversionCategory().getName());
		assertEquals("Data structures", new DataStructuresCategory().getName());
		assertEquals("Date and time", new DateAndTimeCategory().getName());
		assertEquals("Filter", new FilterCategory().getName());
		assertEquals("Contact data", new ContactDataCategory().getName());
		assertEquals("Numbers", new NumbersCategory().getName());
		assertEquals("Scripting", new ScriptingCategory().getName());
		assertEquals("Validation", new ValidationCategory().getName());
	}
	
	public void testEquals() throws Exception {
        assertEquals(new TextCategory(), new TextCategory());
        assertEquals(new TextCategory().hashCode(), new TextCategory().hashCode());
        assertFalse(new NumbersCategory().equals(new TextCategory()));
        assertFalse(new NumbersCategory().hashCode() == new TextCategory().hashCode());
    }
}
