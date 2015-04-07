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
package org.datacleaner.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class UsageAwareCloseableTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(UsageAwareCloseableTest.class);

    private UsageAwareCloseable _closeable;
    private AtomicInteger _closedCounter;
    private AtomicInteger _createdCounter;
    private AtomicInteger _actionCounter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _createdCounter = new AtomicInteger(0);
        _closedCounter = new AtomicInteger(0);
        _actionCounter = new AtomicInteger(0);
        _closeable = createCloseable();
    }

    private UsageAwareCloseable createCloseable() {
        _createdCounter.incrementAndGet();
        return new UsageAwareCloseable() {
            @Override
            protected void closeInternal() {
                _closedCounter.incrementAndGet();
            }
        };
    }

    public void testSimple() throws Exception {
        assertFalse(_closeable.isClosed());
        assertEquals(0, _closedCounter.get());
        _closeable.close();

        assertEquals(1, _closedCounter.get());
        assertTrue(_closeable.isClosed());
    }

    public void testStackedUsage() throws Exception {
        assertFalse(_closeable.isClosed());
        assertEquals(0, _closedCounter.get());
        assertTrue(_closeable.requestUsage());
        assertTrue(_closeable.requestUsage());
        assertFalse(_closeable.isClosed());
        assertEquals(0, _closedCounter.get());
        _closeable.close();
        _closeable.close();
        assertFalse(_closeable.isClosed());
        assertEquals(0, _closedCounter.get());
        _closeable.close();
        assertTrue(_closeable.isClosed());
        assertEquals(1, _closedCounter.get());
    }

    public void testCloseMultipleTimes() throws Exception {
        assertFalse(_closeable.isClosed());
        assertEquals(0, _closedCounter.get());

        _closeable.close();
        assertEquals(1, _closedCounter.get());
        assertTrue(_closeable.isClosed());

        _closeable.close();
        assertEquals(1, _closedCounter.get());
        assertTrue(_closeable.isClosed());
    }

    public void testMultiThreadedUse() throws Throwable {
        // start with a closed instance
        _closeable.close();
        assertTrue(_closeable.isClosed());
        assertEquals(1, _closedCounter.get());
        _createdCounter.set(0);
        _closedCounter.set(0);

        final int numThreads = 3;
        final int numInternalUsages = 500;

        ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        final Future<?>[] futures = new Future[numThreads];
        for (int i = 0; i < numThreads; i++) {
            final int threadNumber = i + 1;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < numInternalUsages; j++) {
                        String action = "Thread" + threadNumber + "_" + numInternalUsages + "_"
                                + _actionCounter.incrementAndGet();

                        logger.debug("Usage (B" + action + "): " + _closeable.getUsageCount() + ", Creates: "
                                + _createdCounter.get() + ", Closes: " + _closedCounter.get());

                        try (UsageAwareCloseable closeable = giveMeUsage(action)) {
                            // do something
                            logger.debug("Usage (A" + action + "): " + _closeable.getUsageCount() + ", Creates: "
                                    + _createdCounter.get() + ", Closes: " + _closedCounter.get());
                        }
                    }
                }
            };
            futures[i] = threadPool.submit(runnable);
        }

        for (int i = 0; i < numThreads; i++) {
            try {
                futures[i].get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        }

        threadPool.shutdown();

        assertEquals(_closedCounter.get(), _createdCounter.get());

        assertTrue("Closed below expected: " + _closedCounter, _closedCounter.get() > 3);
    }

    // will either return the existing or create a new closable for usage.
    private UsageAwareCloseable giveMeUsage(String action) {
        final UsageAwareCloseable closeable = _closeable;
        final boolean useExisting = closeable.requestUsage();
        if (useExisting) {
            int closed = _closedCounter.get();
            int created = _createdCounter.get();
            assertEquals("Action: " + action + ", Closed: " + closed + ", Created: " + created, closed + 1, created);
            return closeable;
        }
        synchronized (this) {
            if (_closeable.isClosed()) {
                int closed = _closedCounter.get();
                int created = _createdCounter.get();
                assertEquals("Action: " + action + ", Closed: " + closed + ", Created: " + created, closed, created);
                _closeable = createCloseable();
                return _closeable;
            }
        }

        // repeat (still unsynchronized)
        return giveMeUsage(action);
    }
}
