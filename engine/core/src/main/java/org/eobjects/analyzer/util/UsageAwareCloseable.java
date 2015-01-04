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
package org.eobjects.analyzer.util;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility {@link Closeable} implementation of {@link Closeable} which is
 * aware of usage/consumption by more than one consumer. Therefore the abstract
 * {@link #closeInternal()} method will only be invoked once, when all usage has
 * stopped.
 */
public abstract class UsageAwareCloseable implements Closeable {

    private static class CloseEvent {
        final String threadName;
        final StackTraceElement[] stackTrace;

        CloseEvent(String name, StackTraceElement[] trace) {
            threadName = name;
            stackTrace = trace;
        }
    }

    private static final int STACK_TRACE_ELEMENTS_TO_LOG = 7;

    private final Logger _logger;

    private final AtomicInteger _usageCount;
    private final AtomicBoolean _closed;
    private final List<CloseEvent> _closeEvents;

    public UsageAwareCloseable() {
        this(LoggerFactory.getLogger(UsageAwareCloseable.class));
    }

    public UsageAwareCloseable(Logger logger) {
        _logger = logger;
        _usageCount = new AtomicInteger(1);
        _closed = new AtomicBoolean(false);

        if (logger.isDebugEnabled()) {
            _closeEvents = new ArrayList<CloseEvent>(2);
            logger.debug("{} instantiated by:", this.getClass().getSimpleName());
            logNearestStack();
        } else {
            _closeEvents = null;
        }
    }

    private void logNearestStack() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        for (int i = 1; i < stackTrace.length && i < 7; i++) {
            StackTraceElement ste = stackTrace[i];
            _logger.debug(" - {} @ line {}", ste.getClassName(), ste.getLineNumber());
        }
    }

    private void logStack(StackTraceElement[] stackTrace) {
        for (int i = 1; i < stackTrace.length && i < STACK_TRACE_ELEMENTS_TO_LOG; i++) {
            StackTraceElement ste = stackTrace[i];
            _logger.debug(" - {} @ line {}", ste.getClassName(), ste.getLineNumber());
        }
    }

    /**
     * Subclasses should implement this method to do the actual closing logic
     */
    protected abstract void closeInternal();

    public final boolean requestUsage() {
        if (isClosed()) {
            return false;
        }

        final int usage;
        synchronized (this) {
            usage = _usageCount.incrementAndGet();
            if (usage == 1) {
                _usageCount.decrementAndGet();
                _logger.debug("{} is closed, request for more usage refused", this.getClass().getSimpleName());
                return false;
            }
        }

        _logger.debug("Usage incremented to {} for {}", usage, this);

        if (_logger.isDebugEnabled()) {
            _logger.debug("Incremented usage by:");
            logNearestStack();
        }
        return true;
    }

    public final boolean isClosed() {
        return _closed.get();
    }

    @Override
    public final void close() {
        if (isClosed()) {
            _logger.warn(this + " is already closed, but close() was invoked!");
            if (_logger.isDebugEnabled()) {
                _closeEvents.add(new CloseEvent(Thread.currentThread().getName(), new Throwable().getStackTrace()));
                final int numCloses = _closeEvents.size();
                int i = 1;
                for (CloseEvent closeEvent : _closeEvents) {
                    _logger.debug("Stack trace when close() no. {} of {}: ", i, numCloses);
                    _logger.debug("Thread name: {}", closeEvent.threadName);
                    logStack(closeEvent.stackTrace);
                    i++;
                }
            }
            return;
        }

        final int usage;

        synchronized (this) {
            usage = _usageCount.decrementAndGet();

            _logger.debug("Method close() invoked, usage decremented to {} for {}", _usageCount, this);
            if (usage == 0) {
                if (_logger.isDebugEnabled()) {
                    _closeEvents.add(new CloseEvent(Thread.currentThread().getName(), new Throwable().getStackTrace()));
                }
                _logger.debug("Closing {}", this);
                closeInternal();
                _closed.set(true);
            }
        }

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (!isClosed()) {
            if (_logger.isWarnEnabled()) {
                _logger.warn("Method finalize() invoked but not all usages closed ({} remaining) (for {}). Closing.",
                        _usageCount, this);
            }
            // in case of gc, also do the closing
            closeInternal();
        }
    }

    /**
     * Gets the amount of usages this datacontext provider currently has.
     * 
     * @return
     */
    protected int getUsageCount() {
        return _usageCount.get();
    }
}
