/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.job.runner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.ConfigurableBeanJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.concurrent.ForkTaskListener;
import org.eobjects.analyzer.job.concurrent.JobCompletionTaskListener;
import org.eobjects.analyzer.job.concurrent.JoinTaskListener;
import org.eobjects.analyzer.job.concurrent.TaskListener;
import org.eobjects.analyzer.job.concurrent.TaskRunnable;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.tasks.CloseReferenceDataTaskListener;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.apache.metamodel.schema.Table;
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
    private final AnalyzerBeansConfiguration _configuration;
    private final TaskRunner _taskRunner;
    private final AnalysisListener _analysisListener;
    private final Queue<JobAndResult> _resultQueue;
    private final ErrorAware _errorAware;
    private final Collection<AnalyzerJob> _analyzerJobs;
    private final Collection<TransformerJob> _transformerJobs;
    private final Collection<FilterJob> _filterJobs;
    private final SourceColumnFinder _sourceColumnFinder;
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
    public AnalysisRunnerJobDelegate(AnalysisJob job, AnalyzerBeansConfiguration configuration, TaskRunner taskRunner,
            AnalysisListener analysisListener, Queue<JobAndResult> resultQueue, ErrorAware errorAware,
            boolean includeNonDistributedTasks) {
        _job = job;
        _configuration = configuration;
        _taskRunner = taskRunner;
        _analysisListener = analysisListener;
        _resultQueue = resultQueue;
        _includeNonDistributedTasks = includeNonDistributedTasks;

        _sourceColumnFinder = new SourceColumnFinder();
        _sourceColumnFinder.addSources(_job);

        _errorAware = errorAware;

        _transformerJobs = _job.getTransformerJobs();
        _filterJobs = _job.getFilterJobs();

        _analyzerJobs = _job.getAnalyzerJobs();
    }

    /**
     * Runs the job
     * 
     * @return
     */
    public AnalysisResultFuture run() {
        try {
            // the injection manager is job scoped
            final InjectionManager injectionManager = _configuration.getInjectionManager(_job);

            final LifeCycleHelper rowProcessingLifeCycleHelper = new LifeCycleHelper(injectionManager,
                    new ReferenceDataActivationManager(), _includeNonDistributedTasks);

            final RowProcessingPublishers publishers = new RowProcessingPublishers(_job, _analysisListener,
                    _taskRunner, rowProcessingLifeCycleHelper, _sourceColumnFinder);

            final AnalysisJobMetrics analysisJobMetrics = publishers.getAnalysisJobMetrics();

            // A task listener that will register either succesfull executions
            // or unexpected errors (which will be delegated to the
            // errorListener)
            JobCompletionTaskListener jobCompletionTaskListener = new JobCompletionTaskListener(analysisJobMetrics,
                    _analysisListener, 1);

            _analysisListener.jobBegin(_job, analysisJobMetrics);

            validateSingleTableInput(_transformerJobs);
            validateSingleTableInput(_filterJobs);
            validateSingleTableInput(_analyzerJobs);

            // at this point we are done validating the job, it will run.
            scheduleRowProcessing(publishers, rowProcessingLifeCycleHelper, jobCompletionTaskListener,
                    analysisJobMetrics);

            return new AnalysisResultFutureImpl(_resultQueue, jobCompletionTaskListener, _errorAware);
        } catch (RuntimeException e) {
            _analysisListener.errorUknown(_job, e);
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

        logger.info("Created {} row processor publishers", publishers.size());

        final List<TaskRunnable> finalTasks = new ArrayList<TaskRunnable>(2);
        finalTasks.add(new TaskRunnable(null, jobCompletionTaskListener));
        finalTasks.add(new TaskRunnable(null, new CloseReferenceDataTaskListener(lifeCycleHelper)));

        final ForkTaskListener finalTaskListener = new ForkTaskListener("All row consumers finished", _taskRunner,
                finalTasks);

        final TaskListener rowProcessorPublishersDoneCompletionListener = new JoinTaskListener(publishers.size(),
                finalTaskListener);

        final Collection<RowProcessingPublisher> rowProcessingPublishers = publishers.getRowProcessingPublishers();
        for (RowProcessingPublisher rowProcessingPublisher : rowProcessingPublishers) {
            logger.debug("Scheduling row processing publisher: {}", rowProcessingPublisher);
            rowProcessingPublisher.runRowProcessing(_resultQueue, rowProcessorPublishersDoneCompletionListener);
        }
    }

    /**
     * Prevents that any row processing components have input from different
     * tables.
     * 
     * @param beanJobs
     */
    private void validateSingleTableInput(Collection<? extends ConfigurableBeanJob<?>> beanJobs) {
        for (ConfigurableBeanJob<?> beanJob : beanJobs) {
            Table originatingTable = null;
            InputColumn<?>[] input = beanJob.getInput();

            for (InputColumn<?> inputColumn : input) {
                Table table = _sourceColumnFinder.findOriginatingTable(inputColumn);
                if (table != null) {
                    if (originatingTable == null) {
                        originatingTable = table;
                    } else {
                        if (!originatingTable.equals(table)) {
                            throw new IllegalArgumentException("Input columns in " + beanJob
                                    + " originate from different tables");
                        }
                    }
                }
            }
        }

    }

}
