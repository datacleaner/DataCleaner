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

import static org.junit.Assert.*;

public class ComponentsStoreHolderTest {
    private ComponentsStoreHolder componentsStoreHolder = new ComponentsStoreHolder();
    private long timeout = 1000L;
    private CreateInput createInput = new CreateInput();
    private String componentId = "componentId";
    private String componentName = "componentName";

    @Test
    public void testConstructor() {
        ComponentsStoreHolder storeHolder = new ComponentsStoreHolder(timeout, createInput, componentId, componentName);
        assertEquals(timeout, storeHolder.getTimeout());
        assertEquals(createInput, storeHolder.getCreateInput());
        assertEquals(componentId, storeHolder.getComponentId());
        assertEquals(componentName, storeHolder.getComponentName());
    }

    @Test
    public void testTimeoutAccessors() throws Exception {
        assertEquals(0L, componentsStoreHolder.getTimeout());
        componentsStoreHolder.setTimeout(timeout);
        assertEquals(timeout, componentsStoreHolder.getTimeout());
    }

    @Test
    public void testCreateInputAccessors() throws Exception {
        assertNull(componentsStoreHolder.getCreateInput());
        componentsStoreHolder.setCreateInput(createInput);
        assertEquals(createInput, componentsStoreHolder.getCreateInput());
    }

    @Test
    public void testComponentId() throws Exception {
        assertNull(componentsStoreHolder.getComponentId());
        componentsStoreHolder.setComponentId(componentId);
        assertEquals(componentId, componentsStoreHolder.getComponentId());
    }

    @Test
    public void testComponentName() throws Exception {
        assertNull(componentsStoreHolder.getComponentName());
        componentsStoreHolder.setComponentName(componentName);
        assertEquals(componentName, componentsStoreHolder.getComponentName());
    }
}