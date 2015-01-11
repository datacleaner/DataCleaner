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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.metamodel.util.HasName;
import org.apache.metamodel.util.Ref;
import org.apache.metamodel.util.SharedExecutorService;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.AnalyzerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an {@link AnalyzerResult} that is still being produced.
 * 
 * Usually {@link AnalyzerResult}s are produced immediately by the
 * {@link Analyzer#getResult()} method, but in cases where this may take a long
 * time, an {@link Analyzer} can instead return a result of this type and
 * thereby indicate that some process is still going on, but the rest of the job
 * is ready to return.
 *
 * @param <R>
 *            the wrapped {@link AnalyzerResult} type.
 */
public class AnalyzerResultFuture<R extends AnalyzerResult> implements AnalyzerResult, HasName, Ref<R> {

    /**
     * Listener interface for objects that want to be notified when the wrapped
     * {@link AnalyzerResult} is ready.
     *
     * @param <R>
     */
    public static interface Listener<R extends AnalyzerResult> {

        public void onSuccess(R result);

        public void onError(RuntimeException error);
    }

    private static final Logger logger = LoggerFactory.getLogger(AnalyzerResultFuture.class);

    private static final long serialVersionUID = 1L;

    private transient final CountDownLatch _countDownLatch;
    private transient List<Listener<R>> _listeners;

    private final String _name;
    private R _result;
    private RuntimeException _error;

    /**
     * Constructs an {@link AnalyzerResultFuture}
     * 
     * @param name
     *            a name/label to use for presenting and distinguishing this
     *            result from others.
     * @param resultRef
     *            a reference for the result being processed.
     */
    public AnalyzerResultFuture(String name, final Ref<? extends R> resultRef) {
        _name = name;
        _countDownLatch = new CountDownLatch(1);
        _result = null;
        _error = null;

        SharedExecutorService.get().submit(new Runnable() {

            @Override
            public void run() {
                try {
                    _result = resultRef.get();
                    onSuccess();
                } catch (RuntimeException e) {
                    _error = e;
                    onError();
                } finally {
                    _countDownLatch.countDown();
                }
            }
        });
    }

    /**
     * Adds a {@link Listener} to this {@link AnalyzerResultFuture}.
     * 
     * @param listener
     */
    public synchronized void addListener(Listener<R> listener) {
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

    /**
     * Removes a {@link Listener} from this {@link AnalyzerResultFuture}.
     * 
     * @param listener
     */
    public synchronized void removeListener(Listener<R> listener) {
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
            for (final Listener<R> listener : _listeners) {
                try {
                    listener.onSuccess(_result);
                } catch (Exception e) {
                    logger.warn("Unexpected exception while informing listener of success: {}", listener, e);
                }
            }
        } catch (Exception e) {
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
            for (final Listener<R> listener : _listeners) {
                try {
                    listener.onError(_error);
                } catch (Exception e) {
                    logger.warn("Unexpected exception while informing listener on error: {}", listener, e);
                }
            }
        } catch (Exception e) {
            logger.warn("Unexpected exception while iterating listeners on error", e);
        } finally {
            _listeners = null;
        }
    }

    /**
     * Determines if the wrapped {@link AnalyzerResult} is ready or if
     * processing is still going on to produce it.
     * 
     * Once ready, call {@link #get()} to get it.
     * 
     * @return true if the wrapped {@link AnalyzerResult} is ready or false if
     *         it is not.
     */
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
                _countDownLatch.await();
            } catch (InterruptedException e) {
                // do nothing
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
    private void writeObject(ObjectOutputStream out) throws IOException {
        logger.info("Serialization requested, awaiting reference to load.");
        get();
        out.defaultWriteObject();
        logger.info("Serialization finished!");
    }
}
