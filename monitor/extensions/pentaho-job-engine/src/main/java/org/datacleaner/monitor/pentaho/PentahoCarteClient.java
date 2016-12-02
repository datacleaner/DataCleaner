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
package org.datacleaner.monitor.pentaho;

import java.io.Closeable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.monitor.pentaho.jaxb.PentahoJobType;
import org.datacleaner.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Helper class for HTTP interactions with Carte
 */
public final class PentahoCarteClient implements Closeable {

    private final PentahoJobType _pentahoJobType;
    private final CloseableHttpClient _httpClient;
    private final HttpClientContext _httpClientContext;

    public PentahoCarteClient(final PentahoJobType pentahoJobType) {
        _pentahoJobType = pentahoJobType;
        _httpClientContext = createHttpClientContext(pentahoJobType);
        _httpClient = HttpClients.custom().useSystemProperties().build();
    }

    @Override
    public void close() {
        FileHelper.safeClose(_httpClient);
    }

    private HttpClientContext createHttpClientContext(final PentahoJobType pentahoJobType) {
        final String hostname = pentahoJobType.getCarteHostname();
        final Integer port = pentahoJobType.getCartePort();
        final String username = pentahoJobType.getCarteUsername();
        final String password = pentahoJobType.getCartePassword();

        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(hostname, port), credentials);

        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credentialsProvider);

        return context;
    }

    public List<PentahoTransformation> getAvailableTransformations() throws PentahoJobException {
        final String statusUrl = getUrl("status", null, null);

        final HttpGet request = new HttpGet(statusUrl);
        try {
            final HttpResponse response = execute(request);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                final List<PentahoTransformation> result = new ArrayList<>();
                final Document doc = parse(response.getEntity());
                final Element serverstatusElement = doc.getDocumentElement();
                final Element transstatuslistElement =
                        DomUtils.getChildElementByTagName(serverstatusElement, "transstatuslist");
                final List<Element> transstatusElements = DomUtils.getChildElements(transstatuslistElement);
                for (final Element transstatusElement : transstatusElements) {
                    final String transId = DomUtils.getChildElementValueByTagName(transstatusElement, "id");
                    final String transName = DomUtils.getChildElementValueByTagName(transstatusElement, "transname");
                    final PentahoTransformation transformation = new PentahoTransformation(transId, transName);
                    result.add(transformation);
                }
                return result;
            } else {
                throw new PentahoJobException(
                        "Unexpected response status when updating transformation status: " + statusCode);
            }
        } finally {
            request.releaseConnection();
        }
    }

    public HttpResponse execute(final HttpGet request) {
        try {
            return _httpClient.execute(request, _httpClientContext);
        } catch (final Exception e) {
            throw new PentahoJobException("Failed to invoke HTTP request: " + e.getMessage(), e);
        }
    }

    private DocumentBuilder getDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to instantiate a DocumentBuilder for parsing XML", e);
        }
    }

    public Document parse(final HttpEntity entity) throws PentahoJobException {
        final DocumentBuilder documentBuilder = getDocumentBuilder();
        InputStream content = null;
        try {
            content = entity.getContent();
            return documentBuilder.parse(content);
        } catch (final Exception e) {
            throw new PentahoJobException("Failed to parse XML response", e);
        } finally {
            FileHelper.safeClose(content);
        }
    }

    private String getUrl(final String serviceName, final String id, final String name) throws PentahoJobException {
        final URLCodec urlCodec = new URLCodec();

        final StringBuilder url = new StringBuilder();
        url.append("http://");
        url.append(_pentahoJobType.getCarteHostname());
        url.append(':');
        url.append(_pentahoJobType.getCartePort());
        url.append("/kettle/");
        url.append(serviceName);
        url.append("/?xml=y");
        if (!StringUtils.isNullOrEmpty(id)) {
            url.append("&id=");
            try {
                final String encodedId = urlCodec.encode(id);
                url.append(encodedId);
            } catch (final EncoderException e) {
                throw new PentahoJobException("Failed to encode transformation id: " + id);
            }
        }

        if (!StringUtils.isNullOrEmpty(name)) {
            url.append("&name=");
            try {
                final String encodedName = urlCodec.encode(name);
                url.append(encodedName);
            } catch (final EncoderException e) {
                throw new PentahoJobException("Failed to encode transformation name: " + name);
            }
        }

        return url.toString();
    }

    public String getUrl(final String serviceName) throws PentahoJobException {
        return getUrl(serviceName, _pentahoJobType.getTransformationId(), _pentahoJobType.getTransformationName());
    }
}
