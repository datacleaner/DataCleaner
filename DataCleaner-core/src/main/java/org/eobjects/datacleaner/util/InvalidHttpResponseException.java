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

/**
 * Represents an exception occurring because a HTTP response was invalid
 * (typically based on status code).
 * 
 * @author Kasper Sørensen
 */
public class InvalidHttpResponseException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final HttpResponse _response;
	private final String _url;

	public InvalidHttpResponseException(String url, HttpResponse response) {
		_response = response;
		_url = url;
	}

	@Override
	public String getMessage() {
		return "Invalid HTTP response status code: " + getStatusCode() + " (" + _url + ")";
	}

	public String getUrl() {
		return _url;
	}

	public HttpResponse getResponse() {
		return _response;
	}

	public int getStatusCode() {
		return _response.getStatusLine().getStatusCode();
	}
}
