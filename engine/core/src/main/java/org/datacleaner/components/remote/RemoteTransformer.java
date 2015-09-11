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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
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
import org.datacleaner.descriptors.JsonSchemaConfiguredPropertyDescriptorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.IOException;
import java.util.*;

/**
 * @Since 9/1/15
 */
public class RemoteTransformer implements Transformer {

    private static final Logger logger = LoggerFactory.getLogger(RemoteTransformer.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ResponseHandler respHandler = new ResponseHandler();

    static {
        SimpleModule myModule = new SimpleModule("RemoteTransformersModule", new Version(1, 0, 0, null, "org.datacleaner", "DataCleaner-remote-transformers"));
        myModule.addSerializer(new MyInputColumnsSerializer()); // assuming serializer declares correct class to bind to
        mapper.registerModule(myModule);
    }

    private String remoteUrl;

    private CloseableHttpClient client;
    private Map<String, Object> configuredProperties = new HashMap<>();

    public RemoteTransformer(String url) {
        this.remoteUrl = url;
    }

    public void init() {
        logger.debug("Initializing - " + remoteUrl);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("admin", "admin"));
        client = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
    }

    public void close() {
        logger.debug("closing");
        if(client != null) {
            try {
                client.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                client = null;
            }
        }
    }

    @Override
    public OutputColumns getOutputColumns() {
        boolean close = false;
        if(client == null) {
            init();
            close = true;
        }
        try {
            HttpPost request = new HttpPost(remoteUrl + "/_outputColumns");
            String requestContent = "{\n" +
                    "\"configuration\":" + getConfigurationContent() + "\n" +
                    "}";
            request.setEntity(new StringEntity(requestContent, ContentType.APPLICATION_JSON));
            try {
                logger.trace("Calling {}", request);
                JsonNode response = client.execute(request, respHandler);
                JsonNode cols = response.get("columns");
                OutputColumns outCols = new OutputColumns(cols.size(), Object.class);
                int i = 0;
                for(JsonNode col: cols) {
                    outCols.setColumnName(i, col.get("name").asText());
                    // TODO: what to do with data types - classes that are not on our classpath?
                    // Provide it as pure JsonNode?
                    outCols.setColumnType(i, mapper.readValue(new TreeTraversingParser(col.get("type")), Class.class));
                    i++;
                }
                logger.debug("Returning columns: " + outCols);
                return outCols;
            } catch (Exception e) {
                return new OutputColumns(String.class, "TEST");
                //throw new RuntimeException(e);
            }
        } finally {
            if(close) {
                try {
                    close();
                } catch(Exception e) {
                    // TODO log debug
                }
            }
        }
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

        try {

            List values = new ArrayList();
            String[] cols = getOutputColumnNames();
            for(String col: cols) {
                for(InputColumn inputCol: inputRow.getInputColumns()) {
                    if(inputCol.getName().equals(col)) {
                        values.add(inputRow.getValue(inputCol));
                    }
                }
            }

            Object[] rows = new Object[] {values};

            HttpPut request = new HttpPut(remoteUrl);
            String requestContent = "{\n" +
                    "\"configuration\":" + getConfigurationContent() + ",\n" +
                    "\"data\":" + mapper.writeValueAsString(rows) + "\n" +
                    "}";
            request.setEntity(new StringEntity(requestContent, ContentType.APPLICATION_JSON));
            logger.trace("Calling {}", request);
            JsonNode response = client.execute(request, respHandler);
            return transformResponse(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    static class ResponseHandler implements org.apache.http.client.ResponseHandler<JsonNode> {
        @Override
        public JsonNode handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            StatusLine status = response.getStatusLine();
            if(status.getStatusCode() >= 300) {
                throw new HttpResponseException(status.getStatusCode(), "HTTP Response status '" + status.getStatusCode() + "'");
            }
            HttpEntity entity = response.getEntity();
            if(entity == null) {
                throw new ClientProtocolException("Emtpy response");
            }
            return mapper.readTree(entity.getContent());
        }
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
