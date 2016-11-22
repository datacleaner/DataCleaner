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
package org.datacleaner.monitor.util;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestException;

/**
 * Convenient wrapper around the somewhat cumbersome errorhandling in
 * {@link RequestBuilder}.
 */
public class DCRequestBuilder {

    private final RequestBuilder _requestBuilder;

    public DCRequestBuilder(final Method method, final String url) {
        _requestBuilder = new RequestBuilder(method, url);
    }

    public void send(final String requestData, final DCRequestCallback callback) {
        try {
            _requestBuilder.sendRequest(requestData, callback);
        } catch (final RequestException e) {
            ErrorHandler.showErrorDialog("Error sending request", e.getMessage(), e);
        }
    }

    public void setHeader(final String header, final String value) {
        _requestBuilder.setHeader(header, value);
    }
}
