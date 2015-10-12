package org.datacleaner.documentation.swagger;

import org.junit.Assert;
import org.junit.Test;

public class SwaggerDefinitionsTest {
    private final SwaggerDefinitions swaggerDefinitions = new SwaggerDefinitions();

    @Test
    public void testSetAndGetList() throws Exception {
        Assert.assertNotNull(swaggerDefinitions.getList());
        final String[] list = new String[] { "item1", "item2" };
        swaggerDefinitions.setList(list);
        Assert.assertEquals(list[0], swaggerDefinitions.getList()[0]);
    }
}