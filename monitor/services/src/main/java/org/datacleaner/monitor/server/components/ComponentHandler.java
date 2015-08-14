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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.metamodel.data.DataSetHeader;
import org.apache.metamodel.data.DefaultRow;
import org.apache.metamodel.data.SimpleDataSetHeader;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;
import org.datacleaner.api.*;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MetaModelInputRow;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.monitor.configuration.ComponentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a component type independent wrapper that decides the proper handler and provides its results.
 * @author j.horcicka (GMC)
 * @since 14. 07. 2015
 */
public class ComponentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentHandler.class);

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


        // create "table" according to the columns spcecification (for now only a list of names)
        int index = 0;
        for (String columnName : componentConfiguration.getColumns()) {
            MutableColumn column = columns.get(columnName);
            if(column == null) {
                column = new MutableColumn(columnName, ColumnType.VARCHAR, table, index, true);
                columns.put(columnName, column);
            } else {
                column.setColumnNumber(index);
            }
            table.addColumn(index, column);
            index++;
        }

        // Set the configured properties

        for(String propertyName: componentConfiguration.getPropertiesNames()) {
            ConfiguredPropertyDescriptor propDesc = descriptor.getConfiguredProperty(propertyName);
            if(propDesc == null) {
                LOGGER.debug("Unknown configuration property '" + propertyName + "'");
                continue;
            }
            JsonNode userPropValue = componentConfiguration.getProperty(propDesc.getName());
            if(userPropValue != null) {
                if(propDesc.isInputColumn()) {
                    List<String> colNames = convertToStringArray(userPropValue);
                    List<InputColumn> inputCols = new ArrayList<>();
                    for(String columnName: colNames) {
                        inputCols.add(getOrCreateInputColumn(columnName));
                    }
                    propDesc.setValue(component, inputCols.toArray(new InputColumn[inputCols.size()]));
                } else {
                    Object value = convertPropertyValue(propDesc, userPropValue);
                    propDesc.setValue(component, value);
                }
            }
        }

        LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(dcConfiguration, null, false);
        lifeCycleHelper.assignProvidedProperties(descriptor, component);
        lifeCycleHelper.validate(descriptor, component);
        lifeCycleHelper.initialize(descriptor, component);
    }

    public List<Object[]> runComponent(JsonNode data) {
        List<InputRow> inputRows;
        DataSetHeader header = new SimpleDataSetHeader(table.getColumns());
        inputRows = new ArrayList<>();
        int id = 0;
        for (JsonNode row : ((ArrayNode)data)) {
            DefaultRow inputRow = new DefaultRow(header, toRowValues((ArrayNode) row));
            inputRows.add(new MetaModelInputRow(id, inputRow));
            id++;
        }

        // TODO: run with the engine thread-pool executor
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

    private InputColumn getOrCreateInputColumn(String columnName) {
        MutableColumn column = columns.get(columnName);
        InputColumn inputColumn;
        if(column == null) {
            columns.put(columnName, column = new MutableColumn(columnName, ColumnType.VARCHAR, table, columns.size(), true));
        }
        inputColumn = inputColumns.get(columnName);
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
        }
        return values.toArray(new Object[values.size()]);
    }

    private Object convertPropertyValue(ConfiguredPropertyDescriptor propDesc, JsonNode userPropValue) {
        // TODO: other properties type. Find some generic way to convert JsonNode to the specific property type
        Class type = propDesc.getType();
        if(String.class.isAssignableFrom(type)) {
            return toString(userPropValue);
        } else {
            return null;
        }
    }

    private Object convertTableValue(Class type, JsonNode value) {
        if(String.class.isAssignableFrom(type)) {
            return toString(value);
        } else {
            // TODO: various data types. But for now user cannot specify column data type. So the type is only String for now.
            return toString(value);
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
