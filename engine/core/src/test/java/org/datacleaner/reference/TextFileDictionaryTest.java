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
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;

public class TextFileDictionaryTest extends TestCase {

    private final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();

    public void testCaseSensitiveAndCaseInsensitive() throws Exception {
        final TextFileDictionary caseSensitiveDict = new TextFileDictionary("foobar",
                "src/test/resources/lastnames.txt", "UTF-8", true);
        final TextFileDictionary caseInsensitiveDict = new TextFileDictionary("foobar",
                "src/test/resources/lastnames.txt", "UTF-8", false);

        try (DictionaryConnection connection = caseSensitiveDict.openConnection(configuration)) {
            assertTrue(connection.containsValue("Ellison"));
            assertTrue(connection.containsValue("Gates"));
            assertFalse(connection.containsValue("ellison"));
            assertFalse(connection.containsValue("gates"));
        }
        try (DictionaryConnection connection = caseInsensitiveDict.openConnection(configuration)) {
            assertTrue(connection.containsValue("Ellison"));
            assertTrue(connection.containsValue("Gates"));
            assertTrue(connection.containsValue("ellison"));
            assertTrue(connection.containsValue("gates"));
        }
    }

    public void testThreadSafety() throws Exception {
        final TextFileDictionary dict = new TextFileDictionary("foobar", "src/test/resources/lastnames.txt", "UTF-8");

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                try (DictionaryConnection connection = dict.openConnection(configuration)) {
                    assertTrue(connection.containsValue("Ellison"));
                    assertTrue(connection.containsValue("Gates"));
                    assertFalse(connection.containsValue("John Doe"));
                    assertTrue(connection.containsValue("Jobs"));
                    assertFalse(connection.containsValue("Foobar"));
                }
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

    public void testChangesInbetweenUsage() throws Exception {
        File file = new File("target/TextBasedDictionaryTest-modification.txt");
        FileHelper.writeStringAsFile(file, "foo\nbar");

        TextFileDictionary dict = new TextFileDictionary("dict", file.getPath(), "UTF-8");
        try (DictionaryConnection connection = dict.openConnection(configuration)) {
            assertTrue(connection.containsValue("foo"));
            assertTrue(connection.containsValue("bar"));
            assertFalse(connection.containsValue("foobar"));
        }

        FileHelper.writeStringAsFile(file, "foo\nfoobar");

        try (DictionaryConnection connection = dict.openConnection(configuration)) {
            assertTrue(connection.containsValue("foo"));
            assertFalse(connection.containsValue("bar"));
            assertTrue(connection.containsValue("foobar"));
        }
    }
}
