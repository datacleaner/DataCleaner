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

import org.junit.Assert;
import org.junit.Test;

public class SwaggerInfoTest {
    private final SwaggerInfo swaggerInfo = new SwaggerInfo();

    @Test
    public void testSetAndGetDescription() throws Exception {
        Assert.assertNotNull(swaggerInfo.getDescription());
        final String description = "description";
        swaggerInfo.setDescription(description);
        Assert.assertEquals(description, swaggerInfo.getDescription());
    }

    @Test
    public void testSetAndGetVersion() throws Exception {
        Assert.assertNotNull(swaggerInfo.getVersion());
        final String version = "1.0";
        swaggerInfo.setVersion(version);
        Assert.assertEquals(version, swaggerInfo.getVersion());
    }

    @Test
    public void testSetAndGetTitle() throws Exception {
        Assert.assertNotNull(swaggerInfo.getTitle());
        final String title = "title";
        swaggerInfo.setTitle(title);
        Assert.assertEquals(title, swaggerInfo.getTitle());
    }

    @Test
    public void testSetAndGetTermsOfService() throws Exception {
        Assert.assertNotNull(swaggerInfo.getTermsOfService());
        final String termsOfService = "terms of service...";
        swaggerInfo.setTermsOfService(termsOfService);
        Assert.assertEquals(termsOfService, swaggerInfo.getTermsOfService());
    }

    @Test
    public void testSetAndGetContact() throws Exception {
        Assert.assertNotNull(swaggerInfo.getContact());
        final SwaggerContact swaggerContact = new SwaggerContact();
        final String email = "john@doe.com";
        swaggerContact.setEmail(email);
        swaggerInfo.setContact(swaggerContact);
        Assert.assertEquals(email, swaggerInfo.getContact().getEmail());
    }

    @Test
    public void testSetAndGetLicense() throws Exception {
        Assert.assertNotNull(swaggerInfo.getLicense());
        final SwaggerLicense swaggerLicense = new SwaggerLicense();
        final String name = "GNU GPL";
        swaggerLicense.setName(name);
        swaggerInfo.setLicense(swaggerLicense);
        Assert.assertEquals(name, swaggerInfo.getLicense().getName());
    }
}