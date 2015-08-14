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
 package org.datacleaner.metamodel.datahub;

import junit.framework.TestCase;

import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.datacleaner.metamodel.datahub.utils.DatahubColumnBuilder;


public class DatahubDataContextTest extends TestCase  
{
    public void testDummy() {
        // there should be at least one test  method
    }
    
    public void xtestMDMRepoConnection() {
        String host = "mdmregtest.humaninference.com";
        Integer port = 8443;
        String username = "cdiadmin";
        String password = "cdi123";
        String tenantId = "mdmregtest";
        boolean https = false;
        
        DatahubDataContext context = new DatahubDataContext(host, port, username, password, tenantId, https);
        Schema schema = context.testGetMainSchema();
        //assertEquals(4, schema.getTableCount());
        //assertEquals(13, schema.getTableByName("CUSTOMERS").getColumnCount());
        
    }

    public void xtestMonitorDemoRepoConnection() {
        String host = "localhost";
        Integer port = 8081;
        String username = "admin";
        String password = "admin";
        String tenantId = "demo";
        boolean https = false;
        
        DatahubDataContext context = new DatahubDataContext(host, port, username, password, tenantId, https);
        Schema schema = context.testGetMainSchema();
        assertEquals(6, schema.getTableCount());
        assertEquals(13, schema.getTableByName("CUSTOMERS").getColumnCount());
        
    }

    public void xtestMonitorDemoMaterializeSchema() {
        String host = "localhost";
        Integer port = 8081;
        String username = "admin";
        String password = "admin";
        String tenantId = "demo";
        boolean https = false;
        
        DatahubDataContext context = new DatahubDataContext(host, port, username, password, tenantId, https);
        
        DatahubTable table = new DatahubTable();
        table.setName("CUSTOMERS");
        Column[] columns = new Column[1];
        DatahubColumnBuilder builder = new DatahubColumnBuilder();
        builder.withName("name").withNumber(2);
        columns[0] = builder.build();
        int maxRows = 5;
        DataSet dataset = context.testMaterializeMainSchemaTable(table, columns, maxRows);
        assertNotNull(dataset);
        
    }

}
