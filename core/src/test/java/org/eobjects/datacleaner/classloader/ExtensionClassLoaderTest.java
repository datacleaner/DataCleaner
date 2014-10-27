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
