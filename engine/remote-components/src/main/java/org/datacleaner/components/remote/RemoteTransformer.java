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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.metamodel.schema.ColumnTypeImpl;
import org.apache.metamodel.util.EqualsBuilder;
import org.datacleaner.api.Close;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.configuration.RemoteServerData;
import org.datacleaner.job.concurrent.PreviousErrorsExistException;
import org.datacleaner.restclient.ComponentConfiguration;
import org.datacleaner.restclient.ComponentRESTClient;
import org.datacleaner.restclient.ComponentsRestClientUtils;
import org.datacleaner.restclient.CreateInput;
import org.datacleaner.restclient.ProcessStatelessInput;
import org.datacleaner.restclient.ProcessStatelessOutput;
import org.datacleaner.restclient.Serializator;
import org.datacleaner.util.batch.BatchRowCollectingTransformer;
import org.datacleaner.util.batch.BatchSink;
import org.datacleaner.util.batch.BatchSource;
import org.datacleaner.util.convert.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema;

/**
 * Transformer that is actually a proxy to a remote transformer sitting at DataCleaner Monitor server.
 * Instances of this transformer can be created only by
 * {@link org.datacleaner.descriptors.RemoteTransformerDescriptorImpl} component descriptors.
 *
 * @Since 9/1/15
 */
public class RemoteTransformer extends BatchRowCollectingTransformer {

    private static final Logger logger = LoggerFactory.getLogger(RemoteTransformer.class);
    private static final ObjectMapper mapper = Serializator.getJacksonObjectMapper();

    private final RemoteServerData serverData;
    private String componentDisplayName;

    private OutputColumns lastOutputColumns;
    private CreateInput lastCreateInput;

    private ComponentRESTClient client;
    private Map<String, Object> configuredProperties = new TreeMap<>();

    private final AtomicBoolean failed = new AtomicBoolean(false);

    public RemoteTransformer(RemoteServerData serverData, String componentDisplayName) {
        this.serverData = serverData;
        this.componentDisplayName = componentDisplayName;
    }

    @Initialize
    public void initClient() throws RemoteComponentException {
        try {
            logger.debug("Initializing '{}' @{}", componentDisplayName, this.hashCode());
            client = new ComponentRESTClient(serverData.getUrl(), serverData.getUsername(), serverData.getPassword());
        } catch (Exception e) {
            throw new RemoteComponentException(
                    "Remote component '" + componentDisplayName + "' is temporarily unavailable. \n" + e.getMessage());
        }
    }

    @Close
    public void closeClient() {
        logger.debug("closing '{}' @{}", componentDisplayName, this.hashCode());
        client = null;
        // TODO: client now misses a "close" method (although Jersey client has a "destroy" method).
    }

    @Override
    public OutputColumns getOutputColumns() {
        OutputColumns outCols;

        try {
            CreateInput createInput = new CreateInput();
            createInput.configuration = getConfiguration(getUsedInputColumns());
            if (lastOutputColumns != null && createInput.equals(lastCreateInput)) {
                logger.debug("Reusing cached output columns, nothing changed");
                outCols = lastOutputColumns;
            } else {
                logger.debug("Getting output columns from server");
                boolean wasInit = false;
                if (client == null) {
                    wasInit = true;
                    initClient();
                }
                try {
                    org.datacleaner.restclient.OutputColumns columnsSpec = client.getOutputColumns(componentDisplayName, createInput);

                    outCols = new OutputColumns(columnsSpec.getColumns().size(), Object.class);
                    int i = 0;
                    for (org.datacleaner.restclient.OutputColumns.OutputColumn colSpec : columnsSpec.getColumns()) {
                        outCols.setColumnName(i, colSpec.name);
                        try {
                            outCols.setColumnType(i, Class.forName(colSpec.type));
                        } catch (ClassNotFoundException e) {
                            final Class<?> type;
                            if (isOutputColumnEnumeration(colSpec.schema)) {
                                type = String.class;
                            } else {
                                type = JsonNode.class;
                            }
                            outCols.setColumnType(i, type);
                        }
                        i++;
                    }
                    lastOutputColumns = outCols;
                    lastCreateInput = createInput;
                } finally {
                    if (wasInit) {
                        closeClient();
                    }
                }
            }
            return outCols;
        } catch(Exception e) {
            logger.debug("Error retrieving columns of transformer '" + componentDisplayName + "': " + e.toString());
            return OutputColumns.NO_OUTPUT_COLUMNS;
        }
    }

    private boolean isOutputColumnEnumeration(JsonSchema schema) {
        if(schema == null){
            return false;
        }
        boolean isArray = schema.isArraySchema();
        JsonSchema baseSchema;
        if (isArray) {
            baseSchema = ((ArraySchema) schema).getItems().asSingleItems().getSchema();
        } else {
            baseSchema = schema;
        }

        if (baseSchema instanceof ValueTypeSchema) {
            Set<String> enums = ((ValueTypeSchema) baseSchema).getEnums();
            if (enums != null && !enums.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private ComponentConfiguration getConfiguration(List<InputColumn<?>> inputColumns) {
        ComponentConfiguration configuration = new ComponentConfiguration();
        for(Map.Entry<String, Object> propertyE: configuredProperties.entrySet()) {
            configuration.getProperties().put(propertyE.getKey(), mapper.valueToTree(propertyE.getValue()));
        }

        for(InputColumn<?> col: inputColumns) {
            configuration.getColumns().add(ComponentsRestClientUtils.createInputColumnSpecification(
                    col.getName(),
                    col.getDataType(),
                    ColumnTypeImpl.convertColumnType(col.getDataType()).getName(),
                    mapper.getNodeFactory()));
        }
        return configuration;
    }

    private List<InputColumn<?>> getUsedInputColumns() {
        ArrayList<InputColumn<?>> columns = new ArrayList<>();
        for(Object propValue: configuredProperties.values()) {
            if(propValue instanceof InputColumn) {
                columns.add((InputColumn<?>) propValue);
            } else if(propValue instanceof InputColumn[]) {
                for(InputColumn<?> col: ((InputColumn[])propValue)) {
                    columns.add(col);
                }
            } else if(propValue instanceof Collection) {
                for(Object value: ((Collection<?>)propValue)) {
                    if(value instanceof InputColumn) {
                        columns.add((InputColumn<?>)value);
                    } else {
                        // don't iterate the rest if the first item is not an input column.
                        break;
                    }
                }
            }
            // TODO: are maps possible?
        }
        return columns;
    }

    private void convertOutputRows(JsonNode rowSets, BatchSink<Collection<Object[]>> sink, int sinkSize) {
        OutputColumns outCols = getOutputColumns();
        if(rowSets == null || rowSets.size() < 1) { throw new RuntimeException("Expected exactly 1 row in response"); }

        int rowI = 0;
        for(JsonNode rowSet: rowSets) {
            if(rowI >= sinkSize) {
                throw new RuntimeException("Expected " + sinkSize + " rows, but got more");
            }

            List<Object[]> outRowSet = new ArrayList<>();

            for(JsonNode row: rowSet) {
                final List<Object> values = new ArrayList<>();
                int i = 0;
                for (JsonNode value : row) {
                    // TODO: should JsonNode be the default?
                    Class<?> cl = String.class;
                    if (i < outCols.getColumnCount()) {
                        cl = outCols.getColumnType(i);
                    }
                    values.add(convertOutputValue(value, cl));
                    i++;
                }
                outRowSet.add(values.toArray(new Object[values.size()]));
            }
            sink.setOutput(rowI, outRowSet);
            rowI++;
        }
        if(rowI < sinkSize) {
            throw new RuntimeException("Expected " + sinkSize + " rows, but got only " + rowI);
        }
    }

    private Object convertOutputValue(JsonNode value, Class<?> cl) {
        try {
            if(cl == JsonNode.class) {
                return value;
            }
            if(cl == File.class) {
                return StringConverter.simpleInstance().deserialize(value.asText(), cl);
            }
            return mapper.readValue(value.traverse(), cl);
        } catch(Exception e) {
            throw new RuntimeException("Cannot convert table value of type '" + cl + "': " + value.toString(), e);
        }
    }

    public void setPropertyValue(String propertyName, Object value) {
        if(EqualsBuilder.equals(value, configuredProperties.get(propertyName))) {
            return;
        }
        logger.debug("Setting '{}'.'{}' = {}", componentDisplayName, propertyName, value);

        if(value == null) {
            configuredProperties.remove(propertyName);
        } else {
            configuredProperties.put(propertyName, value);
        }
    }

    public Object getPropertyValue(String propertyName) {
        return configuredProperties.get(propertyName);
    }

    @Override
    public void map(BatchSource<InputRow> source, BatchSink<Collection<Object[]>> sink) {
        List<InputColumn<?>> cols = getUsedInputColumns();
        int size = source.size();
        Object[] rows = new Object[size];
        for(int i = 0; i < size; i++) {
            InputRow inputRow = source.getInput(i);
            Object[] values = new Object[cols.size()];
            int j = 0;
            for(InputColumn<?> col: cols) {
                values[j] = inputRow.getValue(col);
                j++;
            }
            rows[i] = values;
        }

        ProcessStatelessInput input = new ProcessStatelessInput();
        input.configuration = getConfiguration(cols);
        input.data = mapper.valueToTree(rows);

        logger.debug("Processing remotely {} rows", size);

        if (client == null) {
            if (failed.get()) {
                throw new PreviousErrorsExistException();
            }
            throw new RuntimeException("Remote transformer's connection has already been closed. ");
        }
        ProcessStatelessOutput out;
        try {
            out = client.processStateless(componentDisplayName, input);
        } catch (RuntimeException e) {
            boolean alreadyFailed = failed.getAndSet(true);
            if (!alreadyFailed) {
                throw new RuntimeException("Remote transformer failed: " + e.getMessage(), e);
            } else {
                throw new PreviousErrorsExistException();
            }
        }
        convertOutputRows(out.rows, sink, size);
    }
}
