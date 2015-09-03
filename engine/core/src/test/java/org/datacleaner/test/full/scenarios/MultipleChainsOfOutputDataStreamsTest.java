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

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.connection.Datastore;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.ListResult;
import org.datacleaner.test.MockOutputDataStreamAnalyzer;
import org.datacleaner.test.TestEnvironment;
import org.datacleaner.test.TestHelper;

public class MultipleChainsOfOutputDataStreamsTest extends TestCase {

    private final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
    private DataCleanerEnvironment environment = TestEnvironment.getEnvironment();
    private final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore)
            .withEnvironment(environment);

    public void testSimpleBuildAndExecuteScenario() throws Throwable {
        final AnalysisJob job;
        try (final AnalysisJobBuilder ajb1 = new AnalysisJobBuilder(configuration)) {
            ajb1.setDatastore(datastore);

            ajb1.addSourceColumns("customers.city");

            final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer1 = ajb1
                    .addAnalyzer(MockOutputDataStreamAnalyzer.class);
            analyzer1.addInputColumn(ajb1.getSourceColumns().get(0));
            analyzer1.setConfiguredProperty(MockOutputDataStreamAnalyzer.PROPERTY_IDENTIFIER, "analyzer1");

            final AnalysisJobBuilder ajb2 = analyzer1.getOutputDataStreamJobBuilder(analyzer1.getOutputDataStreams()
                    .get(0));
            final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer2 = ajb2
                    .addAnalyzer(MockOutputDataStreamAnalyzer.class);
            analyzer2.addInputColumn(ajb2.getSourceColumns().get(0));
            analyzer2.setConfiguredProperty(MockOutputDataStreamAnalyzer.PROPERTY_IDENTIFIER, "analyzer2");

            final AnalysisJobBuilder ajb3 = analyzer2.getOutputDataStreamJobBuilder(analyzer2.getOutputDataStreams()
                    .get(0));
            final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer3 = ajb3
                    .addAnalyzer(MockOutputDataStreamAnalyzer.class);
            analyzer3.addInputColumn(ajb3.getSourceColumns().get(0));
            analyzer3.setConfiguredProperty(MockOutputDataStreamAnalyzer.PROPERTY_IDENTIFIER, "analyzer3");

            job = ajb1.toAnalysisJob();
        }

        // now run the job(s)
        final AnalysisRunnerImpl runner = new AnalysisRunnerImpl(configuration);
        final AnalysisResultFuture resultFuture = runner.run(job);
        resultFuture.await();

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        assertEquals(3, resultFuture.getResults().size());

        @SuppressWarnings("unchecked")
        final List<ListResult<?>> results = (List<ListResult<?>>) resultFuture.getResults(ListResult.class);

        // for every result we expect a drop-off of 1/3 values
        assertEquals(40, results.get(0).getValues().size());
        assertEquals(27, results.get(1).getValues().size());
        assertEquals(19, results.get(2).getValues().size());
    }
}
