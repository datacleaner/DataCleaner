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
package org.datacleaner.metamodel.datahub;

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.datacleaner.metamodel.datahub.DataHubConnection.DEFAULT_SCHEMA;
import static org.datacleaner.metamodel.datahub.DataHubConnectionHelper.validateReponseStatusCode;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.metamodel.AbstractDataContext;
import org.apache.metamodel.DefaultUpdateSummary;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateSummary;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.metamodel.datahub.update.SourceRecordIdentifier;
import org.datacleaner.metamodel.datahub.update.UpdateData;
import org.datacleaner.metamodel.datahub.utils.JsonSchemasResponseParser;
import org.datacleaner.metamodel.datahub.utils.JsonUpdateDataBuilder;
import org.datacleaner.util.http.MonitorHttpClient;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DataHubDataContext extends AbstractDataContext implements UpdateableDataContext {

    private static class UserInfo {
        @SuppressWarnings("unused")
        public String username;
        public String tenant;
    }

    private static final String JSON_CONTENT_TYPE = ContentType.APPLICATION_JSON.getMimeType();
    private final String _tenantName;
    private DataHubRepoConnection _repoConnection;
    private DataHubUpdateConnection _updateConnection;
    private Map<String, DataHubSchema> _schemas;

    public DataHubDataContext(final DataHubConnection connection) {
        _repoConnection = new DataHubRepoConnection(connection);
        _updateConnection = new DataHubUpdateConnection(connection);
        _tenantName = retrieveTenantName();
        _schemas = getDatahubSchemas();
    }

    private Map<String, DataHubSchema> getDatahubSchemas() {
        final Map<String, DataHubSchema> schemas = new HashMap<>();
        for (final String datastoreName : getDataStoreNames()) {
            final String uri = _repoConnection.getSchemaUrl(_tenantName, datastoreName);
            final HttpGet request = new HttpGet(uri);
            final HttpResponse response = executeRequest(request, _repoConnection.getHttpClient());
            final HttpEntity entity = response.getEntity();
            final JsonSchemasResponseParser parser = new JsonSchemasResponseParser();
            try {
                final DataHubSchema schema = parser.parseJsonSchema(entity.getContent());
                schema.setDatastoreName(datastoreName);
                schemas.put(schema.getName(), schema);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return schemas;
    }

    @Override
    public UpdateSummary executeUpdate(final UpdateScript script) {
        try (DataHubUpdateCallback callback = new DataHubUpdateCallback(this)) {
            script.run(callback);
        } catch (final RuntimeException e) {
            throw e;
        }
        return DefaultUpdateSummary.unknownUpdates();
    }

    @Override
    public DataSet executeQuery(final Query query) {
        return new DataHubDataSet(_tenantName, query, _repoConnection);
    }

    private List<String> getDataStoreNames() {
        final String uri = _repoConnection.getDatastoreUrl(_tenantName);
        final HttpGet request = new HttpGet(uri);
        final HttpResponse response = executeRequest(request, _repoConnection.getHttpClient());
        final HttpEntity entity = response.getEntity();
        final JsonSchemasResponseParser parser = new JsonSchemasResponseParser();
        try {
            return parser.parseDataStoreArray(entity.getContent());
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Schema testGetMainSchema() {
        return getDefaultSchema();
    }

    private HttpResponse executeRequest(final HttpUriRequest request, final MonitorHttpClient httpClient) {

        final HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

        validateReponseStatusCode(response);

        return response;
    }

    @Override
    protected String[] getSchemaNamesInternal() {
        return _schemas.keySet().toArray(new String[_schemas.size()]);
    }

    @Override
    protected String getDefaultSchemaName() {
        return DEFAULT_SCHEMA;
    }

    @Override
    protected Schema getSchemaByNameInternal(final String name) {
        return _schemas.get(name);
    }

    public void executeUpdates(final List<UpdateData> pendingUpdates) {
        final String uri = _updateConnection.getGoldenRecordBatchUrl();
        final HttpPut request = new HttpPut(uri);
        request.addHeader(CONTENT_TYPE, JSON_CONTENT_TYPE);
        request.addHeader(ACCEPT, JSON_CONTENT_TYPE);
        request.setEntity(
                new StringEntity(JsonUpdateDataBuilder.buildJsonArray(pendingUpdates), ContentType.APPLICATION_JSON));
        executeRequest(request, _updateConnection.getHttpClient());
    }

    /**
     * Invokes DataHub REST service to delete a batch of golden records.
     *
     * @param pendingGoldenRecordDeletes The golden records to delete.
     */
    public void executeGoldenRecordDelete(final List<String> pendingGoldenRecordDeletes) {
        final String uri = _updateConnection.getGoldenRecordBatchUrl();
        final HttpPost request = new HttpPost(uri);
        request.addHeader(CONTENT_TYPE, JSON_CONTENT_TYPE);
        request.addHeader(ACCEPT, JSON_CONTENT_TYPE);
        request.setEntity(new StringEntity(JsonUpdateDataBuilder.buildJsonArray(pendingGoldenRecordDeletes),
                ContentType.APPLICATION_JSON));
        executeRequest(request, _updateConnection.getHttpClient());
    }

    /**
     * Invokes DataHub REST service to delete a batch of source records.
     *
     * @param pendingSourceDeletes The batch of sources to delete.
     */
    public void executeSourceDelete(final List<SourceRecordIdentifier> pendingSourceDeletes) {
        final String uri = _updateConnection.getSourceRecordBatchUrl();
        final HttpPost request = new HttpPost(uri);
        request.addHeader(CONTENT_TYPE, JSON_CONTENT_TYPE);
        request.addHeader(ACCEPT, JSON_CONTENT_TYPE);
        request.setEntity(new StringEntity(JsonUpdateDataBuilder.buildJsonArray(pendingSourceDeletes),
                ContentType.APPLICATION_JSON));
        executeRequest(request, _updateConnection.getHttpClient());
    }

    private String retrieveTenantName() {
        final String getUserInfoUrl = _repoConnection.getUserInfoUrl();
        final HttpGet request = new HttpGet(getUserInfoUrl);
        try (MonitorHttpClient monitorHttpClient = _repoConnection.getHttpClient()) {
            final HttpResponse response = monitorHttpClient.execute(request);

            final StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK || statusLine.getStatusCode() == HttpStatus.SC_CREATED) {
                // read response as JSON.
                final InputStream content = response.getEntity().getContent();
                final UserInfo userInfo;
                try {
                    userInfo = new ObjectMapper().readValue(content, UserInfo.class);
                    return userInfo.tenant;
                } finally {
                    FileHelper.safeClose(content);
                }
            } else {
                final String reasonPhrase = statusLine.getReasonPhrase();
                throw new RuntimeException("Failed to retrieve the tenant name: " + reasonPhrase);
            }
        } catch (final Exception exception) {
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            }
            throw new RuntimeException("Failed to retrieve the tenant name: " + exception.getMessage());
        }
    }

}
