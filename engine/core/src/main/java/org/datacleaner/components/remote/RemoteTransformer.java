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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.schema.ColumnTypeImpl;
import org.datacleaner.api.Close;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.restclient.ComponentConfiguration;
import org.datacleaner.restclient.ComponentRESTClient;
import org.datacleaner.restclient.CreateInput;
import org.datacleaner.restclient.ProcessStatelessInput;
import org.datacleaner.restclient.ProcessStatelessOutput;
import org.datacleaner.util.convert.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

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
    private OutputColumns cachedOutputColumns;

    private ComponentRESTClient client;
    private Map<String, Object> configuredProperties = new HashMap<>();

    public RemoteTransformer(String baseUrl, String url, String componentDisplayName, String tenant, String username, String password) {
        this.baseUrl = baseUrl;
        this.componentUrl = url;
        this.username = username;
        this.password = password;
        this.tenant = tenant;
        this.componentDisplayName = componentDisplayName;
    }

    @Initialize
    public void init() {
        logger.debug("Initializing - " + componentUrl);
        client = new ComponentRESTClient(baseUrl, username, password);
    }

    @Close
    public void close() {
        logger.debug("closing");
        client = null;
        // TODO: client now misses a "close" method (although Jersey client has a "destroy" method).
    }

    @Override
    public OutputColumns getOutputColumns() {
        OutputColumns outCols = cachedOutputColumns;
        if(outCols != null) { return outCols; }

        boolean wasInit = false;
        if(client == null) {
            wasInit = true;
            init();
        }
        try {
            CreateInput createInput = new CreateInput();
            createInput.configuration = getConfiguration();
            org.datacleaner.restclient.OutputColumns columnsSpec = client.getOutputColumns(tenant, componentDisplayName, createInput);
            outCols = new OutputColumns(columnsSpec.getColumns().size(), Object.class);
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
            cachedOutputColumns = outCols;
            return outCols;
        } finally {
            if(wasInit) {
                close();
            }
        }
    }

    private ComponentConfiguration getConfiguration() {
        ComponentConfiguration configuration = new ComponentConfiguration();
        for(Map.Entry<String, Object> propertyE: configuredProperties.entrySet()) {
            configuration.getProperties().put(propertyE.getKey(), mapper.valueToTree(propertyE.getValue()));
        }
        // TODO: provide also the column type.
        for(InputColumn col: getInputColumnNames()) {
            ObjectNode colSpec = new ObjectNode(mapper.getNodeFactory());
            colSpec.set("name", new TextNode(col.getName()));
            colSpec.set("type", new TextNode(ColumnTypeImpl.convertColumnType(col.getDataType()).getName()));
            colSpec.set("className", new TextNode(col.getDataType().getName()));
            configuration.getColumns().add(colSpec);
        }
        return configuration;
    }

    private List<InputColumn> getInputColumnNames() {
        ArrayList<InputColumn> columns = new ArrayList<>();
        for(Object propValue: configuredProperties.values()) {
            if(propValue instanceof InputColumn) {
                columns.add((InputColumn) propValue);
            } else if(propValue instanceof InputColumn[]) {
                for(InputColumn col: ((InputColumn[])propValue)) {
                    columns.add(col);
                }
            } else if(propValue instanceof Collection) {
                for(Object value: ((Collection)propValue)) {
                    if(value instanceof InputColumn) {
                        columns.add((InputColumn)value);
                    }
                }
            }
        }
        return columns;
    }

    @Override
    public Object[] transform(InputRow inputRow) {

        if(client == null) {
            throw new RuntimeException("Remote transformer not initialized");
        }

        List values = new ArrayList();
        List<InputColumn> cols = getInputColumnNames();
        for(InputColumn col: cols) {
            values.add(inputRow.getValue(col));
        }

        Object[] rows = new Object[] {values};

        ProcessStatelessInput input = new ProcessStatelessInput();
        input.configuration = getConfiguration();
        input.data = mapper.valueToTree(rows);
        ProcessStatelessOutput out = client.processStateless(tenant, componentDisplayName, input);
        return transformResponse(out.rows);
    }

    private Object[] transformResponse(JsonNode rows) {
        OutputColumns outCols = getOutputColumns();
        if(rows == null || rows.size() < 1 || rows.size() > 1) { throw new RuntimeException("Expected exactly 1 row in response"); }
        List values = new ArrayList();
        JsonNode row1 = rows.get(0);
        int i = 0;
        for(JsonNode value: row1) {
            // TODO: should JsonNode be the default?
            Class cl = String.class;
            if(i < outCols.getColumnCount()) {
                cl = outCols.getColumnType(i);
            }
            values.add(transformValue(value, cl));
            i++;
        }
        return values.toArray(new Object[values.size()]);
    }

    private Object transformValue(JsonNode value, Class cl) {
        // TODO: this is code duplicate with ComponentHandler.convertTableValue
        // (which is used to transform input rows on the server side)
        try {
            if(value.isArray() || value.isObject()) {
                return mapper.readValue(value.traverse(), cl);
            } else {
                return StringConverter.simpleInstance().deserialize(value.asText(), cl);
            }
        } catch(Exception e) {
            throw new RuntimeException("Cannot convert table value of type '" + cl + "': " + value.toString(), e);
        }
    }

    public void setPropertyValue(String propertyName, Object value) {
        if(value == null) {
            configuredProperties.remove(propertyName);
        } else {
            configuredProperties.put(propertyName, value);
        }
        // invalidate the cached output columns
        cachedOutputColumns = null;
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
