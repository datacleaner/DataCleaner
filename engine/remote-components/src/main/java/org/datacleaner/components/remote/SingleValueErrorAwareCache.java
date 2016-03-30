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
package org.datacleaner.components.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache for single key value. If a value retrieval throws an exception, this exception is cached instead of
 * the value and will be thrown each time the value is requested.
 * The cached value never expires, but the exception expires after 1 second.
 *<p>
 * This class is NOT thread-safe.
 */
public abstract class SingleValueErrorAwareCache<K,V> {

    private static final Logger logger = LoggerFactory.getLogger(SingleValueErrorAwareCache.class);
    private static final long EXCEPTION_EXPIRATION_TIME = 1000;

    private Exception cachedException;
    private long cachedExceptionTimestamp;
    private V cachedValue;
    private K cachedValueKey;

    /** The actual value retrieval procedure */
    abstract protected V fetch(K key) throws Exception;

    /** Returns the cached value/exception. If nothing cached, calls {@link #fetch}, caches the value/exception and returns/throws it. */
    public V getCachedValue(K key) throws Exception {
        if (cachedValueKey != null) {
            if (key.equals(cachedValueKey)) {
                if (cachedValue != null) {
                    logger.debug("Reusing cached output columns, nothing changed");
                    return cachedValue;
                } else if (cachedException != null && System.currentTimeMillis() < (cachedExceptionTimestamp + EXCEPTION_EXPIRATION_TIME)) {
                    logger.debug("Retrowing last exception, nothing changed in last seconds");
                    throw cachedException;
                }
            }
        }
        cachedValueKey = key;
        cachedException = null;
        cachedValue = null;
        try {
            return cachedValue = fetch(key);
        } catch(Exception e) {
            cachedException = e;
            cachedExceptionTimestamp = System.currentTimeMillis();
            throw e;
        }
    }

}
