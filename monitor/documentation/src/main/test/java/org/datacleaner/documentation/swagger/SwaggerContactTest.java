package org.datacleaner.documentation.swagger;

import org.junit.Assert;
import org.junit.Test;

public class SwaggerContactTest {
    private final SwaggerContact swaggerContact = new SwaggerContact();

    @Test
    public void testSetAndGetEmail() throws Exception {
        Assert.assertNotNull(swaggerContact.getEmail());
        final String email = "john@doe.com";
        swaggerContact.setEmail(email);
        Assert.assertEquals(email, swaggerContact.getEmail());
    }
}