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
package org.eobjects.analyzer.util;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

public class ChangeAwareObjectInputStreamTest extends TestCase {

    private final File JAR_FILE = new File("src/test/resources/code-company.jar");

    // contains a serialized Company object with an Employee array
    private final File SERIALIZED_OBJECT_FILE = new File("src/test/resources/serialized-company.ser");

    public void testDeserializeArrayFromAlternativeClassloaders() throws Exception {
        final URL[] urls = new URL[] { JAR_FILE.toURI().toURL() };
        final ClassLoader classLoader = new URLClassLoader(urls);

        final Object obj;
        try (final ChangeAwareObjectInputStream ois = new ChangeAwareObjectInputStream(new FileInputStream(
                SERIALIZED_OBJECT_FILE))) {
            ois.addClassLoader(classLoader);

            try {
                obj = ois.readObject();
            } finally {
                ois.close();
            }
        }

        assertNotNull(obj);
        assertEquals("com.hi.example.Company", obj.getClass().getName());
    }
}
