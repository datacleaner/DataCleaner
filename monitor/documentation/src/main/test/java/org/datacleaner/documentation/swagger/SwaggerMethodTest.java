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

    @Test
    public void testSetAndGetSecurity() throws Exception {
        Assert.assertNotNull(swaggerMethod.getSecurity());
        final String[] security = new String[] { "security" };
        swaggerMethod.setSecurity(security);
        Assert.assertEquals(security[0], swaggerMethod.getSecurity()[0]);
    }
}