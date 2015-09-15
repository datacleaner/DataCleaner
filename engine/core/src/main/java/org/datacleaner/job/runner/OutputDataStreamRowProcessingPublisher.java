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

import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.job.concurrent.ForkTaskListener;
import org.datacleaner.job.concurrent.JoinTaskListener;
import org.datacleaner.job.concurrent.TaskListener;
import org.datacleaner.job.concurrent.TaskRunnable;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.tasks.RunRowProcessingPublisherTask;
import org.datacleaner.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RowProcessingPublisher} implementation for {@link OutputDataStream}s.
 */
public final class OutputDataStreamRowProcessingPublisher extends AbstractRowProcessingPublisher {

    private final static Logger logger = LoggerFactory.getLogger(OutputDataStreamRowProcessingPublisher.class);

    private final RowProcessingPublisher _parentPublisher;

    /**
     * Constructor to use for {@link OutputDataStreamRowProcessingPublisher}s
     * that are parented by another
     * {@link OutputDataStreamRowProcessingPublisher}. When a parent publisher
     * exists the execution flow is adapted since records will be dispatched by
     * a (component within the) parent instead of sourced by the
     * {@link OutputDataStreamRowProcessingPublisher} itself.
     * 
     * @param publishers
     * @param parentPublisher
     * @param stream
     */
    public OutputDataStreamRowProcessingPublisher(RowProcessingPublishers publishers,
            RowProcessingPublisher firstParentPublisher, RowProcessingStream stream) {
        super(publishers, stream);
        if (firstParentPublisher == null) {
            throw new IllegalArgumentException("Parent RowProcessingPublisher cannot be null");
        }
        _parentPublisher = firstParentPublisher;
    }

    @Override
    public void onAllConsumersRegistered() {
        // do nothing
    }

    @Override
    protected boolean processRowsInternal(AnalysisListener listener, RowProcessingMetrics rowProcessingMetrics) {
        final List<RowProcessingConsumer> consumers = _parentPublisher.getConsumers();
        for (RowProcessingConsumer consumer : consumers) {
            final Collection<ActiveOutputDataStream> activeOutputDataStreams = consumer.getActiveOutputDataStreams();
            for (ActiveOutputDataStream activeOutputDataStream : activeOutputDataStreams) {
                try {
                    activeOutputDataStream.await();
                } catch (InterruptedException e) {
                    logger.error("Unexpected error awaiting output data stream", e);
                    listener.errorUnknown(getAnalysisJob(), e);
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void runRowProcessing(Queue<JobAndResult> resultQueue, TaskListener finishedTaskListener) {
        final List<RowProcessingConsumer> configurableConsumers = getConsumers();

        final int numConsumers = configurableConsumers.size();

        // add tasks for closing components
        final JoinTaskListener closeTaskListener = new JoinTaskListener(numConsumers, finishedTaskListener);
        final List<TaskRunnable> closeTasks = new ArrayList<>();
        for (RowProcessingConsumer consumer : configurableConsumers) {
            closeTasks.add(createCloseTask(consumer, closeTaskListener));
        }
        final TaskRunner taskRunner = getTaskRunner();

        final TaskListener getResultCompletionListener = new ForkTaskListener("collect results (" + getStream() + ")",
                taskRunner, closeTasks);

        // add tasks for collecting results
        final TaskListener getResultTaskListener = new JoinTaskListener(numConsumers, getResultCompletionListener);
        final List<TaskRunnable> getResultTasks = new ArrayList<>();
        for (RowProcessingConsumer consumer : configurableConsumers) {
            final Task collectResultTask = createCollectResultTask(consumer, resultQueue);
            if (collectResultTask == null) {
                getResultTasks.add(new TaskRunnable(null, getResultTaskListener));
            } else {
                getResultTasks.add(new TaskRunnable(collectResultTask, getResultTaskListener));
            }
        }

        final TaskListener runCompletionListener = new ForkTaskListener("run row processing (" + getStream() + ")",
                taskRunner, getResultTasks);

        final RowProcessingMetrics rowProcessingMetrics = getRowProcessingMetrics();
        final RunRowProcessingPublisherTask runTask = new RunRowProcessingPublisherTask(this, rowProcessingMetrics);

        taskRunner.run(runTask, runCompletionListener);
    }

    @Override
    protected RowProcessingQueryOptimizer getQueryOptimizer() {
        final Table table = getStream().getTable();
        final Query q = new Query().from(table).select(table.getColumns());
        return new NoopRowProcessingQueryOptimizer(q, getConsumers());
    }
}
