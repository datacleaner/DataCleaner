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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.pojo.PojoDataContext;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.api.ComponentContext;
import org.datacleaner.api.InputColumn;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.FileDatastore;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.connection.PojoDatastore;
import org.datacleaner.connection.UpdateableDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.MetricDescriptor;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DeleteFromTableAnalyzerTest {

    private static final String TEST_TABLE_NAME = "test_table";
    private static final String VARCHAR_COLUMN_NAME = "foo";
    private static final String INTEGER_COLUMN_NAME = "bar";
    private static final String VARCHAR_COLUMN_VALUE = "StringValue";
    private static final int INTEGER_COLUMN_VALUE = 1;

    private UpdateableDatastore updateableDatastore;

    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        updateableDatastore = new PojoDatastore("my datastore");
        final UpdateableDatastoreConnection con = updateableDatastore.openConnection();
        final UpdateableDataContext dc = con.getUpdateableDataContext();
        if (dc.getDefaultSchema().getTableByName(TEST_TABLE_NAME) == null) {
            dc.executeUpdate(new UpdateScript() {
                @Override
                public void run(UpdateCallback cb) {
                    cb.createTable(dc.getDefaultSchema(), TEST_TABLE_NAME).withColumn(VARCHAR_COLUMN_NAME)
                            .ofType(ColumnType.VARCHAR).withColumn(INTEGER_COLUMN_NAME).ofType(ColumnType.INTEGER)
                            .execute();
                    cb.insertInto(TEST_TABLE_NAME).value(VARCHAR_COLUMN_NAME, VARCHAR_COLUMN_VALUE)
                            .value(INTEGER_COLUMN_NAME, INTEGER_COLUMN_VALUE).execute();
                }
            });
        }
        con.close();
    }

    @Test
    public void shouldReturnTheCorrectMetricsFromDescriptor() throws Exception {
        AnalyzerDescriptor<?> descriptor = Descriptors.ofAnalyzer(DeleteFromTableAnalyzer.class);
        Set<MetricDescriptor> metrics = descriptor.getResultMetrics();
        assertThat(metrics.size(), is(3));
        WriteDataResult result = new WriteDataResultImpl(10, 5, null, null, null);
        assertThat(descriptor.getResultMetric("Inserts").getValue(result, null).intValue(), is(10));
        assertThat(descriptor.getResultMetric("Updates").getValue(result, null).intValue(), is(5));
        assertThat(descriptor.getResultMetric("Errornous rows").getValue(result, null).intValue(), is(0));
    }

    @Test
    public void shouldThrowExceptionForIncorrectErrorHandlingFile() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Error log file does not have required column header: foo");
        final DeleteFromTableAnalyzer deleteFromTable = new DeleteFromTableAnalyzer();
        deleteFromTable.datastore = updateableDatastore;
        deleteFromTable.tableName = "test_table";
        deleteFromTable.errorHandlingOption = ErrorHandlingOption.SAVE_TO_FILE;
        deleteFromTable.errorLogFile = new File("src/test/resources/invalid-error-handling-file.csv");
        deleteFromTable.conditionColumnNames = new String[] { "col1", "col2" };

        InputColumn<Object> col1 = new MockInputColumn<Object>("in1", Object.class);
        InputColumn<Object> col2 = new MockInputColumn<Object>("in2", Object.class);
        deleteFromTable.conditionValues = new InputColumn[] { col1, col2 };

        deleteFromTable.validate();
    }

    @Test
    public void shouldCorrectlyAppendErrorHandlingInfo() throws Exception {
        final File file = new File("target/valid-error-handling-file-for-update.csv.csv");
        FileHelper.copy(new File("src/test/resources/valid-error-handling-file-for-update.csv"), file);

        final DeleteFromTableAnalyzer deleteFromTable = new DeleteFromTableAnalyzer();
        deleteFromTable.datastore = updateableDatastore;
        deleteFromTable.tableName = TEST_TABLE_NAME;
        deleteFromTable.errorHandlingOption = ErrorHandlingOption.SAVE_TO_FILE;
        deleteFromTable.errorLogFile = file;
        deleteFromTable.conditionColumnNames = new String[] { INTEGER_COLUMN_NAME };

        InputColumn<Object> inputColumn = new MockInputColumn<Object>(INTEGER_COLUMN_NAME, Object.class);
        deleteFromTable.conditionValues = new InputColumn[] { inputColumn };

        deleteFromTable.validate();
        deleteFromTable.init();

        deleteFromTable.run(new MockInputRow().put(inputColumn, "blabla"), 1);

        WriteDataResult result = deleteFromTable.getResult();
        assertThat(result.getUpdatesCount(), is(0));
        assertThat(result.getErrorRowCount(), is(1));
        assertThat(FileHelper.readFileAsString(file).replaceAll("\n", "\\[newline\\]"),
                is(equalTo("foo,bar,extra1,update_table_error_message,extra2[newline]" + //
                        "f,b,e1,m,e2[newline]" + //
                        "\"\",\"\",\"\",\"Could not convert blabla to number\",\"\"")));//
    }

    @Test
    public void shouldSuccessFullyDeleteRowFromTable() throws Exception {
        final DeleteFromTableAnalyzer deleteFromTable = new DeleteFromTableAnalyzer();
        deleteFromTable.datastore = updateableDatastore;
        deleteFromTable.tableName = TEST_TABLE_NAME;
        deleteFromTable.conditionColumnNames = new String[] { VARCHAR_COLUMN_NAME, INTEGER_COLUMN_NAME };
        deleteFromTable.errorHandlingOption = ErrorHandlingOption.SAVE_TO_FILE;
        deleteFromTable.errorLogFile = null;
        deleteFromTable._componentContext = EasyMock.createMock(ComponentContext.class);

        InputColumn<Object> col1 = new MockInputColumn<Object>(VARCHAR_COLUMN_NAME, Object.class);
        InputColumn<Object> col2 = new MockInputColumn<Object>("in2", Object.class);

        deleteFromTable.conditionValues = new InputColumn[] { col1, col2 };

        deleteFromTable.validate();
        deleteFromTable.init();

        deleteFromTable.run(new MockInputRow().put(col1, VARCHAR_COLUMN_VALUE), 1);

        WriteDataResult result = deleteFromTable.getResult();
        assertThat(result.getUpdatesCount(), is(1));
        assertThat(result.getErrorRowCount(), is(0));
        FileDatastore errorDatastore = result.getErrorDatastore();
        DatastoreConnection con = errorDatastore.openConnection();
        Table table = con.getDataContext().getDefaultSchema().getTables()[0];
        assertThat(Arrays.toString(table.getColumnNames()), is(equalTo("[foo, bar, update_table_error_message]")));

        DataSet ds = con.getDataContext().query().from(table).select(table.getColumns()).execute();
        assertThat(ds.next(), is(false));
        ds.close();

        con.close();
    }

}
