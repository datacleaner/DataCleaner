package org.datacleaner.documentation.swagger;

import org.junit.Assert;
import org.junit.Test;

public class SwaggerTagTest {
    private final SwaggerTag swaggerTag = new SwaggerTag();

    @Test
    public void testSetAndGetName() throws Exception {
        Assert.assertNotNull(swaggerTag.getName());
        final String name = "tag-name";
        swaggerTag.setName(name);
        Assert.assertEquals(name, swaggerTag.getName());
    }

    @Test
    public void testSetAndGetDescription() throws Exception {
        Assert.assertNotNull(swaggerTag.getDescription());
        final String description = "description";
        swaggerTag.setDescription(description);
        Assert.assertEquals(description, swaggerTag.getDescription());
    }
}