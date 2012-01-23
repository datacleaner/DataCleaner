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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.eobjects.datacleaner.user.UserPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility class for working with HTTP and XML.
 * 
 * @author Kasper Sørensen
 */
public final class HttpXmlUtils {

	private static final Logger logger = LoggerFactory.getLogger(HttpXmlUtils.class);

	private final UserPreferences _userPreferences;

	public HttpXmlUtils(UserPreferences userPreferences) {
		_userPreferences = userPreferences;
	}

	public String getUrlContent(String url, Map<String, String> params) throws IOException {
		if (params == null) {
			params = Collections.emptyMap();
		}
		logger.info("getUrlContent({},{})", url, params);
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

	public HttpClient getHttpClient() {
		final DefaultHttpClient httpClient = new DefaultHttpClient();

		if (_userPreferences.isProxyEnabled()) {
			// set up HTTP proxy
			final String proxyHostname = _userPreferences.getProxyHostname();
			final int proxyPort = _userPreferences.getProxyPort();

			try {
				final HttpHost proxy = new HttpHost(proxyHostname, proxyPort);
				httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

				if (_userPreferences.isProxyAuthenticationEnabled()) {
					final AuthScope authScope = new AuthScope(proxyHostname, proxyPort);
					final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
							_userPreferences.getProxyUsername(), _userPreferences.getProxyPassword());
					httpClient.getCredentialsProvider().setCredentials(authScope, credentials);
				}
			} catch (Exception e) {
				// ignore proxy creation and return http client without it
				logger.error("Unexpected error occurred while initializing HTTP proxy", e);
			}
		}

		return httpClient;
	}

	public static Element getRootNode(HttpClient httpClient, String url) throws InvalidHttpResponseException {
		logger.info("getRootNode({})", url);
		try {
			HttpGet method = new HttpGet(url);
			HttpResponse response = httpClient.execute(method);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				logger.error("Response status code was: {} (url={})", statusCode, url);
				throw new InvalidHttpResponseException(url, response);
			}
			InputStream inputStream = response.getEntity().getContent();
			Document document = createDocumentBuilder().parse(inputStream);
			return (Element) document.getFirstChild();
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new IllegalStateException("Could not get root XML node of url=" + url, e);
		}
	}

	public static DocumentBuilder createDocumentBuilder() {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringComments(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db;
		} catch (Exception e) {
			// This shouldn't be possible
			throw new RuntimeException(e);
		}
	}

	public static List<Node> getChildNodesByName(Node parentNode, String childNodeName) {
		List<Node> result = new ArrayList<Node>();
		if (childNodeName != null) {
			NodeList childNodes = parentNode.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node childNode = childNodes.item(i);
				if (childNodeName.equals(childNode.getNodeName())) {
					result.add(childNode);
				}
			}
		}
		return result;
	}

	public static String getChildNodeText(Node node, String childNodeName) {
		List<Node> childNodes = getChildNodesByName(node, childNodeName);
		if (childNodes.isEmpty()) {
			return null;
		}
		if (childNodes.size() > 1) {
			throw new IllegalArgumentException("The node " + node + " contains several childNodes named " + childNodeName);
		}
		return getText(childNodes.get(0));
	}

	public static String getText(Node node) {
		Element element = (Element) node;
		return element.getTextContent();
	}
}
