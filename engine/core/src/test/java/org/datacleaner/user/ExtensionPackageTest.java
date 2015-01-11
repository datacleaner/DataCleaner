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
package org.datacleaner.user;

import java.io.File;

import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.user.ExtensionPackage;

import junit.framework.TestCase;

public class ExtensionPackageTest extends TestCase {

    public void testLoadExtension() throws Exception {
        File file = new File("src/test/resources/extensions/DataCleaner-basic-transformers.jar");
        assertTrue("example plugin jar does not exist", file.exists());

        ExtensionPackage extensionPackage = new ExtensionPackage("foobar plugin", "org.datacleaner", true, new File[] { file });

        assertFalse(extensionPackage.isLoaded());

        ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider();
        extensionPackage.loadDescriptors(descriptorProvider);

        assertTrue(extensionPackage.isLoaded());
        assertEquals(0, extensionPackage.getLoadedAnalyzers());
        assertEquals(27, extensionPackage.getLoadedTransformers());
        assertEquals(0, extensionPackage.getLoadedFilters());
    }
}
