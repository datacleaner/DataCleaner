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
package org.eobjects.analyzer.result;

import java.io.Serializable;

import junit.framework.TestCase;

public class CrosstabTest extends TestCase {

	public void testCastValueClass() throws Exception {
		Crosstab<String> c1 = new Crosstab<String>(String.class, "foo", "bar");
		c1.where("foo", "a").where("bar", "b").put("yes", true);

		Crosstab<Serializable> c2 = c1.castValueClass(Serializable.class);
		try {
			c2.where("foo", "a").where("bar", "b").put(3l);
			fail("Excepted exception");
		} catch (IllegalArgumentException e) {
			assertEquals(
					"Cannot put value [3] of type [class java.lang.Long] when Crosstab.valueClass is [class java.lang.String]",
					e.getMessage());
		}

		try {
			c2.castValueClass(Number.class);
			fail("Excepted exception");
		} catch (IllegalArgumentException e) {
			assertEquals(
					"Unable to cast [class java.lang.String] to [class java.lang.Number]",
					e.getMessage());
		}

	}
	
	public void testGetAndSafeGet() throws Exception {
	    Crosstab<String> c1 = new Crosstab<String>(String.class, "foo", "bar");
        CrosstabNavigator<String> nav = c1.where("foo", "a").where("bar", "b");
        nav.put("yes", true);
        
        assertEquals("yes", nav.get());
        assertEquals("yes", nav.safeGet("hello world"));
        
        nav = nav.where("foo", "b");
        
        try {
            nav.get();
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown category [b] for dimension [foo]", e.getMessage());
        }
        assertEquals("hello world", nav.safeGet("hello world"));
    }
}
