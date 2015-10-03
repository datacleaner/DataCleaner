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

import org.apache.commons.collections.CollectionUtils;
import org.datacleaner.components.mock.AnalyzerMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * @Since 9/8/15
 */
public class CompositeDescriptorProviderTest {

    CompositeDescriptorProvider dp;
    AnalyzerDescriptor<?> ad1, ad2;

    @Before
    public void init() {
        SimpleDescriptorProvider p1 = new SimpleDescriptorProvider(false);
        SimpleDescriptorProvider p2 = new SimpleDescriptorProvider(false);
        ad1 = Descriptors.ofAnalyzer(AnalyzerMock.class);
        ad2 = Descriptors.ofAnalyzer(AnnotationBasedAnalyzerComponentDescriptorTest.OneMoreMockAnalyzer.class);
        p1.addAnalyzerBeanDescriptor(ad1);
        p2.addAnalyzerBeanDescriptor(ad2);
        dp = new CompositeDescriptorProvider(p1, p2);
    }

    @Test
    public void testGetComponentDescritptors() {
        Collection<? extends ComponentDescriptor<?>> descritptors = dp.getComponentDescriptors();
        CollectionUtils.isEqualCollection(Arrays.asList(ad1, ad2), descritptors);
    }

    @Test
    public void testGetAnalyzerDescriptorByClass() {
        Assert.assertEquals(ad1, dp.getAnalyzerDescriptorForClass(AnalyzerMock.class));
        Assert.assertEquals(ad2, dp.getAnalyzerDescriptorForClass(AnnotationBasedAnalyzerComponentDescriptorTest.OneMoreMockAnalyzer.class));
    }
}
