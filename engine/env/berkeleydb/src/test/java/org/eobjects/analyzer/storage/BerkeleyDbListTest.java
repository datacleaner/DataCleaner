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
package org.eobjects.analyzer.storage;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

public class BerkeleyDbListTest extends TestCase {

	private BerkeleyDbStorageProvider sp = new BerkeleyDbStorageProvider(new File("target"));

	public void testAdd() throws Exception {
		BerkeleyDbList<String> list = (BerkeleyDbList<String>) sp.createList(String.class);

		list.add("foo1");
		list.add("foo2");
		list.add("foo3");
		list.add("foo4");

		assertEquals("[foo1, foo2, foo3, foo4]", list.toString());

		list.add(2, "foo5");

		assertEquals("[foo1, foo2, foo5, foo3, foo4]", list.toString());

		list.add(null);

		assertEquals("[foo1, foo2, foo5, foo3, foo4, null]", list.toString());
		
	}

	public void testSet() throws Exception {
		List<String> list = sp.createList(String.class);

		list.add("foo1");
		list.add("foo2");
		list.add("foo3");
		list.add("foo4");

		list.set(1, "foobar");

		assertEquals("[foo1, foobar, foo3, foo4]", list.toString());
	}

	public void testRemove() throws Exception {
		List<String> list = sp.createList(String.class);

		list.add("foo1");
		list.add("foo2");
		list.add("foo3");
		list.add("foo4");

		list.remove("foo2");

		assertEquals("[foo1, foo3, foo4]", list.toString());

		list.remove(0);

		assertEquals("[foo3, foo4]", list.toString());

		list.remove(1);

		assertEquals("[foo3]", list.toString());
	}
}
