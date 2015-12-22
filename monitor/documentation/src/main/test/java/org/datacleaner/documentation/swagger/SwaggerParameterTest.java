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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class SwaggerParameterTest {
    private final SwaggerParameter swaggerParameter = new SwaggerParameter();

    @Test
    public void testSetAndGetIn() throws Exception {
        Assert.assertNotNull(swaggerParameter.getIn());
        final String in = SwaggerParameter.In.BODY.getValue();
        swaggerParameter.setIn(in);
        Assert.assertEquals(in, swaggerParameter.getIn());
    }

    @Test
    public void testSetAndGetName() throws Exception {
        Assert.assertNotNull(swaggerParameter.getName());
        final String name = "name";
        swaggerParameter.setName(name);
        Assert.assertEquals(name, swaggerParameter.getName());
    }

    @Test
    public void testSetAndGetDescription() throws Exception {
        Assert.assertNotNull(swaggerParameter.getDescription());
        final String description = "description";
        swaggerParameter.setDescription(description);
        Assert.assertEquals(description, swaggerParameter.getDescription());
    }

    @Test
    public void testSetAndGetRequired() throws Exception {
        Assert.assertFalse(swaggerParameter.getRequired());
        swaggerParameter.setRequired(true);
        Assert.assertEquals(true, swaggerParameter.getRequired());
    }

    @Test
    public void testSetAndGetType() throws Exception {
        Assert.assertNotNull(swaggerParameter.getType());
        final String type = "type";
        swaggerParameter.setType(type);
        Assert.assertEquals(type, swaggerParameter.getType());
    }

    @Test
    public void testSetAndGetItems() throws Exception {
        Assert.assertNotNull(swaggerParameter.getItems());
        final Map<String, String> itemsMap = new HashMap<>();
        final String key = "key";
        final String value = "value";
        itemsMap.put(key, value);
        swaggerParameter.setItems(itemsMap);
        Assert.assertEquals(value, swaggerParameter.getItems().get(key));
    }

    @Test
    public void testSetAndGetSchema() throws Exception {
        Assert.assertNotNull(swaggerParameter.getSchema());
        final Map<String, String> schema = new HashMap<>();
        final String key = "key";
        final String value = "value";
        schema.put(key, value);
        swaggerParameter.setSchema(schema);
        Assert.assertEquals(value, swaggerParameter.getSchema().get(key));
    }

    @Test
    public void testSetTypeByClass() throws Exception {
        Assert.assertNotNull(swaggerParameter.getType());

        swaggerParameter.setTypeByClass(String.class);
        Assert.assertEquals(SwaggerParameter.Type.STRING.getValue(), swaggerParameter.getType());

        swaggerParameter.setTypeByClass(Integer.class);
        Assert.assertEquals(SwaggerParameter.Type.INTEGER.getValue(), swaggerParameter.getType());

        swaggerParameter.setTypeByClass(Boolean.class);
        Assert.assertEquals(SwaggerParameter.Type.BOOLEAN.getValue(), swaggerParameter.getType());

        swaggerParameter.setTypeByClass(Class.class);
        Assert.assertEquals(SwaggerParameter.Type.OBJECT.getValue(), swaggerParameter.getType());
    }
}