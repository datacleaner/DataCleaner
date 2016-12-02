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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.datacleaner.restclient.CreateInput;
import org.junit.Test;

public class ComponentStoreHolderTest {
    private ComponentStoreHolder componentStoreHolder = new ComponentStoreHolder();
    private long timeout = 1000L;
    private CreateInput createInput = new CreateInput();
    private String instanceId = "instanceId";
    private String componentName = "componentName";

    @Test
    public void testConstructor() {
        final ComponentStoreHolder storeHolder =
                new ComponentStoreHolder(timeout, createInput, instanceId, componentName);
        assertEquals(timeout, storeHolder.getTimeout());
        assertEquals(createInput, storeHolder.getCreateInput());
        assertEquals(instanceId, storeHolder.getInstanceId());
        assertEquals(componentName, storeHolder.getComponentName());
    }

    @Test
    public void testTimeoutAccessors() throws Exception {
        assertEquals(0L, componentStoreHolder.getTimeout());
        componentStoreHolder.setTimeout(timeout);
        assertEquals(timeout, componentStoreHolder.getTimeout());
    }

    @Test
    public void testCreateInputAccessors() throws Exception {
        assertNull(componentStoreHolder.getCreateInput());
        componentStoreHolder.setCreateInput(createInput);
        assertEquals(createInput, componentStoreHolder.getCreateInput());
    }

    @Test
    public void testInstanceId() throws Exception {
        assertNull(componentStoreHolder.getInstanceId());
        componentStoreHolder.setInstanceId(instanceId);
        assertEquals(instanceId, componentStoreHolder.getInstanceId());
    }

    @Test
    public void testComponentName() throws Exception {
        assertNull(componentStoreHolder.getComponentName());
        componentStoreHolder.setComponentName(componentName);
        assertEquals(componentName, componentStoreHolder.getComponentName());
    }
}
