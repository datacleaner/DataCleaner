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
package org.datacleaner.util;

import junit.framework.TestCase;

public class ImmutableEntryTest extends TestCase {

	public void testEntryMethods() throws Exception {
		final ImmutableEntry<String, Integer> e1 = new ImmutableEntry<String, Integer>("hi", 45);
		assertEquals("hi", e1.getKey());
		assertEquals(45, e1.getValue().intValue());

		try {
			e1.setValue(55);
			fail("Exception expected");
		} catch (UnsupportedOperationException e) {
			assertNull(e.getMessage());
		}

		final ImmutableEntry<String, Integer> e2 = new ImmutableEntry<String, Integer>("hi", 45);
		assertEquals(e1.hashCode(), e2.hashCode());
		assertEquals(e1.toString(), e2.toString());
		assertEquals(e1, e2);
	}
}
