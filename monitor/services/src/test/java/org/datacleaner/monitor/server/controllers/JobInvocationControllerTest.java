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
package org.datacleaner.monitor.server.controllers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.schema.ColumnType;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.server.job.MockJobEngineManager;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;

import junit.framework.TestCase;

public class JobInvocationControllerTest extends TestCase {

    public void testInvokeDatabaseSchema() throws Throwable {
        final Repository repository = new FileRepository("src/test/resources/example_repo");
        final TenantContextFactory contextFactory =
                new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(), new MockJobEngineManager());
        final JobInvocationController controller = new JobInvocationController();
        controller._contextFactory = contextFactory;

        final JobInvocationPayload sourceRecords = new JobInvocationPayload();
        sourceRecords.addRow(new Object[] { "kasper@eobjects.dk" });
        sourceRecords.addRow(new Object[] { "kasper.sorensen@humaninference.com" });

        final JobInvocationPayload result = controller.invokeJob("tenant1", "email_standardizer", sourceRecords);

        assertEquals("[Username, Domain]", result.getColumns().toString());

        final List<JobInvocationRowData> rows = result.getRows();
        assertEquals(2, rows.size());

        assertEquals("[kasper, eobjects.dk]", Arrays.toString(rows.get(0).getValues()));
        assertEquals("[kasper.sorensen, humaninference.com]", Arrays.toString(rows.get(1).getValues()));
    }

    public void testInvokeFileWithExtensionNameSchema() throws Throwable {
        final Repository repository = new FileRepository("src/test/resources/example_repo");
        final TenantContextFactory contextFactory =
                new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(), new MockJobEngineManager());
        final JobInvocationController controller = new JobInvocationController();
        controller._contextFactory = contextFactory;

        final JobInvocationPayload sourceRecords = new JobInvocationPayload();

        final int input = 123;
        sourceRecords.addRow(new Object[] { input });

        final JobInvocationPayload result = controller.invokeJob("tenant1", "random_number_generation", sourceRecords);

        assertEquals("[Random number]", result.getColumns().toString());

        final List<JobInvocationRowData> rows = result.getRows();
        assertEquals(1, rows.size());

        final Object[] values = rows.get(0).getValues();
        assertEquals(1, values.length);
        final Number number = (Number) values[0];
        assertTrue(number.doubleValue() >= 0);
        assertTrue(number.doubleValue() <= input);
    }

    public void testInvokeFileWithoutAnalyzers() throws Throwable {
        final Repository repository = new FileRepository("src/test/resources/example_repo");
        final TenantContextFactory contextFactory =
                new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(), new MockJobEngineManager());
        final JobInvocationController controller = new JobInvocationController();
        controller._contextFactory = contextFactory;

        final JobInvocationPayload sourceRecords = new JobInvocationPayload();

        sourceRecords.addRow(new Object[] { "foo", "bar" });

        final JobInvocationPayload result = controller.invokeJob("tenant1", "concat_job_no_analyzers", sourceRecords);

        final List<JobInvocationRowData> rows = result.getRows();
        assertEquals(1, rows.size());

        final Object[] values = rows.get(0).getValues();
        assertEquals("[bar foo]", Arrays.toString(values));
    }

    public void testInvokeFileWithShortColumnPaths() throws Throwable {
        final Repository repository = new FileRepository("src/test/resources/example_repo");
        final TenantContextFactory contextFactory =
                new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(), new MockJobEngineManager());
        final JobInvocationController controller = new JobInvocationController();
        controller._contextFactory = contextFactory;

        final JobInvocationPayload sourceRecords = new JobInvocationPayload();

        sourceRecords.addRow(new Object[] { "foo", "bar" });

        final JobInvocationPayload result =
                controller.invokeJob("tenant1", "concat_job_short_column_paths", sourceRecords);

        final List<JobInvocationRowData> rows = result.getRows();
        assertEquals(1, rows.size());

        final Object[] values = rows.get(0).getValues();
        assertEquals("[bar foo]", Arrays.toString(values));
    }

    public void testInvokeFileWithoutDatastoreMatch() throws Throwable {
        final Repository repository = new FileRepository("src/test/resources/example_repo");
        final TenantContextFactory contextFactory =
                new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(), new MockJobEngineManager());
        final JobInvocationController controller = new JobInvocationController();
        controller._contextFactory = contextFactory;

        final JobInvocationPayload sourceRecords = new JobInvocationPayload();

        sourceRecords.addRow(new Object[] { "foo", "bar" });

        final JobInvocationPayload result = controller.invokeJob("tenant1", "concat_job_no_datastore", sourceRecords);

        final List<JobInvocationRowData> rows = result.getRows();
        assertEquals(1, rows.size());

        final Object[] values = rows.get(0).getValues();
        assertEquals("[bar foo]", Arrays.toString(values));
    }

    public void testInvokeJobMapped() throws Throwable {
        final Repository repository = new FileRepository("src/test/resources/example_repo");
        final TenantContextFactory contextFactory =
                new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(), new MockJobEngineManager());
        final JobInvocationController controller = new JobInvocationController();
        controller._contextFactory = contextFactory;

        final Map<String, Object> value1 = new HashMap<>();
        value1.put("EMAIL", "kasper@eobjects.dk");
        final Map<String, Object> value2 = new HashMap<>();
        value2.put("EMAIL", "kasper.sorensen@humaninference.com");
        final List<Map<String, Object>> inputColumnValueMap = Arrays.asList(value1, value2);
        final JobInvocationPayload sourceRecords = new JobInvocationPayload();
        sourceRecords.setColumnValueMap(inputColumnValueMap);

        final JobInvocationPayload result = controller.invokeJobMapped("tenant1", "email_standardizer", sourceRecords);

        assertEquals("[Username, Domain]", result.getColumns().toString());

        final List<JobInvocationRowData> rows = result.getRows();
        assertEquals(2, rows.size());
        assertEquals("[kasper, eobjects.dk]", Arrays.toString(rows.get(0).getValues()));
        assertEquals("[kasper.sorensen, humaninference.com]", Arrays.toString(rows.get(1).getValues()));

        final List<Map<String, Object>> columnValueMap = result.getColumnValueMap();
        assertEquals(2, columnValueMap.size());
        assertEquals("kasper", columnValueMap.get(0).get("Username"));
        assertEquals("eobjects.dk", columnValueMap.get(0).get("Domain"));
        assertEquals("kasper.sorensen", columnValueMap.get(1).get("Username"));
        assertEquals("humaninference.com", columnValueMap.get(1).get("Domain"));
    }

    public void testInvokeJobWithAnalyzersMapped() throws Throwable {
        final Repository repository = new FileRepository("src/test/resources/example_repo");
        final TenantContextFactory contextFactory = new TenantContextFactoryImpl(repository,
                new DataCleanerEnvironmentImpl(), new MockJobEngineManager());
        final JobInvocationController controller = new JobInvocationController();
        controller._contextFactory = contextFactory;

        final Map<String, Object> value1 = new HashMap<>();
        value1.put("EMAIL", "kasper@eobjects.dk");
        final Map<String, Object> value2 = new HashMap<>();
        value2.put("EMAIL", "jasper@eobjects.dk");
        final List<Map<String, Object>> inputColumnValueMap = Arrays.asList(value1, value2);
        final JobInvocationPayload sourceRecords = new JobInvocationPayload();
        sourceRecords.setColumnValueMap(inputColumnValueMap);

        final String tableName = "test_table";
        final String columnName = "email";
        final JdbcDatastore jdbcDatastore = createDataBaseTable(tableName, columnName);

        controller.invokeJobWithAnalyzersMapped("tenant7", "email_standardizer_to_db", sourceRecords);

        final UpdateableDatastoreConnection con = jdbcDatastore.openConnection();
        final DataContext dc = con.getDataContext();
        final DataSet ds = dc.query().from(tableName).select(columnName).orderBy(columnName).execute();
        assertTrue(ds.next());
        assertEquals("Row[values=[jasper@eobjects.dk]]", ds.getRow().toString());
        assertTrue(ds.next());
        assertEquals("Row[values=[kasper@eobjects.dk]]", ds.getRow().toString());
        assertFalse(ds.next());
        con.close();
    }

    private JdbcDatastore createDataBaseTable(String tableName, String columnName) {
        final JdbcDatastore jdbcDatastore = new JdbcDatastore("my_datastore", "jdbc:hsqldb:mem:UpdateTable_test",
                "org.hsqldb.jdbcDriver");
        final UpdateableDatastoreConnection con = jdbcDatastore.openConnection();
        final UpdateableDataContext dc = con.getUpdateableDataContext();
        dc.executeUpdate(new UpdateScript() {
            @Override
            public void run(UpdateCallback cb) {
                cb.createTable(dc.getDefaultSchema(), tableName).withColumn(columnName).ofType(ColumnType.VARCHAR)
                .execute();
            }
        });
        con.close();
        return jdbcDatastore;
    }

}
