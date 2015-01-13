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

/**
 * Represents a session of interacting with a web service. The result of service
 * invocations will be {@link ServiceResult}, which allows for explicit error
 * handling, and for implicit aspects like retrying, pooling/limiting of
 * requests etc.
 * 
 * @param <R>
 *            the type of result to expect from service invocations in this
 *            session.
 */
public interface ServiceSession<R> {

    /**
     * Invokes the main service that this session pertains to.
     * 
     * @param callable
     * @return
     */
    public ServiceResult<R> invokeService(Callable<R> callable);

    /**
     * Invokes an adhoc/additional service which yields a different response
     * type than the main service. This method is useful in cases of eg. session
     * handling or cleanup tasks on the server side etc.
     * 
     * When calling adhoc services it is assumed that the response HAS TO BE
     * SUCCESSFUL. Therefore the method returns the result type, not a service
     * result.
     * 
     * @param callable
     * @return
     */
    public <E> E invokeAdhocService(Callable<E> callable) throws RuntimeException, IllegalStateException;
}
