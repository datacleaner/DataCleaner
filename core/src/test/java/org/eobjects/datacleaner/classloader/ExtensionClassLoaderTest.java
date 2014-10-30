/**
 * DataCleaner (community edition) Copyright (C) 2014 Neopost - Customer
 * Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to: Free Software Foundation,
 * Inc. 51 Franklin Street, Fifth Floor Boston, MA 02110-1301 USA
 * 
 */
package org.eobjects.datacleaner.classloader;

import java.io.File;

import org.eobjects.analyzer.util.ClassLoaderUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for the extension class loader.
 *
 */
public class ExtensionClassLoaderTest {

    /**
     * Loads two extensions, checking if each loads their jars in a ClassLoader
     * of its own.
     * 
     * @throws ClassNotFoundException
     */
    @Test
    public void testExtensionClassLoader() throws ClassNotFoundException {
        final ClassLoader globalParent = ClassLoaderUtils.getParentClassLoader();
        final File[] jarFiles = new File[] { new File("src/test/resources/FooBarPlugin.jar") };
        // Load the first instance
        final ClassLoader parent1 = ClassLoaderUtils.createClassLoader(jarFiles, null);
        final ClassLoader c1 = new ExtensionClassLoader(parent1, globalParent, "Extension1");
        final Class<?> bazTransformer1 = c1.loadClass("foo.bar.transformer.BazTransformer");
        Assert.assertNotNull(bazTransformer1);
        // Load the second instance...
        final ClassLoader parent2 = ClassLoaderUtils.createClassLoader(jarFiles, null);
        final ClassLoader c2 = new ExtensionClassLoader(parent2, globalParent, "Extension2");
        final Class<?> bazTransformer2 = c2.loadClass("foo.bar.transformer.BazTransformer");
        Assert.assertNotNull(bazTransformer2);
        // Check each instance is loaded by their own loader
        final ClassLoader loader1 = bazTransformer1.getClassLoader();
        final ClassLoader loader2 = bazTransformer2.getClassLoader();
        Assert.assertEquals("Extension classloader for: Extension1", loader1.toString());
        Assert.assertEquals("Extension classloader for: Extension2", loader2.toString());
        // Check the loaded transformer classes really are separate
        Assert.assertNotEquals(bazTransformer1, bazTransformer2);
    }

    /**
     * Check that a class present in the lib folder is always read by the global
     * parent.
     * 
     * @throws ClassNotFoundException
     */
    @Test
    public void testSharedClassesAreResolvedByParent() throws ClassNotFoundException {
        final ClassLoader globalParent = ClassLoaderUtils.getParentClassLoader();
        final File[] jarFiles = new File[] { new File("src/test/resources/FooBarPlugin.jar") };
        // Load the first instance
        final ClassLoader parent1 = ClassLoaderUtils.createClassLoader(jarFiles, null);
        final ClassLoader c1 = new ExtensionClassLoader(parent1, globalParent, "Extension1");
        final Class<?> bazTransformer1 = c1.loadClass("foo.bar.transformer.BazTransformer");
        Assert.assertNotNull(bazTransformer1);
        final ClassLoader classLoader = org.eobjects.analyzer.beans.api.Transformer.class.getClassLoader();
        Assert.assertFalse(classLoader.toString().startsWith("Extension Classloader for: Extension1"));
    }
}
