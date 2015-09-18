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
package org.datacleaner.restclient;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;

/**
 * Class for input/output data types conversion from/into String.
 * @since 11. 09. 2015
 */
public class Serializator {
    private static final Logger logger = LoggerFactory.getLogger(Serializator.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static ComponentList componentList(String response) {
        ComponentList components = Serializator.fromString(response, ComponentList.class);

        workaroundJacksonBug(components, response);

        return components;
    }

    /**
     * Workaround Jackson bug https://github.com/FasterXML/jackson-module-jsonSchema/issues/77
     * "Schema with enum array - when deserialized, the enum values are lost"
     */
    private static void workaroundJacksonBug(ComponentList components, String response) {
        try {
            JsonNode rootN = objectMapper.readTree(response);
            JsonNode componentsN = rootN.get("components");
            if(componentsN == null) { return; }
            for(JsonNode componentN: componentsN) {
                String componentName = componentN.get("name").asText();
                JsonNode propsN = componentN.get("properties");
                if(propsN == null || !propsN.isObject()) { continue; }
                for(Iterator<Map.Entry<String, JsonNode>> propIt = propsN.fields(); propIt.hasNext();) {
                    Map.Entry<String, JsonNode> propE = propIt.next();
                    String propName = propE.getKey();
                    JsonNode propInfoN = propE.getValue();
                    JsonNode schemaN = propInfoN.get("schema");
                    if(schemaN == null) { continue; }
                    JsonNode schemaTypeN = schemaN.get("type");
                    if(schemaTypeN == null || !"array".equals(schemaTypeN.asText())) { continue; }
                    JsonNode itemsN = schemaN.get("items");
                    if(itemsN == null) { continue; }
                    JsonNode enumN = itemsN.get("enum");
                    if(enumN != null) {
                        // We have component with property of type = array of enums. Lets repair it in the component list.
                        for(ComponentList.ComponentInfo componentInfo: components.getComponents()) {
                            if(componentName.equals(componentInfo.getName())) {
                                ComponentList.PropertyInfo propInfo = componentInfo.getProperties().get(propName);
                                if(propInfo != null) {
                                    JsonSchema propSchema = propInfo.getSchema();
                                    Set<String> enumSet = new HashSet<>();
                                    for(JsonNode enumVal: enumN) {
                                        enumSet.add(enumVal.asText());
                                    }
                                    logger.debug("Repaired json enum schema of '{}'.'{}'", componentName, propName);
                                    ((StringSchema)propSchema.asArraySchema().getItems().asSingleItems().getSchema()).setEnums(enumSet);
                                }
                            }
                        }
                    }
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ComponentList.ComponentInfo componentInfo(String response) {
        return (ComponentList.ComponentInfo) Serializator.fromString(response, ComponentList.ComponentInfo.class);
    }

    public static String stringProcessStatelessInput(ProcessStatelessInput processStatelessInput) {
        return Serializator.intoString(processStatelessInput);
    }

    public static OutputColumns outputColumnsOutput(String response) {
        return Serializator.fromString(response, OutputColumns.class);
    }

    public static ProcessStatelessOutput processStatelessOutput(String response) {
        return (ProcessStatelessOutput) Serializator.fromString(response, ProcessStatelessOutput.class);
    }

    public static String stringCreateInput(CreateInput createInput) {
        return Serializator.intoString(createInput);
    }

    public static String stringProcessInput(ProcessInput processInput) {
        return Serializator.intoString(processInput);
    }

    public static ProcessOutput processOutput(String response) {
        return (ProcessOutput) Serializator.fromString(response, ProcessOutput.class);
    }

    public static ProcessResult processResult(String response) {
        return (ProcessResult) Serializator.fromString(response, ProcessResult.class);
    }

    private static String intoString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T fromString(String value, Class<T> type) {
        try {
            if (value instanceof String && (value == null || value.equals(""))) {
                return null;
            }

            return objectMapper.readValue(value, type);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
