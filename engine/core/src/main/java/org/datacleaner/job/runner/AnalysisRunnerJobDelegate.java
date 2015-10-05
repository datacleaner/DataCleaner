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
package org.datacleaner.job.runner;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.metamodel.schema.Table;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.OutputDataStreamJob;
import org.datacleaner.job.concurrent.JobCompletionTaskListener;
import org.datacleaner.job.concurrent.JoinTaskListener;
import org.datacleaner.job.concurrent.TaskListener;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.util.SourceColumnFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A delegate for the AnalysisRunner to put the state of a single job into.
 * 
 * As opposed to the AnalysisRunner, this class is NOT thread-safe (which is why
 * the AnalysisRunner instantiates a new delegate for each execution).
 */
final class AnalysisRunnerJobDelegate {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisRunnerJobDelegate.class);

    private final AnalysisJob _job;
    private final DataCleanerConfiguration _configuration;
    private final TaskRunner _taskRunner;
    private final AnalysisListener _analysisListener;
    private final Queue<JobAndResult> _resultQueue;
    private final ErrorAware _errorAware;
    private final boolean _includeNonDistributedTasks;

    /**
     * 
     * @param job
     * @param configuration
     * @param taskRunner
     * @param analysisListener
     * @param resultQueue
     * @param errorAware
     * @param includeNonDistributedTasks
     *            determines if non-distributed tasks on components, such as
     *            {@link Initialize} methods that are not distributed, should be
     *            executed or not. On single-node executions, this will
     *            typically be true, on slave nodes in a cluster, this will
     *            typically be false.
     */
    public AnalysisRunnerJobDelegate(AnalysisJob job, DataCleanerConfiguration configuration, TaskRunner taskRunner,
            AnalysisListener analysisListener, Queue<JobAndResult> resultQueue, ErrorAware errorAware,
            boolean includeNonDistributedTasks) {
        _job = job;
        _configuration = configuration;
        _taskRunner = taskRunner;
        _analysisListener = analysisListener;
        _resultQueue = resultQueue;
        _includeNonDistributedTasks = includeNonDistributedTasks;
        _errorAware = errorAware;
    }

    /**
     * Runs the job
     * 
     * @return
     */
    public AnalysisResultFuture run() {
        try {
            // the injection manager is job scoped
            final InjectionManager injectionManager = _configuration.getEnvironment().getInjectionManagerFactory()
                    .getInjectionManager(_configuration, _job);

            final LifeCycleHelper rowProcessingLifeCycleHelper = new LifeCycleHelper(injectionManager,
                    _includeNonDistributedTasks);

            final RowProcessingPublishers publishers = new RowProcessingPublishers(_job, _analysisListener,
                    _taskRunner, rowProcessingLifeCycleHelper);

            final AnalysisJobMetrics analysisJobMetrics = publishers.getAnalysisJobMetrics();

            // A task listener that will register either succesfull executions
            // or unexpected errors (which will be delegated to the
            // errorListener)
            final JobCompletionTaskListener jobCompletionTaskListener = new JobCompletionTaskListener(
                    analysisJobMetrics, _analysisListener, 1);

            _analysisListener.jobBegin(_job, analysisJobMetrics);

            validateSingleTableInput(_job);

            // at this point we are done validating the job, it will run.
            scheduleRowProcessing(publishers, rowProcessingLifeCycleHelper, jobCompletionTaskListener,
                    analysisJobMetrics);

            return new AnalysisResultFutureImpl(_resultQueue, jobCompletionTaskListener, _errorAware);
        } catch (RuntimeException e) {
            _analysisListener.errorUnknown(_job, e);
            throw e;
        }

    }

    /**
     * Starts row processing job flows.
     * 
     * @param publishers
     * @param analysisJobMetrics
     * 
     * @param injectionManager
     */
    private void scheduleRowProcessing(RowProcessingPublishers publishers, LifeCycleHelper lifeCycleHelper,
            JobCompletionTaskListener jobCompletionTaskListener, AnalysisJobMetrics analysisJobMetrics) {

        logger.info("Created {} row processor publisher(s)", publishers.size());
        final TaskListener rowProcessorPublishersDoneCompletionListener = new JoinTaskListener(publishers.size(),
                jobCompletionTaskListener);

        final Collection<RowProcessingPublisher> rowProcessingPublishers = publishers.getRowProcessingPublishers();
        logger.debug("RowProcessingPublishers: {}", rowProcessingPublishers);

        dispatchWhenReady(rowProcessingPublishers, rowProcessorPublishersDoneCompletionListener);
    }

    private void dispatchWhenReady(final Collection<RowProcessingPublisher> rowProcessingPublishers,
            final TaskListener rowProcessorPublishersDoneCompletionListener) {
        final LinkedList<RowProcessingPublisher> remainingRowProcessingPublishers = new LinkedList<>(
                rowProcessingPublishers);

        while (!remainingRowProcessingPublishers.isEmpty()) {
            boolean progressThisIteration = false;

            for (Iterator<RowProcessingPublisher> it = remainingRowProcessingPublishers.iterator(); it.hasNext();) {
                final RowProcessingPublisher rowProcessingPublisher = it.next();
                final boolean started = rowProcessingPublisher.runRowProcessing(_resultQueue,
                        rowProcessorPublishersDoneCompletionListener);
                if (started) {
                    logger.debug("Scheduled row processing publisher: {}", rowProcessingPublisher);
                    it.remove();
                    progressThisIteration = true;
                }
            }

            if (!progressThisIteration) {
                _taskRunner.assistExecution();
            }
        }
    }

    /**
     * Prevents that any row processing components have input from different
     * tables.
     * 
     * @param job
     */
    private void validateSingleTableInput(AnalysisJob job) {
        final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(job);
        validateSingleTableInput(sourceColumnFinder, job.getTransformerJobs());
        validateSingleTableInput(sourceColumnFinder, job.getFilterJobs());
        validateSingleTableInput(sourceColumnFinder, job.getAnalyzerJobs());
    }

    /**
     * Prevents that any row processing components have input from different
     * tables.
     * 
     * @param sourceColumnFinder
     * @param componentJobs
     */
    private void validateSingleTableInput(final SourceColumnFinder sourceColumnFinder,
            final Collection<? extends ComponentJob> componentJobs) {
        for (ComponentJob componentJob : componentJobs) {
            if (!componentJob.getDescriptor().isMultiStreamComponent()) {
                Table originatingTable = null;
                final InputColumn<?>[] input = componentJob.getInput();

                for (InputColumn<?> inputColumn : input) {
                    final Table table = sourceColumnFinder.findOriginatingTable(inputColumn);
                    if (table != null) {
                        if (originatingTable == null) {
                            originatingTable = table;
                        } else {
                            if (!originatingTable.equals(table)) {
                                throw new IllegalArgumentException("Input columns in " + componentJob
                                        + " originate from different tables");
                            }
                        }
                    }
                }
            }

            final OutputDataStreamJob[] outputDataStreamJobs = componentJob.getOutputDataStreamJobs();
            for (OutputDataStreamJob outputDataStreamJob : outputDataStreamJobs) {
                validateSingleTableInput(outputDataStreamJob.getJob());
            }
        }

    }

}
