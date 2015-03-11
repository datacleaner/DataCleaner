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
package org.datacleaner.job.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.datacleaner.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The preferred {@link TaskRunner} implementation based on the
 * java.util.concurrent package (specifically the {@link ExecutorService}
 * class).
 */
public final class MultiThreadedTaskRunner implements TaskRunner {

    private static final Logger logger = LoggerFactory.getLogger(MultiThreadedTaskRunner.class);
    private final ThreadFactory _threadFactory;

    private final ExecutorService _executorService;
    private final int _numThreads;
    private final BlockingQueue<Runnable> _workQueue;

    public MultiThreadedTaskRunner() {
        this(30);
    }

    public MultiThreadedTaskRunner(int numThreads) {
        _numThreads = numThreads;

        // if all threads are busy, newly submitted tasks will be run by caller
        final ThreadPoolExecutor.CallerRunsPolicy rejectionHandler = new ThreadPoolExecutor.CallerRunsPolicy();

        // there will be a minimum task capacity of 1000, and preferably
        // numThreads * 10 (to avoid blocking buffer behaviour and to prepare
        // tasks for working threads in advance)
        final int taskCapacity = Math.max(1000, numThreads * 10);

        _threadFactory = new DaemonThreadFactory();

        // This queue is a buffer for tasks to be processed.
        // It uses a hack that forces the ThreadPoolExecutor to block if a
        // caller tries to submit a
        // task when the pool is fully loaded. This will prevent to not process
        // input row
        // inside a RunRowProcessingPublisherTask thread (CallerRunsPolicy). If
        // processing of such row
        // would take a long time, it would cause other processing threads
        // starvation after they
        // finish their current work. So we rather block until the queue has
        // place in it.
        _workQueue = new AlwaysBlockingQueue<>(taskCapacity);

        _executorService = new ThreadPoolExecutor(numThreads, numThreads, 60, TimeUnit.SECONDS, _workQueue,
                _threadFactory, rejectionHandler);
    }

    /**
     * @return the amount of threads in the thread pool, or -1 if this
     *         information is not available
     */
    public int getNumThreads() {
        return _numThreads;
    }

    @Override
    public void run(final Task task, final TaskListener listener) {
        logger.debug("run({},{})", task, listener);
        executeInternal(new TaskRunnable(task, listener));
    }

    @Override
    public void run(TaskRunnable taskRunnable) {
        logger.debug("run({})", taskRunnable);
        executeInternal(taskRunnable);
    }

    private void executeInternal(TaskRunnable taskRunnable) {
        try {
            _executorService.execute(taskRunnable);
        } catch (RejectedExecutionException e) {
            logger.error("Unexpected rejected execution!", e);
        }
    }

    @Override
    public void shutdown() {
        logger.info("shutdown() called, shutting down executor service");
        _executorService.shutdown();
    }

    public ExecutorService getExecutorService() {
        return _executorService;
    }

    @Override
    protected void finalize() throws Throwable {
        shutdown();
    }

    @Override
    public void assistExecution() {
        Runnable task = _workQueue.poll();
        if (task != null) {
            task.run();
        }
    }

    /**
     * We keep this class as private because it shouldn't be used anywhere else,
     * it is a hack. It actually breaks a blocking queue contract. The reason
     * for it is that Java ThreadPoolExecutor does not support blocking
     * behaviour for the caller. So we override the non-blocking method of the
     * queue to behave as a blocking one.
     */
    class AlwaysBlockingQueue<T> extends ArrayBlockingQueue<T> {

        private static final long serialVersionUID = 1L;

        AlwaysBlockingQueue(int size) {
            super(size);
        }

        public boolean offer(T task) {
            try {
                this.put(task);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
    }

}
