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
package org.datacleaner.util.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.user.UserPreferences;

/**
 * Simple HTTP client implementation that does not do anything except delegate
 * to a wrapped {@link HttpClient}. Can be used for non-secured connection or
 * wrapping {@link UserPreferences#createHttpClient()}.
 */
public class SimpleWebServiceHttpClient implements WebServiceHttpClient {

    private final CloseableHttpClient _httpClient;

    public SimpleWebServiceHttpClient() {
        this(HttpClients.createSystem());
    }

    public SimpleWebServiceHttpClient(final CloseableHttpClient httpClient) {
        _httpClient = httpClient;
    }

    @Override
    public HttpResponse execute(final HttpUriRequest request) throws Exception {
        return _httpClient.execute(request);
    }

    @Override
    public void close() {
        FileHelper.safeClose(_httpClient);
    }
}
