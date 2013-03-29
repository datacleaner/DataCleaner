/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.extensions;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eobjects.datacleaner.user.ExtensionPackage;

import junit.framework.TestCase;

public class ExtensionReaderTest extends TestCase {

    public void testGetInternalExtensions() throws Exception {
        final List<ExtensionPackage> extensions = new ExtensionReader().getInternalExtensions();
        assertEquals(1, extensions.size());
        
        final ExtensionPackage extensionPackage = extensions.get(0);
        assertEquals("Test extension", extensionPackage.getName());
        assertEquals("org.eobjects.foobar", extensionPackage.getScanPackage());
        assertEquals(false, extensionPackage.isExternal());
        assertEquals(false, extensionPackage.isLoaded());
        
        final Map<String, String> additionalProperties = extensionPackage.getAdditionalProperties();
        assertEquals("{description=This is just a dummy test file for unittesting the extension file reader.}", additionalProperties.toString());
    }

    public void testAutoDetectPackageName() throws Exception {
        File file = new File("src/test/resources/FooBarPlugin.jar");
        assertTrue("example plugin jar does not exist", file.exists());

        assertEquals("foo.bar", new ExtensionReader().autoDetectPackageName(file));
    }
}
