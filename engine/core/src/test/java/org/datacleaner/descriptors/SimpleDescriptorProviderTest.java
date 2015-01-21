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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.components.convert.ConvertToBooleanTransformer;
import org.datacleaner.components.convert.ConvertToDateTransformer;
import org.datacleaner.components.mock.AnalyzerMock;
import org.datacleaner.components.mock.TransformerMock;
import org.datacleaner.descriptors.AnnotationBasedAnalyzerComponentDescriptorTest.OneMoreMockAnalyzer;

public class SimpleDescriptorProviderTest extends TestCase {

    public void testSetBeanClassNames() throws Exception {
        SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider(false);

        assertNull(descriptorProvider.getAnalyzerDescriptorForClass(AnalyzerMock.class));
        assertNull(descriptorProvider.getAnalyzerDescriptorForClass(OneMoreMockAnalyzer.class));
        assertNull(descriptorProvider.getTransformerDescriptorForClass(ConvertToBooleanTransformer.class));

        descriptorProvider.setAnalyzerClassNames(Arrays.asList(AnalyzerMock.class.getName(),
                OneMoreMockAnalyzer.class.getName()));

        assertEquals(2, descriptorProvider.getAnalyzerDescriptors().size());

        descriptorProvider.setTransformerClassNames(Arrays.asList(ConvertToBooleanTransformer.class.getName(),
                ConvertToDateTransformer.class.getName()));

        assertEquals(2, descriptorProvider.getTransformerDescriptors().size());

        descriptorProvider.setTransformerClassNames(Arrays.asList(ConvertToBooleanTransformer.class.getName()));

        assertEquals(2, descriptorProvider.getTransformerDescriptors().size());

        assertEquals("AnnotationBasedAnalyzerComponentDescriptor[" + AnalyzerMock.class.getName() + "]", descriptorProvider
                .getAnalyzerDescriptorForClass(AnalyzerMock.class).toString());

        assertEquals("AnnotationBasedTransformerComponentDescriptor[" + ConvertToBooleanTransformer.class.getName() + "]",
                descriptorProvider.getTransformerDescriptorForClass(ConvertToBooleanTransformer.class).toString());
    }

    public void testSetClassNamesNoDiscover() throws Exception {
        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider(true);
        final List<String> classNames = new ArrayList<String>();

        // add the same classname twice
        classNames.add(AnalyzerMock.class.getName());
        classNames.add(AnalyzerMock.class.getName());
        descriptorProvider.setAnalyzerClassNames(classNames);

        final AnalyzerDescriptor<?> descriptor = descriptorProvider
                .getAnalyzerDescriptorByDisplayName("Row-processing mock");
        assertNotNull(descriptor);
        assertEquals(AnalyzerMock.class, descriptor.getComponentClass());

        // check that the same analyzer was not duplicated, even thought the
        // classname appeared twice.
        assertEquals(1, descriptorProvider.getAnalyzerDescriptors().size());
    }

    public void testGetBeanByAlias() throws Exception {
        SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider(false);
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(TransformerMock.class));

        TransformerDescriptor<?> descriptor1 = descriptorProvider
                .getTransformerDescriptorByDisplayName("Transformer mock");
        TransformerDescriptor<?> descriptor2 = descriptorProvider
                .getTransformerDescriptorByDisplayName("Mock transformer");

        assertSame(descriptor1, descriptor2);
    }
}
