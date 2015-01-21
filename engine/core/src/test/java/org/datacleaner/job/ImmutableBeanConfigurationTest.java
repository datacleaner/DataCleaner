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
package org.datacleaner.job;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.datacleaner.api.Configured;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.Descriptors;

public class ImmutableBeanConfigurationTest extends TestCase {

    @Configured
    String[] conf;

    public void testEqualsArrayValue() throws Exception {
        ComponentDescriptor<? extends ImmutableBeanConfigurationTest> componentDescriptor = Descriptors
                .ofComponent(getClass());
        ConfiguredPropertyDescriptor propertyDescriptor = componentDescriptor.getConfiguredProperty("Conf");
        assertNotNull(propertyDescriptor);

        Map<ConfiguredPropertyDescriptor, Object> properties1 = new HashMap<ConfiguredPropertyDescriptor, Object>();
        properties1.put(propertyDescriptor, new String[] { "hello", "world" });

        Map<ConfiguredPropertyDescriptor, Object> properties2 = new HashMap<ConfiguredPropertyDescriptor, Object>();
        properties2.put(propertyDescriptor, new String[] { "hello", "world" });

        ImmutableBeanConfiguration conf1 = new ImmutableBeanConfiguration(properties1);
        ImmutableBeanConfiguration conf2 = new ImmutableBeanConfiguration(properties2);
        assertEquals(conf1.hashCode(), conf2.hashCode());
        assertEquals(conf1, conf2);
    }
}
