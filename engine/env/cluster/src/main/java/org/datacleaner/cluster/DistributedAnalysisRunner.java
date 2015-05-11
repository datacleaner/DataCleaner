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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.SharedExecutorService;
import org.datacleaner.api.InputColumn;
import org.datacleaner.cluster.virtual.VirtualClusterManager;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.components.maxrows.MaxRowsFilter.Category;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskListener;
import org.datacleaner.job.runner.AnalysisJobMetrics;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.CompositeAnalysisListener;
import org.datacleaner.job.runner.RowProcessingMetrics;
import org.datacleaner.job.runner.RowProcessingPublisher;
import org.datacleaner.job.runner.RowProcessingPublishers;
import org.datacleaner.job.tasks.Task;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.util.SourceColumnFinder;
import org.datacleaner.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AnalysisRunner} which executes {@link AnalysisJob}s accross a
 * distributed set of slave nodes.
 */
public final class DistributedAnalysisRunner implements AnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(DistributedAnalysisRunner.class);

    private final ClusterManager _clusterManager;
    private final DataCleanerConfiguration _configuration;
    private final CompositeAnalysisListener _analysisListener;

    public DistributedAnalysisRunner(DataCleanerConfiguration configuration, ClusterManager clusterManager) {
        this(configuration, clusterManager, new AnalysisListener[0]);
    }

    public DistributedAnalysisRunner(DataCleanerConfiguration configuration, ClusterManager clusterManager,
            AnalysisListener... listeners) {
        _configuration = configuration;
        _clusterManager = clusterManager;
        _analysisListener = new CompositeAnalysisListener(listeners);
    }

    /**
     * Determines if an {@link AnalysisJob} is distributable or not. If this
     * method returns false, calling {@link #run(AnalysisJob)} with the job will
     * typically throw a {@link UnsupportedOperationException}.
     * 
     * @param job
     * @return
     */
    public boolean isDistributable(final AnalysisJob job) {
        try {
            failIfJobIsUnsupported(job);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException
     *             if the job is not distributable (either because components
     *             are not distributable in their nature, or because some
     *             features are limited).
     */
    @Override
    public AnalysisResultFuture run(final AnalysisJob job) throws UnsupportedOperationException {
        logger.info("Validating distributed job: {}", job);

        failIfJobIsUnsupported(job);

        final InjectionManager injectionManager = _configuration.getEnvironment().getInjectionManagerFactory()
                .getInjectionManager(_configuration, job);
        final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(injectionManager, true);
        final RowProcessingPublishers publishers = getRowProcessingPublishers(job, lifeCycleHelper);
        final RowProcessingPublisher publisher = getRowProcessingPublisher(publishers);
        publisher.initializeConsumers(new TaskListener() {
            @Override
            public void onError(Task task, Throwable throwable) {
                logger.error("Failed to initialize consumers at master node!", throwable);
            }

            @Override
            public void onComplete(Task task) {
            }

            @Override
            public void onBegin(Task task) {
            }
        });

        logger.info("Validation passed! Chunking job for distribution amongst slaves: {}", job);

        // since we always use a SingleThreadedTaskRunner, the above operation
        // will be synchronized/blocking.

        final AnalysisJobMetrics analysisJobMetrics = publishers.getAnalysisJobMetrics();
        _analysisListener.jobBegin(job, analysisJobMetrics);

        final RowProcessingMetrics rowProcessingMetrics = publisher.getRowProcessingMetrics();
        _analysisListener.rowProcessingBegin(job, rowProcessingMetrics);

        final AnalysisResultFuture resultFuture;

        try {
            final int expectedRows = rowProcessingMetrics.getExpectedRows();
            if (expectedRows == 0) {
                logger.info("Expected rows of the job was zero. Job will run on a local virtual slave.");

                // when there are no expected rows we still need to build a
                // single slave job, but run it locally, since the job lifecycle
                // still needs to be guaranteed.
                final DistributedJobContext context = new DistributedJobContextImpl(_configuration, job, 0, 1);

                // use a virtual cluster, which just runs the job locally.
                final VirtualClusterManager localCluster = new VirtualClusterManager(_configuration, 1);
                resultFuture = localCluster.dispatchJob(job, context);
            } else {
                final JobDivisionManager jobDivisionManager = _clusterManager.getJobDivisionManager();
                final int chunks = jobDivisionManager.calculateDivisionCount(job, expectedRows);
                final int rowsPerChunk = (expectedRows + 1) / chunks;

                logger.info(
                        "Expected rows was {}. A total number of {} slave jobs will be built, each of approx. {} rows.",
                        expectedRows, chunks, rowsPerChunk);

                final List<AnalysisResultFuture> results = dispatchJobs(job, chunks, rowsPerChunk, publisher);
                final DistributedAnalysisResultReducer reducer = new DistributedAnalysisResultReducer(job,
                        lifeCycleHelper, publisher, _analysisListener);
                resultFuture = new DistributedAnalysisResultFuture(results, reducer);
            }

        } catch (RuntimeException e) {
            _analysisListener.errorUnknown(job, e);
            throw e;
        }

        if (!_analysisListener.isEmpty()) {
            awaitAndInformListener(job, analysisJobMetrics, rowProcessingMetrics, resultFuture);
        }

        return resultFuture;
    }

    /**
     * Spawns a new thread for awaiting the result future (which will force the
     * reducer to inform about the progress).
     * 
     * @param job
     * @param analysisJobMetrics
     * @param resultFuture
     */
    private void awaitAndInformListener(final AnalysisJob job, final AnalysisJobMetrics analysisJobMetrics,
            final RowProcessingMetrics rowProcessingMetrics, final AnalysisResultFuture resultFuture) {
        SharedExecutorService.get().execute(new Runnable() {
            @Override
            public void run() {
                resultFuture.await();
                if (resultFuture.isSuccessful()) {
                    _analysisListener.jobSuccess(job, analysisJobMetrics);
                }
            }
        });
    }

    public List<AnalysisResultFuture> dispatchJobs(final AnalysisJob job, final int chunks, final int rowsPerChunk,
            final RowProcessingPublisher publisher) {
        final List<AnalysisResultFuture> results = new ArrayList<AnalysisResultFuture>();
        for (int i = 0; i < chunks; i++) {
            final int firstRow = (i * rowsPerChunk) + 1;
            final int maxRows;
            if (i == chunks - 1) {
                maxRows = Integer.MAX_VALUE - firstRow - 1;
            } else {
                maxRows = rowsPerChunk;
            }

            final AnalysisJob slaveJob = buildSlaveJob(job, i, firstRow, maxRows);
            final DistributedJobContext context = new DistributedJobContextImpl(_configuration, job, i, chunks);

            try {
                logger.info("Dispatching slave job {} of {}", i + 1, chunks);
                final AnalysisResultFuture slaveResultFuture = _clusterManager.dispatchJob(slaveJob, context);
                results.add(slaveResultFuture);
            } catch (Exception e) {
                _analysisListener.errorUnknown(job, e);
                // exceptions due to dispatching jobs are added as the first of
                // the job's errors, and the rest of the execution is aborted.
                AnalysisResultFuture errorResult = new FailedAnalysisResultFuture(e);
                results.add(0, errorResult);
                break;
            }
        }
        return results;
    }

    /**
     * Creates a slave job by copying the original job and adding a
     * {@link MaxRowsFilter} as a default requirement.
     * 
     * @param job
     * @param firstRow
     * @param maxRows
     * @return
     */
    private AnalysisJob buildSlaveJob(AnalysisJob job, int slaveJobIndex, int firstRow, int maxRows) {
        logger.info("Building slave job {} with firstRow={} and maxRow={}", slaveJobIndex + 1, firstRow, maxRows);

        try (final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(_configuration, job)) {

            final FilterComponentBuilder<MaxRowsFilter, Category> maxRowsFilter = jobBuilder
                    .addFilter(MaxRowsFilter.class);
            maxRowsFilter.getComponentInstance().setFirstRow(firstRow);
            maxRowsFilter.getComponentInstance().setMaxRows(maxRows);

            final boolean naturalRecordOrderConsistent = jobBuilder.getDatastore().getPerformanceCharacteristics()
                    .isNaturalRecordOrderConsistent();
            if (!naturalRecordOrderConsistent) {
                final InputColumn<?> orderColumn = findOrderByColumn(jobBuilder);
                maxRowsFilter.getComponentInstance().setOrderColumn(orderColumn);
            }

            jobBuilder.setDefaultRequirement(maxRowsFilter, MaxRowsFilter.Category.VALID);

            // in assertion/test mode do an early validation
            assert jobBuilder.isConfigured(true);

            return jobBuilder.toAnalysisJob();
        }
    }

    /**
     * Finds a source column which is appropriate for an ORDER BY clause in the
     * generated paginated queries
     * 
     * @param jobBuilder
     * @return
     */
    private InputColumn<?> findOrderByColumn(AnalysisJobBuilder jobBuilder) {
        final Table sourceTable = jobBuilder.getSourceTables().get(0);

        // preferred strategy: Use the primary key
        final Column[] primaryKeys = sourceTable.getPrimaryKeys();
        if (primaryKeys.length == 1) {
            final Column primaryKey = primaryKeys[0];
            final InputColumn<?> sourceColumn = jobBuilder.getSourceColumnByName(primaryKey.getName());
            if (sourceColumn == null) {
                jobBuilder.addSourceColumn(primaryKey);
                logger.info("Added PK source column for ORDER BY clause on slave jobs: {}", sourceColumn);
                return jobBuilder.getSourceColumnByName(primaryKey.getName());
            } else {
                logger.info("Using existing PK source column for ORDER BY clause on slave jobs: {}", sourceColumn);
                return sourceColumn;
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Found {} primary keys, cannot select a single for ORDER BY clause on slave jobs: {}",
                        primaryKeys.length, Arrays.toString(primaryKeys));
            }
        }

        // secondary strategy: See if there's a source column called something
        // like 'ID' or so, and use that.
        final List<MetaModelInputColumn> sourceColumns = jobBuilder.getSourceColumns();
        final String tableName = sourceTable.getName().toLowerCase();
        for (final MetaModelInputColumn sourceColumn : sourceColumns) {
            String name = sourceColumn.getName();
            if (name != null) {
                name = StringUtils.replaceWhitespaces(name, "");
                name = StringUtils.replaceAll(name, "_", "");
                name = StringUtils.replaceAll(name, "-", "");
                name = name.toLowerCase();
                if ("id".equals(name) || (tableName + "id").equals(name) || (tableName + "number").equals(name)
                        || (tableName + "key").equals(name)) {
                    logger.info("Using existing source column for ORDER BY clause on slave jobs: {}", sourceColumn);
                    return sourceColumn;
                }
            }
        }

        // last resort: Pick any source column and sort on that (might not work
        // if the column contains a lot of repeated values)
        final MetaModelInputColumn sourceColumn = sourceColumns.get(0);
        logger.warn(
                "Couldn't pick a good source column for ORDER BY clause on slave jobs. Picking the first column: {}",
                sourceColumn);
        return sourceColumn;
    }

    private RowProcessingPublishers getRowProcessingPublishers(AnalysisJob job, LifeCycleHelper lifeCycleHelper) {
        final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(job);

        final SingleThreadedTaskRunner taskRunner = new SingleThreadedTaskRunner();

        final RowProcessingPublishers publishers = new RowProcessingPublishers(job, null, taskRunner, lifeCycleHelper,
                sourceColumnFinder);

        return publishers;
    }

    private RowProcessingPublisher getRowProcessingPublisher(RowProcessingPublishers publishers) {
        final Table[] tables = publishers.getTables();

        if (tables.length != 1) {
            throw new UnsupportedOperationException("Jobs with multiple source tables are not distributable");
        }

        final Table table = tables[0];

        final RowProcessingPublisher publisher = publishers.getRowProcessingPublisher(table);
        return publisher;
    }

    private void failIfJobIsUnsupported(AnalysisJob job) throws UnsupportedOperationException {
        failIfComponentsAreUnsupported(job.getFilterJobs());
        failIfComponentsAreUnsupported(job.getTransformerJobs());
        failIfComponentsAreUnsupported(job.getAnalyzerJobs());
    }

    private void failIfComponentsAreUnsupported(Collection<? extends ComponentJob> jobs)
            throws UnsupportedOperationException {
        for (ComponentJob job : jobs) {
            final ComponentDescriptor<?> descriptor = job.getDescriptor();
            final boolean distributable = descriptor.isDistributable();
            if (!distributable) {
                throw new UnsupportedOperationException("Component is not distributable: " + job);
            }
        }
    }

}
