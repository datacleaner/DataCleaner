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
package org.eobjects.analyzer.util.ws;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import junit.framework.TestCase;

public class ServiceSessionTest extends TestCase {

    public void testConnectionPool() throws Exception {
        final int poolSize = 5;
        final int executionTimeMillis = 200;

        final ServiceSession<Boolean> session = new PooledServiceSession<Boolean>(poolSize);
        final List<Long> executionTimes = Collections.synchronizedList(new ArrayList<Long>());
        final Thread[] threads = new Thread[poolSize + 1];
        for (int i = 0; i < threads.length; i++) {
            Thread thread = new Thread() {
                public void run() {
                    final long timeBefore = System.currentTimeMillis();
                    session.invokeService(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            Thread.sleep(executionTimeMillis);
                            long timeAfter = System.currentTimeMillis();
                            executionTimes.add(timeAfter - timeBefore);
                            return Boolean.TRUE;
                        }
                    });
                }
            };
            thread.start();
            threads[i] = thread;
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        assertEquals(threads.length, executionTimes.size());

        for (int i = 0; i < poolSize; i++) {
            // the first threads will take only some millis to run
            final double executionTime = executionTimes.get(i).doubleValue();
            final double marginValue = executionTimeMillis * 4;
            assertTrue("executionTime=" + executionTime + ", should be less than " + marginValue,
                    executionTime < marginValue);
        }

        // The last thread will have taken more time, because it has
        // to wait for the pool.
        final Long executionTimeForLastThread = executionTimes.get(poolSize);
        final double marginValue = executionTimeMillis * 1.1;
        assertTrue("Execution time for last thread: " + executionTimeForLastThread + ", should be less than "
                + marginValue, executionTimeForLastThread > marginValue);
    }
}
