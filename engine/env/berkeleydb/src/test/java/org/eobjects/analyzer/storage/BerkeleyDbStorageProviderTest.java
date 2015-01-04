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

import junit.framework.TestCase;

public class BerkeleyDbStorageProviderTest extends TestCase {

	private BerkeleyDbStorageProvider sp;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		sp = new BerkeleyDbStorageProvider(new File("target/berkeleydbtest"));
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCreateMap() throws Throwable {
		BerkeleyDbMap<String, Long> map = sp
				.createMap(String.class, Long.class);
		assertNotNull(map);
		map.finalize();
	}

	public void testCreateList() throws Throwable {
		BerkeleyDbList<String> list = sp.createList(String.class);
		assertNotNull(list);
		list.add("hello");
		list.add("hello");
		assertEquals(2, list.size());
		list.add("hi");
		assertEquals(3, list.size());

		list.finalize();
	}

	public void testCreateSet() throws Throwable {
		BerkeleyDbSet<String> set = sp.createSet(String.class);
		set.add("hello");
		set.add("hello");
		assertEquals(1, set.size());

		set.add("world");
		set.add("world");
		set.add("world");
		set.add("world");
		assertEquals(2, set.size());

		set.remove("world");
		assertEquals(1, set.size());

		set.finalize();
	}

	public void testCleanDirectory() throws Throwable {
		BerkeleyDbSet<String> set = sp.createSet(String.class);
		set.add("hello");
		set.add("hello");

		File parentDirectory = sp.getParentDirectory();
		assertTrue(countDbFiles(parentDirectory) > 2);

		set.finalize();

		sp.finalize();
		assertEquals(0, countDbFiles(parentDirectory));
	}

	private int countDbFiles(File dir) {
		int result = 0;
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				result += countDbFiles(file);
			} else if ("je.lck".equals(file.getName())) {
				// ignore lock files
			} else if (file.getName().startsWith(".nfs")) {
				// ignore .nfs files (sometimes shows up on *nix systems)
			} else {
				result++;
				System.out.print("Found berkeleydb temp file: " + file);
			}
		}
		return result;
	}
}
