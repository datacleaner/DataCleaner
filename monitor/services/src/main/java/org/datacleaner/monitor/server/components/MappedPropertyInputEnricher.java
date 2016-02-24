package org.datacleaner.monitor.server.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

/**
 * Added by jakub on 24.2.16
 */
public class MappedPropertyInputEnricher implements InputEnricher {

    public boolean enrichStatelessInputForTransformer(TransformerDescriptor transformer, ProcessStatelessInput input) {
        ConfiguredPropertyDescriptor inputColumnProp = null;
        ConfiguredPropertyDescriptor mappedProp = null;
        Set<ConfiguredPropertyDescriptor> props = transformer.getConfiguredProperties();
        for(ConfiguredPropertyDescriptor prop : props) {
            if(prop.isInputColumn()) {
                if(prop.isRequired()) {
                    // we have the second input column property - not supported by this enricher
                    if (inputColumnProp != null) {
                        return false;
                    }
                    inputColumnProp = prop;
                }
            }
        }
        if(inputColumnProp == null) {
            return false;
        }
        for(ConfiguredPropertyDescriptor prop : props) {
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

        return enrichMappedProperty(inputColumnProp, mappedProp, input);
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
