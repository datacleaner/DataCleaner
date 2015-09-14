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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.datacleaner.restclient.ProcessStatelessOutput;
import org.junit.Before;
import org.junit.Test;

public class ProcessStatelessOutputTest {
    private ProcessStatelessOutput processStatelessOutput = new ProcessStatelessOutput();
    private JsonNode resultMock = null;
    private JsonNode rows = null;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        resultMock = createNiceMock(JsonNode.class);
        rows = getJsonNode("rows");
        replay(resultMock);
    }

    @Test
    public void testProperties() {
        assertNull(processStatelessOutput.result);
        assertNull(processStatelessOutput.rows);

        processStatelessOutput.result = resultMock;
        processStatelessOutput.rows = rows;

        assertEquals(resultMock, processStatelessOutput.result);
        assertEquals(rows, processStatelessOutput.rows);
    }

    private JsonNode getJsonNode(Object value) {
        return objectMapper.convertValue(value, JsonNode.class);
    }
}