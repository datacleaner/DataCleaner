package org.datacleaner.documentation.swagger;

import org.junit.Assert;
import org.junit.Test;

public class SwaggerLicenseTest {
    private final SwaggerLicense swaggerLicense = new SwaggerLicense();

    @Test
    public void testSetAndGetName() throws Exception {
        Assert.assertNotNull(swaggerLicense.getName());
        final String name = "name";
        swaggerLicense.setName(name);
        Assert.assertEquals(name, swaggerLicense.getName());
    }

    @Test
    public void testSetAndGetUrl() throws Exception {
        Assert.assertNotNull(swaggerLicense.getUrl());
        final String url = "url";
        swaggerLicense.setUrl(url);
        Assert.assertEquals(url, swaggerLicense.getUrl());
    }
}