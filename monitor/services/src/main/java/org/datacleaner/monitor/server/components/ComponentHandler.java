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
package org.datacleaner.monitor.server.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.metamodel.data.DataSetHeader;
import org.apache.metamodel.data.DefaultRow;
import org.apache.metamodel.data.SimpleDataSetHeader;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.ColumnTypeImpl;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Component;
import org.datacleaner.api.HasAnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MetaModelInputRow;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.PropertyDescriptor;
import org.datacleaner.desktop.api.HiddenProperty;
import org.datacleaner.job.ImmutableComponentConfiguration;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.restclient.ComponentConfiguration;
import org.datacleaner.restclient.ComponentNotFoundException;
import org.datacleaner.restclient.Serializator;
import org.datacleaner.util.convert.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * This class is a component type independent wrapper that decides the proper handler and provides its results.
 * @since 14. 07. 2015
 */
public class ComponentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentHandler.class);

    public static ObjectMapper mapper = Serializator.getJacksonObjectMapper();

    private final String componentName;
    private DataCleanerConfiguration dcConfiguration;

    ComponentDescriptor descriptor;
    Map<String, MutableColumn> columns;
    Map<String, InputColumn> inputColumns;
    MyMutableTable table;
    Component component;

    public ComponentHandler(DataCleanerConfiguration dcConfiguration, String componentName) {
        this.dcConfiguration = dcConfiguration;
        this.componentName = componentName;
    }

    public void createComponent(ComponentConfiguration componentConfiguration) {
        columns = new HashMap<>();
        inputColumns = new HashMap<>();
        descriptor = dcConfiguration.getEnvironment().getDescriptorProvider().getTransformerDescriptorByDisplayName(componentName);
        table = new MyMutableTable("inputData");
        if(descriptor == null) {
            descriptor = dcConfiguration.getEnvironment().getDescriptorProvider().getAnalyzerDescriptorByDisplayName(componentName);
        }
        if(descriptor == null) {
            throw ComponentNotFoundException.createTypeNotFound(componentName);
        }

        component = (Component) descriptor.newInstance();

        // create "table" according to the columns specification (for now only a list of names)
        int index = 0;
        for (JsonNode columnSpec : componentConfiguration.getColumns()) {
            String columnName;
            String columnTypeName;
            if(columnSpec.isObject()) {
                ObjectNode columnSpecO = (ObjectNode)columnSpec;
                columnName = columnSpecO.get("name").asText();
                if(columnSpecO.get("type") == null) {
                    columnTypeName = ColumnType.VARCHAR.getName();
                } else {
                    columnTypeName = columnSpecO.get("type").asText();
                }
            } else {
                columnName = columnSpec.asText();
                columnTypeName = ColumnType.VARCHAR.getName();
            }

            MutableColumn column = columns.get(columnName);
            if(column != null) {
                throw new RuntimeException("Multiple column definition of name '" + columnName + "'");
            }
            ColumnType columnType = ColumnTypeImpl.valueOf(columnTypeName);
            if(columnType == null) {
                throw new RuntimeException("Column '" + columnName + "' has unknown type '" + columnTypeName + "'");
            }
            column = new MutableColumn(columnName, columnType, table, index, true);
            columns.put(columnName, column);
            table.addColumn(index, column);
            index++;
        }

        // Set the configured properties

        // First, copy current values = the defaults
        Map<PropertyDescriptor, Object> configuredProperties = new HashMap<>();
        Set<ConfiguredPropertyDescriptor> props = descriptor.getConfiguredProperties();
        for(ConfiguredPropertyDescriptor propDesc: props) {
            Object defaultValue = propDesc.getValue(component);
            if(defaultValue != null) {
                configuredProperties.put(propDesc, defaultValue);
            }
        }
        for(String propertyName: componentConfiguration.getProperties().keySet()) {
            ConfiguredPropertyDescriptor propDesc = descriptor.getConfiguredProperty(propertyName);
            if(propDesc == null) {
                LOGGER.debug("Unknown configuration property '{}'. ", propertyName);
                continue;
            }

            if (propDesc.getAnnotation(HiddenProperty.class) != null) {
                LOGGER.debug("Hidden property '{}' is skipped. ", propertyName);
                continue;
            }

            JsonNode userPropValue = componentConfiguration.getProperties().get(propertyName);
            if(userPropValue != null) {
                if(propDesc.isInputColumn()) {
                    List<String> colNames = convertToStringArray(userPropValue);
                    List<InputColumn> inputCols = new ArrayList<>();
                    for(String columnName: colNames) {
                        inputCols.add(getOrCreateInputColumn(columnName, propertyName));
                    }
                    configuredProperties.put(propDesc, inputCols.toArray(new InputColumn[inputCols.size()]));
                } else {
                    Object value = convertPropertyValue(propDesc, userPropValue);
                    configuredProperties.put(propDesc, value);
                }
            }
        }
        org.datacleaner.job.ComponentConfiguration config = new ImmutableComponentConfiguration(configuredProperties);

        LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(dcConfiguration, null, false);
        lifeCycleHelper.assignConfiguredProperties(descriptor, component, config);
        lifeCycleHelper.assignProvidedProperties(descriptor, component);
        lifeCycleHelper.validate(descriptor, component);
        lifeCycleHelper.initialize(descriptor, component);
    }

    public OutputColumns getOutputColumns() {
        return ((Transformer)component).getOutputColumns();
    }

    public List<Object[]> runComponent(JsonNode data) {
        List<InputRow> inputRows;
        DataSetHeader header = new SimpleDataSetHeader(table.getColumns());
        inputRows = new ArrayList<>();
        int id = 0;
        for (JsonNode row : data) {
            DefaultRow inputRow = new DefaultRow(header, toRowValues((ArrayNode) row));
            inputRows.add(new MetaModelInputRow(id, inputRow));
            id++;
        }

        if(component instanceof Transformer) {
            List results = new ArrayList();
            for (InputRow inputRow : inputRows) {
                results.add(((Transformer)component).transform(inputRow));
            }
            return results;
        } else if(component instanceof Analyzer) {
            for (InputRow inputRow : inputRows) {
                ((Analyzer)component).run(inputRow, 1);
            }
            return null;
        } else {
            throw new IllegalArgumentException("Unknown component type " + component.getClass());
        }
    }

    public AnalyzerResult closeComponent() {
        LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(dcConfiguration, null, false);
        lifeCycleHelper.close(descriptor, component, true);
        if(component instanceof HasAnalyzerResult) {
            return ((HasAnalyzerResult)component).getResult();
        } else {
            return null;
        }
    }

    private InputColumn getOrCreateInputColumn(String columnName, String propertyName) {
        MutableColumn column = columns.get(columnName);
        if(column == null) {
            throw new RuntimeException("Column '" + columnName + "' specified in property '" + propertyName + "' was not found in table columns specification");
        }
        InputColumn inputColumn = inputColumns.get(columnName);
        if(inputColumn == null) {
            inputColumns.put(columnName, inputColumn = new MetaModelInputColumn(column));
        }
        return inputColumn;
    }

    private Object[] toRowValues(ArrayNode row) {
        ArrayList<Object> values = new ArrayList<>();
        int i = 0;
        for(JsonNode value: row) {
            if(i >= table.getColumnsCount()) {
                LOGGER.debug("Data contain more columns than specified. Will be ignored.");
                break;
            }
            Column col = table.getColumn(i);
            values.add(convertTableValue(col.getType().getJavaEquivalentClass(), value));
            i++;
        }
        return values.toArray(new Object[values.size()]);
    }

    private Object convertPropertyValue(ConfiguredPropertyDescriptor propDesc, JsonNode value) {
        Class type = propDesc.getType();
        try {
            if(value.isArray() || value.isObject() || type.isEnum()) {
                return mapper.readValue(value.traverse(), type);
            } else {
                return new StringConverter(dcConfiguration).deserialize(value.asText(), type, propDesc.getCustomConverter());
            }
        } catch(Exception e) {
            throw new RuntimeException("Cannot convert property '" + propDesc.getName() + " value ' of type '" + type + "': " + value.toString(), e);
        }
    }

    private Object convertTableValue(Class type, JsonNode value) {
        try {
            if(value.isArray() || value.isObject()) {
                return mapper.readValue(value.traverse(), type);
            } else {
                return StringConverter.simpleInstance().deserialize(value.asText(), type);
            }
        } catch(Exception e) {
            throw new RuntimeException("Cannot convert table value of type '" + type + "': " + value.toString(), e);
        }
    }

    private String toString(JsonNode item) {
        if(item.getNodeType() == JsonNodeType.STRING) {
            return ((TextNode)item).textValue();
        } else if(item.isArray() && ((ArrayNode)item).size() < 1) {
            if(((ArrayNode)item).size() == 0) {
                return "";
            } else {
                return toString(((ArrayNode) item).get(0));
            }
        }
        return item.toString();
    }

    private List<String> convertToStringArray(JsonNode json) {
        List<String> result = new ArrayList<>();
        if(json.isArray()) {
            for(JsonNode item: ((ArrayNode)json)) {
                result.add(toString(item));
            }
        } else {
            result.add(toString(json));
        }
        return result;
    }

    // TODO: implement the methods written here directly in MutableTable (which is in dependent project)
    static class MyMutableTable extends MutableTable {
        public MyMutableTable(String name) {
            super(name);
        }
        public Column getColumn(int index) throws IndexOutOfBoundsException {
            return _columns.get(index);
        }
        public int getColumnsCount() {
            return _columns.size();
        }
    }
}
