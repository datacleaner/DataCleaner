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
package org.datacleaner.documentation.swagger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class SwaggerMethodTest {
    private final SwaggerMethod swaggerMethod = new SwaggerMethod();

    @Test
    public void testSetAndGetTags() throws Exception {
        Assert.assertNotNull(swaggerMethod.getTags());
        final List<String> tags = new ArrayList<>();
        final String testTag = "test-tag";
        tags.add(testTag);
        swaggerMethod.setTags(tags);
        Assert.assertEquals(testTag, swaggerMethod.getTags().get(0));
    }

    @Test
    public void testSetAndGetSummary() throws Exception {
        Assert.assertNotNull(swaggerMethod.getSummary());
        final String summary = "summary";
        swaggerMethod.setSummary(summary);
        Assert.assertEquals(summary, swaggerMethod.getSummary());
    }

    @Test
    public void testSetAndGetDescription() throws Exception {
        Assert.assertNotNull(swaggerMethod.getDescription());
        final String description = "description";
        swaggerMethod.setDescription(description);
        Assert.assertEquals(description, swaggerMethod.getDescription());
    }

    @Test
    public void testSetAndGetOperationId() throws Exception {
        Assert.assertNotNull(swaggerMethod.getOperationId());
        final String operationId = "operation-id";
        swaggerMethod.setOperationId(operationId);
        Assert.assertEquals(operationId, swaggerMethod.getOperationId());
    }

    @Test
    public void testSetAndGetConsumes() throws Exception {
        Assert.assertNotNull(swaggerMethod.getConsumes());
        final String[] consumes = new String[] { "application/json" };
        swaggerMethod.setConsumes(consumes);
        Assert.assertEquals(consumes[0], swaggerMethod.getConsumes()[0]);
    }

    @Test
    public void testSetAndGetProduces() throws Exception {
        Assert.assertNotNull(swaggerMethod.getProduces());
        final String[] produces = new String[] { "application/json" };
        swaggerMethod.setProduces(produces);
        Assert.assertEquals(produces[0], swaggerMethod.getProduces()[0]);
    }

    @Test
    public void testSetAndGetParameters() throws Exception {
        Assert.assertNotNull(swaggerMethod.getParameters());
        final SwaggerParameter parameter = new SwaggerParameter();
        final String name = "parameter-name";
        parameter.setName(name);
        final SwaggerParameter[] swaggerParameters = new SwaggerParameter[] { parameter };
        swaggerMethod.setParameters(swaggerParameters);
        Assert.assertEquals(name, swaggerMethod.getParameters()[0].getName());
    }

    @Test
    public void testSetAndGetResponses() throws Exception {
        Assert.assertNotNull(swaggerMethod.getResponses());
        final Map<String, SwaggerResponse> responseMap = new HashMap<>();
        final SwaggerResponse swaggerResponse = new SwaggerResponse();
        final String key = "key";
        final String description = "description";
        swaggerResponse.setDescription(description);
        responseMap.put(key, swaggerResponse);
        swaggerMethod.setResponses(responseMap);
        Assert.assertEquals(description, swaggerMethod.getResponses().get(key).getDescription());
    }
}