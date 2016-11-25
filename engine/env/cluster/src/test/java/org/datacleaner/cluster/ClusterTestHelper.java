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
package org.datacleaner.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Schema;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.beans.CompletenessAnalyzer;
import org.datacleaner.beans.CompletenessAnalyzer.Condition;
import org.datacleaner.beans.CompletenessAnalyzerResult;
import org.datacleaner.beans.NumberAnalyzer;
import org.datacleaner.beans.NumberAnalyzerResult;
import org.datacleaner.beans.StringAnalyzer;
import org.datacleaner.beans.StringAnalyzerResult;
import org.datacleaner.beans.filter.EqualsFilter;
import org.datacleaner.beans.transform.ConcatenatorTransformer;
import org.datacleaner.beans.valuematch.ValueMatchAnalyzer;
import org.datacleaner.beans.valuematch.ValueMatchAnalyzerResult;
import org.datacleaner.beans.writers.InsertIntoTableAnalyzer;
import org.datacleaner.beans.writers.WriteBufferSizeOption;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.components.maxrows.MaxRowsFilter.Category;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.concurrent.MultiThreadedTaskRunner;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.JobStatus;
import org.datacleaner.test.TestHelper;
import org.junit.Assert;

public class ClusterTestHelper {

    /**
     * Creates a {@link DataCleanerConfiguration} object (based on a few
     * parameters), typically to use in test methods of this class.
     *
     * @param testName
     * @param multiThreaded
     * @return
     */
    public static DataCleanerConfiguration createConfiguration(final String testName, final boolean multiThreaded) {
        final JdbcDatastore csvDatastore =
                new JdbcDatastore("csv", "jdbc:h2:mem:" + testName, "org.h2.Driver", "SA", "", true);
        final UpdateableDatastoreConnection con = csvDatastore.openConnection();
        con.getUpdateableDataContext().executeUpdate(callback -> {
            final Schema schema = callback.getDataContext().getDefaultSchema();
            if (schema.getTableByName("testtable") != null) {
                return;
            }
            callback.createTable(schema, "testtable").withColumn("id").ofType(ColumnType.INTEGER).withColumn("name")
                    .ofType(ColumnType.VARCHAR).execute();
        });
        con.close();

        final Datastore databaseDatastore = TestHelper.createSampleDatabaseDatastore("orderdb");

        final DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(databaseDatastore, csvDatastore);
        final TaskRunner taskRunner;
        if (multiThreaded) {
            taskRunner = new MultiThreadedTaskRunner(20);
        } else {
            taskRunner = new SingleThreadedTaskRunner();
        }
        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider(true);
        descriptorProvider.addFilterBeanDescriptor(Descriptors.ofFilter(MaxRowsFilter.class));
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(MockTransformerThatWillFail.class));
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(ConcatenatorTransformer.class));
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(InsertIntoTableAnalyzer.class));
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(CompletenessAnalyzer.class));
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(ValueMatchAnalyzer.class));
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(MockAnalyzerWithBadReducer.class));

        final DataCleanerEnvironment environment =
                new DataCleanerEnvironmentImpl().withTaskRunner(taskRunner).withDescriptorProvider(descriptorProvider);

        return new DataCleanerConfigurationImpl().withDatastoreCatalog(datastoreCatalog).withEnvironment(environment);
    }

    /**
     * Runs a job that verifies that errors (caused by the
     * {@link MockTransformerThatWillFail} dummy component) are picked up
     * correctly from the slave nodes.
     *
     * @param configuration
     * @param virtualClusterManager
     * @return the list of errors returned, to perform further assertions
     */
    public static List<Throwable> runErrorHandlingJob(final DataCleanerConfiguration configuration,
            final ClusterManager clusterManager) {
        final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);
        jobBuilder.setDatastore("orderdb");
        jobBuilder.addSourceColumns("CUSTOMERS.CUSTOMERNUMBER");

        final TransformerComponentBuilder<MockTransformerThatWillFail> transformer =
                jobBuilder.addTransformer(MockTransformerThatWillFail.class);
        transformer.addInputColumns(jobBuilder.getSourceColumns());

        final AnalyzerComponentBuilder<CompletenessAnalyzer> analyzer =
                jobBuilder.addAnalyzer(CompletenessAnalyzer.class);
        analyzer.addInputColumns(transformer.getOutputColumns());
        analyzer.setConfiguredProperty("Conditions",
                new CompletenessAnalyzer.Condition[] { CompletenessAnalyzer.Condition.NOT_BLANK_OR_NULL });

        // build the job
        final AnalysisJob job = jobBuilder.toAnalysisJob();

        // run the job in a distributed fashion
        final DistributedAnalysisRunner runner = new DistributedAnalysisRunner(configuration, clusterManager);
        final AnalysisResultFuture resultFuture = runner.run(job);

        switch (resultFuture.getStatus()) {
        case NOT_FINISHED:
        case ERRORNOUS:
            break;
        default:
            Assert.fail("Unexpected job status: " + resultFuture.getStatus());
        }

        resultFuture.await();

        if (resultFuture.isSuccessful()) {
            Assert.fail("Job that was supposed to fail was succesful! Results: " + resultFuture.getResultMap());
        }

        Assert.assertEquals(JobStatus.ERRORNOUS, resultFuture.getStatus());

        final List<Throwable> errors = resultFuture.getErrors();

        Assert.assertNotNull(errors);
        Assert.assertFalse(errors.isEmpty());

        jobBuilder.close();

        return errors;
    }

    public static void runBasicAnalyzersJob(final DataCleanerConfiguration configuration,
            final ClusterManager clusterManager) throws Throwable {
        // build a job that concats names and inserts the concatenated names
        // into a file
        final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);
        jobBuilder.setDatastore("orderdb");
        jobBuilder.addSourceColumns("CUSTOMERS.CUSTOMERNUMBER");
        jobBuilder.addSourceColumns("CUSTOMERS.COUNTRY");

        final AnalyzerComponentBuilder<StringAnalyzer> stringAnalyzer = jobBuilder.addAnalyzer(StringAnalyzer.class);
        stringAnalyzer.addInputColumns(jobBuilder.getAvailableInputColumns(String.class));

        final AnalyzerComponentBuilder<NumberAnalyzer> numberAnalyzer = jobBuilder.addAnalyzer(NumberAnalyzer.class);
        numberAnalyzer.addInputColumns(jobBuilder.getAvailableInputColumns(Number.class));

        final AnalysisJob job = jobBuilder.toAnalysisJob();

        // run the job in a distributed fashion
        final DistributedAnalysisRunner runner = new DistributedAnalysisRunner(configuration, clusterManager);
        final AnalysisResultFuture resultFuture = runner.run(job);
        jobBuilder.close();

        Assert.assertTrue(
                resultFuture.getStatus() == JobStatus.NOT_FINISHED || resultFuture.getStatus() == JobStatus.SUCCESSFUL);

        resultFuture.await();

        if (resultFuture.isErrornous()) {
            final List<Throwable> errors = resultFuture.getErrors();
            throw errors.get(0);
        }

        Assert.assertEquals(JobStatus.SUCCESSFUL, resultFuture.getStatus());

        final List<AnalyzerResult> results = resultFuture.getResults();

        Assert.assertEquals(2, results.size());

        for (final AnalyzerResult analyzerResult : results) {
            Assert.assertNotNull(analyzerResult);
            if (analyzerResult instanceof StringAnalyzerResult) {
                final StringAnalyzerResult stringAnalyzerResult = (StringAnalyzerResult) analyzerResult;

                final InputColumn<String>[] columns = stringAnalyzerResult.getColumns();
                Assert.assertEquals(1, columns.length);

                final InputColumn<String> column = columns[0];
                Assert.assertEquals("COUNTRY", column.getName());

                // test reduction: various ways of aggregating crosstab metrics
                // - min, max, avg, sum
                Assert.assertEquals(214, stringAnalyzerResult.getRowCount(column));
                Assert.assertEquals(0, stringAnalyzerResult.getMinWords(column));
                Assert.assertEquals(2, stringAnalyzerResult.getMaxWords(column));
                Assert.assertEquals(5.34, stringAnalyzerResult.getAvgChars(column), 0.1d);
                Assert.assertEquals(1091, stringAnalyzerResult.getTotalCharCount(column));

            } else if (analyzerResult instanceof NumberAnalyzerResult) {
                final NumberAnalyzerResult numberAnalyzerResult = (NumberAnalyzerResult) analyzerResult;

                final InputColumn<? extends Number>[] columns = numberAnalyzerResult.getColumns();
                Assert.assertEquals(1, columns.length);

                final InputColumn<? extends Number> column = columns[0];
                Assert.assertEquals("CUSTOMERNUMBER", column.getName());

                Assert.assertEquals(214, numberAnalyzerResult.getRowCount(column));
                Assert.assertEquals(298175.0, numberAnalyzerResult.getSum(column).doubleValue(), 0.1);
                Assert.assertEquals(1393.34, numberAnalyzerResult.getMean(column).doubleValue(), 0.1);
                Assert.assertEquals(5106, numberAnalyzerResult.getHighestValue(column).doubleValue(), 0.1);
                Assert.assertEquals(103.0, numberAnalyzerResult.getLowestValue(column).doubleValue(), 0.1);
                Assert.assertEquals(1646.7, numberAnalyzerResult.getStandardDeviation(column).doubleValue(), 0.8);
                Assert.assertEquals(null, numberAnalyzerResult.getMedian(column));
            } else {
                Assert.fail("Unexpected analyzer result found: " + analyzerResult);
            }
        }
    }

    public static void runCompletenessAndValueMatcherAnalyzerJob(final DataCleanerConfiguration configuration,
            final ClusterManager clusterManager) throws Throwable {
        // build a job that concats names and inserts the concatenated names
        // into a file
        final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);
        jobBuilder.setDatastore("orderdb");
        jobBuilder
                .addSourceColumns("CUSTOMERS.CUSTOMERNUMBER", "CUSTOMERS.CONTACTFIRSTNAME", "CUSTOMERS.CONTACTLASTNAME",
                        "CUSTOMERS.COUNTRY", "CUSTOMERS.ADDRESSLINE2");

        final List<MetaModelInputColumn> cols = jobBuilder.getSourceColumns();
        final AnalyzerComponentBuilder<CompletenessAnalyzer> completeness =
                jobBuilder.addAnalyzer(CompletenessAnalyzer.class);
        completeness.addInputColumns(cols);
        final Condition[] conditions = new CompletenessAnalyzer.Condition[cols.size()];
        for (int i = 0; i < conditions.length; i++) {
            conditions[i] = Condition.NOT_BLANK_OR_NULL;
        }
        completeness.setConfiguredProperty("Conditions", conditions);

        final AnalyzerComponentBuilder<ValueMatchAnalyzer> valueMatch =
                jobBuilder.addAnalyzer(ValueMatchAnalyzer.class);
        valueMatch.addInputColumn(jobBuilder.getSourceColumnByName("COUNTRY"));
        valueMatch.setConfiguredProperty("Expected values",
                new String[] { "United States", "USA", "Denmark", "Danmark", "Netherlands" });

        final AnalysisJob job = jobBuilder.toAnalysisJob();

        // run the job in a distributed fashion
        final DistributedAnalysisRunner runner = new DistributedAnalysisRunner(configuration, clusterManager);
        final AnalysisResultFuture resultFuture = runner.run(job);

        jobBuilder.close();

        if (resultFuture.getStatus() == JobStatus.NOT_FINISHED) {
            resultFuture.await();

            if (resultFuture.isErrornous()) {
                final List<Throwable> errors = resultFuture.getErrors();
                throw errors.get(0);
            }
        }

        Assert.assertEquals(JobStatus.SUCCESSFUL, resultFuture.getStatus());

        final List<AnalyzerResult> results = resultFuture.getResults();
        Assert.assertEquals(2, results.size());

        for (final AnalyzerResult analyzerResult : results) {
            Assert.assertNotNull(analyzerResult);
            if (analyzerResult instanceof CompletenessAnalyzerResult) {
                // Check completeness analyzer result

                final CompletenessAnalyzerResult completenessAnalyzerResult =
                        (CompletenessAnalyzerResult) analyzerResult;

                Assert.assertEquals(193, completenessAnalyzerResult.getInvalidRowCount());

                final List<InputRow> rows = completenessAnalyzerResult.getSampleRows();
                Assert.assertNotNull(rows);

                Assert.assertTrue("No annotated rows available in CompletenessAnalyzer's result", rows.size() > 0);

            } else if (analyzerResult instanceof ValueMatchAnalyzerResult) {

                final ValueMatchAnalyzerResult valueMatchAnalyzerResult = (ValueMatchAnalyzerResult) analyzerResult;
                Assert.assertEquals(10, valueMatchAnalyzerResult.getNullCount());

                Assert.assertEquals(150, valueMatchAnalyzerResult.getUnexpectedValueCount().intValue());
                List<InputRow> rows = valueMatchAnalyzerResult.getAnnotatedRowsForUnexpectedValues().getSampleRows();
                Assert.assertTrue(rows.size() > 0);
                Assert.assertTrue(rows.size() <= 150);

                Assert.assertEquals(8, valueMatchAnalyzerResult.getCount("Denmark").intValue());
                rows = new ArrayList<>(valueMatchAnalyzerResult.getAnnotatedRowsForValue("Denmark").getSampleRows());
                Assert.assertEquals(8, rows.size());

                Collections.sort(rows, (o1, o2) -> (int) (o1.getId() - o2.getId()));

                Assert.assertEquals("MetaModelInputRow[Row[values=[145, Jytte, Petersen, Denmark, null]]]",
                        rows.get(0).toString());
                Assert.assertEquals("MetaModelInputRow[Row[values=[287, Jytte, Pedersen, Denmark, 1734 Kbh]]]",
                        rows.get(2).toString());
            } else {
                Assert.fail("Unexpected analyzer result found: " + analyzerResult);
            }
        }
    }

    public static void runExistingMaxRowsJob(final DataCleanerConfiguration configuration,
            final ClusterManager clusterManager) throws Throwable {
        final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);
        jobBuilder.setDatastore("orderdb");
        jobBuilder.addSourceColumns("CUSTOMERS.CUSTOMERNUMBER", "CUSTOMERS.CONTACTFIRSTNAME",
                "CUSTOMERS.CONTACTLASTNAME");

        final InputColumn<?> col1 = jobBuilder.getSourceColumnByName("CONTACTFIRSTNAME");
        final InputColumn<?> col2 = jobBuilder.getSourceColumnByName("CONTACTLASTNAME");

        final FilterComponentBuilder<MaxRowsFilter, Category> filter = jobBuilder.addFilter(MaxRowsFilter.class);
        filter.getComponentInstance().setFirstRow(5);
        filter.getComponentInstance().setMaxRows(20);

        final AnalyzerComponentBuilder<StringAnalyzer> analyzer = jobBuilder.addAnalyzer(StringAnalyzer.class);
        analyzer.addInputColumn(col1);
        analyzer.addInputColumn(col2);
        analyzer.setRequirement(filter, MaxRowsFilter.Category.VALID);

        final AnalysisJob job = jobBuilder.toAnalysisJob();

        jobBuilder.close();

        final DistributedAnalysisRunner runner = new DistributedAnalysisRunner(configuration, clusterManager);

        try {
            runner.run(job);
            Assert.fail("Exception expected");
        } catch (final Exception e) {
            Assert.assertEquals("Job is not distributable!", e.getMessage());
        }
    }

    /**
     * Runs a simple job that is fully distributable and should be able to
     * execute in all contexts. The job does one transformation (concatenates
     * two fields) and inserts this field, together with a source field, into
     * another table.
     *
     * @param configuration
     * @param clusterManager
     * @throws Throwable
     */
    public static void runConcatAndInsertJob(final DataCleanerConfiguration configuration,
            final ClusterManager clusterManager) throws Throwable {
        // build a job that concats names and inserts the concatenated names
        // into a file
        final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);
        jobBuilder.setDatastore("orderdb");
        jobBuilder.addSourceColumns("CUSTOMERS.CUSTOMERNUMBER", "CUSTOMERS.CONTACTFIRSTNAME",
                "CUSTOMERS.CONTACTLASTNAME");

        // concatenate firstname + lastname
        final TransformerComponentBuilder<ConcatenatorTransformer> concatenator =
                jobBuilder.addTransformer(ConcatenatorTransformer.class);
        concatenator.addInputColumn(jobBuilder.getSourceColumnByName("CONTACTFIRSTNAME"));
        concatenator.addInputColumn(jobBuilder.getSourceColumnByName("CONTACTLASTNAME"));
        concatenator.setConfiguredProperty("Separator", " ");

        // insert into CSV file
        final Datastore csvDatastore = configuration.getDatastoreCatalog().getDatastore("csv");
        final Datastore dbDatastore = configuration.getDatastoreCatalog().getDatastore("orderdb");
        final DatastoreConnection csvCon = csvDatastore.openConnection();
        final DatastoreConnection dbCon = dbDatastore.openConnection();

        try {
            final Schema schema = csvCon.getDataContext().getDefaultSchema();
            final String schemaName = schema.getName();
            final String tableName = schema.getTable(0).getName();

            final AnalyzerComponentBuilder<InsertIntoTableAnalyzer> insert =
                    jobBuilder.addAnalyzer(InsertIntoTableAnalyzer.class);
            insert.setConfiguredProperty("Datastore", csvDatastore);
            insert.addInputColumn(jobBuilder.getSourceColumnByName("CUSTOMERNUMBER"));
            insert.addInputColumn(concatenator.getOutputColumns().get(0));
            insert.setConfiguredProperty("Schema name", schemaName);
            insert.setConfiguredProperty("Table name", tableName);
            insert.setConfiguredProperty("Column names", new String[] { "id", "name" });
            insert.setConfiguredProperty("Buffer size", WriteBufferSizeOption.TINY);

            // build the job
            final AnalysisJob job = jobBuilder.toAnalysisJob();

            // run the job in a distributed fashion
            final DistributedAnalysisRunner runner = new DistributedAnalysisRunner(configuration, clusterManager);
            final AnalysisResultFuture resultFuture = runner.run(job);

            if (resultFuture.getStatus() == JobStatus.NOT_FINISHED) {
                resultFuture.await();

                if (resultFuture.isErrornous()) {
                    final List<Throwable> errors = resultFuture.getErrors();
                    throw errors.get(0);
                }
            }

            Assert.assertEquals(JobStatus.SUCCESSFUL, resultFuture.getStatus());

            // check that the file created has the same amount of records as the
            // CUSTOMER table of orderdb.
            try (DataSet ds1 = dbCon.getDataContext().query().from("CUSTOMERS").selectCount().execute();
                 DataSet ds2 = csvCon.getDataContext().query().from(tableName).selectCount().execute()) {
                Assert.assertTrue(ds1.next());
                Assert.assertTrue(ds2.next());
                Assert.assertEquals(ds1.getRow().toString(), ds2.getRow().toString());
            }

            // await multiple times to ensure that second time isn't distorting
            // the result
            resultFuture.await();
            resultFuture.await();

            // check that the analysis result elements are there...
            final Map<ComponentJob, AnalyzerResult> resultMap = resultFuture.getResultMap();
            Assert.assertEquals(1, resultMap.size());
            Assert.assertEquals("{ImmutableAnalyzerJob[name=null,analyzer=Insert into table]=214 inserts executed}",
                    resultMap.toString());

        } finally {
            dbCon.close();
            csvCon.close();
            jobBuilder.close();
        }
    }

    public static void runNoExpectedRecordsJob(final DataCleanerConfiguration configuration) throws Throwable {
        final AnalysisJob job;
        {
            try (AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration)) {
                // build a job that concats names and inserts the concatenated
                // names
                // into a file
                jobBuilder.setDatastore("orderdb");
                jobBuilder.addSourceColumns("CUSTOMERS.CUSTOMERNUMBER", "CUSTOMERS.CONTACTFIRSTNAME",
                        "CUSTOMERS.CONTACTLASTNAME");

                final FilterComponentBuilder<EqualsFilter, EqualsFilter.Category> equalsFilter =
                        jobBuilder.addFilter(EqualsFilter.class);
                equalsFilter.addInputColumn(jobBuilder.getSourceColumnByName("CUSTOMERNUMBER"));
                equalsFilter.getComponentInstance().setValues(new String[] { "-1000000" });

                final AnalyzerComponentBuilder<StringAnalyzer> stringAnalyzer =
                        jobBuilder.addAnalyzer(StringAnalyzer.class);
                stringAnalyzer.addInputColumns(jobBuilder.getAvailableInputColumns(String.class));
                stringAnalyzer.setRequirement(equalsFilter, EqualsFilter.Category.EQUALS);

                job = jobBuilder.toAnalysisJob();
            }
        }

        final DistributedAnalysisRunner analysisRunner =
                new DistributedAnalysisRunner(configuration, new ClusterManager() {
                    @Override
                    public JobDivisionManager getJobDivisionManager() {
                        throw new IllegalStateException(
                                "Since this job should yield 0 expected records, this method should not be invoked");
                    }

                    @Override
                    public AnalysisResultFuture dispatchJob(final AnalysisJob job, final DistributedJobContext context)
                            throws Exception {
                        throw new IllegalStateException(
                                "Since this job should yield 0 expected records, this method should not be invoked");
                    }
                });

        final AnalysisResultFuture resultFuture = analysisRunner.run(job);
        resultFuture.await();
        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        final List<AnalyzerResult> results = resultFuture.getResults();
        Assert.assertEquals(1, results.size());

        final AnalyzerResult analyzerResult = results.get(0);
        Assert.assertTrue(analyzerResult instanceof StringAnalyzerResult);
    }

    public static void runCancelJobJob(final DataCleanerConfiguration configuration,
            final ClusterManager clusterManager) throws Throwable {
        // build a job that concats names and inserts the concatenated names
        // into a file
        final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);
        jobBuilder.setDatastore("orderdb");
        jobBuilder.addSourceColumns("CUSTOMERS.CUSTOMERNUMBER", "CUSTOMERS.CONTACTFIRSTNAME",
                "CUSTOMERS.CONTACTLASTNAME");

        // concatenate firstname + lastname
        final TransformerComponentBuilder<ConcatenatorTransformer> concatenator =
                jobBuilder.addTransformer(ConcatenatorTransformer.class);
        concatenator.addInputColumn(jobBuilder.getSourceColumnByName("CONTACTFIRSTNAME"));
        concatenator.addInputColumn(jobBuilder.getSourceColumnByName("CONTACTLASTNAME"));
        concatenator.setConfiguredProperty("Separator", " ");

        // insert into CSV file
        final Datastore csvDatastore = configuration.getDatastoreCatalog().getDatastore("csv");
        final Datastore dbDatastore = configuration.getDatastoreCatalog().getDatastore("orderdb");
        final DatastoreConnection csvCon = csvDatastore.openConnection();
        final DatastoreConnection dbCon = dbDatastore.openConnection();

        try {
            final Schema schema = csvCon.getDataContext().getDefaultSchema();
            final String schemaName = schema.getName();
            final String tableName = schema.getTable(0).getName();

            final AnalyzerComponentBuilder<InsertIntoTableAnalyzer> insert =
                    jobBuilder.addAnalyzer(InsertIntoTableAnalyzer.class);
            insert.setConfiguredProperty("Datastore", csvDatastore);
            insert.addInputColumn(jobBuilder.getSourceColumnByName("CUSTOMERNUMBER"));
            insert.addInputColumn(concatenator.getOutputColumns().get(0));
            insert.setConfiguredProperty("Schema name", schemaName);
            insert.setConfiguredProperty("Table name", tableName);
            insert.setConfiguredProperty("Column names", new String[] { "id", "name" });
            insert.setConfiguredProperty("Buffer size", WriteBufferSizeOption.TINY);

            // build the job
            final AnalysisJob job = jobBuilder.toAnalysisJob();

            // run the job in a distributed fashion
            final DistributedAnalysisRunner runner = new DistributedAnalysisRunner(configuration, clusterManager);
            final AnalysisResultFuture resultFuture = runner.run(job);

            resultFuture.cancel();

            Assert.assertTrue(resultFuture.isCancelled());

        } finally {
            dbCon.close();
            csvCon.close();
            jobBuilder.close();
        }
    }
}

