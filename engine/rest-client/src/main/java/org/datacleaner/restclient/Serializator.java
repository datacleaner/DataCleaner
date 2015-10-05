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
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.metamodel.util.HasName;
import org.datacleaner.api.InputColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;

/**
 * Class for input/output data types conversion from/into String.
 * @since 11. 09. 2015
 */
public class Serializator {
    private static final Logger logger = LoggerFactory.getLogger(Serializator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        SimpleModule myModule = new SimpleModule("RemoteTransformersModule", new Version(1, 0, 0, null, "org.datacleaner", "DataCleaner-remote-transformers"));
        // our custom serializers
        myModule.addSerializer(new MyInputColumnSerializer());
        myModule.addSerializer(new MyEnumSerializer());
        objectMapper.registerModule(myModule);
    }

    public static ObjectMapper getJacksonObjectMapper() {
        return objectMapper;
    }

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

    /**
     * When calling the REST API, we want the input columns to be specified only by its name in the JSON payload.
     */
    private static class MyInputColumnSerializer extends StdSerializer<InputColumn> {
        protected MyInputColumnSerializer() {
            super(InputColumn.class);
        }
        @Override
        public void serialize(InputColumn value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getName());
        }
    }

    /** Custom enum serializer. Serializes as the default one (using the enum name()), but
     * generates customized Json schema. In this schema, the enum constants are provided
     * and we provide a concatenatin of enum name() and a human-readable form for the enumeration
     * (via interface {@link HasName} or toString()).
     * <p>The DataCleaner Desktop then interprets it and uses the name() part for json queries and the
     * second part for GUI.
     * <p>An example of enumeration json schema:
     * <pre>
     * {
     *   "type": "string",
     *   "enum": [
     *     "TRUE_FALSE::True or false",
     *     "INPUT_OR_NULL::Corrected value or null"
     *   ]
     * }</pre>
     */
    private static class MyEnumSerializer extends StdSerializer<Enum> {
        protected MyEnumSerializer() {
            super(Enum.class);
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            ObjectNode objectNode = createSchemaNode("string", true);
            if (typeHint != null) {
                JavaType type = provider.constructType(typeHint);
                if (type.isEnumType()) {
                    ArrayNode enumNode = objectNode.putArray("enum");
                    for (Object value : type.getRawClass().getEnumConstants()) {
                        enumNode.add(enumValueToSchemaString((Enum) value));
                    }
                }
            }
            return objectNode;
        }

        @Override
        public void serialize(Enum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.name());
        }

        @Override
        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
            JsonStringFormatVisitor stringVisitor = visitor.expectStringFormat(typeHint);
            if (typeHint != null && stringVisitor != null) {
                if (typeHint.isEnumType()) {
                    Set<String> enums = new LinkedHashSet<String>();
                    for (Object value : typeHint.getRawClass().getEnumConstants()) {
                        enums.add(enumValueToSchemaString((Enum) value));
                    }
                    stringVisitor.enumTypes(enums);
                }
            }
        }

        protected String enumValueToSchemaString(Enum value) {
            if(value instanceof  HasName) {
                return value.name() + "::" + ((HasName)value).getName();
            } else {
                return value.name() + "::" + String.valueOf(value);
            }
        }
    }
}
