package org.datacleaner.documentation.swagger;

import org.junit.Assert;
import org.junit.Test;

public class SwaggerExternalDocsTest {
    private final SwaggerExternalDocs swaggerExternalDocs = new SwaggerExternalDocs();

    @Test
    public void testSetAndGetDescription() throws Exception {
        Assert.assertNotNull(swaggerExternalDocs.getDescription());
        final String description = "description";
        swaggerExternalDocs.setDescription(description);
        Assert.assertEquals(description, swaggerExternalDocs.getDescription());
    }

    @Test
    public void testSetAndGetUrl() throws Exception {
        Assert.assertNotNull(swaggerExternalDocs.getUrl());
        final String url = "http://localhost";
        swaggerExternalDocs.setUrl(url);
        Assert.assertEquals(url, swaggerExternalDocs.getUrl());
    }
}