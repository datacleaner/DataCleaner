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

import java.util.List;

import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;

import junit.framework.TestCase;

/**
 * Integration test, can only run if mdm is running on the expected host and port
 * @author hetty
 *
 */
public class DatahubDataContextTest extends TestCase {

    private DatahubDataContext createContext() {
        String host = "mdmregtest.humaninference.com";
        Integer port = 8443;
        String tenantId = "mdmregtest";
        boolean https = true;
        boolean acceptUnverifiedSslPeers = true;
        String securityMode = "cas";
        String username = "cdiadmin";
        String password = "cdi123";
        return new DatahubDataContext(null);
    }

    public void testDummy() {
        
    }

    public void xtestMDMRepoGetSchema() {
        
        DatahubDataContext context = createContext();
        Schema schema = context.getSchemaByName("GoldenRecords");
        assertEquals(2, schema.getTableCount());
        assertEquals(162, schema.getTableByName("person").getColumnCount());
        assertEquals(153, schema.getTableByName("organization").getColumnCount());
        
    }

    public void xtestExecuteQuery() {
        DatahubDataContext context = createContext();
        Schema schema = context.getSchemaByName("GoldenRecords");
        Table personTable = schema.getTableByName("person");
        Column[] columns = personTable.getColumns();

        Query query = new Query();
        query.select(columns);
        query.from(personTable);
        query.setMaxRows(50);
        query.setFirstRow(1);
        DataSet result = context.executeQuery(query);
        List<Row> rows = result.toRows();
        assertEquals(50, rows.size());
        assertTrue(result.next());
        assertEquals(columns.length, result.getRow().size());
        
    }
    
    public void xtestExecuteQueryWithWhereClause() {
        
        DatahubDataContext context = createContext();
        Schema schema = context.getSchemaByName("GoldenRecords");
        Table personTable = schema.getTableByName("person");
        Column[] columns = personTable.getColumns();
        Column whereColumn = personTable.getColumnByName("name1.name");

        Query query = new Query();
        query.select(columns);
        query.from(personTable);
        query.setMaxRows(50);
        query.setFirstRow(1);
        query.where(whereColumn, OperatorType.EQUALS_TO, "Dimmendaal");
        
        DataSet result = context.executeQuery(query);
        
        List<Row> rows = result.toRows();
        assertEquals(1, rows.size());
        assertTrue(result.next());
        Row resultRow = result.getRow();
        assertEquals("Dimmendaal", resultRow.getValue(whereColumn));
        
    }

}
