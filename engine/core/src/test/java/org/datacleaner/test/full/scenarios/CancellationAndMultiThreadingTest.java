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

import junit.framework.TestCase;

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
import org.datacleaner.test.TestHelper;

public class CancellationAndMultiThreadingTest extends TestCase {

    public void test10Times() throws Exception {
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            Thread thread = new Thread() {
                public void run() {
                    runScenario();
                };
            };
            thread.start();
            threads[i] = thread;
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
    }

    public void runScenario() {
        MultiThreadedTaskRunner taskRunner = new MultiThreadedTaskRunner(30);

        ThreadPoolExecutor executorService = (ThreadPoolExecutor) taskRunner.getExecutorService();
        assertEquals(30, executorService.getMaximumPoolSize());
        assertEquals(0, executorService.getActiveCount());

        DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl()
                .withEnvironment(new DataCleanerEnvironmentImpl().withTaskRunner(taskRunner));

        AnalysisRunner runner = new AnalysisRunnerImpl(configuration);

        Datastore ds = TestHelper.createSampleDatabaseDatastore("foobar");
        try (DatastoreConnection con = ds.openConnection()) {

            AnalysisJob job;
            try (AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(configuration)) {
                analysisJobBuilder.setDatastore(ds);

                Table table = con.getDataContext().getDefaultSchema().getTableByName("ORDERFACT");
                assertNotNull(table);

                Column statusColumn = table.getColumnByName("STATUS");
                Column commentsColumn = table.getColumnByName("COMMENTS");

                analysisJobBuilder.addSourceColumns(statusColumn, commentsColumn);
                analysisJobBuilder.addAnalyzer(AnalyzerMock.class).addInputColumns(
                        analysisJobBuilder.getSourceColumns());

                job = analysisJobBuilder.toAnalysisJob();
            }

            AnalysisResultFuture resultFuture = runner.run(job);

            try {
                Thread.sleep(550);
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail("Interrupted! " + e.getMessage());
            }

            resultFuture.cancel();

            assertFalse(resultFuture.isSuccessful());
            assertTrue(resultFuture.isCancelled());
            assertTrue(resultFuture.isErrornous());

            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail("Interrupted! " + e.getMessage());
            }

            assertEquals(30, executorService.getMaximumPoolSize());

            long completedTaskCount = executorService.getCompletedTaskCount();
            assertTrue("completedTaskCount was: " + completedTaskCount, completedTaskCount > 3);

            int largestPoolSize = executorService.getLargestPoolSize();
            assertTrue("largestPoolSize was: " + largestPoolSize, largestPoolSize > 5);
            assertEquals(0, executorService.getActiveCount());
        }

        taskRunner.shutdown();
    }
}
