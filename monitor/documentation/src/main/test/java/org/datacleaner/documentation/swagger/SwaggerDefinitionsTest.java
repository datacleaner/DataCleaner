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