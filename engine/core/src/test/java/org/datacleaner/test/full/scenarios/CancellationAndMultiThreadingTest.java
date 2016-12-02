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

import java.util.concurrent.ThreadPoolExecutor;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.components.mock.AnalyzerMock;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.concurrent.MultiThreadedTaskRunner;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.test.TestEnvironment;
import org.datacleaner.test.TestHelper;

import junit.framework.TestCase;

public class CancellationAndMultiThreadingTest extends TestCase {

    public void test10Times() throws Exception {
        final Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            final Thread thread = new Thread() {
                public void run() {
                    runScenario();
                }

            };
            thread.start();
            threads[i] = thread;
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
    }

    public void runScenario() {
        final MultiThreadedTaskRunner taskRunner = TestEnvironment.getMultiThreadedTaskRunner();

        final ThreadPoolExecutor executorService = (ThreadPoolExecutor) taskRunner.getExecutorService();
        assertEquals(TestEnvironment.THREAD_COUNT, executorService.getMaximumPoolSize());
        assertEquals(0, executorService.getActiveCount());

        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl()
                .withEnvironment(new DataCleanerEnvironmentImpl().withTaskRunner(taskRunner));

        final AnalysisRunner runner = new AnalysisRunnerImpl(configuration);

        final Datastore ds = TestHelper.createSampleDatabaseDatastore("foobar");
        try (DatastoreConnection con = ds.openConnection()) {

            final AnalysisJob job;
            try (AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(configuration)) {
                analysisJobBuilder.setDatastore(ds);

                final Table table = con.getDataContext().getDefaultSchema().getTableByName("ORDERFACT");
                assertNotNull(table);

                final Column statusColumn = table.getColumnByName("STATUS");
                final Column commentsColumn = table.getColumnByName("COMMENTS");

                analysisJobBuilder.addSourceColumns(statusColumn, commentsColumn);
                analysisJobBuilder.addAnalyzer(AnalyzerMock.class)
                        .addInputColumns(analysisJobBuilder.getSourceColumns());

                job = analysisJobBuilder.toAnalysisJob();
            }

            final AnalysisResultFuture resultFuture = runner.run(job);

            try {
                Thread.sleep(550);
            } catch (final InterruptedException e) {
                e.printStackTrace();
                fail("Interrupted! " + e.getMessage());
            }

            resultFuture.cancel();

            assertFalse(resultFuture.isSuccessful());
            assertTrue(resultFuture.isCancelled());
            assertTrue(resultFuture.isErrornous());

            try {
                Thread.sleep(400);
            } catch (final InterruptedException e) {
                e.printStackTrace();
                fail("Interrupted! " + e.getMessage());
            }

            assertEquals(TestEnvironment.THREAD_COUNT, executorService.getMaximumPoolSize());

            final long completedTaskCount = executorService.getCompletedTaskCount();
            assertTrue("completedTaskCount was: " + completedTaskCount, completedTaskCount > 3);

            final int largestPoolSize = executorService.getLargestPoolSize();
            assertTrue("largestPoolSize was: " + largestPoolSize, largestPoolSize > 5);
            assertEquals(0, executorService.getActiveCount());
        }

        taskRunner.shutdown();
    }
}
