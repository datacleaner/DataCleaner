/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eobjects.datacleaner.user.UserPreferences;

/**
 * Simple HTTP client implementation that does not do anything except delegate
 * to a wrapped {@link HttpClient}. Can be used for non-secured connection or
 * wrapping {@link UserPreferences#createHttpClient()}.
 */
public class SimpleWebServiceHttpClient implements WebServiceHttpClient, MonitorHttpClient {

    private final HttpClient _httpClient;
    
    public SimpleWebServiceHttpClient() {
        this(new DefaultHttpClient());
    }

    public SimpleWebServiceHttpClient(HttpClient httpClient) {
        _httpClient = httpClient;
    }

    @Override
    public HttpResponse execute(HttpUriRequest request) throws Exception {
        return _httpClient.execute(request);
    }

}
