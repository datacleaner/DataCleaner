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
package org.eobjects.analyzer.util.ws;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool.PoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Very simple implementation of {@link PoolableObjectFactory}. Since the
 * connection pool in {@link OldServiceSession} does not really need to use the
 * objects for anything, except restricting the amount of active connections, we
 * simply create unique integers as pool objects.
 */
final class ConnectionPoolObjectFactory implements PoolableObjectFactory<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionPoolObjectFactory.class);

    private final AtomicInteger _counter;

    public ConnectionPoolObjectFactory() {
        _counter = new AtomicInteger(0);
    }

    @Override
    public Integer makeObject() throws Exception {
        Integer obj = _counter.incrementAndGet();
        logger.debug("makeObject: {}", obj);
        return obj;
    }

    @Override
    public void destroyObject(Integer obj) throws Exception {
        logger.debug("destroyObject: {}", obj);
    }

    @Override
    public boolean validateObject(Integer obj) {
        logger.debug("validateObject: {}", obj);
        return true;
    }

    @Override
    public void activateObject(Integer obj) throws Exception {
        logger.debug("activateObject: {}", obj);
    }

    @Override
    public void passivateObject(Integer obj) throws Exception {
        logger.debug("passivateObject: {}", obj);
    }
}
