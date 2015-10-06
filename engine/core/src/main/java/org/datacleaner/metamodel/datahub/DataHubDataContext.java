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
import static org.datacleaner.metamodel.datahub.utils.JsonUpdateDataBuilder.buildJsonArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.metamodel.AbstractDataContext;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Schema;
import org.datacleaner.metamodel.datahub.update.UpdateData;
import org.datacleaner.metamodel.datahub.utils.JsonSchemasResponseParser;
import org.datacleaner.util.http.MonitorHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataHubDataContext extends AbstractDataContext implements UpdateableDataContext {
    private static final Logger logger = LoggerFactory.getLogger(DataHubDataContext.class);

    private static final String JSON_CONTENT_TYPE = "application/json";

    private DataHubRepoConnection _repoConnection;
    private DataHubUpdateConnection _updateConnection;
    private Map<String, DataHubSchema> _schemas;

    public DataHubDataContext(DataHubConnection connection) {
        _repoConnection = new DataHubRepoConnection(connection);
        _updateConnection = new DataHubUpdateConnection(connection);
        _schemas = getDatahubSchemas();
    }

    private Map<String, DataHubSchema> getDatahubSchemas() {
        Map<String, DataHubSchema> schemas = new HashMap<String, DataHubSchema>();
        for (final String datastoreName : getDataStoreNames()) {
            final String uri = _repoConnection.getSchemaUrl(datastoreName);
            logger.debug("request {}", uri);
            final HttpGet request = new HttpGet(uri);
            final HttpResponse response = executeRequest(request, _repoConnection.getHttpClient());
            final HttpEntity entity = response.getEntity();
            final JsonSchemasResponseParser parser = new JsonSchemasResponseParser();
            try {
                final DataHubSchema schema = parser.parseJsonSchema(entity.getContent());
                schema.setDatastoreName(datastoreName);
                schemas.put(schema.getName(), schema);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return schemas;
    }

    @Override
    public void executeUpdate(UpdateScript script) {
        try (final DataHubUpdateCallback callback = new DataHubUpdateCallback(this)) {
            script.run(callback);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    @Override
    public DataSet executeQuery(final Query query) {
        return new DataHubDataSet(query, _repoConnection);
    }

    private List<String> getDataStoreNames() {
        String uri = _repoConnection.getDatastoreUrl();
        logger.debug("request {}", uri);
        HttpGet request = new HttpGet(uri);
        HttpResponse response = executeRequest(request, _repoConnection.getHttpClient());
        HttpEntity entity = response.getEntity();
        JsonSchemasResponseParser parser = new JsonSchemasResponseParser();
        try {
            return parser.parseDataStoreArray(entity.getContent());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Schema testGetMainSchema() {
        return getDefaultSchema();
    }

    private HttpResponse executeRequest(HttpUriRequest request, MonitorHttpClient httpClient) {

        HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (Exception e) {
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
    protected Schema getSchemaByNameInternal(String name) {
        return _schemas.get(name);
    }

    public void executeUpdates(List<UpdateData> pendingUpdates) {
        String uri = _updateConnection.getUpdateUrl();
        logger.debug("request {}", uri);
        final HttpPost request = new HttpPost(uri);
        request.addHeader(CONTENT_TYPE, JSON_CONTENT_TYPE);
        request.addHeader(ACCEPT, JSON_CONTENT_TYPE);

        request.setEntity(new StringEntity(buildJsonArray(pendingUpdates), ContentType.APPLICATION_JSON));
        executeRequest(request, _updateConnection.getHttpClient());
    }
    
    public void executeGoldenRecordDelete(String goldenRecordId) {        
        String uri = _updateConnection.getDeleteGoldenRecordUrl();
        uri = uri + "/" + goldenRecordId;
        logger.debug("request {}", uri);
        final HttpDelete request = new HttpDelete(uri);

        executeRequest(request, _updateConnection.getHttpClient());
    }

    //"/sources/delete/{source}/{id}/{recordType}"
    public void executeDeleteSourceRecord(String source, String id, String recordType) {
        String uri = _updateConnection.getDeleteSourceRecordUrl();
        uri = uri + "/" + source + "/" + id + "/" + recordType;
        logger.debug("request {}", uri);
        final HttpDelete request = new HttpDelete(uri);
        executeRequest(request, _updateConnection.getHttpClient());
        
    }
}
