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
package org.datacleaner.components.remote;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import jdk.nashorn.internal.runtime.regexp.joni.ast.StringNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.datacleaner.api.*;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.restclient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * @Since 9/1/15
 */
public class RemoteTransformer implements Transformer {

    private static final Logger logger = LoggerFactory.getLogger(RemoteTransformer.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        SimpleModule myModule = new SimpleModule("RemoteTransformersModule", new Version(1, 0, 0, null, "org.datacleaner", "DataCleaner-remote-transformers"));
        myModule.addSerializer(new MyInputColumnsSerializer()); // assuming serializer declares correct class to bind to
        mapper.registerModule(myModule);
    }

    private String componentUrl;
    private String baseUrl;
    private String componentDisplayName;
    private String username, password, tenant;

    private ComponentRESTClient client;
    private CloseableHttpClient clientRaw;
    private Map<String, Object> configuredProperties = new HashMap<>();

    public RemoteTransformer(String baseUrl, String url, String componentDisplayName, String tenant, String username, String password) {
        this.baseUrl = baseUrl;
        this.componentUrl = url;
        this.username = username;
        this.password = password;
        this.tenant = tenant;
        this.componentDisplayName = componentDisplayName;
    }

    public void init() {
        logger.debug("Initializing - " + componentUrl);
        client = new ComponentRESTClient(baseUrl, username, password);

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        clientRaw = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
    }

    public void close() {
        logger.debug("closing");
        if(clientRaw != null) {
            try {
                clientRaw.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                clientRaw = null;
            }
        }
    }

    @Override
    public OutputColumns getOutputColumns() {
        org.datacleaner.restclient.OutputColumns columnsSpec = client.getOutputColumns(tenant, componentDisplayName, getCreateInput());
        OutputColumns outCols = new OutputColumns(columnsSpec.getColumns().size(), Object.class);
        int i = 0;
        for(org.datacleaner.restclient.OutputColumns.OutputColumn colSpec: columnsSpec.getColumns()) {
            outCols.setColumnName(i, colSpec.name);
            try {
                outCols.setColumnType(i, Class.forName(colSpec.type));
            } catch (ClassNotFoundException e) {
                // TODO: what to do with data types - classes that are not on our classpath?
                // Provide it as pure JsonNode?
                outCols.setColumnType(i, Object.class);
            }
            i++;
        }
        return outCols;
    }

    private CreateInput getCreateInput() {
        CreateInput result = new CreateInput();
        result.configuration = new ComponentConfiguration();
        for(Map.Entry<String, Object> propertyE: configuredProperties.entrySet()) {
            result.configuration.getProperties().put(propertyE.getKey(), mapper.valueToTree(propertyE.getValue()));
        }
        for(String col: getOutputColumnNames()) {
            result.configuration.getColumns().add(new TextNode(col));
        }
        return result;
    }

    private String getConfigurationContent() {
        try {
            return "{ \"properties\": " + mapper.writeValueAsString(configuredProperties) + ",\n" +
                    "  \"columns\": " + mapper.writeValueAsString(getOutputColumnNames()) + "}";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getOutputColumnNames() {
        ArrayList<String> colNames = new ArrayList<>();
        for(Object propValue: configuredProperties.values()) {
            if(propValue instanceof InputColumn) {
                colNames.add(((InputColumn) propValue).getName());
            } else if(propValue instanceof InputColumn[]) {
                for(InputColumn col: ((InputColumn[])propValue)) {
                    colNames.add(col.getName());
                }
            } else if(propValue instanceof Collection) {
                for(Object value: ((Collection)propValue)) {
                    if(value instanceof InputColumn) {
                        colNames.add(((InputColumn)value).getName());
                    }
                }
            }
        }
        return colNames.toArray(new String[colNames.size()]);
    }

    @Override
    public Object[] transform(InputRow inputRow) {
        List values = new ArrayList();
        String[] cols = getOutputColumnNames();
        // TODO: more better way?
        for(String col: cols) {
            for(InputColumn inputCol: inputRow.getInputColumns()) {
                if(inputCol.getName().equals(col)) {
                    values.add(inputRow.getValue(inputCol));
                }
            }
        }

        Object[] rows = new Object[] {values};

        ProcessStatelessInput input = new ProcessStatelessInput();
        input.configuration = getCreateInput().configuration;
        input.data = mapper.valueToTree(rows);
        ProcessStatelessOutput out = client.processStateless(tenant, componentDisplayName, input);
        return transformResponse(out.rows);
    }

    private Object[] transformResponse(JsonNode response) {
        JsonNode rows = response.get("rows");
        if(rows == null || rows.size() < 1 || rows.size() > 1) { throw new RuntimeException("Expected exactly 1 row in response"); }
        List values = new ArrayList();
        JsonNode row1 = rows.get(0);
        for(JsonNode value: row1) {
            values.add(transformValue(value));
        }
        return values.toArray(new Object[values.size()]);
    }

    private Object transformValue(JsonNode value) {
        return value.asText();
    }

    public void setPropertyValue(String propertyName, Object value) {
        configuredProperties.put(propertyName, value);
    }

    public Object getProperty(String propertyName) {
        return configuredProperties.get(propertyName);
    }

    private static class MyInputColumnsSerializer extends StdSerializer<InputColumn> {

        protected MyInputColumnsSerializer() {
            super(InputColumn.class);
        }

        @Override
        public void serialize(InputColumn value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getName());
        }
    }
}
