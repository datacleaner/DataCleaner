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
package org.datacleaner.job.tasks;

import java.util.List;

import org.datacleaner.api.Close;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.connection.Datastore;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.concurrent.PreviousErrorsExistException;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.test.MockOutputDataStreamAnalyzer;
import org.datacleaner.test.TestEnvironment;
import org.datacleaner.test.TestHelper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CloseTaskListenerTest {
    public static class OnCloseFailingMockAnalyzer extends MockOutputDataStreamAnalyzer {
        static final String OUCH_ON_CLOSE = "Ouch on close!";

        @Close
        public void failingClose() {
            throw new RuntimeException(OUCH_ON_CLOSE);
        }

    }

    public static class OnExecutionAndCloseFailingMockAnalyzer extends OnCloseFailingMockAnalyzer {
        static final String OUCH_IN_RUN = "Ouch in run!";

        @Override
        public void run(final InputRow row, final int distinctCount) {
            throw new RuntimeException(OUCH_IN_RUN);
        }
    }

    private final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
    private DataCleanerEnvironment environment = TestEnvironment.getEnvironment();
    private final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore)
            .withEnvironment(environment);

    @Test(timeout = 15000L) // If the error code fails, this would freeze forever otherwise
    public void testFailingClose() throws Throwable {
        final AnalysisResultFuture resultFuture = runAnalysisJob(OnCloseFailingMockAnalyzer.class);

        resultFuture.await();

        assertTrue(resultFuture.isErrornous());
        assertEquals(OnCloseFailingMockAnalyzer.OUCH_ON_CLOSE, resultFuture.getErrors().get(0).getMessage());
    }

    @Test(timeout = 15000L) // If the error code fails, this would freeze forever otherwise
    public void testFailingJobAndClose() throws Throwable {
        final AnalysisResultFuture resultFuture = runAnalysisJob(OnExecutionAndCloseFailingMockAnalyzer.class);

        resultFuture.await();

        assertTrue(resultFuture.isErrornous());
        final List<Throwable> errors = resultFuture.getErrors();
        assertEquals(OnExecutionAndCloseFailingMockAnalyzer.OUCH_IN_RUN, errors.get(0).getMessage());

        boolean hasCorrectException = false; // We can't really trust the order of errors.
        for (Throwable error : errors) {
            if(error instanceof  PreviousErrorsExistException) {
                hasCorrectException = true;
                assertEquals(3, error.getSuppressed().length);
                assertEquals(OnCloseFailingMockAnalyzer.OUCH_ON_CLOSE, error.getSuppressed()[0].getMessage());

            }
        }
        assertTrue(hasCorrectException);
    }

    /*
     * This is probably a bigger test than needed, but it was how issue #1247 was explained.
     * TODO: Maybe slim it down later.
     */
    private AnalysisResultFuture runAnalysisJob(Class<? extends MockOutputDataStreamAnalyzer> analyserClass) {
        final AnalysisJob job;
        try (final AnalysisJobBuilder ajb1 = new AnalysisJobBuilder(configuration)) {
            ajb1.setDatastore(datastore);

            ajb1.addSourceColumns("customers.city");

            final AnalyzerComponentBuilder<?> analyzer1 = ajb1
                    .addAnalyzer(analyserClass);
            analyzer1.addInputColumn(ajb1.getSourceColumns().get(0));
            analyzer1.setConfiguredProperty(OnExecutionAndCloseFailingMockAnalyzer.PROPERTY_IDENTIFIER, "analyzer1");

            final AnalysisJobBuilder ajb2 = analyzer1.getOutputDataStreamJobBuilder(analyzer1.getOutputDataStreams()
                    .get(0));
            final AnalyzerComponentBuilder<?> analyzer2 = ajb2
                    .addAnalyzer(analyserClass);
            analyzer2.addInputColumn(ajb2.getSourceColumns().get(0));
            analyzer2.setConfiguredProperty(OnExecutionAndCloseFailingMockAnalyzer.PROPERTY_IDENTIFIER, "analyzer2");

            final AnalysisJobBuilder ajb3 = analyzer2.getOutputDataStreamJobBuilder(analyzer2.getOutputDataStreams()
                    .get(0));
            final AnalyzerComponentBuilder<?> analyzer3 = ajb3
                    .addAnalyzer(analyserClass);
            analyzer3.addInputColumn(ajb3.getSourceColumns().get(0));
            analyzer3.setConfiguredProperty(OnExecutionAndCloseFailingMockAnalyzer.PROPERTY_IDENTIFIER, "analyzer3");

            job = ajb1.toAnalysisJob();
        }

        // now run the job(s)
        final AnalysisRunnerImpl runner = new AnalysisRunnerImpl(configuration);
        return runner.run(job);
    }

}