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
package org.datacleaner.components.fuse;

import java.util.Arrays;
import java.util.List;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.MetaModelHelper;
import org.apache.metamodel.data.Row;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.OutputDataStreamJob;
import org.datacleaner.job.TransformerJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.concurrent.MultiThreadedTaskRunner;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.ListResult;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.MockOutputDataStreamAnalyzer;
import org.datacleaner.test.TestHelper;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class FuseStreamsComponentIntegrationTest {

    private static final int COUNT_EMPLOYEES = 23;
    private static final int COUNT_CUSTOMERS = 122;

    private final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
    private final DataCleanerConfigurationImpl singleThreadedConfiguration = new DataCleanerConfigurationImpl()
            .withDatastores(datastore);
    private final DataCleanerConfigurationImpl multiThreadedConfiguration = new DataCleanerConfigurationImpl()
            .withDatastores(datastore).withEnvironment(
                    new DataCleanerEnvironmentImpl().withTaskRunner(new MultiThreadedTaskRunner(4)));

    @Test
    public void testAssumptionsAboutOrderdb() throws Exception {
        try (final DatastoreConnection connection = datastore.openConnection()) {
            final DataContext dataContext = connection.getDataContext();

            final Row countCustomers = MetaModelHelper.executeSingleRowQuery(dataContext,
                    dataContext.query().from("customers").selectCount().toQuery());
            Assert.assertEquals(COUNT_CUSTOMERS, countCustomers.getValue(0));

            final Row countEmployees = MetaModelHelper.executeSingleRowQuery(dataContext,
                    dataContext.query().from("employees").selectCount().toQuery());
            Assert.assertEquals(COUNT_EMPLOYEES, countEmployees.getValue(0));
        }
    }

    @Test(timeout = 20 * 1000)
    public void testUnionTablesSingleThreaded() throws Throwable {
        testUnionTables(singleThreadedConfiguration);
    }

    @Test(timeout = 20 * 1000)
    public void testUnionTablesMultiThreaded() throws Throwable {
        testUnionTables(multiThreadedConfiguration);
    }

    private static void testUnionTables(DataCleanerConfiguration configuration) throws Throwable {
        final AnalysisJob job;
        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {
            ajb.setDatastore("orderdb");
            ajb.addSourceColumns("customers.contactfirstname", "customers.contactlastname");
            ajb.addSourceColumns("employees.firstname", "employees.lastname");

            final CoalesceUnit unit1 = new CoalesceUnit("firstname", "contactfirstname");
            final CoalesceUnit unit2 = new CoalesceUnit("lastname", "contactlastname");
            final CoalesceUnit[] units = new CoalesceUnit[] { unit1, unit2 };

            final TransformerComponentBuilder<FuseStreamsComponent> fuse = ajb
                    .addTransformer(FuseStreamsComponent.class);
            fuse.addInputColumns(ajb.getSourceColumns());
            fuse.setConfiguredProperty(FuseStreamsComponent.PROPERTY_UNITS, units);

            final AnalysisJobBuilder fusedStreamJobBuilder = fuse
                    .getOutputDataStreamJobBuilder(FuseStreamsComponent.OUTPUT_DATA_STREAM_NAME);
            final List<MetaModelInputColumn> fusedColumns = fusedStreamJobBuilder.getSourceColumns();
            Assert.assertEquals("[MetaModelInputColumn[output.firstname], MetaModelInputColumn[output.lastname]]",
                    fusedColumns.toString());

            final AnalyzerComponentBuilder<MockAnalyzer> analyzer = fusedStreamJobBuilder
                    .addAnalyzer(MockAnalyzer.class);
            analyzer.addInputColumns(fusedColumns);

            job = ajb.toAnalysisJob();
        }

        Assert.assertNotNull(job);

        final AnalysisRunnerImpl runner = new AnalysisRunnerImpl(configuration);
        final AnalysisResultFuture resultFuture = runner.run(job);

        Assert.assertNotNull(resultFuture);

        resultFuture.await();
        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        final List<AnalyzerResult> results = resultFuture.getResults();
        Assert.assertEquals(1, results.size());

        // expect that the number of records kept in the list is equal to the
        // size of BOTH "employees" and "customers" tables.
        final ListResult<?> result = (ListResult<?>) results.get(0);
        Assert.assertEquals(COUNT_CUSTOMERS + COUNT_EMPLOYEES, result.getValues().size());
    }

    @Test(timeout = 20 * 1000)
    public void testFuseOutputDataStreamsSingleThreaded() throws Throwable {
        testFuseOutputDataStreams(singleThreadedConfiguration);
    }

    @Test(timeout = 20 * 1000)
    public void testFuseOutputDataStreamsMultiThreaded() throws Throwable {
        testFuseOutputDataStreams(multiThreadedConfiguration);
    }

    private static void testFuseOutputDataStreams(DataCleanerConfiguration configuration) throws Throwable {
        final AnalysisJob job;
        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {
            ajb.setDatastore("orderdb");
            ajb.addSourceColumns("customers.customernumber");

            // add an analyzer to create two streams
            final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer1 = ajb
                    .addAnalyzer(MockOutputDataStreamAnalyzer.class);
            analyzer1.addInputColumns(ajb.getSourceColumns());

            final AnalysisJobBuilder streamJobBuilder1 = analyzer1
                    .getOutputDataStreamJobBuilder(MockOutputDataStreamAnalyzer.STREAM_NAME1);
            final AnalysisJobBuilder streamJobBuilder2 = analyzer1
                    .getOutputDataStreamJobBuilder(MockOutputDataStreamAnalyzer.STREAM_NAME2);

            // add fuse streams component to both streams
            final TransformerComponentBuilder<FuseStreamsComponent> fuse1 = streamJobBuilder1
                    .addTransformer(FuseStreamsComponent.class);
            final TransformerComponentBuilder<FuseStreamsComponent> fuse2 = streamJobBuilder2.addTransformer(fuse1);
            Assert.assertSame(fuse1, fuse2);

            // add input columns from both streams
            fuse1.addInputColumns(streamJobBuilder1.getSourceColumns());
            fuse1.addInputColumns(streamJobBuilder2.getSourceColumns());

            final CoalesceUnit unit = new CoalesceUnit(streamJobBuilder1.getSourceColumns().get(0), streamJobBuilder2
                    .getSourceColumns().get(0));
            final CoalesceUnit[] units = new CoalesceUnit[] { unit };
            fuse1.setConfiguredProperty(FuseStreamsComponent.PROPERTY_UNITS, units);

            // now consume the fused output
            final AnalysisJobBuilder fusedStreamJobBuilder = fuse1
                    .getOutputDataStreamJobBuilder(FuseStreamsComponent.OUTPUT_DATA_STREAM_NAME);
            final AnalyzerComponentBuilder<MockAnalyzer> mockAnalyzerBuilder = fusedStreamJobBuilder
                    .addAnalyzer(MockAnalyzer.class);
            mockAnalyzerBuilder.addInputColumns(fusedStreamJobBuilder.getSourceColumns());

            job = ajb.toAnalysisJob();
        }

        // assert on the structure of the job and get a reference to the mock
        // analyzer
        final AnalyzerJob mockAnalyzer;
        {
            final AnalyzerJob analyzer1 = job.getAnalyzerJobs().get(0);
            Assert.assertEquals(MockOutputDataStreamAnalyzer.class, analyzer1.getDescriptor().getComponentClass());

            final OutputDataStreamJob[] outputDataStreamJobs = analyzer1.getOutputDataStreamJobs();
            Assert.assertEquals(2, outputDataStreamJobs.length);

            final TransformerJob fuse1 = outputDataStreamJobs[0].getJob().getTransformerJobs().get(0);
            Assert.assertEquals(FuseStreamsComponent.class, fuse1.getDescriptor().getComponentClass());
            // the created fuse refers to both streams
            Assert.assertEquals(
                    "[MetaModelInputColumn[foo bar records.foo], MetaModelInputColumn[foo bar records.bar], "
                            + "MetaModelInputColumn[counter records.count], MetaModelInputColumn[counter records.uuid]]",
                    Arrays.toString(fuse1.getInput()));
            final TransformerJob fuse2 = outputDataStreamJobs[1].getJob().getTransformerJobs().get(0);
            Assert.assertSame(fuse1, fuse2);

            mockAnalyzer = fuse1.getOutputDataStreamJobs()[0].getJob().getAnalyzerJobs().get(0);
            Assert.assertEquals(MockAnalyzer.class, mockAnalyzer.getDescriptor().getComponentClass());
        }

        // now run the job
        final AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(configuration).run(job);
        resultFuture.await();

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        final ListResult<?> result = (ListResult<?>) resultFuture.getResult(mockAnalyzer);

        // a bit of dark math here ... The MockOutputDataStream discards 1/3 of
        // records but emits two records for the rest. And then 2 additional
        // records in the end.
        final int expectedValues = 2 * (COUNT_CUSTOMERS - (COUNT_CUSTOMERS / 3)) + 2;

        Assert.assertEquals(expectedValues, result.getValues().size());
    }

    @Test
    @Ignore("Not yet implemented")
    public void testFuseSourceTableAndOutputDataStream() throws Exception {
        Assert.fail("Not yet implemented");
    }
}
