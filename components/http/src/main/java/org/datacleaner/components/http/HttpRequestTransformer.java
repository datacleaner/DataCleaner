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

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Close;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.MappedProperty;
import org.datacleaner.api.NumberProperty;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.StringProperty;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.ImproveSuperCategory;
import org.datacleaner.components.categories.ReferenceDataCategory;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.ws.PooledServiceSession;
import org.datacleaner.util.ws.ServiceResult;

import com.google.common.base.Strings;

@Named("HTTP request")
@Categorized(value = ReferenceDataCategory.class, superCategory = ImproveSuperCategory.class)
@Description("Sends a HTTP request for each record and retrieves the response as transformation output.\n"
        + "For each request you can have dynamic elements in the URL or in the request body that is sent. Provide variable names that are unique to the URL and request body and reference them there. For instance:\n"
        + "<table><tr><td>URL:</td><td>http://www.google.com/?q=${term}</td></tr>"
        + "<tr><td>Input:</td><td>column1</td></tr>"
        + "<tr><td>Variable:</td><td>${term}</td></tr></table>")
public class HttpRequestTransformer implements Transformer {

    public static final String PROPERTY_INPUT_COLUMNS = "Input";
    public static final String PROPERTY_VARIABLE_NAMES = "Variable names";
    private static final String PROPERTY_URL = "URL";

    @Inject
    @Configured(value = PROPERTY_URL, order = 1)
    @Description("The URL to invoke. The URL will be pre-processed by replacing any variable names in it with the corresponding dynamic values.")
    String url = "http://";

    @Inject
    @Configured(order = 2)
    HttpMethod method = HttpMethod.POST;

    @Inject
    @Configured(value = PROPERTY_INPUT_COLUMNS, order = 3)
    InputColumn<?>[] input;

    @Inject
    @Configured(value = PROPERTY_VARIABLE_NAMES, order = 4)
    @MappedProperty(PROPERTY_INPUT_COLUMNS)
    String[] variableNames;

    @Inject
    @Configured(order = 5)
    @StringProperty(multiline = true, emptyString = true)
    @Description("The body of the request to invoke. The request body will be pre-processed by replacing any variable names in it with the corresponding dynamic values.")
    String requestBody = "";

    @Inject
    @Configured(required = false, order = 100)
    Map<String, String> headers;

    @Inject
    @Configured(required = false, order = 101)
    String charset = HTTP.DEF_CONTENT_CHARSET.name();

    @Inject
    @Configured(required = false, order = 150)
    @NumberProperty(negative = false, zero = false, positive = true)
    @Description("The maximum number of requests that may be fired at the same time.\n"
            + "Higher values may provide better throughput while it may also add load to the HTTP server.")
    int maxConcurrentRequests = 20;

    private CloseableHttpClient _httpClient;
    private PooledServiceSession<Object[]> _session;

    @Initialize
    public void init() {
        _httpClient = HttpClients.createSystem();
        _session = new PooledServiceSession<>(maxConcurrentRequests);
    }

    @Close
    public void close() {
        FileHelper.safeClose(_httpClient, _session);
    }

    @Override
    public OutputColumns getOutputColumns() {
        final String[] columnNames = { "Response status code", "Response body" };
        final Class<?>[] columnTypes = { Integer.class, String.class };
        return new OutputColumns(columnNames, columnTypes);
    }

    @Override
    public Object[] transform(InputRow inputRow) {
        final Charset usedCharset = Charset.forName(charset);

        final String requestBody = applyVariablesToString(this.requestBody, inputRow);
        final String url = applyVariablesToString(this.url, inputRow);

        final HttpUriRequest request = method.createRequest(url);
        if (!Strings.isNullOrEmpty(requestBody) && request instanceof HttpEntityEnclosingRequest) {
            final HttpEntity entity = new StringEntity(requestBody, usedCharset);
            ((HttpEntityEnclosingRequest) request).setEntity(entity);
        }

        if (headers != null) {
            final Set<Entry<String, String>> entries = headers.entrySet();
            for (Entry<String, String> entry : entries) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }

        final ServiceResult<Object[]> result = _session.invokeService(new Callable<Object[]>() {
            @Override
            public Object[] call() throws Exception {
                final HttpResponse response = _httpClient.execute(request);
                final int statusCode = response.getStatusLine().getStatusCode();
                final String body = EntityUtils.toString(response.getEntity(), usedCharset);
                return new Object[] { statusCode, body };
            }
        });

        if (!result.isSuccesfull()) {
            final Throwable error = result.getError();
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            }
            throw new RuntimeException(error);
        }

        return result.getResponse();
    }

    /**
     * Creates a string with all variable names replaced with dynamic values
     * coming from the {@link InputRow}'s values.
     * 
     * @param str
     *            the string to prepare with variables
     * @param inputRow
     *            the input row containing the dynamic values to insert into the
     *            string
     * @return
     */
    protected String applyVariablesToString(String str, InputRow inputRow) {
        if (Strings.isNullOrEmpty(str)) {
            return null;
        }
        String result = str;
        final List<Object> values = inputRow.getValues(input);
        for (int i = 0; i < input.length; i++) {
            final Object value = values.get(i);
            final String valueStr;
            if (value == null) {
                valueStr = "";
            } else {
                valueStr = value.toString();
            }
            result = StringUtils.replaceAll(result, variableNames[i], valueStr);
        }
        return result;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setMaxConcurrentRequests(int maxConcurrentRequests) {
        this.maxConcurrentRequests = maxConcurrentRequests;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
        _httpClient = httpClient;
    }

    public void setInputAndVariables(InputColumn<?>[] input, String[] variableNames) {
        this.input = input;
        this.variableNames = variableNames;
    }
}
