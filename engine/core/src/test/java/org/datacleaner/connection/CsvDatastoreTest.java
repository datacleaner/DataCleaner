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
package org.datacleaner.connection;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.Resource;

import junit.framework.TestCase;

public class CsvDatastoreTest extends TestCase {

    public void testConvertToColumnWithNoSchema() throws Exception {
        final CsvDatastore datastore = new CsvDatastore("foo", "src/test/resources/projects.csv");
        final SchemaNavigator schemaNavigator = datastore.openConnection().getSchemaNavigator();
        Column col = schemaNavigator.convertToColumn("product");
        assertNotNull(col);

        final Table table = datastore.openConnection().getDataContext().getDefaultSchema().getTable(0);
        assertEquals("projects.csv", table.getName());
        col = schemaNavigator.convertToColumn("projects.csv.product");
        assertNotNull(col);
    }

    public void testGetResourceBasedOnString() throws Exception {
        final CsvDatastore datastore = new CsvDatastore("foo", "src/test/resources/projects.csv");
        final Resource resource = datastore.getResource();
        assertNotNull(resource);
        assertEquals("projects.csv", resource.getName());
    }
}
