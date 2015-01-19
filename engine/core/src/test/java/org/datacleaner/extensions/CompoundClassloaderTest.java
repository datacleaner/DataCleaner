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
package org.datacleaner.extensions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.datacleaner.extensions.ClassLoaderUtils;
import org.datacleaner.extensions.CompoundClassLoader;
import org.datacleaner.extensions.ExtensionClassLoader;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for the scanning classloader.
 *
 */
public class CompoundClassloaderTest {

    /**
     * The FooBarPlugin is read using a class loader. Another class loader loads
     * a test class. The test creates a ScanningClassLoader combining the two
     * class loaders. The FooBarPlugin and the Baz class must both be resolvable
     * from the ScanningClassLoader.
     * 
     * @throws ClassNotFoundException
     */
    @Test
    public void shouldresolve() throws ClassNotFoundException {
        final ClassLoader globalParent = ClassLoaderUtils.getParentClassLoader();
        final File[] jarFiles = new File[] { new File("src/test/resources/FooBarPlugin.jar") };

        final ClassLoader parent1 = ClassLoaderUtils.createClassLoader(jarFiles, null);
        final ClassLoader c1 = new ExtensionClassLoader(parent1, globalParent, "Extension1");

        final File[] classFiles = new File[] { new File("target/test-classes") };
        final ClassLoader parent2 = ClassLoaderUtils.createClassLoader(classFiles, null);
        final ClassLoader c2 = new ExtensionClassLoader(parent2, globalParent, "Extension2");

        final Collection<ClassLoader> loaders = new ArrayList<ClassLoader>();
        loaders.add(c1);
        loaders.add(c2);
        final CompoundClassLoader loader = new CompoundClassLoader(loaders);

        Class<?> cResult1 = loader.loadClass("foo.bar.transformer.BarTransformer");
        Assert.assertNotNull(cResult1);

        Class<?> cResult2 = loader.loadClass("foo.bar.Baz");
        Assert.assertNotNull(cResult2);

    }
}
