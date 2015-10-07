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

import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.metamodel.query.Query;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.concurrent.TaskListener;
import org.datacleaner.util.SourceColumnFinder;

public interface RowProcessingPublisher {

    /**
     * Gets metrics for this row processing session. Note that consumers are
     * assumed to be initialized at this point. See
     * {@link #initializeConsumers(TaskListener)}.
     * 
     * @return
     */
    public RowProcessingMetrics getRowProcessingMetrics();

    public RowProcessingStream getStream();

    public Query getQuery();

    public void registerConsumer(final RowProcessingConsumer consumer);

    public RowProcessingConsumer getConsumer(ComponentJob componentJob);

    public List<RowProcessingConsumer> getConsumers();

    public RowProcessingPublishers getPublishers();

    public void onAllConsumersRegistered();

    /**
     * Fires the actual row processing. This method assumes that consumers have
     * been initialized and the publisher is ready to start processing.
     * 
     * @see #runRowProcessing(Queue, TaskListener)
     */
    public void processRows(RowProcessingMetrics rowProcessingMetrics);

    /**
     * Runs the whole row processing logic, start to finish, including
     * initialization, process rows, result collection and cleanup/closing
     * resources.
     * 
     * @param resultQueue
     *            a queue on which to append results
     * @param finishedTaskListener
     *            a task listener which will be invoked once the processing is
     *            done.
     * @return a boolean indicating whether or not the
     *         {@link RowProcessingPublisher} was ready to start processing. If
     *         false is returned it is typically because some dependent
     *         {@link RowProcessingPublisher}s should be run first in which case
     *         the call to {@link #runRowProcessing(Queue, TaskListener)} should
     *         be repeated again when other publishers have run.
     * 
     * @see #processRows(RowProcessingMetrics)
     * @see #initializeConsumers(TaskListener)
     */
    public boolean runRowProcessing(Queue<JobAndResult> resultQueue, TaskListener finishedTaskListener);

    /**
     * Initializes consumers of this {@link RowProcessingPublisher}.
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
    public void initializeConsumers(TaskListener finishedListener);

    /**
     * Closes consumers of this {@link RowProcessingPublisher}. Usually this
     * will be done automatically when
     * {@link #runRowProcessing(Queue, TaskListener)} is invoked.
     */
    public void closeConsumers();

    public SourceColumnFinder getSourceColumnFinder();

    public AnalysisJob getAnalysisJob();

    public AnalysisListener getAnalysisListener();

    public ConsumeRowHandler createConsumeRowHandler();
}
