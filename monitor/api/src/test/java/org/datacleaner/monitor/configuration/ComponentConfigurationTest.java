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
package org.datacleaner.monitor.configuration;

import org.junit.Test;

import static junit.framework.TestCase.*;

public class ComponentConfigurationTest {
    private ComponentConfiguration componentConfiguration = new ComponentConfiguration();
    private String propertyName = "propertyName";

    @Test
    public void testGetProperties() throws Exception {
        assertNotNull(componentConfiguration.getProperties());
    }

    @Test
    public void testGetColumns() throws Exception {
        assertNotNull(componentConfiguration.getColumns());
    }

    @Test
    public void testGetProperty() throws Exception {
        assertNull(componentConfiguration.getProperty(propertyName));
    }

    @Test
    public void testGetPropertiesNames() throws Exception {
        assertNotNull(componentConfiguration.getPropertiesNames());
    }
}