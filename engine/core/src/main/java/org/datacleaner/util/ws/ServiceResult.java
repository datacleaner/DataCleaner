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

/**
 * Represents the result of invoking a web service using the
 * {@link OldServiceSession}.
 * 
 * @param <E>
 *            the result type
 */
public class ServiceResult<E> {

    private final E _response;
    private final boolean _succesfull;
    private final Throwable _error;

    public ServiceResult(Throwable error) {
        if (error == null) {
            throw new IllegalArgumentException("Error cannot be null");
        }
        _response = null;
        _succesfull = false;
        _error = error;
    }

    public ServiceResult(E response) {
        if (response == null) {
            throw new IllegalArgumentException("Response cannot be null");
        }
        _response = response;
        _succesfull = true;
        _error = null;
    }

    public boolean isSuccesfull() {
        return _succesfull;
    }

    public Throwable getError() {
        return _error;
    }

    public E getResponse() {
        return _response;
    }
}
