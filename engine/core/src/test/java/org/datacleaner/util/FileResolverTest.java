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

import java.io.File;

import junit.framework.TestCase;

public class FileResolverTest extends TestCase {

    public void testUsingDefaultBaseDir() throws Exception {
        FileResolver fileResolver = new FileResolver(new File("."));

        File file = fileResolver.toFile("pom.xml");
        assertNotNull(file);
        assertTrue(file.exists());
        assertFalse(file.isAbsolute());
        assertEquals("pom.xml", normalizeSeparators(file.getPath()));
        assertEquals("pom.xml", fileResolver.toPath(file));

        File directory = fileResolver.toFile("src/main");
        assertNotNull(directory);
        assertTrue(directory.exists());
        assertFalse(directory.isAbsolute());
        assertEquals("src/main", normalizeSeparators(directory.getPath()));
        assertEquals("src/main", fileResolver.toPath(directory));
    }

    public void testUsingRelativeBaseDir() throws Exception {
        FileResolver fileResolver = new FileResolver(new File(".."));

        File file = fileResolver.toFile("pom.xml");
        assertNotNull(file);
        assertTrue(file.exists());
        assertFalse(file.isAbsolute());
        assertEquals("../pom.xml", normalizeSeparators(file.getPath()));
        assertEquals("pom.xml", fileResolver.toPath(file));

        File directory = fileResolver.toFile("core");
        assertNotNull(directory);
        assertTrue(directory.exists());
        assertFalse(directory.isAbsolute());
        assertEquals("../core", normalizeSeparators(directory.getPath()));
        assertEquals("core", fileResolver.toPath(directory));
    }

    public void testUsingCustomBaseDir() throws Exception {
        FileResolver fileResolver = new FileResolver(new File("src/test/resources"));

        File file = fileResolver.toFile("employees.csv");
        assertNotNull(file);
        assertTrue(file.exists());
        assertFalse(file.isAbsolute());
        assertEquals("src/test/resources/employees.csv", normalizeSeparators(file.getPath()));
        assertEquals("employees.csv", fileResolver.toPath(file));

        File directory = fileResolver.toFile("example_folders/folder1");
        assertNotNull(directory);
        assertTrue(directory.exists());
        assertFalse(directory.isAbsolute());
        assertEquals("src/test/resources/example_folders/folder1", normalizeSeparators(directory.getPath()));
        assertEquals("example_folders/folder1", fileResolver.toPath(directory));
    }

    public void testAbsolutePaths() throws Exception {
        String userHome = System.getProperty("user.home");
        String parent = new File(userHome).getAbsolutePath();

        FileResolver fileResolver = new FileResolver(new File("."));

        File file = fileResolver.toFile(parent);
        assertNotNull(file);
        assertTrue(file.isAbsolute());
        assertEquals(userHome, file.getPath());

        String path = fileResolver.toPath(file);

        assertEquals(normalizeSeparators(userHome), path);
    }

    private String normalizeSeparators(String path) {
        return StringUtils.replaceAll(path, "\\", "/");
    }
}
