package org.datacleaner.documentation.swagger;

import org.junit.Assert;
import org.junit.Test;

public class SwaggerResponseTest {
    private final SwaggerResponse swaggerResponse = new SwaggerResponse();

    @Test
    public void testSetAndGetDescription() throws Exception {
        Assert.assertNotNull(swaggerResponse.getDescription());
        final String description = "description";
        swaggerResponse.setDescription(description);
        Assert.assertEquals(description, swaggerResponse.getDescription());
    }
}