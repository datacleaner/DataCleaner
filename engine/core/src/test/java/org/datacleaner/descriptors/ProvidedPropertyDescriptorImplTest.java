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

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.datacleaner.api.Provided;

public class ProvidedPropertyDescriptorImplTest extends TestCase {

    @Provided
    Map<String, Boolean> stringMap;

    @Inject
    @Provided
    Map<String, Integer> intMap;

    @Inject
    Map<String, Date> dateMap;

    public void testDiscovery() throws Exception {
        SimpleComponentDescriptor<ProvidedPropertyDescriptorImplTest> desc = new SimpleComponentDescriptor<ProvidedPropertyDescriptorImplTest>(
                ProvidedPropertyDescriptorImplTest.class, true);

        Set<ProvidedPropertyDescriptor> properties = desc.getProvidedProperties();
        assertEquals(3, properties.size());

        assertEquals("[ProvidedPropertyDescriptorImpl[field=dateMap,baseType=interface java.util.Map], "
                + "ProvidedPropertyDescriptorImpl[field=intMap,baseType=interface java.util.Map], "
                + "ProvidedPropertyDescriptorImpl[field=stringMap,baseType=interface java.util.Map]]",
                properties.toString());
    }

    public void testGenericTypes() throws Exception {
        Field stringMapField = getClass().getDeclaredField("stringMap");
        ProvidedPropertyDescriptorImpl descriptor = new ProvidedPropertyDescriptorImpl(stringMapField, null);

        assertEquals("ProvidedPropertyDescriptorImpl[field=stringMap,baseType=interface java.util.Map]",
                descriptor.toString());

        assertEquals(2, descriptor.getTypeArgumentCount());
        assertEquals(String.class, descriptor.getTypeArgument(0));
        assertEquals(Boolean.class, descriptor.getTypeArgument(1));

        Field intMapField = getClass().getDeclaredField("intMap");
        descriptor = new ProvidedPropertyDescriptorImpl(intMapField, null);
        assertEquals("ProvidedPropertyDescriptorImpl[field=intMap,baseType=interface java.util.Map]",
                descriptor.toString());

        assertEquals(2, descriptor.getTypeArgumentCount());
        assertEquals(String.class, descriptor.getTypeArgument(0));
        assertEquals(Integer.class, descriptor.getTypeArgument(1));

        Field dateMapField = getClass().getDeclaredField("dateMap");
        descriptor = new ProvidedPropertyDescriptorImpl(dateMapField, null);
        assertEquals("ProvidedPropertyDescriptorImpl[field=dateMap,baseType=interface java.util.Map]",
                descriptor.toString());

        assertEquals(2, descriptor.getTypeArgumentCount());
        assertEquals(String.class, descriptor.getTypeArgument(0));
        assertEquals(Date.class, descriptor.getTypeArgument(1));
    }
}
