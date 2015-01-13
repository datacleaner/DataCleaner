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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;

import junit.framework.TestCase;

public class CollectionFactoryImplTest extends TestCase {

	public void testFactoryMethods() throws Exception {
		StorageProvider storageProvider = EasyMock.createMock(StorageProvider.class);

		List<String> list1 = new ArrayList<String>();
		Set<String> set1 = new HashSet<String>();
		Map<Integer, String> map1 = new HashMap<Integer, String>();

		EasyMock.expect(storageProvider.createList(String.class)).andReturn(list1);
		EasyMock.expect(storageProvider.createSet(String.class)).andReturn(set1);
		EasyMock.expect(storageProvider.createMap(Integer.class, String.class)).andReturn(map1);

		CollectionFactory collectionFactory = new CollectionFactoryImpl(storageProvider);

		EasyMock.replay(storageProvider);

		List<String> list2 = collectionFactory.createList(String.class);
		assertSame(list1, list2);

		Set<String> set2 = collectionFactory.createSet(String.class);
		assertSame(set1, set2);

		Map<Integer, String> map2 = collectionFactory.createMap(Integer.class, String.class);
		assertSame(map1, map2);

		EasyMock.verify(storageProvider);
	}
}
