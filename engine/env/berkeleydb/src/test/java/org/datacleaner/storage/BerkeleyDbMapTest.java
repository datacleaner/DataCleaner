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
package org.datacleaner.storage;

import java.io.File;
import java.util.Map;

import junit.framework.TestCase;

public class BerkeleyDbMapTest extends TestCase {

	public void testNull() throws Exception {
		BerkeleyDbStorageProvider sp = new BerkeleyDbStorageProvider(new File("target"));
		Map<Integer, String> map = sp.createMap(Integer.class, String.class);
		
		assertNull(map.get(1));
		
		map.put(1,"foo");
		assertEquals("foo", map.get(1));
		
		map.put(2,"");
		assertEquals("", map.get(2));
		
		map.put(3,null);
		assertNull(map.get(3));

	}
}
