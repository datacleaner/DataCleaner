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
package org.datacleaner.util;

import java.io.Closeable;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * Defines a HTTP client for DataCleaner monitor connectivity.
 * 
 * A monitor http client is STATEFUL and must be closed after usage. This is
 * because some implementations will contain security tokens etc. which is
 * reused accross requests.
 */
public interface MonitorHttpClient extends WebServiceHttpClient, Closeable {

    @Override
    public HttpResponse execute(HttpUriRequest request) throws Exception;

    @Override
    public void close();
}
