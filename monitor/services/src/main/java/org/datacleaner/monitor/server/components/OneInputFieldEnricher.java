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

public class OneInputFieldEnricher implements InputEnricher {

    public boolean enrichStatelessInputForTransformer(TransformerDescriptor transformer, ProcessStatelessInput input) {
        ConfiguredPropertyDescriptor inputColumnProp = null;
        Set<ConfiguredPropertyDescriptor> props = transformer.getConfiguredProperties();
        for(ConfiguredPropertyDescriptor prop : props) {
            if(prop.isInputColumn()) {
                // we have the second input column property - not supported by this enricher
                if(inputColumnProp != null) {
                    return false;
                }
                inputColumnProp = prop;
            }
            if(prop.getAnnotation(MappedProperty.class) != null) {
                // mapped annotation not supported by this enricher.
                return false;
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

        // first repair the input data
        if(input.data.isArray()) {
            if(inputColumnProp.isArray()) {
                // create single row containing the original data
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

    private boolean allItemsAreArray(ArrayNode array) {
        for(JsonNode item: array) {
            if(!item.isArray()) {
                return false;
            }
        }
        return true;
    }
}
