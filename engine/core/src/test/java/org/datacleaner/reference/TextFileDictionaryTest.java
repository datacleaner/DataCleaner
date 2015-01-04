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
package org.datacleaner.reference;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.TestCase;

import org.apache.metamodel.util.FileHelper;

public class TextFileDictionaryTest extends TestCase {

	public void testThreadSafety() throws Exception {
		final TextFileDictionary dict = new TextFileDictionary("foobar", "src/test/resources/lastnames.txt", "UTF-8");

		dict.init();
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				assertTrue(dict.containsValue("Ellison"));
				assertTrue(dict.containsValue("Gates"));
				assertFalse(dict.containsValue("John Doe"));
				assertTrue(dict.containsValue("Jobs"));
				assertFalse(dict.containsValue("Foobar"));
			}
		};

		ExecutorService threadPool = Executors.newFixedThreadPool(20);
		Future<?>[] futures = new Future[20];

		for (int i = 0; i < futures.length; i++) {
			futures[i] = threadPool.submit(r);
		}

		for (int i = 0; i < futures.length; i++) {
			futures[i].get();
		}
	}

	public void testModificationsClearCache() throws Exception {
		File file = new File("target/TextBasedDictionaryTest-modification.txt");
		FileHelper.writeStringAsFile(file, "foo\nbar");

		TextFileDictionary dict = new TextFileDictionary("dict", file.getPath(), "UTF-8");
		dict.init();
		assertTrue(dict.containsValue("foo"));
		assertTrue(dict.containsValue("bar"));
		assertFalse(dict.containsValue("foobar"));

		// sleep to make sure the file monitor will catch a file change
		Thread.sleep(2000);
		
		FileHelper.writeStringAsFile(file, "foo\nfoobar");
		dict.init();

		assertTrue(dict.containsValue("foo"));
		assertFalse(dict.containsValue("bar"));
		assertTrue(dict.containsValue("foobar"));
	}
}
