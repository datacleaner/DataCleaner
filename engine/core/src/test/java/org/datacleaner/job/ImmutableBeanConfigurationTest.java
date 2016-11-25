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

import org.datacleaner.api.Configured;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.Descriptors;

import junit.framework.TestCase;

public class ImmutableBeanConfigurationTest extends TestCase {

    @Configured
    String[] conf;

    public void testEqualsArrayValue() throws Exception {
        final ComponentDescriptor<? extends ImmutableBeanConfigurationTest> componentDescriptor =
                Descriptors.ofComponent(getClass());
        final ConfiguredPropertyDescriptor propertyDescriptor = componentDescriptor.getConfiguredProperty("Conf");
        assertNotNull(propertyDescriptor);

        final Map<ConfiguredPropertyDescriptor, Object> properties1 = new HashMap<>();
        properties1.put(propertyDescriptor, new String[] { "hello", "world" });

        final Map<ConfiguredPropertyDescriptor, Object> properties2 = new HashMap<>();
        properties2.put(propertyDescriptor, new String[] { "hello", "world" });

        final ImmutableComponentConfiguration conf1 = new ImmutableComponentConfiguration(properties1);
        final ImmutableComponentConfiguration conf2 = new ImmutableComponentConfiguration(properties2);
        assertEquals(conf1.hashCode(), conf2.hashCode());
        assertEquals(conf1, conf2);
    }
}
