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
package org.datacleaner.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.SerializationUtils;
import org.apache.metamodel.util.ImmutableRef;
import org.apache.metamodel.util.LazyRef;
import org.apache.metamodel.util.Ref;
import org.datacleaner.api.AnalyzerResultFuture.Listener;

import junit.framework.TestCase;

public class AnalyzerResultFutureTest extends TestCase {

    private static class NumberResult implements AnalyzerResult {

        private static final long serialVersionUID = 1L;
        private final Number _number;

        public NumberResult(final int number) {
            _number = number;
        }

        @Override
        public String toString() {
            if (_number == null) {
                return "<null>";
            }
            return _number.toString();
        }
    }

    public void testAddListenerWhenResultIsReady() throws Exception {
        final NumberResult result1 = new NumberResult(42);

        final AnalyzerResultFuture<NumberResult> future =
                new AnalyzerResultFutureImpl<>("foo", new ImmutableRef<>(result1));

        final NumberResult result2 = future.get();

        assertEquals(result1, result2);

        final AtomicBoolean b = new AtomicBoolean(false);

        future.addListener(new Listener<NumberResult>() {
            @Override
            public void onSuccess(final NumberResult result) {
                assertEquals(result1, result);
                b.set(true);
            }

            @Override
            public void onError(final RuntimeException error) {
                fail("This should never happen");
            }
        });

        assertTrue(b.get());

        assertEquals("AnalyzerResultFuture[foo]", future.toString());
    }

    public void testMultiThreadedListenerScenario() throws Exception {
        final int threadCount = 10;

        final Thread[] threads = new Thread[threadCount];
        @SuppressWarnings({ "unchecked" }) final Listener<NumberResult>[] listeners = new Listener[threadCount];
        final ArrayBlockingQueue<Object> resultQueue = new ArrayBlockingQueue<>(threadCount);

        for (int i = 0; i < listeners.length; i++) {
            listeners[i] = new Listener<NumberResult>() {
                @Override
                public void onSuccess(final NumberResult result) {
                    resultQueue.add(result);
                }

                @Override
                public void onError(final RuntimeException error) {
                    resultQueue.add(error);
                }
            };
        }

        final Ref<NumberResult> resultRef = new LazyRef<NumberResult>() {
            @Override
            protected NumberResult fetch() throws Throwable {
                final long randomSleepTime = (long) (1000 * Math.random());
                Thread.sleep(randomSleepTime);
                return new NumberResult(43);
            }
        };

        final AnalyzerResultFuture<NumberResult> future = new AnalyzerResultFutureImpl<>("foo", resultRef);

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

        // to avoid any race conditions we use the drainTo method before calling
        // toString().
        final List<Object> result = new ArrayList<>();
        resultQueue.drainTo(result);

        assertEquals("[43, 43, 43, 43, 43]", result.toString());
        assertEquals(halfOfTheThreads, result.size());

        for (int i = halfOfTheThreads; i < threads.length; i++) {
            threads[i].start();
        }
        for (int i = halfOfTheThreads; i < threads.length; i++) {
            threads[i].join();
        }

        resultQueue.drainTo(result);

        assertEquals("[43, 43, 43, 43, 43, 43, 43, 43, 43, 43]", result.toString());
        assertEquals(threads.length, result.size());
    }

    public void testSerializationAndDeserialization() throws Exception {
        final NumberResult result1 = new NumberResult(42);

        final AnalyzerResultFuture<NumberResult> future =
                new AnalyzerResultFutureImpl<>("foo", new ImmutableRef<>(result1));

        future.addListener(new Listener<NumberResult>() {
            @Override
            public void onSuccess(final NumberResult result) {
                // do nothing - this is just a non-serializable listener
            }

            @Override
            public void onError(final RuntimeException error) {
                // do nothing - this is just a non-serializable listener
            }
        });

        final byte[] bytes = SerializationUtils.serialize(future);

        final AnalyzerResultFuture<?> copy = (AnalyzerResultFuture<?>) SerializationUtils.deserialize(bytes);

        assertEquals("foo", copy.getName());
        assertEquals("42", copy.get().toString());
    }
}
