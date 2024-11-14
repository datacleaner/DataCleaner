/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import org.datacleaner.job.concurrent.MultiThreadedTaskRunner;
import org.datacleaner.test.TestEnvironment;

import junit.framework.TestCase;

public class ClasspathScanDescriptorProviderTest extends TestCase {

    private MultiThreadedTaskRunner taskRunner = TestEnvironment.getMultiThreadedTaskRunner();

    public void testScanNonExistingPackage() throws Exception {
        final ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);
        final Collection<AnalyzerDescriptor<?>> analyzerDescriptors =
                provider.scanPackage("org.datacleaner.nonexistingbeans", true).getAnalyzerDescriptors();
        assertEquals("[]", Arrays.toString(analyzerDescriptors.toArray()));

        assertEquals("[]", provider.getTransformerDescriptors().toString());
        assertEquals("[]", provider.getRendererBeanDescriptors().toString());
    }

    public void testScanPackageRecursive() throws Exception {
        final ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider(taskRunner);
        Collection<AnalyzerDescriptor<?>> analyzerDescriptors =
                descriptorProvider.scanPackage("org.datacleaner.components.mock", true).getAnalyzerDescriptors();
        final Object[] array = analyzerDescriptors.toArray();
        assertEquals("[AnnotationBasedAnalyzerComponentDescriptor[org.datacleaner.components.mock.AnalyzerMock]]",
                Arrays.toString(array));

        final Collection<TransformerDescriptor<?>> transformerComponentDescriptors =
                descriptorProvider.getTransformerDescriptors();
        assertEquals("[AnnotationBasedTransformerComponentDescriptor[org.datacleaner.components.mock.TransformerMock]]",
                Arrays.toString(transformerComponentDescriptors.toArray()));

        analyzerDescriptors =
                new ClasspathScanDescriptorProvider(taskRunner).scanPackage("org.datacleaner.job.builder", true)
                        .getAnalyzerDescriptors();
        assertEquals(0, analyzerDescriptors.size());
    }

    public void testScanRenderers() throws Exception {
        final ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider(taskRunner);
        final Collection<RendererBeanDescriptor<?>> rendererComponentDescriptors =
                descriptorProvider.scanPackage("org.datacleaner.result.renderer", true).getRendererBeanDescriptors();
        assertEquals("[AnnotationBasedRendererBeanDescriptor[org.datacleaner.result.renderer.CrosstabTextRenderer], "
                        + "AnnotationBasedRendererBeanDescriptor[org.datacleaner.result.renderer.MetricBasedResultTextRenderer], "
                        + "AnnotationBasedRendererBeanDescriptor[org.datacleaner.result.renderer.ToStringFutureTextRenderer], "
                        + "AnnotationBasedRendererBeanDescriptor[org.datacleaner.result.renderer.ToStringTextRenderer]]",
                new TreeSet<>(rendererComponentDescriptors).toString());
    }

    public void testIsClassInPackageNonRecursive() throws Exception {
        final ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);

        assertTrue(provider.isClassInPackage("foo/bar/Baz.class", "foo/bar", false));
        assertTrue(provider.isClassInPackage("foo/bar/Foobar.class", "foo/bar", false));

        assertFalse(provider.isClassInPackage("foo/bar/baz/Baz.class", "foo/bar", false));

        assertFalse(provider.isClassInPackage("foo/baz/Baz.class", "foo/bar", false));
        assertFalse(provider.isClassInPackage("foo/Baz.class", "foo/bar", false));
    }

    public void testIsClassInPackageRecursive() throws Exception {
        final ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);

        assertTrue(provider.isClassInPackage("foo/bar/Baz.class", "foo/bar", true));
        assertTrue(provider.isClassInPackage("foo/bar/Foobar.class", "foo/bar", true));

        assertTrue(provider.isClassInPackage("foo/bar/baz/Baz.class", "foo/bar", true));

        assertFalse(provider.isClassInPackage("foo/baz/Baz.class", "foo/bar", true));
        assertFalse(provider.isClassInPackage("foo/Baz.class", "foo/bar", true));
    }
}
