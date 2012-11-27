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

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

public class MonitorHttpClientCASImpl implements MonitorHttpClient {

    private final HttpClient _httpClient;
    private final String _casUrl;
    private final String _username;
    private final String _password;

    public MonitorHttpClientCASImpl(HttpClient httpClient, String casUrl, String username, String password) {
        _httpClient = httpClient;
        _casUrl = casUrl;
        _username = username;
        _password = password;
    }

    @Override
    public HttpResponse execute(HttpRequest request) throws Exception {
        HttpPost httpurl = new HttpPost(_casUrl);
        httpurl.setEntity(new StringEntity("username=" + _username + "&password=" + _password));
        HttpResponse casResponse = _httpClient.execute(httpurl);
        Header firstHeader = casResponse.getFirstHeader("Location");
        int statusCode = casResponse.getStatusLine().getStatusCode();
        switch (statusCode) {
        case 201: {
            if (firstHeader != null) {
                httpurl.releaseConnection();
                String locationResponse = firstHeader.getValue();
                String ticket = locationResponse.substring(locationResponse.indexOf("TGT"));
                if (ticket != null) {
                    // user is authenticated
                    if (request instanceof HttpGet) {
                        return _httpClient.execute((HttpGet) request);
                    }
                    return _httpClient.execute((HttpPost) request);
                }
            }
            }
        }
        return casResponse;
    }
}
