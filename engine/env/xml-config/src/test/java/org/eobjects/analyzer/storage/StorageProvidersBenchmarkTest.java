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
package org.eobjects.analyzer.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.eobjects.analyzer.storage.BerkeleyDbStorageProvider;
import org.eobjects.analyzer.storage.H2StorageProvider;
import org.eobjects.analyzer.storage.HsqldbStorageProvider;
import org.eobjects.analyzer.storage.InMemoryStorageProvider;
import org.eobjects.analyzer.storage.StorageProvider;
import org.junit.Ignore;

/**
 * A benchmark program (which is why it is @Ignore'd) used to show the
 * difference in performance between the different collection provider
 * implementations.
 * 
 * 
 */
@Ignore
public class StorageProvidersBenchmarkTest extends TestCase {

	private Map<String, StorageProvider> _storageProviders;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_storageProviders = new HashMap<String, StorageProvider>();
		_storageProviders.put("1) In-Memory", new InMemoryStorageProvider());
		_storageProviders.put("2) H2", new H2StorageProvider());
		_storageProviders.put("3) Hsqldb", new HsqldbStorageProvider());
		_storageProviders.put("4) Berkeley DB", new BerkeleyDbStorageProvider(new File("target")));
	}

	public void testOneBigBatch() throws Exception {
		runBenchmarkTests(1, 100000);
	}

	public void testManyMinorBatches() throws Exception {
		runBenchmarkTests(50, 50);
	}

	private void runBenchmarkTests(int numCollections, int numElems) {
		System.out.println(getName() + " beginning.");
		System.out.println("(" + numCollections + " collections with " + numElems + " elements in them)");

		for (String cpName : _storageProviders.keySet()) {
			System.out.println(cpName + " results:");

			List<Collection<?>> collections = new ArrayList<Collection<?>>(numCollections);

			long timeBefore = System.currentTimeMillis();
			StorageProvider cp = _storageProviders.get(cpName);
			for (int i = 0; i < numCollections; i++) {
				Set<Integer> set = cp.createSet(Integer.class);
				for (int j = 0; j < numElems; j++) {
					set.add(j);
				}
				collections.add(set);
			}

			long timeAfterAdd = System.currentTimeMillis();
			System.out.println("- time to add elements: " + (timeAfterAdd - timeBefore));

			for (Collection<?> col : collections) {
				for (Object object : col) {
					object.hashCode();
				}
			}

			long timeAfterIterate = System.currentTimeMillis();
			System.out.println("- time to iterate through collection: " + (timeAfterIterate - timeAfterAdd));

			collections.clear();
			System.gc();
			System.runFinalization();
			// TODO:check that the collections have actually been cleaned up

			long timeAfterCleanup = System.currentTimeMillis();
			System.out.println("- TOTAL time: " + (timeAfterCleanup - timeBefore));
		}
		System.out.println(getName() + " finished.");
	}
}
