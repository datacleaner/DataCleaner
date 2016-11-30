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

import java.util.Arrays;

import org.apache.metamodel.schema.Column;

import junit.framework.TestCase;

public class ExcelDatastoreTest extends TestCase {

    private static final String NR_ENTRIES_IN_SPREADSHEET2007 = "4";

    public void testOpenSpreadsheetXls() throws Exception {
        final Datastore datastore = new ExcelDatastore("foobar", null, "src/test/resources/Spreadsheet2003.xls");
        assertEquals("foobar", datastore.getName());
        final DatastoreConnection con = datastore.openConnection();
        assertNotNull(con);

        final Column col1 = con.getSchemaNavigator().convertToColumn("string");
        assertNotNull(col1);

        final Column col2 = con.getSchemaNavigator().convertToColumn("number");
        assertNotNull(col2);

        final Column col3 = con.getSchemaNavigator().convertToColumn("date");
        assertNotNull(col3);
        assertEquals("Column[name=date,columnNumber=2,type=VARCHAR,nullable=true,nativeType=null,columnSize=null]",
                col3.toString());
    }

    public void testOpenSpreadsheetXlsx() throws Exception {
        final Datastore datastore = new ExcelDatastore("foobar", null, "src/test/resources/Spreadsheet2007.xlsx");
        assertEquals("foobar", datastore.getName());
        final DatastoreConnection con = datastore.openConnection();
        assertNotNull(con);

        final Column col1 = con.getSchemaNavigator().convertToColumn("string");
        assertNotNull(col1);

        final Column col2 = con.getSchemaNavigator().convertToColumn("number");
        assertNotNull(col2);

        final Column col3 = con.getSchemaNavigator().convertToColumn("date");
        assertNotNull(col3);
        assertEquals("Column[name=date,columnNumber=2,type=STRING,nullable=true,nativeType=null,columnSize=null]",
                col3.toString());

        assertEquals(NR_ENTRIES_IN_SPREADSHEET2007,
                con.getDataContext().executeQuery("select count(string) from Sheet1").toRows().get(0).getValue(0)
                        .toString());
    }

    public void testCustomColumnNaming() throws Exception {
        final DatastoreConnection con = new ExcelDatastore("foobar", null, "src/test/resources/Spreadsheet2007.xlsx",
                Arrays.asList("first", "second", "third")).openConnection();

        assertNotNull(con.getSchemaNavigator().convertToColumn("first"));
        assertNotNull(con.getSchemaNavigator().convertToColumn("second"));

        final Column col3 = con.getSchemaNavigator().convertToColumn("third");
        assertNotNull(col3);
        assertEquals("Column[name=third,columnNumber=2,type=STRING,nullable=true,nativeType=null,columnSize=null]",
                col3.toString());

        assertEquals(NR_ENTRIES_IN_SPREADSHEET2007,
                con.getDataContext().executeQuery("select count(third) from Sheet1").toRows().get(0).getValue(0)
                        .toString());
    }

    public void testToString() throws Exception {
        final Datastore datastore = new ExcelDatastore("foobar", null, "src/test/resources/Spreadsheet2007.xlsx");
        assertEquals("ExcelDatastore[name=foobar]", datastore.toString());
    }
}
