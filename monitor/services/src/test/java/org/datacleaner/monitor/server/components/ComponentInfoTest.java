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
package org.datacleaner.monitor.server.components;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertTrue;

import org.datacleaner.restclient.ComponentConfiguration;
import org.datacleaner.restclient.ComponentList.ComponentInfo;
import org.junit.Before;
import org.junit.Test;

public class ComponentInfoTest {
    private ComponentInfo componentInfo = new ComponentInfo();
    private String name = "name";
    private String description = "description";
    private String createURL = "create URL";
    private ComponentConfiguration componentConfigurationMock = null;

    @Before
    public void setUp() {
        componentConfigurationMock = createNiceMock(ComponentConfiguration.class);

        replay(componentConfigurationMock);
    }

    @Test
    public void testGetName() throws Exception {
        assertTrue(componentInfo.getName().isEmpty());
        componentInfo.setName(name);
        assertTrue(componentInfo.getName().equals(name));
    }

    @Test
    public void testSetName() throws Exception {
        componentInfo.setName(name);
        assertTrue(componentInfo.getName().equals(name));
    }

    @Test
    public void testGetDescription() throws Exception {
        assertTrue(componentInfo.getDescription().isEmpty());
        componentInfo.setDescription(description);
        assertTrue(componentInfo.getDescription().equals(description));
    }

    @Test
    public void testSetDescription() throws Exception {
        componentInfo.setDescription(description);
        assertTrue(componentInfo.getDescription().equals(description));
    }

    @Test
    public void testGetCreateURL() throws Exception {
        assertTrue(componentInfo.getCreateURL().isEmpty());
        componentInfo.setCreateURL(createURL);
        assertTrue(componentInfo.getCreateURL().equals(createURL));
    }

    @Test
    public void testSetCreateURL() throws Exception {
        componentInfo.setCreateURL(createURL);
        assertTrue(componentInfo.getCreateURL().equals(createURL));
    }
}