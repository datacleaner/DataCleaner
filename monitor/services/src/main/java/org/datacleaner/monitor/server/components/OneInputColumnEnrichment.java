package org.datacleaner.monitor.server.components;

import java.util.Set;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.restclient.ComponentConfiguration;
import org.datacleaner.restclient.ProcessStatelessInput;
import org.datacleaner.restclient.Serializator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * Added by jakub on 23.2.16
 */
public class OneInputColumnEnrichment implements InputEnricher {

    public boolean enrichStatelessInputForTransformer(TransformerDescriptor transformer, ProcessStatelessInput input) {
        ConfiguredPropertyDescriptor inputColumnProp = null;
        Set<ConfiguredPropertyDescriptor> props = transformer.getConfiguredProperties();
        for(ConfiguredPropertyDescriptor prop : props) {
            if(prop.isInputColumn()) {
                // we have the second input column property - not yet supported
                if(inputColumnProp != null) {
                    return false;
                }
                // arrays not supported by this enricher
                if(prop.isArray()) {
                    return false;
                }
                inputColumnProp = prop;
            }
        }
        if(inputColumnProp == null) {
            return false;
        }
        enrichWithSingleInputProperty(inputColumnProp, input);
        return true;
    }

    /**
     * Enrich the input with:
     * <li> the input column property to value "c1"
     * <li> Configuration with one input column called "c1", default data type
     * <li> Check if data are not array, take the value and create a one-item array from it.
     */
    private void enrichWithSingleInputProperty(ConfiguredPropertyDescriptor inputColumnProp, ProcessStatelessInput input) {
        JsonNodeFactory json = Serializator.getJacksonObjectMapper().getNodeFactory();
        if(input.configuration == null) {
            input.configuration = new ComponentConfiguration();
        }
        input.configuration.getColumns().add(json.textNode("c1"));
        input.configuration.getProperties().put(inputColumnProp.getName(), json.textNode("c1"));

        if(input.data.isArray()) {
            // check if also the values are array. If not,
            // change them to an array.
            int i = 0;
            for(JsonNode row: input.data) {
                if(!row.isArray()) {
                    ArrayNode columnsArray = json.arrayNode();
                    columnsArray.add(row);
                    ((ArrayNode)input.data).set(i, columnsArray);
                }
                i++;
            }
        } else {
            ArrayNode columnsArray = json.arrayNode();
            columnsArray.add(input.data);
            ArrayNode rowsArray = json.arrayNode();
            rowsArray.add(columnsArray);
            input.data = rowsArray;
        }
    }
}
