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

import org.datacleaner.monitor.server.components.ComponentHandler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ComponentConfigHolderTest {
    private ComponentConfigHolder componentConfigHolder = new ComponentConfigHolder();
    private long timeoutMs = 1000L;
    private CreateInput createInput = new CreateInput();
    private String componentId = "componentId";
    private String componentName = "componentName";
    private ComponentHandler componentHandler = new ComponentHandler(null, componentName);

    @Test
    public void testClose() throws Exception {
        componentConfigHolder.close();
    }

    @Test
    public void testTimeoutMsAccessors() throws Exception {
        assertEquals(0L, componentConfigHolder.getTimeoutMs());
        componentConfigHolder.setTimeoutMs(timeoutMs);
        assertEquals(timeoutMs, componentConfigHolder.getTimeoutMs());
    }

    @Test
    public void testComponentIdAccessors() throws Exception {
        assertNull(componentConfigHolder.getComponentId());
        componentConfigHolder.setComponentId(componentId);
        assertEquals(componentId, componentConfigHolder.getComponentId());
    }

    @Test
    public void testCreateInputAccessors() throws Exception {
        assertNull(componentConfigHolder.getCreateInput());
        componentConfigHolder.setCreateInput(createInput);
        assertEquals(createInput, componentConfigHolder.getCreateInput());
    }

    @Test
    public void testHandlerAccessors() throws Exception {
        assertNull(componentConfigHolder.getHandler());
        componentConfigHolder.setHandler(componentHandler);
        assertEquals(componentHandler, componentConfigHolder.getHandler());
    }

    @Test
    public void testComponentNameAccessors() throws Exception {
        assertNull(componentConfigHolder.getComponentName());
        componentConfigHolder.setComponentName(componentName);
        assertEquals(componentName, componentConfigHolder.getComponentName());
    }
}