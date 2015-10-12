package org.datacleaner.documentation.swagger;

import org.junit.Assert;
import org.junit.Test;

public class SwaggerSecurityDefinitionsTest {
    private final SwaggerSecurityDefinitions swaggerSecurityDefinitions = new SwaggerSecurityDefinitions();

    @Test
    public void testSetAndGetList() throws Exception {
        Assert.assertNotNull(swaggerSecurityDefinitions.getList());
        final String name = "security-definition";
        final String[] list = new String[] { name };
        swaggerSecurityDefinitions.setList(list);
        Assert.assertEquals(name, swaggerSecurityDefinitions.getList()[0]);
    }
}