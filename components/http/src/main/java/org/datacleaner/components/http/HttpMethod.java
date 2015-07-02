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
package org.datacleaner.components.http;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;

enum HttpMethod {

    GET, POST, PUT, DELETE, HEAD;

    public HttpUriRequest createRequest(String uri) {
        final HttpMethod m = this;
        switch (m) {
        case GET:
            return new HttpGet(uri);
        case POST:
            return new HttpPost(uri);
        case PUT:
            return new HttpPut(uri);
        case DELETE:
            return new HttpDelete(uri);
        case HEAD:
            return new HttpHead(uri);
        }
        throw new UnsupportedOperationException("Method not supported: " + m);
    }
}
