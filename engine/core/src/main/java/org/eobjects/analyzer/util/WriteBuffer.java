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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.metamodel.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a buffering mechanism that enables writing rows periodically instead
 * of instantly.
 */
public class WriteBuffer {

    private static final Logger logger = LoggerFactory.getLogger(WriteBuffer.class);

    private final BlockingQueue<Object[]> _buffer;
    private final Action<Iterable<Object[]>> _flushAction;
    private final AtomicInteger _batchNumber;

    public WriteBuffer(int bufferSize, Action<Iterable<Object[]>> flushAction) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be a positive integer");
        }
        _batchNumber = new AtomicInteger();
        _buffer = new ArrayBlockingQueue<Object[]>(bufferSize);
        _flushAction = flushAction;
    }

    protected Queue<Object[]> getBuffer() {
        return _buffer;
    }

    public final void addToBuffer(Object[] rowData) {
        while (!_buffer.offer(rowData)) {
            flushBuffer();
        }
    }

    public final void flushBuffer() {
        int flushSize = _buffer.size();
        if (flushSize == 0) {
            return;
        }
        
        logger.info("Flushing {} rows in write buffer", flushSize);

        final List<Object[]> copy = new ArrayList<Object[]>(flushSize);
        _buffer.drainTo(copy, flushSize);

        if (copy.isEmpty()) {
            // this can happen when there's a race going on for flushing the
            // buffer concurrently.
            return;
        }

        try {
            int batchNo = _batchNumber.incrementAndGet();
            logger.info("Write batch no. {} starting", batchNo);
            _flushAction.run(copy);
            logger.info("Write batch no. {} finished", batchNo);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException(e);
        }
    }
}
