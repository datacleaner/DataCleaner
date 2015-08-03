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
package org.datacleaner.test.full.scenarios;

import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.api.OutputDataStream;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.connection.Datastore;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.OutputDataStreamJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.ListResult;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.MockOutputDataStreamAnalyzer;
import org.datacleaner.test.TestEnvironment;
import org.datacleaner.test.TestHelper;

/**
 * Basic acceptance test for DC issue #224: Output DataSet (renamed to 'data
 * stream') producers and jobs. This test uses the additions to the builder API
 * to build a job with {@link OutputDataStream}s and executes it to verify the
 * invocation and completion of the {@link OutputDataStreamJob}s.
 */
public class JobWithOutputDataStreamsTest extends TestCase {

    private final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
    private DataCleanerEnvironment environment = TestEnvironment.getEnvironment();
    private final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore)
            .withEnvironment(environment);
    
    public void testSimpleBuildAndExecuteScenario() throws Throwable {
        final AnalysisJob job;
        try (final AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {
            ajb.setDatastore(datastore);

            ajb.addSourceColumns("customers.contactfirstname");
            ajb.addSourceColumns("customers.contactlastname");

            final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer1 = ajb
                    .addAnalyzer(MockOutputDataStreamAnalyzer.class);

            // analyzer is still unconfigured
            assertEquals(0, analyzer1.getOutputDataStreams().size());

            // now configure it
            final List<MetaModelInputColumn> sourceColumns = ajb.getSourceColumns();
            analyzer1.setName("analyzer1");
            analyzer1.addInputColumn(sourceColumns.get(0));
            assertTrue(analyzer1.isConfigured());

            final List<OutputDataStream> dataStreams = analyzer1.getOutputDataStreams();

            assertEquals(2, dataStreams.size());
            assertEquals("foo bar records", dataStreams.get(0).getName());
            assertEquals("counter records", dataStreams.get(1).getName());

            final OutputDataStream dataStream = analyzer1.getOutputDataStream("foo bar records");
            // assert that the same instance is reused when re-referred to
            assertSame(dataStreams.get(0), dataStream);

            // the stream is still not "consumed" yet
            assertFalse(analyzer1.isOutputDataStreamConsumed(dataStream));

            final AnalysisJobBuilder outputDataStreamJobBuilder = analyzer1.getOutputDataStreamJobBuilder(dataStream);
            final List<MetaModelInputColumn> outputDataStreamColumns = outputDataStreamJobBuilder.getSourceColumns();
            assertEquals(2, outputDataStreamColumns.size());
            assertEquals("MetaModelInputColumn[foo bar records.foo]", outputDataStreamColumns.get(0).toString());
            assertEquals("MetaModelInputColumn[foo bar records.bar]", outputDataStreamColumns.get(1).toString());

            // the stream is still not "consumed" because no components exist in
            // the output stream
            assertFalse(analyzer1.isOutputDataStreamConsumed(dataStream));

            final AnalyzerComponentBuilder<MockAnalyzer> analyzer2 = outputDataStreamJobBuilder
                    .addAnalyzer(MockAnalyzer.class);
            analyzer2.addInputColumns(outputDataStreamColumns);
            analyzer2.setName("analyzer2");
            assertTrue(analyzer2.isConfigured());

            // now the stream is consumed
            assertTrue(analyzer1.isOutputDataStreamConsumed(dataStream));

            job = ajb.toAnalysisJob();
        }

        // do some assertions on the built job to check that the data stream is
        // represented there also
        assertEquals(1, job.getAnalyzerJobs().size());
        final AnalyzerJob analyzerJob1 = job.getAnalyzerJobs().get(0);
        assertEquals("analyzer1", analyzerJob1.getName());
        final OutputDataStreamJob[] outputDataStreamJobs = analyzerJob1.getOutputDataStreamJobs();
        assertEquals(1, outputDataStreamJobs.length);

        final OutputDataStreamJob outputDataStreamJob = outputDataStreamJobs[0];
        assertEquals("foo bar records", outputDataStreamJob.getOutputDataStream().getName());
        final AnalysisJob job2 = outputDataStreamJob.getJob();
        assertEquals(2, job2.getSourceColumns().size());
        assertEquals("foo", job2.getSourceColumns().get(0).getName());
        assertEquals("bar", job2.getSourceColumns().get(1).getName());
        assertEquals(1, job2.getAnalyzerJobs().size());
        final AnalyzerJob analyzerJob2 = job2.getAnalyzerJobs().get(0);
        assertEquals("analyzer2", analyzerJob2.getName());

        // now run the job(s)
        final AnalysisRunnerImpl runner = new AnalysisRunnerImpl(configuration);
        final AnalysisResultFuture resultFuture = runner.run(job);
        resultFuture.await();

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        assertEquals(2, resultFuture.getResults().size());

        // the first result should be trivial - it was also there before issue
        // #224
        final ListResult<?> result1 = (ListResult<?>) resultFuture.getResult(analyzerJob1);
        assertNotNull(result1);
        assertEquals(40, result1.getValues().size());

        // this result is the "new part" of issue #224
        final ListResult<?> result2 = (ListResult<?>) resultFuture.getResult(analyzerJob2);
        assertNotNull(result2);
        assertEquals(83, result2.getValues().size());
        final Object lastElement = result2.getValues().get(result2.getValues().size() - 1);
        assertEquals("MetaModelInputRow[Row[values=[baz, null]]]", lastElement.toString());
    }
}
