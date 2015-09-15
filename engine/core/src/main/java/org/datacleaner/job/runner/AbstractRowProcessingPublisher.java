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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.metamodel.query.Query;
import org.datacleaner.api.HasAnalyzerResult;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.concurrent.JoinTaskListener;
import org.datacleaner.job.concurrent.TaskListener;
import org.datacleaner.job.concurrent.TaskRunnable;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.tasks.CloseTaskListener;
import org.datacleaner.job.tasks.CollectResultsTask;
import org.datacleaner.job.tasks.InitializeTask;
import org.datacleaner.job.tasks.Task;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.util.SourceColumnFinder;

public abstract class AbstractRowProcessingPublisher implements RowProcessingPublisher {

    private final AtomicBoolean _success;
    private final RowProcessingPublishers _publishers;
    private final RowProcessingStream _stream;
    private final List<RowProcessingConsumer> _consumers;
    private final SourceColumnFinder _sourceColumnFinder;

    public AbstractRowProcessingPublisher(RowProcessingPublishers publishers, RowProcessingStream stream) {
        if (publishers == null) {
            throw new IllegalArgumentException("RowProcessingPublishers cannot be null");
        }
        if (stream == null) {
            throw new IllegalArgumentException("RowProcessingStream cannot be null");
        }
        _publishers = publishers;
        _stream = stream;
        _sourceColumnFinder = new SourceColumnFinder();
        _sourceColumnFinder.addSources(stream.getAnalysisJob());
        _consumers = new ArrayList<RowProcessingConsumer>();
        _success = new AtomicBoolean(true);
    }

    /**
     * Gets a {@link RowProcessingQueryOptimizer} instance from the subclass,
     * used to get the query and any optimized consumer list.
     * 
     * @return
     */
    protected abstract RowProcessingQueryOptimizer getQueryOptimizer();

    /**
     * Equivalent method to {@link #processRows(RowProcessingMetrics)}, which
     * returns whether or not the processing went well.
     * 
     * Subclasses should not be invoking
     * {@link AnalysisListener#rowProcessingSuccess(AnalysisJob, RowProcessingMetrics)}
     * .
     * 
     * @param analysisListener
     * @param rowProcessingMetrics
     * @return
     */
    protected abstract boolean processRowsInternal(AnalysisListener analysisListener,
            RowProcessingMetrics rowProcessingMetrics);

    @Override
    public final SourceColumnFinder getSourceColumnFinder() {
        return _sourceColumnFinder;
    }

    @Override
    public final RowProcessingStream getStream() {
        return _stream;
    }

    @Override
    public final List<RowProcessingConsumer> getConsumers() {
        return _consumers;
    }

    @Override
    public final void registerConsumer(final RowProcessingConsumer consumer) {
        _consumers.add(consumer);
    }

    @Override
    public final RowProcessingMetrics getRowProcessingMetrics() {
        return new RowProcessingMetricsImpl(_publishers, this);
    }

    @Override
    public final RowProcessingConsumer getConsumer(ComponentJob componentJob) {
        for (RowProcessingConsumer consumer : _consumers) {
            if (componentJob.equals(consumer.getComponentJob())) {
                return consumer;
            }
        }
        return null;
    }

    @Override
    public final RowProcessingPublishers getPublishers() {
        return _publishers;
    }

    @Override
    public final Query getQuery() {
        return getQueryOptimizer().getOptimizedQuery();
    }

    @Override
    public final ConsumeRowHandler createConsumeRowHandler() {
        final RowProcessingQueryOptimizer queryOptimizer = getQueryOptimizer();
        final Query finalQuery = queryOptimizer.getOptimizedQuery();

        final RowIdGenerator idGenerator;
        if (finalQuery.getFirstRow() == null) {
            idGenerator = new SimpleRowIdGenerator();
        } else {
            idGenerator = new SimpleRowIdGenerator(finalQuery.getFirstRow());
        }

        final RowProcessingPublishers publishers = getPublishers();
        final AnalysisListener analysisListener = publishers.getAnalysisListener();

        for (RowProcessingConsumer consumer : getConsumers()) {
            final ComponentJob componentJob = consumer.getComponentJob();
            final RowProcessingMetrics rowProcessingMetrics = getRowProcessingMetrics();
            final ComponentMetrics metrics = rowProcessingMetrics.getAnalysisJobMetrics().getComponentMetrics(
                    componentJob);
            analysisListener.componentBegin(getStream().getAnalysisJob(), componentJob, metrics);

            if (consumer instanceof TransformerConsumer) {
                ((TransformerConsumer) consumer).setRowIdGenerator(idGenerator);
            }
        }
        final List<RowProcessingConsumer> consumers = queryOptimizer.getOptimizedConsumers();
        final Collection<? extends FilterOutcome> availableOutcomes = queryOptimizer.getOptimizedAvailableOutcomes();
        final ConsumeRowHandler consumeRowHandler = new ConsumeRowHandler(consumers, availableOutcomes);
        return consumeRowHandler;
    }

    protected final Task createCollectResultTask(RowProcessingConsumer consumer, Queue<JobAndResult> resultQueue) {
        final Object component = consumer.getComponent();
        if (component instanceof HasAnalyzerResult) {
            final HasAnalyzerResult<?> hasAnalyzerResult = (HasAnalyzerResult<?>) component;
            final AnalysisListener analysisListener = _publishers.getAnalysisListener();
            return new CollectResultsTask(hasAnalyzerResult, _stream.getAnalysisJob(), consumer.getComponentJob(),
                    resultQueue, analysisListener);
        }
        return null;
    }

    protected final TaskRunnable createCloseTask(RowProcessingConsumer consumer, TaskListener closeTaskListener) {
        final LifeCycleHelper lifeCycleHelper = _publishers.getConsumerSpecificLifeCycleHelper(consumer);
        final CloseTaskListener taskListener = new CloseTaskListener(lifeCycleHelper, consumer, _success,
                closeTaskListener, _publishers.getAnalysisListener(), _stream.getAnalysisJob());
        return new TaskRunnable(null, taskListener);
    }

    protected final TaskRunnable createInitTask(RowProcessingConsumer consumer, TaskListener listener) {
        final LifeCycleHelper lifeCycleHelper = _publishers.getConsumerSpecificLifeCycleHelper(consumer);
        final InitializeTask task = new InitializeTask(lifeCycleHelper, consumer);
        return new TaskRunnable(task, listener);
    }

    @Override
    public final void processRows(RowProcessingMetrics rowProcessingMetrics) {
        final AnalysisListener analysisListener = getAnalysisListener();

        final boolean success = processRowsInternal(analysisListener, rowProcessingMetrics);

        if (!success) {
            _success.set(false);
            return;
        }

        analysisListener.rowProcessingSuccess(getAnalysisJob(), rowProcessingMetrics);
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "[stream=" + getStream() + ", consumers=" + _consumers.size() + "]";
    }

    protected final TaskRunner getTaskRunner() {
        return getPublishers().getTaskRunner();
    }

    @Override
    public final AnalysisJob getAnalysisJob() {
        return getStream().getAnalysisJob();
    }

    @Override
    public final AnalysisListener getAnalysisListener() {
        return getPublishers().getAnalysisListener();
    }

    /**
     * Initializes consumers of this {@link SourceTableRowProcessingPublisher}.
     * 
     * This method will not initialize consumers containing
     * {@link MultiStreamComponent}s. Ensure that
     * {@link #initializeMultiStreamConsumers(Set)} is also invoked.
     * 
     * Once consumers are initialized, row processing can begin, expected rows
     * can be calculated and more.
     * 
     * @param finishedListener
     */
    @Override
    public final void initializeConsumers(TaskListener finishedListener) {
        final TaskRunner taskRunner = getTaskRunner();
        final List<RowProcessingConsumer> configurableConsumers = getConsumers();
        final int numConfigurableConsumers = configurableConsumers.size();
        final JoinTaskListener initFinishedListener = new JoinTaskListener(numConfigurableConsumers, finishedListener);
        for (RowProcessingConsumer consumer : configurableConsumers) {
            final TaskRunnable task = createInitTask(consumer, initFinishedListener);
            taskRunner.run(task);
        }
    }

    /**
     * Closes consumers of this {@link SourceTableRowProcessingPublisher}.
     * Usually this will be done automatically when
     * {@link #runRowProcessing(Queue, TaskListener)} is invoked.
     */
    @Override
    public final void closeConsumers() {
        final TaskRunner taskRunner = getTaskRunner();
        final List<RowProcessingConsumer> configurableConsumers = getConsumers();
        for (RowProcessingConsumer consumer : configurableConsumers) {
            final TaskRunnable task = createCloseTask(consumer, null);
            taskRunner.run(task);
        }
    }
}
