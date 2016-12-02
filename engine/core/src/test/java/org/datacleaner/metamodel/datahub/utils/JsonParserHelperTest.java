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
package org.datacleaner.metamodel.datahub.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Table;
import org.datacleaner.metamodel.datahub.DataHubSchema;

import com.fasterxml.jackson.core.JsonParseException;

import junit.framework.TestCase;

public class JsonParserHelperTest extends TestCase {

    public void testShouldParseAllSchemasAndTables() throws JsonParseException, IOException {

        final String jsonString = "{\"schemas\":[" + "{\"tables\":[],\"name\":\"INFORMATION_SCHEMA\"},{\"tables\":["
                + "{\"name\":\"CUSTOMERS\",\"columns\":["
                + "{\"indexed\":true,\"quote\":\"\\\"\",\"primaryKey\":true,\"name\":\"CUSTOMERNUMBER\","
                + "\"remarks\":\"\",\"nullable\":false,\"type\":\"INTEGER\",\"nativeType\":\"INTEGER\","
                + "\"size\":\"0\",\"number\": 0},"
                + "{\"indexed\":true,\"quote\":null,\"primaryKey\":false,\"name\":\"CUSTOMERNAME\","
                + "\"remarks\":null,\"nullable\":false,\"type\":\"VARCHAR\",\"nativeType\":\"VARCHAR\","
                + "\"size\":\"50\",\"number\":1},"
                + "{\"indexed\":false,\"quote\":\"\\\"\",\"primaryKey\":false,\"name\":\"CONTACTLASTNAME\","
                + "\"remarks\":\"\",\"nullable\":false,\"type\":\"VARCHAR\",\"nativeType\":\"VARCHAR\","
                + "\"size\":\"50\",\"number\":2},"
                + "{\"indexed\":false,\"quote\":\"\\\"\",\"primaryKey\":false,\"name\":\"CONTACTFIRSTNAME\","
                + "\"remarks\":\"\",\"nullable\":false,\"type\":\"VARCHAR\",\"nativeType\":\"VARCHAR\","
                + "\"size\":\"50\",\"number\":3},"
                + "{\"indexed\":false,\"quote\":\"\\\"\",\"primaryKey\":false,\"name\":\"PHONE\",\"remarks\":\"\","
                + "\"nullable\":false,\"type\":\"VARCHAR\",\"nativeType\":\"VARCHAR\",\"size\":\"50\",\"number\": 4}]},"
                + "{\"name\":\"SUPPLIERS\",\"columns\":["
                + "{\"indexed\":true,\"quote\":\"\\\"\",\"primaryKey\":true,\"name\":\"SUPPLIERNUMBER\","
                + "\"remarks\":\"\",\"nullable\":false,\"type\":\"INTEGER\",\"nativeType\":\"INTEGER\","
                + "\"size\":\"0\",\"number\":0},"
                + "{\"indexed\":false,\"quote\":\"\\\"\",\"primaryKey\":false,\"name\":\"SUPPLIERNAME\","
                + "\"remarks\":\"\",\"nullable\":false,\"type\":\"VARCHAR\","
                + "\"nativeType\":\"VARCHAR\",\"size\":\"50\",\"number\":1},"
                + "{\"indexed\":false,\"quote\":\"\\\"\",\"primaryKey\":false,\"name\":\"ACTIVE\","
                + "\"remarks\":\"\",\"nullable\":false,\"type\":\"BOOLEAN\",\"nativeType\":\"VARCHAR\","
                + "\"size\":\"50\",\"number\":2}]}],\"name\":\"PUBLIC\"}]}";
        final InputStream is = new ByteArrayInputStream(jsonString.getBytes());
        final JsonSchemasResponseParser parser = new JsonSchemasResponseParser();
        final DataHubSchema schema = parser.parseJsonSchema(is);
        assertNotNull(schema);
        assertEquals("PUBLIC", schema.getName());
        assertEquals(2, schema.getTableCount());
        final Table customersTable = schema.getTableByName("CUSTOMERS");
        assertNotNull(customersTable);
        assertEquals(5, customersTable.getColumnCount());
        final Column customernameColumn = customersTable.getColumnByName("CUSTOMERNAME");
        assertEquals(true, customernameColumn.isIndexed());
        assertEquals(false, customernameColumn.isPrimaryKey());
        assertEquals(false, customernameColumn.isNullable().booleanValue());
        assertEquals(null, customernameColumn.getQuote());
        assertEquals(null, customernameColumn.getRemarks());
        assertEquals(customersTable, customernameColumn.getTable());
        assertEquals(ColumnType.VARCHAR, customernameColumn.getType());
        assertEquals("VARCHAR", customernameColumn.getNativeType());
        assertEquals(50, customernameColumn.getColumnSize().intValue());
        assertEquals(1, customernameColumn.getColumnNumber());
        final Table suppliersTable = schema.getTableByName("SUPPLIERS");
        assertNotNull(suppliersTable);
        assertEquals(3, suppliersTable.getColumnCount());
        assertEquals(ColumnType.INTEGER, suppliersTable.getColumnByName("SUPPLIERNUMBER").getType());
        assertEquals(ColumnType.VARCHAR, suppliersTable.getColumnByName("SUPPLIERNAME").getType());
        assertEquals(ColumnType.BOOLEAN, suppliersTable.getColumnByName("ACTIVE").getType());
    }

    public void testShouldOnlyParseGoldenRecordDataStore() throws IOException {
        final String jsonString = "[{\"name\":\"CSV datastore for Person Golden Records\",\"description\":"
                + "\"The CSV datastore containing offboarded golden records for person configuration\","
                + "\"type\":\"CsvDatastore\"},{\"name\":\"Enrichment Service Call Logs\",\"description\":"
                + "\"The CSV data store containing enrichment service call logs\",\"type\":\"CsvDatastore\"},"
                + "{\"name\":\"Golden records\",\"description\":\"Virtual datastore for Golden Records\","
                + "\"type\":\"GoldenRecordDatastore\"},"
                + "{\"name\":\"GoldenRecord Organization Export\",\"description\":null,\"type\":\"CsvDatastore\"},"
                + "{\"name\":\"GoldenRecord Person Export\",\"description\":null,\"type\":\"CsvDatastore\"},"
                + "{\"name\":\"Golden_records20150810133835556.CSV\",\"description\":null,\"type\":\"CsvDatastore\"},"
                + "{\"name\":\"Golden_records20150810133955872.CSV\",\"description\":null,\"type\":\"CsvDatastore\"},"
                + "{\"name\":\"MDM datastore\",\"description\":\"Physical datastore of MDM\",\"type\":\"MDMDatastore\"}]";
        final InputStream is = new ByteArrayInputStream(jsonString.getBytes());
        final JsonSchemasResponseParser parser = new JsonSchemasResponseParser();

        final List<String> names = parser.parseDataStoreArray(is);

        assertEquals(1, names.size());
    }

}
