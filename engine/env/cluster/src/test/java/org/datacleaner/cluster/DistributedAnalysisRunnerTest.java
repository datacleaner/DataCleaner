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

import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.cluster.virtual.VirtualClusterManager;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.test.TestHelper;

public class DistributedAnalysisRunnerTest extends TestCase {

    public void testNoRecords() throws Throwable {
        final DataCleanerConfiguration configuration = ClusterTestHelper.createConfiguration(getName(), true);

        ClusterTestHelper.runNoExpectedRecordsJob(configuration);
    }

    public void testCancel() throws Throwable {
        final DataCleanerConfiguration configuration = ClusterTestHelper.createConfiguration(getName(), true);

        ClusterTestHelper.runCancelJobJob(configuration, new VirtualClusterManager(configuration, 2));
    }

    public void testExistingMaxRowsScenario() throws Throwable {
        final DataCleanerConfiguration configuration = ClusterTestHelper.createConfiguration(getName(), true);

        ClusterTestHelper.runExistingMaxRowsJob(configuration, new VirtualClusterManager(configuration, 2));
    }

    public void testVanillaScenarioSingleSlave() throws Throwable {
        final DataCleanerConfiguration configuration = ClusterTestHelper.createConfiguration(getName(), true);

        ClusterTestHelper.runConcatAndInsertJob(configuration, new VirtualClusterManager(configuration, 1));
    }

    public void testVanillaScenarioFourSlaves() throws Throwable {
        final DataCleanerConfiguration configuration = ClusterTestHelper.createConfiguration(getName(), true);

        ClusterTestHelper.runConcatAndInsertJob(configuration, new VirtualClusterManager(configuration, 4));
    }

    public void testRunCompletenessAnalyzer() throws Throwable {
        final DataCleanerConfiguration configuration = ClusterTestHelper.createConfiguration(getName(), true);

        // run with only a single node to verify a baseline scenario
        ClusterTestHelper.runCompletenessAndValueMatcherAnalyzerJob(configuration, new VirtualClusterManager(
                configuration, 1));

        ClusterTestHelper.runCompletenessAndValueMatcherAnalyzerJob(configuration, new VirtualClusterManager(
                configuration, 10));
        ClusterTestHelper.runCompletenessAndValueMatcherAnalyzerJob(configuration, new VirtualClusterManager(
                configuration, 3));
    }

    public void testRunBasicAnalyzers() throws Throwable {
        final DataCleanerConfiguration configuration = ClusterTestHelper.createConfiguration(getName(), true);

        // run with only a single node to verify a baseline scenario
        ClusterTestHelper.runBasicAnalyzersJob(configuration, new VirtualClusterManager(configuration, 1));

        ClusterTestHelper.runBasicAnalyzersJob(configuration, new VirtualClusterManager(configuration, 6));
        ClusterTestHelper.runBasicAnalyzersJob(configuration, new VirtualClusterManager(configuration, 10));
    }

    public void testErrorHandlingSingleSlave() throws Exception {
        final DataCleanerConfiguration configuration = ClusterTestHelper.createConfiguration(getName(), false);

        List<Throwable> errors = ClusterTestHelper.runErrorHandlingJob(configuration, new VirtualClusterManager(
                configuration, 1));

        assertEquals("I am just a dummy transformer!", errors.get(0).getMessage());
        assertEquals("A previous exception has occurred", errors.get(1).getMessage());
        assertEquals(2, errors.size());
    }

    public void testErrorHandlingFourSlaves() throws Exception {
        final DataCleanerConfiguration configuration = ClusterTestHelper.createConfiguration(getName(), true);

        List<Throwable> errors = ClusterTestHelper.runErrorHandlingJob(configuration, new VirtualClusterManager(
                configuration, 4));

        for (Throwable throwable : errors) {
            String message = throwable.getMessage();
            if (!"I am just a dummy transformer!".equals(message)
                    && !"A previous exception has occurred".equals(message)) {
                fail("Unexpected exception: " + message + " (" + throwable.getClass().getName() + ")");
            }
        }

        // there might be (a lot) more than 8 errors since each node was
        // multi-threaded
        assertTrue(errors.size() >= 8);
    }

    public void testUndistributableAnalyzer() throws Exception {
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore);

        final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);
        jobBuilder.setDatastore(datastore);
        jobBuilder.addSourceColumns("CUSTOMERS.CUSTOMERNAME");

        // The String Analyzer is (currently) not distributable
        final AnalyzerComponentBuilder<MockAnalyzerWithoutReducer> analyzer = jobBuilder
                .addAnalyzer(MockAnalyzerWithoutReducer.class);
        analyzer.addInputColumns(jobBuilder.getSourceColumns());

        AnalysisJob job = jobBuilder.toAnalysisJob();

        DistributedAnalysisRunner runner = new DistributedAnalysisRunner(configuration, new VirtualClusterManager(
                configuration, 2));

        try {
            runner.run(job);
            fail("Exception expected");
        } catch (UnsupportedOperationException e) {
            assertEquals("Component is not distributable: "
                    + "ImmutableAnalyzerJob[name=null,analyzer=Analyzer without reducer]", e.getMessage());
        } finally {
            jobBuilder.close();
        }
    }

    public void testErrorHandlingInReductionPhase() throws Exception {
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore);

        final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);
        jobBuilder.setDatastore(datastore);
        jobBuilder.addSourceColumns("CUSTOMERS.CUSTOMERNAME");

        // The String Analyzer is (currently) not distributable
        final AnalyzerComponentBuilder<MockAnalyzerWithBadReducer> analyzer = jobBuilder
                .addAnalyzer(MockAnalyzerWithBadReducer.class);
        analyzer.addInputColumns(jobBuilder.getSourceColumns());

        AnalysisJob job = jobBuilder.toAnalysisJob();
        jobBuilder.close();

        DistributedAnalysisRunner runner = new DistributedAnalysisRunner(configuration, new VirtualClusterManager(
                configuration, 2));

        AnalysisResultFuture result = runner.run(job);

        if (result.isSuccessful()) {
            fail("Expected result to be errornous. Got result: " + result.getResults());
        }

        List<Throwable> errors = result.getErrors();

        assertEquals(
                "Failed to reduce results for ImmutableAnalyzerJob[name=null,analyzer=Analyzer with bad reducer]: Damn, I failed during reduction phase",
                errors.get(0).getMessage());

        assertEquals(1, errors.size());
    }
}
