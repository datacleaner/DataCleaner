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
package org.datacleaner.result;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

import org.apache.commons.lang.SerializationUtils;
import org.apache.metamodel.util.ImmutableRef;
import org.apache.metamodel.util.LazyRef;
import org.apache.metamodel.util.Ref;
import org.datacleaner.result.AnalyzerResultFuture.Listener;

public class AnalyzerResultFutureTest extends TestCase {

    public void testAddListenerWhenResultIsReady() throws Exception {
        final NumberResult result1 = new NumberResult(42);

        final AnalyzerResultFuture<NumberResult> future = new AnalyzerResultFuture<>("foo",
                new ImmutableRef<NumberResult>(result1));

        final NumberResult result2 = future.get();

        assertEquals(result1, result2);

        final AtomicBoolean b = new AtomicBoolean(false);

        future.addListener(new Listener<NumberResult>() {
            @Override
            public void onSuccess(NumberResult result) {
                assertEquals(result1, result);
                b.set(true);
            }

            @Override
            public void onError(RuntimeException error) {
                fail("This should never happen");
            }
        });

        assertTrue(b.get());

        assertEquals("AnalyzerResultFuture[foo]", future.toString());
    }

    public void testMultiThreadedListenerScenario() throws Exception {
        final int threadCount = 10;

        final Thread[] threads = new Thread[threadCount];
        @SuppressWarnings({ "unchecked" })
        final Listener<NumberResult>[] listeners = new Listener[threadCount];
        final Queue<Object> resultQueue = new ArrayBlockingQueue<>(threadCount);

        for (int i = 0; i < listeners.length; i++) {
            listeners[i] = new Listener<NumberResult>() {
                @Override
                public void onSuccess(NumberResult result) {
                    resultQueue.add(result);
                }

                @Override
                public void onError(RuntimeException error) {
                    resultQueue.add(error);
                }
            };
        }

        final Ref<NumberResult> resultRef = new LazyRef<NumberResult>() {
            @Override
            protected NumberResult fetch() throws Throwable {
                long randomSleepTime = (long) (1000 * Math.random());
                Thread.sleep(randomSleepTime);
                return new NumberResult(43);
            }
        };

        final AnalyzerResultFuture<NumberResult> future = new AnalyzerResultFuture<>("foo", resultRef);

        for (int i = 0; i < threads.length; i++) {
            final Listener<NumberResult> listener = listeners[i];
            threads[i] = new Thread() {
                @Override
                public void run() {
                    future.addListener(listener);
                }
            };
        }

        final int halfOfTheThreads = threads.length / 2;
        for (int i = 0; i < halfOfTheThreads; i++) {
            threads[i].start();
        }
        for (int i = 0; i < halfOfTheThreads; i++) {
            threads[i].join();
        }

        future.get();

        assertEquals("[43, 43, 43, 43, 43]", resultQueue.toString());
        assertEquals(halfOfTheThreads, resultQueue.size());

        for (int i = halfOfTheThreads; i < threads.length; i++) {
            threads[i].start();
        }
        for (int i = halfOfTheThreads; i < threads.length; i++) {
            threads[i].join();
        }

        assertEquals("[43, 43, 43, 43, 43, 43, 43, 43, 43, 43]", resultQueue.toString());
        assertEquals(threads.length, resultQueue.size());
    }
    
    public void testSerializationAndDeserialization() throws Exception {
        final NumberResult result1 = new NumberResult(42);

        final AnalyzerResultFuture<NumberResult> future = new AnalyzerResultFuture<>("foo",
                new ImmutableRef<NumberResult>(result1));
        
        future.addListener(new Listener<NumberResult>() {
            @Override
            public void onSuccess(NumberResult result) {
                // do nothing - this is just a non-serializable listener
            }

            @Override
            public void onError(RuntimeException error) {
                // do nothing - this is just a non-serializable listener
            }
        });
        
        final byte[] bytes = SerializationUtils.serialize(future);
        
        final AnalyzerResultFuture<?> copy = (AnalyzerResultFuture<?>) SerializationUtils.deserialize(bytes);
        
        assertEquals("foo", copy.getName());
        assertEquals("42", copy.get().toString());
    }
}
