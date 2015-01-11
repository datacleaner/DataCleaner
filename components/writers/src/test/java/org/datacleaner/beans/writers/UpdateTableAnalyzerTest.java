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
package org.datacleaner.beans.writers;

import java.io.File;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.datacleaner.api.ComponentContext;
import org.datacleaner.api.InputColumn;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;

public class UpdateTableAnalyzerTest extends TestCase {

    private JdbcDatastore jdbcDatastore;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (jdbcDatastore == null) {
            jdbcDatastore = new JdbcDatastore("my datastore", "jdbc:hsqldb:mem:UpdateTable_testErrorHandlingOption",
                    "org.hsqldb.jdbcDriver");
            final UpdateableDatastoreConnection con = jdbcDatastore.openConnection();
            final UpdateableDataContext dc = con.getUpdateableDataContext();
            if (dc.getDefaultSchema().getTableByName("test_table") == null) {
                dc.executeUpdate(new UpdateScript() {
                    @Override
                    public void run(UpdateCallback cb) {
                        Table table = cb.createTable(dc.getDefaultSchema(), "test_table").withColumn("foo")
                                .ofType(ColumnType.VARCHAR).withColumn("bar").ofType(ColumnType.INTEGER)
                                .withColumn("baz").ofType(ColumnType.VARCHAR).execute();

                        cb.insertInto(table).value("foo", "a").value("bar", 1).value("baz", "lorem").execute();
                        cb.insertInto(table).value("foo", "b").value("bar", 2).value("baz", "ipsum").execute();
                        cb.insertInto(table).value("foo", "c").value("bar", 3).value("baz", "dolor").execute();
                        cb.insertInto(table).value("foo", "d").value("bar", 4).value("baz", "sit").execute();
                        cb.insertInto(table).value("foo", "e").value("bar", 5).value("baz", "amet").execute();
                    }
                });
            }
            con.close();
        }
    }

    public void testErrorHandlingToInvalidFile() throws Exception {
        final UpdateTableAnalyzer updateTableAnalyzer = new UpdateTableAnalyzer();
        updateTableAnalyzer.datastore = jdbcDatastore;
        updateTableAnalyzer.tableName = "test_table";
        updateTableAnalyzer.columnNames = new String[] { "foo", "bar" };
        updateTableAnalyzer.errorHandlingOption = ErrorHandlingOption.SAVE_TO_FILE;
        updateTableAnalyzer.errorLogFile = new File("src/test/resources/invalid-error-handling-file.csv");

        InputColumn<Object> col1 = new MockInputColumn<Object>("in1", Object.class);
        InputColumn<Object> col2 = new MockInputColumn<Object>("in2", Object.class);

        updateTableAnalyzer.values = new InputColumn[] { col1, col2 };

        try {
            updateTableAnalyzer.init();
            fail("Exception expected");
        } catch (IllegalStateException e) {
            assertEquals("Error log file does not have required column header: foo", e.getMessage());
        }
    }

    public void testVanillaScenario() throws Exception {
        InputColumn<Object> col1 = new MockInputColumn<Object>("in1", Object.class);
        InputColumn<Object> col2 = new MockInputColumn<Object>("in2", Object.class);
        InputColumn<Object> col3 = new MockInputColumn<Object>("in3", Object.class);

        final UpdateTableAnalyzer updateTableAnalyzer = new UpdateTableAnalyzer();
        updateTableAnalyzer.datastore = jdbcDatastore;
        updateTableAnalyzer.tableName = "test_table";
        updateTableAnalyzer.columnNames = new String[] { "baz", "foo" };
        updateTableAnalyzer.values = new InputColumn<?>[] { col3, col1 };
        updateTableAnalyzer.conditionColumnNames = new String[] { "bar" };
        updateTableAnalyzer.conditionValues = new InputColumn<?>[] { col2 };
        updateTableAnalyzer._componentContext = EasyMock.createMock(ComponentContext.class);
        updateTableAnalyzer.validate();
        updateTableAnalyzer.init();
        updateTableAnalyzer.run(new MockInputRow().put(col1, "aaa").put(col2, 1).put(col3, "hello"), 1);
        updateTableAnalyzer.run(new MockInputRow().put(col1, "bbb").put(col2, 2).put(col3, "world"), 1);
        WriteDataResult result = updateTableAnalyzer.getResult();

        assertEquals(0, result.getErrorRowCount());
        assertEquals(0, result.getWrittenRowCount());
        assertEquals(2, result.getUpdatesCount());

        UpdateableDatastoreConnection con = jdbcDatastore.openConnection();
        DataContext dc = con.getDataContext();
        DataSet ds = dc.query().from("test_table").select("foo", "bar", "baz").orderBy("bar").execute();
        assertTrue(ds.next());
        assertEquals("Row[values=[aaa, 1, hello]]", ds.getRow().toString());
        assertTrue(ds.next());
        assertEquals("Row[values=[bbb, 2, world]]", ds.getRow().toString());
        assertTrue(ds.next());
        assertEquals("Row[values=[c, 3, dolor]]", ds.getRow().toString());
        assertTrue(ds.next());
        assertEquals("Row[values=[d, 4, sit]]", ds.getRow().toString());
        assertTrue(ds.next());
        assertEquals("Row[values=[e, 5, amet]]", ds.getRow().toString());
        assertFalse(ds.next());
        ds.close();
    }

    public void testUpdateCSV() throws Exception {
        final File file = new File("target/example_updated.csv");
        FileHelper.copy(new File("src/test/resources/example_updated.csv"), file);

        final CsvDatastore datastore = new CsvDatastore("example", file.getPath(), null, ',', "UTF8");
        final UpdateableDatastoreConnection connection = datastore.openConnection();
        final DataContext dataContext = connection.getDataContext();
        final Schema schema = dataContext.getDefaultSchema();
        final Table table = schema.getTable(0);

        final UpdateTableAnalyzer updateTableAnalyzer = new UpdateTableAnalyzer();
        updateTableAnalyzer.datastore = datastore;
        updateTableAnalyzer.schemaName = schema.getName();
        updateTableAnalyzer.tableName = table.getName();
        updateTableAnalyzer.columnNames = new String[] { "name" };
        updateTableAnalyzer.conditionColumnNames = new String[] { "id" };
        updateTableAnalyzer.errorHandlingOption = ErrorHandlingOption.SAVE_TO_FILE;
        updateTableAnalyzer._componentContext = EasyMock.createMock(ComponentContext.class);

        InputColumn<Object> inputId = new MockInputColumn<Object>("id", Object.class);
        InputColumn<Object> inputNewName = new MockInputColumn<Object>("new_name", Object.class);
        updateTableAnalyzer.values = new InputColumn[] { inputNewName };
        updateTableAnalyzer.conditionValues = new InputColumn[] { inputId };

        updateTableAnalyzer.validate();
        updateTableAnalyzer.init();

        updateTableAnalyzer.run(new MockInputRow().put(inputId, 1).put(inputNewName, "foo"), 1);
        updateTableAnalyzer.run(new MockInputRow().put(inputId, "2").put(inputNewName, "bar"), 1);
        updateTableAnalyzer.run(new MockInputRow().put(inputId, 3).put(inputNewName, "baz"), 1);

        WriteDataResult result = updateTableAnalyzer.getResult();
        assertEquals(0, result.getErrorRowCount());
        assertEquals(0, result.getWrittenRowCount());
        assertEquals(3, result.getUpdatesCount());

        DataSet dataSet = dataContext.query().from(table).select("id", "name").execute();
        assertTrue(dataSet.next());
        assertEquals("Row[values=[4, hans]]", dataSet.getRow().toString());
        assertTrue(dataSet.next());
        assertEquals("Row[values=[5, manuel]]", dataSet.getRow().toString());
        assertTrue(dataSet.next());
        assertEquals("Row[values=[6, ankit]]", dataSet.getRow().toString());
        assertTrue(dataSet.next());
        assertEquals("Row[values=[1, foo]]", dataSet.getRow().toString());
        assertTrue(dataSet.next());
        assertEquals("Row[values=[2, bar]]", dataSet.getRow().toString());
        assertTrue(dataSet.next());
        assertEquals("Row[values=[3, baz]]", dataSet.getRow().toString());
        assertFalse(dataSet.next());

        connection.close();
    }
}
