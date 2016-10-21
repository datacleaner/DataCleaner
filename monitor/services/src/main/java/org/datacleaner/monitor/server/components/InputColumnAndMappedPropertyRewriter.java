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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.datacleaner.api.MappedProperty;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.restclient.ComponentConfiguration;
import org.datacleaner.restclient.ProcessStatelessInput;
import org.datacleaner.restclient.Serializator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class InputColumnAndMappedPropertyRewriter implements InputRewriter {

    public boolean rewriteInput(TransformerDescriptor<?> transformer, ProcessStatelessInput input) {

        // If columns specification is int the input, we do not rewrite
        if(input.configuration != null && input.configuration.getColumns() != null && !input.configuration.getColumns().isEmpty()) {
            return false;
        }

        ConfiguredPropertyDescriptor inputColumnProp = null;
        ConfiguredPropertyDescriptor mappedProp = null;
        List<ConfiguredPropertyDescriptor> inputColProperties = new ArrayList<>();
        boolean moreRequiredInputColumns = false;

        Set<ConfiguredPropertyDescriptor> props = transformer.getConfiguredProperties();
        for(ConfiguredPropertyDescriptor prop : props) {
            if(prop.isInputColumn()) {
                inputColProperties.add(prop);
                if(prop.isRequired()) {
                    // we have the second required input column property - this not supported by this enricher
                    if (inputColumnProp != null) {
                        moreRequiredInputColumns = true;
                    } else {
                        inputColumnProp = prop;
                    }
                }
            }
        }

        if(inputColumnProp != null) {
            // Check if there is a mapped property for the input column.
            for (ConfiguredPropertyDescriptor prop : props) {
                MappedProperty mappedPropAnnot = prop.getAnnotation(MappedProperty.class);
                if(mappedPropAnnot != null) {
                    if(inputColumnProp.getName().equals(mappedPropAnnot.value())) {
                        if(mappedProp != null) {
                            // second mapped property for the same inputColumn property.
                            // (Is this an error?)
                            return false;
                        }
                        mappedProp = prop;
                    }
                }
            }

            // If mapped property exists and is not configured => let do the MappedPropertyInputEnricher the enrichment
            if(mappedProp != null) {
                if(input.configuration == null || !input.configuration.getProperties().containsKey(mappedProp.getName())) {
                    return enrichMappedProperty(inputColumnProp, mappedProp, input);
                }
            }
        }

        if(enrichMapDataObjectsToInputColumnsByName(inputColProperties, input)) {
            return true;
        }

        enrichWithSingleInputProperty(inputColumnProp, input);
        return true;
    }

    private boolean enrichMapDataObjectsToInputColumnsByName(List<ConfiguredPropertyDescriptor> inputColProperties, ProcessStatelessInput input) {
        if(inputColProperties.isEmpty()) {
            return false;
        }
        // check if we can convert input "map" to the input columns
        if(input.data.isArray()) {
            for(JsonNode item: input.data) {
                if(!item.isObject()) {
                    return false;
                }
            }
            // OK, we have array of objects
        } else if(input.data.isObject()) {
            // OK, we have one object
        } else {
            return false;
        }

        JsonNodeFactory json = Serializator.getJacksonObjectMapper().getNodeFactory();
        if(input.data.isArray()) {
            int i = 0;
            for(JsonNode item: input.data) {
                ((ArrayNode)input.data).set(i++, mapDataObjectToInputColumnsByName(item, inputColProperties));
            }
        } else {
            input.data = json.arrayNode().add(mapDataObjectToInputColumnsByName(input.data, inputColProperties));
        }

        if(input.configuration == null) {
            input.configuration = new ComponentConfiguration();
        }

        int i = 1;
        for(ConfiguredPropertyDescriptor inputCol: inputColProperties) {
            String colName = "c" + i++;
            input.configuration.getProperties().put(inputCol.getName(), json.textNode(colName));
            input.configuration.getColumns().add(json.textNode(colName));
        }

        return true;
    }

    private JsonNode mapDataObjectToInputColumnsByName(JsonNode item, List<ConfiguredPropertyDescriptor> inputColProperties) {
        JsonNodeFactory json = Serializator.getJacksonObjectMapper().getNodeFactory();
        ArrayNode values = json.arrayNode();
        ITER:
        for(ConfiguredPropertyDescriptor inputColPropDesc: inputColProperties) {
            JsonNode value = item.get(inputColPropDesc.getName());
            if(value != null) {
                values.add(value);
                continue;
            }
            String[] aliases = inputColPropDesc.getAliases();
            if(aliases != null) {
                for (String alias : aliases) {
                    value = item.get(alias);
                    if(value != null) {
                        values.add(value);
                        continue ITER;
                    }
                }
            }

            // nothing found - put null value to the values array
            values.add(json.nullNode());
        }
        return values;
    }

    private static class InputColumnPropertyInfo {
        ConfiguredPropertyDescriptor propertyDesc;
        int index;
    }

    private void enrichWithSingleInputProperty(ConfiguredPropertyDescriptor inputColumnProp, ProcessStatelessInput input) {
        JsonNodeFactory json = Serializator.getJacksonObjectMapper().getNodeFactory();

        // first repair the input data
        if(input.data.isArray()) {
            if(inputColumnProp.isArray()) {
                // If all items are already array, do nothing.
                // otherwise, create single row containing the original data
                if(!allItemsAreArray((ArrayNode)input.data)) {
                    input.data = json.arrayNode().add(input.data);
                }
            } else {
                // check if also the values are array. If not,
                // change them to an array. It means
                // create more rows, each having single column with original data.
                int i = 0;
                for (JsonNode row : input.data) {
                    if (!row.isArray()) {
                        ArrayNode columnsArray = json.arrayNode();
                        columnsArray.add(row);
                        ((ArrayNode) input.data).set(i, columnsArray);
                    }
                    i++;
                }
            }
        } else {
            ArrayNode columnsArray = json.arrayNode();
            columnsArray.add(input.data);
            ArrayNode rowsArray = json.arrayNode();
            rowsArray.add(columnsArray);
            input.data = rowsArray;
        }

        // get number of columns from the first row
        int numColumns = input.data.get(0).size();

        if(input.configuration == null) {
            input.configuration = new ComponentConfiguration();
        }
        ArrayNode inputColPropertyValue = json.arrayNode();
        for(int i = 0; i < numColumns; i++) {
            String colName = "c" + i;
            input.configuration.getColumns().add(json.textNode(colName));
            inputColPropertyValue.add(json.textNode(colName));
        }
        input.configuration.getProperties().put(inputColumnProp.getName(), inputColPropertyValue);
    }

    private boolean enrichMappedProperty(ConfiguredPropertyDescriptor inputColumnProp, ConfiguredPropertyDescriptor mappedProp, ProcessStatelessInput input) {
        JsonNodeFactory json = Serializator.getJacksonObjectMapper().getNodeFactory();

        if(!input.data.isArray()) {
            if(!input.data.isObject()) {
                // Do not enrich if input row is not a map
                return false;
            }
            input.data = json.arrayNode().add(input.data);
        }

        // Note, that this MUST be a LinkedHashSet. We need the order of items
        // to be the same as the items were added to it.
        Set<String> existingMappedPropertyValues = new LinkedHashSet<>();

        // first collect the possible mapped property values
        for(JsonNode row: input.data) {
            if(!row.isObject()) {
                // Do not enrich if input row is not a map
                return false;
            }
            for(Iterator<String> mappedPropertyValueIt = row.fieldNames(); mappedPropertyValueIt.hasNext(); ) {
                String mappedPropertyValue = mappedPropertyValueIt.next();
                if(!existingMappedPropertyValues.contains(mappedPropertyValue)) {
                    existingMappedPropertyValues.add(mappedPropertyValue);
                }
            }
        }

        // Now transform the JSON objects to arrays
        ArrayNode transformedData = json.arrayNode();
        for(JsonNode row: input.data) {
            ArrayNode transformedRow = json.arrayNode();
            for(String mappedPropertyValue: existingMappedPropertyValues) {
                transformedRow.add(row.get(mappedPropertyValue));
            }
            transformedData.add(transformedRow);
        }

        input.data = transformedData;

        // number of columns = number of possible mapped property values
        int numColumns = existingMappedPropertyValues.size();

        if(input.configuration == null) {
            input.configuration = new ComponentConfiguration();
        }
        ArrayNode inputColPropertyValue = json.arrayNode();
        for(int i = 0; i < numColumns; i++) {
            String colName = "c" + i;
            input.configuration.getColumns().add(json.textNode(colName));
            inputColPropertyValue.add(json.textNode(colName));
        }
        input.configuration.getProperties().put(inputColumnProp.getName(), inputColPropertyValue);

        ArrayNode mappedPropertyValues = json.arrayNode();
        for(String mappedPropValue: existingMappedPropertyValues) {
            mappedPropertyValues.add(json.textNode(mappedPropValue));
        }
        input.configuration.getProperties().put(mappedProp.getName(), mappedPropertyValues);

        return true;
    }

    private boolean allItemsAreArray(ArrayNode array) {
        for(JsonNode item: array) {
            if(!item.isArray()) {
                return false;
            }
        }
        return true;
    }

}
