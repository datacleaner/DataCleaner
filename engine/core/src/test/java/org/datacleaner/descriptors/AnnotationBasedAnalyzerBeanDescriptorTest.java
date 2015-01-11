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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Named;

import junit.framework.TestCase;

import org.datacleaner.beans.api.Analyzer;
import org.datacleaner.beans.api.Distributed;
import org.datacleaner.beans.mock.AnalyzerMock;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.result.AnalyzerResult;
import org.datacleaner.result.AnalyzerResultReducer;
import org.datacleaner.result.NumberResult;

public class AnnotationBasedAnalyzerBeanDescriptorTest extends TestCase {

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        AnalyzerMock.clearInstances();
    }

    public void testInheritedAnalyzer() throws Exception {
        AnalyzerBeanDescriptor<OneMoreMockAnalyzer> descriptor = Descriptors.ofAnalyzer(OneMoreMockAnalyzer.class);
        assertEquals("One more mock", descriptor.getDisplayName());
    }

    @Named("One more mock")
    @Distributed(false)
    public static class OneMoreMockAnalyzer extends AnalyzerMock {

    }
    
    public static class MockResultReducer implements AnalyzerResultReducer<AnalyzerResult> {
        @Override
        public AnalyzerResult reduce(Collection<? extends AnalyzerResult> results) {
            return results.iterator().next();
        }
    }
    
    @Named("Third analyzer mock")
    @Distributed(reducer = MockResultReducer.class)
    public static class ThirdMockAnalyzer extends AnalyzerMock {

    }
    
    public void testIsDistributed() throws Exception {
        AnalyzerBeanDescriptor<?> desc;
        
        desc = Descriptors.ofAnalyzer(AnalyzerMock.class);
        assertFalse(desc.isDistributable());
        
        desc = Descriptors.ofAnalyzer(OneMoreMockAnalyzer.class);
        assertFalse(desc.isDistributable());
        
        desc = Descriptors.ofAnalyzer(ThirdMockAnalyzer.class);
        assertTrue(desc.isDistributable());
    }

    public void testGetConfiguredPropertiesOfType() throws Exception {
        AnalyzerBeanDescriptor<AnalyzerMock> desc = Descriptors.ofAnalyzer(AnalyzerMock.class);

        Set<ConfiguredPropertyDescriptor> properties = desc.getConfiguredPropertiesByType(Number.class, false);
        assertEquals(1, properties.size());

        properties = desc.getConfiguredPropertiesByType(Number.class, true);
        assertEquals(1, properties.size());

        properties = desc.getConfiguredPropertiesByType(Dictionary.class, false);
        assertEquals(0, properties.size());

        properties = desc.getConfiguredPropertiesByType(String.class, true);
        assertEquals(2, properties.size());

        properties = desc.getConfiguredPropertiesByType(CharSequence.class, false);
        assertEquals(1, properties.size());

        properties = desc.getConfiguredPropertiesByType(CharSequence.class, true);
        assertEquals(2, properties.size());
    }

    public void testRowProcessingType() throws Exception {
        AnalyzerBeanDescriptor<AnalyzerMock> descriptor = Descriptors.ofAnalyzer(AnalyzerMock.class);

        Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor.getConfiguredProperties();
        Iterator<ConfiguredPropertyDescriptor> it = configuredProperties.iterator();
        assertTrue(it.hasNext());
        assertEquals("Columns", it.next().getName());
        assertTrue(it.hasNext());
        assertEquals("Configured1", it.next().getName());
        assertTrue(it.hasNext());
        assertEquals("Configured2", it.next().getName());
        assertTrue(it.hasNext());
        assertEquals("Some string property", it.next().getName());
        assertFalse(it.hasNext());

        AnalyzerMock analyzerBean = new AnalyzerMock();
        ConfiguredPropertyDescriptor configuredProperty = descriptor.getConfiguredProperty("Configured1");
        configuredProperty.setValue(analyzerBean, "foobar");
        assertEquals("foobar", analyzerBean.getConfigured1());
    }

    public void testGetResultMetrics() throws Exception {
        AnalyzerBeanDescriptor<?> descriptor = Descriptors.ofAnalyzer(AnalyzerMock.class);
        assertEquals(NumberResult.class, descriptor.getResultClass());

        Set<MetricDescriptor> resultMetrics = descriptor.getResultMetrics();
        assertEquals("[MetricDescriptorImpl[name=Number]]",
                resultMetrics.toString());

        MetricDescriptor metric = descriptor.getResultMetric("Number");
        assertEquals("MetricDescriptorImpl[name=Number]", metric.toString());
        assertFalse(metric.isParameterizedByInputColumn());
        assertFalse(metric.isParameterizedByString());
        

        metric = descriptor.getResultMetric("Foo bar");
        assertNull(metric);
        
        MetricDescriptor metric2 = descriptor.getResultMetric("Some nUMBer");
        assertSame(metric, metric2);
    }

    public void testAbstractBeanClass() throws Exception {
        try {
            Descriptors.ofComponent(InvalidAnalyzer.class);
            fail("Exception expected");
        } catch (DescriptorException e) {
            assertEquals(
                    "Component (class org.datacleaner.descriptors.AnnotationBasedAnalyzerBeanDescriptorTest$InvalidAnalyzer) is not a non-abstract class",
                    e.getMessage());
        }
    }

    @Named("invalid analyzer")
    public abstract class InvalidAnalyzer implements Analyzer<AnalyzerResult> {
    }
}
