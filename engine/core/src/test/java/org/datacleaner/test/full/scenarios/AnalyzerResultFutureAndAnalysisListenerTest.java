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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import junit.framework.TestCase;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.AnalyzerResultFuture;
import org.datacleaner.api.AnalyzerResultFuture.Listener;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.runner.AnalysisJobMetrics;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.runner.AnalysisListenerAdaptor;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.job.runner.RowProcessingMetrics;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.MockFutureAnalyzer;
import org.datacleaner.test.TestEnvironment;
import org.datacleaner.test.TestHelper;

/**
 * A test that verifies that the order of listener methods in
 * {@link AnalysisListener} is correct also when one of the components is
 * producing an {@link AnalyzerResultFuture}. A particular concern in this
 * scenario is that the
 * {@link AnalysisListener#jobSuccess(org.datacleaner.job.AnalysisJob, org.datacleaner.job.runner.AnalysisJobMetrics)}
 * method should be invoked only after the future result is available.
 */
public class AnalyzerResultFutureAndAnalysisListenerTest extends TestCase {

    private final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
    private final TaskRunner taskRunner = TestEnvironment.getMultiThreadedTaskRunner();
    private final DataCleanerEnvironment environment = new DataCleanerEnvironmentImpl().withTaskRunner(taskRunner);
    private final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withEnvironment(
            environment).withDatastores(datastore);

    public void testScenario() throws Exception {
        final AnalysisJob job;

        // build job
        try (final AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {
            ajb.setDatastore(datastore);
            ajb.addSourceColumns("CUSTOMERS.CUSTOMERNAME", "CUSTOMERS.PHONE");

            final List<MetaModelInputColumn> sourceColumns = ajb.getSourceColumns();

            AnalyzerComponentBuilder<MockAnalyzer> analyzer1 = ajb.addAnalyzer(MockAnalyzer.class);
            analyzer1.setName("normal analyzer");
            analyzer1.addInputColumn(sourceColumns.get(0));

            AnalyzerComponentBuilder<MockFutureAnalyzer> analyzer2 = ajb.addAnalyzer(MockFutureAnalyzer.class);
            analyzer2.setName("analyzer with future result");
            analyzer2.addInputColumn(sourceColumns.get(1));

            job = ajb.toAnalysisJob();
        }

        // make a listener that records the relevant events (for later
        // assertion)
        final BlockingQueue<String> messages = new ArrayBlockingQueue<>(100);
        final AnalysisListener listener = new AnalysisListenerAdaptor() {

            @Override
            public void componentSuccess(AnalysisJob job, ComponentJob componentJob, AnalyzerResult result) {
                messages.add("componentSuccess(" + componentJob.getName() + "," + result.getClass().getSimpleName()
                        + ")");
                if (result instanceof AnalyzerResultFuture) {
                    final AnalyzerResultFuture<?> future = (AnalyzerResultFuture<?>) result;
                    final boolean ready1 = future.isReady();
                    messages.add("AnalyzerResultFuture.isReady() (1) = " + ready1);

                    // add a couple of listeners
                    future.addListener(new Listener<AnalyzerResult>() {
                        @Override
                        public void onSuccess(AnalyzerResult result) {
                            messages.add("AnalyzerResultFuture.Listener (1).onSuccess()");
                        }

                        @Override
                        public void onError(RuntimeException error) {
                            messages.add("AnalyzerResultFuture.Listener (1).onError(): " + error.getMessage());
                        }
                    });

                    future.addListener(new Listener<AnalyzerResult>() {
                        @Override
                        public void onSuccess(AnalyzerResult result) {
                            messages.add("AnalyzerResultFuture.Listener (2).onSuccess()");
                        }

                        @Override
                        public void onError(RuntimeException error) {
                            messages.add("AnalyzerResultFuture.Listener (2).onError(): " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void rowProcessingSuccess(AnalysisJob job, RowProcessingMetrics metrics) {
                messages.add("rowProcessingSuccess");
            }

            @Override
            public void jobSuccess(AnalysisJob job, AnalysisJobMetrics metrics) {
                messages.add("jobSuccess");
            }
        };

        // run the job while also adding a few messages from the main thread
        {
            final AnalysisRunner runner = new AnalysisRunnerImpl(configuration, listener);
            final AnalysisResultFuture resultFuture = runner.run(job);
            messages.add("AnalysisRunner.run(job) returned");

            resultFuture.await();
            messages.add("AnalysisResultFuture.await() returned");

            @SuppressWarnings("rawtypes")
            final List<? extends AnalyzerResultFuture> futureResults = resultFuture
                    .getResults(AnalyzerResultFuture.class);
            assertEquals(1, futureResults.size());

            final AnalyzerResultFuture<?> futureResult = futureResults.get(0);

            final boolean ready2 = futureResult.isReady();
            messages.add("AnalyzerResultFuture.isReady() (2) = " + ready2);
        }

        // make the queue into a list so that we have index positions.
        final List<String> messagesList = new ArrayList<>();
        messages.drainTo(messagesList);

        // export to DS, dup id in RECORDS

        final String originalMessagesString = messagesList.toString();

        final int indexRunReturned = getIndexAndVerifyExists(messagesList, "AnalysisRunner.run(job) returned");
        final int indexRowProcessingSuccess = getIndexAndVerifyExists(messagesList, "rowProcessingSuccess");

        assertTrue(originalMessagesString, indexRowProcessingSuccess > indexRunReturned);

        final int indexNormalAnalyzerSuccess = getIndexAndVerifyExists(messagesList,
                "componentSuccess(normal analyzer,ListResult)");
        final int indexFutureAnalyzerSuccess = getIndexAndVerifyExists(messagesList,
                "componentSuccess(analyzer with future result,AnalyzerResultFutureImpl)");

        assertTrue(originalMessagesString, indexNormalAnalyzerSuccess > indexRowProcessingSuccess);
        assertTrue(originalMessagesString, indexFutureAnalyzerSuccess > indexRowProcessingSuccess);

        final int indexAnalyzerResultNotReady = getIndexAndVerifyExists(messagesList,
                "AnalyzerResultFuture.isReady() (1) = false");
        final int indexListener1Success = getIndexAndVerifyExists(messagesList,
                "AnalyzerResultFuture.Listener (1).onSuccess()");
        final int indexListener2Success = getIndexAndVerifyExists(messagesList,
                "AnalyzerResultFuture.Listener (2).onSuccess()");

        assertTrue(originalMessagesString, indexListener1Success > indexAnalyzerResultNotReady);
        assertTrue(originalMessagesString, indexListener2Success > indexAnalyzerResultNotReady);

        final int indexAnalysisResultFutureAwaitReturned = getIndexAndVerifyExists(messagesList,
                "AnalysisResultFuture.await() returned");

        assertTrue(originalMessagesString, indexAnalysisResultFutureAwaitReturned > indexListener1Success);
        assertTrue(originalMessagesString, indexAnalysisResultFutureAwaitReturned > indexListener2Success);

        final int indexAnalyzerResultReady = getIndexAndVerifyExists(messagesList,
                "AnalyzerResultFuture.isReady() (2) = true");

        assertTrue(originalMessagesString, indexAnalyzerResultReady > indexAnalyzerResultNotReady);

        final int indexJobSuccess = getIndexAndVerifyExists(messagesList, "jobSuccess");

        assertTrue(originalMessagesString, indexJobSuccess > indexFutureAnalyzerSuccess);
    }

    private int getIndexAndVerifyExists(List<String> messagesList, String string) {
        final int index = messagesList.indexOf(string);
        if (index == -1) {
            // use a good message because hunting down the cause of this can be
            // pretty tough.
            final String message = "Failed to find '" + string + "' in messages: " + messagesList.toString();
            assertTrue(message, index != -1);
        }
        return index;
    }
}
