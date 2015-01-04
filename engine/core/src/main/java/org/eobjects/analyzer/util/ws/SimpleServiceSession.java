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

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.ws.WebServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default/simple/base implementation of {@link ServiceSession}.
 * 
 * @param <R>
 */
public class SimpleServiceSession<R> implements ServiceSession<R> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleServiceSession.class);

    private final AtomicInteger _requestCount = new AtomicInteger();
    private final AtomicInteger _activeRequestsCount = new AtomicInteger();

    @Override
    public ServiceResult<R> invokeService(Callable<R> callable) {
        _requestCount.incrementAndGet();
        _activeRequestsCount.incrementAndGet();
        try {
            final R result = callable.call();
            return new ServiceResult<R>(result);
        } catch (Throwable e) {
            if (e instanceof WebServiceException && e.getCause() != null) {
                logger.info("Exception thrown was a WebServiceException. Handling cause exception instead.", e);
                e = e.getCause();
            }
            return new ServiceResult<R>(e);
        } finally {
            _activeRequestsCount.decrementAndGet();
        }
    }

    /**
     * Gets the number of service invocations / requests attempted through
     * {@link #invokeService(Callable)}.
     * 
     * @return
     */
    public int getRequestCount() {
        return _requestCount.get();
    }

    /**
     * Gets the number of active requests currently being processed.
     * 
     * @return
     */
    public int getActiveRequestsCount() {
        return _activeRequestsCount.get();
    }

    @Override
    public <E> E invokeAdhocService(Callable<E> callable) throws RuntimeException, IllegalStateException {
        try {
            final E result = callable.call();
            return result;
        } catch (Throwable e) {
            if (e instanceof WebServiceException && e.getCause() != null) {
                logger.info("Exception thrown was a WebServiceException. Throwing cause exception instead.", e);
                e = e.getCause();
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Failed to invoke adhoc service", e);
        }
    }

}
