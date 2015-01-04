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
package org.datacleaner.util.ws;

import java.util.concurrent.Callable;

import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * A {@link ServiceSession} that has an upper limit to the number of concurrent
 * connections allowed.
 * 
 * @param <R>
 */
public class PooledServiceSession<R> extends RetryServiceSession<R> {

    private final GenericObjectPool<Integer> _connectionPool;

    /**
     * Constructs a {@link PooledServiceSession} with a specific pool.
     * 
     * @see #createPool(int).
     * 
     * @param pool
     */
    public PooledServiceSession(GenericObjectPool<Integer> pool) {
        this(pool, 0, null);
    }

    /**
     * Constructs a {@link PooledServiceSession} with a specific pool, a number
     * of retries and a sleep/backoff time array
     * 
     * @see #createPool(int).
     * 
     * @param pool
     * @param maxRetries
     * @param sleepTimeBetweenRetries
     */
    public PooledServiceSession(GenericObjectPool<Integer> pool, int maxRetries, int[] sleepTimeBetweenRetries) {
        super(maxRetries, sleepTimeBetweenRetries);
        _connectionPool = pool;
    }

    /**
     * Constructs a {@link PooledServiceSession} with a pool size and no
     * retries.
     * 
     * @param maxConnections
     */
    public PooledServiceSession(int maxConnections) {
        this(maxConnections, 0, null);
    }

    /**
     * Constructs a {@link PooledServiceSession} with a pool size, a number of
     * retries and a sleep/backoff time array
     * 
     * @param maxConnections
     * @param maxRetries
     * @param sleepTimeBetweenRetries
     */
    public PooledServiceSession(int maxConnections, int maxRetries, int[] sleepTimeBetweenRetries) {
        super(maxRetries, sleepTimeBetweenRetries);
        _connectionPool = createConnectionPool(maxConnections);
    }

    /**
     * Creates a connection pool that can be used for one or more
     * {@link PooledServiceSession} objects.
     * 
     * @param maxConnections
     * @return
     */
    public static GenericObjectPool<Integer> createConnectionPool(int maxConnections) {
        GenericObjectPool<Integer> connectionPool = new GenericObjectPool<Integer>(new ConnectionPoolObjectFactory());
        connectionPool.setMaxActive(maxConnections);
        connectionPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
        return connectionPool;
    }

    @Override
    public ServiceResult<R> invokeService(Callable<R> callable) {
        final Integer poolObject = borrowObject();
        try {
            return super.invokeService(callable);
        } finally {
            returnObject(poolObject);
        }
    }

    @Override
    public <E> E invokeAdhocService(Callable<E> callable) throws RuntimeException, IllegalStateException {
        final Integer poolObject = borrowObject();
        try {
            return super.invokeAdhocService(callable);
        } finally {
            returnObject(poolObject);
        }
    }

    private Integer borrowObject() {
        try {
            return _connectionPool.borrowObject();
        } catch (Exception e) {
            throw new IllegalStateException("Could not borrow pool object", e);
        }
    }

    private void returnObject(Integer poolObject) {
        try {
            _connectionPool.returnObject(poolObject);
        } catch (Exception e) {
            throw new IllegalStateException("Could not return pool object", e);
        }
    }

}
