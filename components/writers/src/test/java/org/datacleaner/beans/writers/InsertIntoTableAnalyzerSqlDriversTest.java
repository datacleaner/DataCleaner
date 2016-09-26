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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;

import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.create.TableCreationBuilder;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Table;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.database.DatabaseDriverDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.concurrent.MultiThreadedTaskRunner;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Class that tests the multi-threading execution of DataCleaner with single
 * connection to SQL servers. The drivers used are JDTS and Microsoft SQL driver.
 */
@Ignore
public class InsertIntoTableAnalyzerSqlDriversTest {

    @Test
    public void testInsertIntoSqlDatabaseJtdsDriver() throws Throwable {
        final CsvDatastore datastoreIn = new CsvDatastore("in", "src/test/resources/datastorewriter-in.csv");

        // count input lines and get columns
        final Column[] columns;
        {
            DatastoreConnection con = datastoreIn.openConnection();
            Table table = con.getDataContext().getDefaultSchema().getTables()[0];

            columns = table.getColumns();

            DataSet ds = con.getDataContext().query().from(table).selectCount().execute();
            assertTrue(ds.next());
            assertFalse(ds.next());
            ds.close();

            con.close();
        }

        final DatabaseDriverDescriptor jtdsDriver = DatabaseDriverCatalog.getDatabaseDriverByDriverClassName(
                "net.sourceforge.jtds.jdbc.Driver");
        assertNotNull(jtdsDriver);

        final String[] downloadUrls = jtdsDriver.getDownloadUrls();
        final URL downloadURL = new URI(downloadUrls[0]).toURL();
        final URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(classLoader, downloadURL);

        Class.forName("net.sourceforge.jtds.jdbc.Driver");

        final JdbcDatastore myjdbcDatastore = new JdbcDatastore("myConection",
                "jdbc:jtds:sqlserver://localhost:1433/AdventureWorks2014;useUnicode=true;characterEncoding=UTF-8",
                "net.sourceforge.jtds.jdbc.Driver", "sa", "SqlServer!", false);
        final Connection connection = myjdbcDatastore.createConnection();
        assertEquals(false, connection.isClosed());
        {
            final DatastoreConnection con = myjdbcDatastore.openConnection();
            final UpdateableDataContext dc = (UpdateableDataContext) con.getDataContext();
            final Table tableByName = dc.getSchemaByName("dbo").getTableByName("yourtable");
            if (tableByName == null) {

                dc.executeUpdate(new UpdateScript() {
                    @Override
                    public void run(UpdateCallback callback) {
                        TableCreationBuilder createTableBuilder = callback.createTable(dc.getDefaultSchema(),
                                "yourtable");
                        for (Column column : columns) {
                            createTableBuilder = createTableBuilder.withColumn(column.getName()).ofType(
                                    ColumnType.VARCHAR);
                        }
                        createTableBuilder.execute();
                    }
                });
                con.close();
            }
        }

        // run a "copy lines" job with multithreading
        {
            final DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(datastoreIn);

            final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withEnvironment(
                    new DataCleanerEnvironmentImpl().withTaskRunner(new MultiThreadedTaskRunner(10)))
                    .withDatastoreCatalog(datastoreCatalog);

            final AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration);
            try {

                ajb.setDatastore(datastoreIn);

                ajb.addSourceColumns(columns);

                AnalyzerComponentBuilder<InsertIntoTableAnalyzer> analyzerJobBuilder = ajb.addAnalyzer(
                        InsertIntoTableAnalyzer.class);

                analyzerJobBuilder.addInputColumns(ajb.getSourceColumns());
                analyzerJobBuilder.setConfiguredProperty("Datastore", myjdbcDatastore);
                String[] columnName = "col0,col1,col2,col3,col4".split(",");
                analyzerJobBuilder.setConfiguredProperty("Column names", columnName);
                analyzerJobBuilder.setConfiguredProperty("Schema name", "dbo");
                analyzerJobBuilder.setConfiguredProperty("Table name", "yourTable");
                analyzerJobBuilder.setConfiguredProperty("Truncate table", true);

                assertTrue(analyzerJobBuilder.isConfigured());

                AnalysisRunner runner = new AnalysisRunnerImpl(configuration);
                AnalysisResultFuture resultFuture = runner.run(ajb.toAnalysisJob());

                if (resultFuture.isErrornous()) {
                    throw resultFuture.getErrors().get(0);
                }
                assertTrue(resultFuture.isSuccessful());
            } finally {
                ajb.close();
            }
        }
    }

    /**
     * The test fails sometimes because of concurrency issue in Metamodel caused by a
     * unsynchronized method and based most likely on Microsoft SQL driver
     * implementation. We recommend allowing multiple connections for datastore
     * in this case, or run DC single threaded if the datastore has one
     * connection to database.
     * 
     * @throws Throwable
     */
    @Test
    public void testInsertIntoSqlDatabaseMicrosoftDriver() throws Throwable {
        final CsvDatastore datastoreIn = new CsvDatastore("in", "src/test/resources/datastorewriter-in.csv");

        // count input lines and get columns
        final Column[] columns;
        {
            final DatastoreConnection con = datastoreIn.openConnection();
            final Table table = con.getDataContext().getDefaultSchema().getTables()[0];
            columns = table.getColumns();
            final DataSet ds = con.getDataContext().query().from(table).selectCount().execute();
            assertTrue(ds.next());
            assertFalse(ds.next());
            ds.close();

            con.close();
        }

        final File sqlOfficialFile = new File("src/test/resources/sqljdbc42.jar");
        assertTrue(sqlOfficialFile.exists());
        final URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(classLoader, sqlOfficialFile.toURI().toURL());

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        final JdbcDatastore myjdbcDatastore = new JdbcDatastore("myConection",
                "jdbc:sqlserver://localhost:1433;databaseName=AdventureWorks2014;user=sa;password=SqlServer!",
                "com.microsoft.sqlserver.jdbc.SQLServerDriver", "sa", "SqlServer!", false);
        final Connection connection = myjdbcDatastore.createConnection();
        assertEquals(false, connection.isClosed());
        {
            final DatastoreConnection con = myjdbcDatastore.openConnection();
            final UpdateableDataContext dc = (UpdateableDataContext) con.getDataContext();
            Table tableByName = dc.getSchemaByName("dbo").getTableByName("yourtable");
            if (tableByName == null) {

                dc.executeUpdate(new UpdateScript() {
                    @Override
                    public void run(UpdateCallback callback) {
                        TableCreationBuilder createTableBuilder = callback.createTable(dc.getDefaultSchema(),
                                "yourtable");
                        for (Column column : columns) {
                            createTableBuilder = createTableBuilder.withColumn(column.getName()).ofType(
                                    ColumnType.VARCHAR);
                        }
                        createTableBuilder.execute();
                    }
                });
                con.close();
            }
        }

        // run a "copy lines" job with multithreading
        {
            final DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(datastoreIn);

            final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withEnvironment(
                    new DataCleanerEnvironmentImpl().withTaskRunner(new MultiThreadedTaskRunner(10)))
                    .withDatastoreCatalog(datastoreCatalog);

            final AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration);
            try {

                ajb.setDatastore(datastoreIn);

                ajb.addSourceColumns(columns);

                AnalyzerComponentBuilder<InsertIntoTableAnalyzer> analyzerJobBuilder = ajb.addAnalyzer(
                        InsertIntoTableAnalyzer.class);

                analyzerJobBuilder.addInputColumns(ajb.getSourceColumns());
                analyzerJobBuilder.setConfiguredProperty("Datastore", myjdbcDatastore);
                final String[] columnName = "col0,col1,col2,col3,col4".split(",");
                analyzerJobBuilder.setConfiguredProperty("Column names", columnName);
                analyzerJobBuilder.setConfiguredProperty("Schema name", "dbo");
                analyzerJobBuilder.setConfiguredProperty("Table name", "yourTable");
                analyzerJobBuilder.setConfiguredProperty("Truncate table", true);

                assertTrue(analyzerJobBuilder.isConfigured());

                final AnalysisRunner runner = new AnalysisRunnerImpl(configuration);
                final AnalysisResultFuture resultFuture = runner.run(ajb.toAnalysisJob());

                if (resultFuture.isErrornous()) {
                    throw resultFuture.getErrors().get(0);
                }
                assertTrue(resultFuture.isSuccessful());
            } finally {
                ajb.close();
            }
        }
    }
}
