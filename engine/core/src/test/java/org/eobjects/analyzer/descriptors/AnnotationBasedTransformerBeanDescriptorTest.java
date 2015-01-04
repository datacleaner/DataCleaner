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
package org.eobjects.analyzer.descriptors;

import java.util.Set;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.api.OutputRowCollector;
import org.eobjects.analyzer.job.tasks.MockMultiRowTransformer;

public class AnnotationBasedTransformerBeanDescriptorTest extends TestCase {

    public void testGetProvidedPropertiesOfType() throws Exception {
        TransformerBeanDescriptor<MockMultiRowTransformer> descriptor = Descriptors
                .ofTransformer(MockMultiRowTransformer.class);

        Set<ProvidedPropertyDescriptor> properties = descriptor.getProvidedPropertiesByType(OutputRowCollector.class);
        assertEquals(1, properties.size());
        ProvidedPropertyDescriptor property = properties.iterator().next();
        assertEquals(OutputRowCollector.class, property.getType());
        assertEquals("outputRowCollector", property.getName());
    }
}
