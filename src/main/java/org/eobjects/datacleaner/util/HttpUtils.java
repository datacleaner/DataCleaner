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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.eobjects.datacleaner.user.UserPreferences;

public final class HttpUtils {

	public static String getUrlContent(String url, Map<String, String> params) throws IOException {
		HttpPost method = new HttpPost(url);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (Entry<String, String> entry : params.entrySet()) {
			nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		method.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String response = getHttpClient().execute(method, responseHandler);
		return response;
	}

	public static HttpClient getHttpClient() {
		DefaultHttpClient httpClient = new DefaultHttpClient();

		final UserPreferences userPreferences = UserPreferences.getInstance();
		if (userPreferences.isProxyEnabled()) {
			// set up HTTP proxy
			final String proxyHostname = userPreferences.getProxyHostname();
			final int proxyPort = userPreferences.getProxyPort();

			final HttpHost proxy = new HttpHost(proxyHostname, proxyPort);
			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

			if (userPreferences.isProxyAuthenticationEnabled()) {
				final AuthScope authScope = new AuthScope(proxyHostname, proxyPort);
				final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
						userPreferences.getProxyUsername(), userPreferences.getProxyPassword());
				httpClient.getCredentialsProvider().setCredentials(authScope, credentials);
			}
		}

		return httpClient;
	}
}
