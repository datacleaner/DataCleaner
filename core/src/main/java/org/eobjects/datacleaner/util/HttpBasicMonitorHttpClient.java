/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * {@link MonitorHttpClient} for HTTP BASIC based security.
 */
public class HttpBasicMonitorHttpClient implements MonitorHttpClient {

    private final DefaultHttpClient _httpClient;

    public HttpBasicMonitorHttpClient(HttpClient httpClient, String hostname, int port, String username, String password) {
        _httpClient = (DefaultHttpClient) httpClient;

        final CredentialsProvider credentialsProvider = _httpClient.getCredentialsProvider();

        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);

        final List<String> authpref = new ArrayList<String>();
        authpref.add(AuthPolicy.BASIC);
        authpref.add(AuthPolicy.DIGEST);
        _httpClient.getParams().setParameter(AuthPNames.PROXY_AUTH_PREF, authpref);

        credentialsProvider.setCredentials(new AuthScope(hostname, port), credentials);
    }

    @Override
    public HttpResponse execute(HttpUriRequest request) throws Exception {
        return _httpClient.execute(request);
    }

    @Override
    public void close() {
        // do nothing
    }

}
