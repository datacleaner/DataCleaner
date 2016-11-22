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

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.datacleaner.job.runner.AnalysisJobMetrics;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.tasks.Task;
import org.datacleaner.util.ConcurrencyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Completion listener for a full AnalysisJob. Use the isDone() method to ask
 * whether or not the job is finished.
 *
 *
 */
public final class JobCompletionTaskListener implements StatusAwareTaskListener {

    private static final Logger logger = LoggerFactory.getLogger(JobCompletionTaskListener.class);

    private final CountDownLatch _countDownLatch;
    private final AtomicInteger _successCountDown;
    private final AnalysisListener _analysisListener;
    private final AnalysisJobMetrics _analysisJobMetrics;
    private Date _completionTime;

    public JobCompletionTaskListener(final AnalysisJobMetrics analysisJobMetrics,
            final AnalysisListener analysisListener, final int callablesToWaitFor) {
        _analysisJobMetrics = analysisJobMetrics;
        _analysisListener = analysisListener;
        _countDownLatch = new CountDownLatch(callablesToWaitFor);
        _successCountDown = new AtomicInteger(callablesToWaitFor);
    }

    @Override
    public void await() throws InterruptedException {
        ConcurrencyUtils.awaitCountDown(_countDownLatch, "job completion");
    }

    @Override
    public boolean isDone() {
        return _countDownLatch.getCount() == 0;
    }

    @Override
    public void await(final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        _countDownLatch.await(timeout, timeUnit);
    }

    @Override
    public void onBegin(final Task task) {
    }

    @Override
    public void onComplete(final Task task) {
        try {
            logger.debug("onComplete(...)");

            final int successCountDownStatus = _successCountDown.decrementAndGet();
            if (successCountDownStatus == 0) {
                _completionTime = new Date();
                _analysisListener.jobSuccess(_analysisJobMetrics.getAnalysisJob(), _analysisJobMetrics);
            }

        } finally {
            // as the last thing we need to call countDown() to unlock any waiting
            // threads on await()

            _countDownLatch.countDown();
        }
    }

    @Override
    public void onError(final Task task, final Throwable throwable) {
        try {
            logger.debug("onError(...)");
            _analysisListener.errorUnknown(_analysisJobMetrics.getAnalysisJob(), throwable);
        } finally {
            _countDownLatch.countDown();
        }
    }

    @Override
    public Date getCompletionTime() {
        return _completionTime;
    }
}
