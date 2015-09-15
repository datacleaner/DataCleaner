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
import java.util.List;

import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.job.concurrent.ForkTaskListener;
import org.datacleaner.job.concurrent.TaskListener;
import org.datacleaner.job.concurrent.TaskRunnable;
import org.datacleaner.job.tasks.RunRowProcessingPublisherTask;
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
    protected void runRowProcessingInternal(List<TaskRunnable> postProcessingTasks) {
        final TaskListener runCompletionListener = new ForkTaskListener("run row processing (" + getStream() + ")",
                getTaskRunner(), postProcessingTasks);

        final RowProcessingMetrics rowProcessingMetrics = getRowProcessingMetrics();
        final RunRowProcessingPublisherTask runTask = new RunRowProcessingPublisherTask(this, rowProcessingMetrics);

        getTaskRunner().run(runTask, runCompletionListener);
    }

    @Override
    protected RowProcessingQueryOptimizer getQueryOptimizer() {
        final Table table = getStream().getTable();
        final Query q = new Query().from(table).select(table.getColumns());
        return new NoopRowProcessingQueryOptimizer(q, getConsumers());
    }
}
