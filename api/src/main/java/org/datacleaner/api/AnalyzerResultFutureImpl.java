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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.metamodel.util.LazyRef;
import org.apache.metamodel.util.Ref;
import org.apache.metamodel.util.SharedExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of AnalyzerResultFuture which uses a Ref to get the
 * actual result.
 *
 * @param <R>
 *            the wrapped {@link AnalyzerResult} type.
 */
public class AnalyzerResultFutureImpl<R extends AnalyzerResult> implements AnalyzerResultFuture<R> {

    private static final Logger logger = LoggerFactory.getLogger(AnalyzerResultFutureImpl.class);

    private static final long serialVersionUID = 1L;

    private final transient CountDownLatch _countDownLatch;
    private final String _name;
    private transient volatile List<Listener<? super R>> _listeners;
    private volatile R _result;
    private volatile RuntimeException _error;

    /**
     * Constructs an {@link AnalyzerResultFutureImpl}
     *
     * @param name
     *            a name/label to use for presenting and distinguishing this
     *            result from others.
     * @param resultRef
     *            a reference for the result being processed.
     */
    public AnalyzerResultFutureImpl(final String name, final Ref<? extends R> resultRef) {
        _name = name;
        _countDownLatch = new CountDownLatch(1);
        _result = null;
        _error = null;

        SharedExecutorService.get().submit(() -> {
            try {
                _result = resultRef.get();
                if (_result == null && resultRef instanceof LazyRef) {
                    // TODO: workaround - reported as MM bug, remove when
                    // fixed.
                    throw new RuntimeException(((LazyRef<?>) resultRef).getError());
                }
                onSuccess();
            } catch (final RuntimeException e) {
                _error = e;
                onError();
            } finally {
                _countDownLatch.countDown();
            }
        });
    }

    @Override
    public synchronized void addListener(final Listener<? super R> listener) {
        // it might be we add a listener AFTER the result is actually produced,
        // in which case we simply inform the listener immediately.
        if (isReady()) {
            if (_error != null) {
                listener.onError(_error);
            } else {
                listener.onSuccess(_result);
            }
            return;
        }

        if (_listeners == null) {
            _listeners = new LinkedList<>();
        }
        _listeners.add(listener);
    }

    @Override
    public synchronized void removeListener(final Listener<R> listener) {
        if (_listeners == null) {
            return;
        }
        _listeners.remove(listener);
    }

    private synchronized void onSuccess() {
        if (_listeners == null) {
            return;
        }
        try {
            for (final Listener<? super R> listener : _listeners) {
                try {
                    listener.onSuccess(_result);
                } catch (final Exception e) {
                    logger.warn("Unexpected exception while informing listener of success: {}", listener, e);
                }
            }
        } catch (final Exception e) {
            logger.warn("Unexpected exception while iterating listeners on success", e);
        } finally {
            _listeners = null;
        }
    }

    private synchronized void onError() {
        if (_listeners == null) {
            return;
        }
        try {
            for (final Listener<? super R> listener : _listeners) {
                try {
                    listener.onError(_error);
                } catch (final Exception e) {
                    logger.warn("Unexpected exception while informing listener on error: {}", listener, e);
                }
            }
        } catch (final Exception e) {
            logger.warn("Unexpected exception while iterating listeners on error", e);
        } finally {
            _listeners = null;
        }
    }

    @Override
    public boolean isReady() {
        if (_countDownLatch == null) {
            return true;
        }

        return _countDownLatch.getCount() == 0;
    }

    @Override
    public R get() {
        if (_countDownLatch != null) {
            try {
                boolean finished = false;
                int iteration = 0;
                final int minutesToWaitPerIteration = 2;
                while (!finished) {
                    iteration++;
                    finished = _countDownLatch.await(minutesToWaitPerIteration, TimeUnit.MINUTES);
                    if (!finished) {
                        logger.info(
                                "Awaited completion for " + (iteration * minutesToWaitPerIteration) + " minutes...");
                    }
                }
            } catch (final InterruptedException e) {
                throw new IllegalStateException("Awaiting completion of AnalyzerResultFuture was interrupted!", e);
            }
        }
        if (_error != null) {
            throw _error;
        }
        return _result;
    }

    @Override
    public String toString() {
        return "AnalyzerResultFuture[" + _name + "]";
    }

    @Override
    public String getName() {
        return _name;
    }

    /**
     * Method invoked when serialization takes place. Makes sure that we await
     * the loading of the result reference before writing any data.
     *
     * @param out
     * @throws IOException
     */
    private void writeObject(final ObjectOutputStream out) throws IOException {
        logger.info("Serialization requested, awaiting reference to load.");
        get();
        out.defaultWriteObject();
        logger.info("Serialization finished!");
    }
}
