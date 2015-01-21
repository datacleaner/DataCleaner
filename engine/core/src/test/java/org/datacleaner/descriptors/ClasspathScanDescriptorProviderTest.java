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
package org.datacleaner.descriptors;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import org.datacleaner.extensions.ClassLoaderUtils;
import org.datacleaner.job.concurrent.MultiThreadedTaskRunner;

import junit.framework.TestCase;

public class ClasspathScanDescriptorProviderTest extends TestCase {

    private MultiThreadedTaskRunner taskRunner = new MultiThreadedTaskRunner(2);

    public void testScanOnlySingleJar() throws Exception {
        // File that only contains various transformers
        File pluginFile1 = new File("src/test/resources/extensions/DataCleaner-basic-transformers.jar");

        ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);
        assertEquals(0, provider.getAnalyzerDescriptors().size());
        Collection<TransformerDescriptor<?>> transformerComponentDescriptors = provider.getTransformerDescriptors();
        assertEquals(0, transformerComponentDescriptors.size());
        File[] files = new File[] { pluginFile1 };
        provider = provider.scanPackage("org.datacleaner", true, ClassLoaderUtils.createClassLoader(files), false, files);
        assertEquals(0, provider.getAnalyzerDescriptors().size());
        
        transformerComponentDescriptors = provider.getTransformerDescriptors();
        assertEquals(23, transformerComponentDescriptors.size());

        transformerComponentDescriptors = new TreeSet<>(transformerComponentDescriptors);
        
        assertEquals("org.datacleaner.beans.coalesce.CoalesceMultipleFieldsTransformer", transformerComponentDescriptors
                .iterator().next().getComponentClass().getName());
    }

    public void testScanNonExistingPackage() throws Exception {
        ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);
        Collection<AnalyzerDescriptor<?>> analyzerDescriptors = provider.scanPackage(
                "org.datacleaner.nonexistingbeans", true).getAnalyzerDescriptors();
        assertEquals("[]", Arrays.toString(analyzerDescriptors.toArray()));

        assertEquals("[]", provider.getTransformerDescriptors().toString());
        assertEquals("[]", provider.getRendererBeanDescriptors().toString());
    }

    public void testScanPackageRecursive() throws Exception {
        ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider(taskRunner);
        Collection<AnalyzerDescriptor<?>> analyzerDescriptors = descriptorProvider.scanPackage(
                "org.datacleaner.components.mock", true).getAnalyzerDescriptors();
        Object[] array = analyzerDescriptors.toArray();
        assertEquals("[AnnotationBasedAnalyzerComponentDescriptor[org.datacleaner.components.mock.AnalyzerMock]]",
                Arrays.toString(array));

        Collection<TransformerDescriptor<?>> transformerComponentDescriptors = descriptorProvider
                .getTransformerDescriptors();
        assertEquals("[AnnotationBasedTransformerComponentDescriptor[org.datacleaner.components.mock.TransformerMock]]",
                Arrays.toString(transformerComponentDescriptors.toArray()));

        analyzerDescriptors = new ClasspathScanDescriptorProvider(taskRunner).scanPackage(
                "org.datacleaner.job.builder", true).getAnalyzerDescriptors();
        assertEquals(0, analyzerDescriptors.size());
    }

    public void testScanRenderers() throws Exception {
        ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider(taskRunner);
        Collection<RendererBeanDescriptor<?>> rendererComponentDescriptors = descriptorProvider.scanPackage(
                "org.datacleaner.result.renderer", true).getRendererBeanDescriptors();
        assertEquals(
                "[AnnotationBasedRendererBeanDescriptor[org.datacleaner.result.renderer.CrosstabTextRenderer], "
                        + "AnnotationBasedRendererBeanDescriptor[org.datacleaner.result.renderer.MetricBasedResultTextRenderer], "
                        + "AnnotationBasedRendererBeanDescriptor[org.datacleaner.result.renderer.ToStringTextRenderer]]",
                new TreeSet<RendererBeanDescriptor<?>>(rendererComponentDescriptors).toString());
    }

    public void testScanJarFilesOnClasspath() throws Exception {
        // File that contains 24 transformers including XmlDecoderTransformer
        File pluginFile1 = new File("src/test/resources/extensions/DataCleaner-basic-transformers.jar");
        // File that contains 2 writers including InsertIntoTableAnalyzer
        File pluginFile2 = new File("src/test/resources/extensions/DataCleaner-writers.jar");
        assertTrue(pluginFile2.exists());

        File[] files = new File[] { pluginFile1, pluginFile2 };
        ClassLoader classLoader = ClassLoaderUtils.createClassLoader(files);

        ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);

        assertEquals(0, provider.getAnalyzerDescriptors().size());
        assertEquals(0, provider.getTransformerDescriptors().size());

        provider = provider.scanPackage("org.datacleaner", true, classLoader, true);
        assertEquals(23, provider.getTransformerDescriptors().size());

        boolean foundXmlDecoderTransformer = false;
        for (TransformerDescriptor<?> transformerComponentDescriptor : provider.getTransformerDescriptors()) {
            if (transformerComponentDescriptor.getComponentClass().getName()
                    .equals("org.datacleaner.beans.codec.XmlDecoderTransformer")) {
                foundXmlDecoderTransformer = true;
                break;
            }
        }
        assertTrue(foundXmlDecoderTransformer);
    }

    public void testIsClassInPackageNonRecursive() throws Exception {
        ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);

        assertTrue(provider.isClassInPackage("foo/bar/Baz.class", "foo/bar", false));
        assertTrue(provider.isClassInPackage("foo/bar/Foobar.class", "foo/bar", false));

        assertFalse(provider.isClassInPackage("foo/bar/baz/Baz.class", "foo/bar", false));

        assertFalse(provider.isClassInPackage("foo/baz/Baz.class", "foo/bar", false));
        assertFalse(provider.isClassInPackage("foo/Baz.class", "foo/bar", false));
    }

    public void testIsClassInPackageRecursive() throws Exception {
        ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);

        assertTrue(provider.isClassInPackage("foo/bar/Baz.class", "foo/bar", true));
        assertTrue(provider.isClassInPackage("foo/bar/Foobar.class", "foo/bar", true));

        assertTrue(provider.isClassInPackage("foo/bar/baz/Baz.class", "foo/bar", true));

        assertFalse(provider.isClassInPackage("foo/baz/Baz.class", "foo/bar", true));
        assertFalse(provider.isClassInPackage("foo/Baz.class", "foo/bar", true));
    }
}
