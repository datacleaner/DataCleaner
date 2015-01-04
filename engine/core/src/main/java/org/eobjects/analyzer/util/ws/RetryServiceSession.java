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
package org.eobjects.analyzer.util.ws;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ServiceSession} that automatically retries a service request in case
 * it fails.
 */
public class RetryServiceSession<R> extends SimpleServiceSession<R> implements ServiceSession<R> {

    public static final int DEFAULT_RETRY_SLEEP_TIME = 1000;

    private static final Logger logger = LoggerFactory.getLogger(SimpleServiceSession.class);

    private final int _maxRetries;
    private final AtomicInteger _successCount;
    private final AtomicInteger _retryCount;
    private final AtomicInteger _failureCount;
    private final int[] _sleepTimeBetweenRetries;

    /**
     * Constructs a {@link RetryServiceSession}.
     * 
     * @param maxRetries
     *            maximum number of retries
     */
    public RetryServiceSession(int maxRetries) {
        this(maxRetries, null);
    }

    /**
     * Constructs a {@link RetryServiceSession}.
     * 
     * @param maxRetries
     *            maximum number of retries
     * @param sleepTimeBetweenRetries
     *            the amount of time to sleep between retries. When this array
     *            is null, empty or not of appropriate size, a sleep time of
     *            {@link #DEFAULT_RETRY_SLEEP_TIME} will be used.
     */
    public RetryServiceSession(int maxRetries, int[] sleepTimeBetweenRetries) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("Max retries cannot be a negative number");
        }
        _maxRetries = maxRetries;
        _sleepTimeBetweenRetries = sleepTimeBetweenRetries;

        _successCount = new AtomicInteger();
        _retryCount = new AtomicInteger();
        _failureCount = new AtomicInteger();
    }

    @Override
    public ServiceResult<R> invokeService(Callable<R> callable) {
        // note attemptNo is 1-based
        int attemptNo = 1;
        while (true) {
            final ServiceResult<R> result = super.invokeService(callable);
            if (result.isSuccesfull()) {
                _successCount.incrementAndGet();
                return result;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Attempt no. " + attemptNo + " to invoke service failed", result.getError());
                }
            }

            if (attemptNo > _maxRetries) {
                _failureCount.incrementAndGet();
                return result;
            }

            long sleepTime = getSleepTime(attemptNo);

            attemptNo++;
            _retryCount.incrementAndGet();

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }
    }

    private long getSleepTime(int attemptNo) {
        int index = attemptNo - 1;
        if (_sleepTimeBetweenRetries != null && _sleepTimeBetweenRetries.length > index) {
            return _sleepTimeBetweenRetries[index];
        }
        return DEFAULT_RETRY_SLEEP_TIME;
    }

    /**
     * Gets the maximum number of retries tolerated before the service
     * invocation is considered a failure.
     * 
     * @return
     */
    public int getMaxRetries() {
        return _maxRetries;
    }

    /**
     * Gets the actual number of service invocation retries performed
     * 
     * @return
     */
    public int getRetryCount() {
        return _retryCount.get();
    }

    /**
     * Gets the number of failed service invocations. A failure in this sense
     * mean that all retries failed.
     * 
     * @return
     */
    public int getFailureCount() {
        return _failureCount.get();
    }

    /**
     * Gets the number of successful service invocations.
     * 
     * @return
     */
    public int getSuccessCount() {
        return _successCount.get();
    }
}
