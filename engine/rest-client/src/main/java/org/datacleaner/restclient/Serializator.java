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
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.metamodel.util.HasName;
import org.datacleaner.api.ComponentScope;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.ShortNews;
import org.datacleaner.util.HasAliases;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Class for input/output data types conversion from/into String.
 * @since 11. 09. 2015
 */
public class Serializator {
    @JsonDeserialize(using = ComponentScopeServiceTypeDeserializer.class)
    interface ComponentScopeServiceTypeMixin {
    }
    @JsonDeserialize(using = ComponentScopeEntityTypeDeserializer.class)
    interface ComponentScopeEntityTypeMixin {
    }

    /**
     * When calling the REST API, we want the input columns to be specified only by its name in the JSON payload.
     */
    @SuppressWarnings("rawtypes")
    private static class MyInputColumnSerializer extends StdSerializer<InputColumn> {
        protected MyInputColumnSerializer() {
            super(InputColumn.class);
        }

        @Override
        public void serialize(final InputColumn value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
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
    @SuppressWarnings("rawtypes")
    private static class MyEnumSerializer extends StdSerializer<Enum> {
        protected MyEnumSerializer() {
            super(Enum.class);
        }

        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            final ObjectNode objectNode = createSchemaNode("string", true);
            if (typeHint != null) {
                final JavaType type = provider.constructType(typeHint);
                if (type.isEnumType()) {
                    final ArrayNode enumNode = objectNode.putArray("enum");
                    for (final Object value : type.getRawClass().getEnumConstants()) {
                        enumNode.add(enumValueToSchemaString((Enum) value));
                    }
                }
            }
            return objectNode;
        }

        @Override
        public void serialize(final Enum value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
            gen.writeString(value.name());
        }

        @Override
        public void acceptJsonFormatVisitor(final JsonFormatVisitorWrapper visitor, final JavaType typeHint)
                throws JsonMappingException {
            final JsonStringFormatVisitor stringVisitor = visitor.expectStringFormat(typeHint);
            if (typeHint != null && stringVisitor != null) {
                if (typeHint.isEnumType()) {
                    final Set<String> enums = new LinkedHashSet<String>();
                    for (final Object value : typeHint.getRawClass().getEnumConstants()) {
                        enums.add(enumValueToSchemaString((Enum) value));
                    }
                    stringVisitor.enumTypes(enums);
                }
            }
        }

        protected String enumValueToSchemaString(final Enum<?> value) {
            final String enumValue = value.name();
            final String enumName;
            String[] aliases = null;
            if (value instanceof HasName) {
                enumName = ((HasName) value).getName();
            } else {
                enumName = String.valueOf(value);
            }
            if (value instanceof HasAliases) {
                aliases = ((HasAliases) value).getAliases();
            }
            return Serializator.enumValueToSchemaString(enumValue, enumName, aliases);
        }
    }

    private static final class ComponentScopeServiceTypeDeserializer
            extends JsonDeserializer<ComponentScope.ServiceType> {
        @Override
        public ComponentScope.ServiceType deserialize(final JsonParser jsonParser,
                final DeserializationContext deserializationContext)
                throws IOException {
            final String jsonParserText = jsonParser.getText();
            for (final ComponentScope.ServiceType value : ComponentScope.ServiceType.values()) {
                if (value.name().equals(jsonParserText)) {
                    return value;
                }
            }
            return null;
        }
    }

    private static final class ComponentScopeEntityTypeDeserializer
            extends JsonDeserializer<ComponentScope.EntityType> {
        @Override
        public ComponentScope.EntityType deserialize(final JsonParser jsonParser,
                final DeserializationContext deserializationContext)
                throws IOException {
            final String jsonParserText = jsonParser.getText();
            for (final ComponentScope.EntityType value : ComponentScope.EntityType.values()) {
                if (value.name().equals(jsonParserText)) {
                    return value;
                }
            }
            return null;
        }
    }
    public static final String ENUM_ALIAS_SEPARATOR = "::";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        final SimpleModule myModule = new SimpleModule("RemoteTransformersModule",
                new Version(1, 0, 0, null, "org.datacleaner", "DataCleaner-remote-transformers"));
        // our custom serializers
        myModule.addSerializer(new MyInputColumnSerializer());
        myModule.addSerializer(new MyEnumSerializer());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(myModule);

        //For better deserialize of enumeration in ComponentScope annotation. - We will ignore unknown values.
        objectMapper.registerModule(new SimpleModule() {
            @Override
            public void setupModule(final SetupContext context) {
                context.setMixInAnnotations(ComponentScope.ServiceType.class, ComponentScopeServiceTypeMixin.class);
                context.setMixInAnnotations(ComponentScope.EntityType.class, ComponentScopeEntityTypeMixin.class);
            }
        });
    }

    public static ObjectMapper getJacksonObjectMapper() {
        return objectMapper;
    }

    public static ShortNews shortNewsList(final String response) {
        return Serializator.fromString(response, ShortNews.class);
    }

    public static ComponentList componentList(final String response) {
        final ComponentList components = Serializator.fromString(response, ComponentList.class);
        return components;
    }

    public static ComponentList.ComponentInfo componentInfo(final String response) {
        return (ComponentList.ComponentInfo) Serializator.fromString(response, ComponentList.ComponentInfo.class);
    }

    public static String stringProcessStatelessInput(final ProcessStatelessInput processStatelessInput) {
        return Serializator.intoString(processStatelessInput);
    }

    public static OutputColumns outputColumnsOutput(final String response) {
        return Serializator.fromString(response, OutputColumns.class);
    }

    public static ProcessStatelessOutput processStatelessOutput(final String response) {
        return (ProcessStatelessOutput) Serializator.fromString(response, ProcessStatelessOutput.class);
    }

    public static String stringCreateInput(final CreateInput createInput) {
        return Serializator.intoString(createInput);
    }

    public static String stringProcessInput(final ProcessInput processInput) {
        return Serializator.intoString(processInput);
    }

    public static ProcessOutput processOutput(final String response) {
        return (ProcessOutput) Serializator.fromString(response, ProcessOutput.class);
    }

    public static ProcessResult processResult(final String response) {
        return (ProcessResult) Serializator.fromString(response, ProcessResult.class);
    }

    public static DataCloudUser processDataCloudUser(final String response) {
        return (DataCloudUser) Serializator.fromString(response, DataCloudUser.class);
    }

    private static String intoString(final Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T fromString(final String value, final Class<T> type) {
        try {
            if (value == null || value.equals("")) {
                return null;
            }

            return objectMapper.readValue(value, type);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String enumValueToSchemaString(final String enumValue, final String enumName, final String[] aliases) {
        final StringBuilder serialized = new StringBuilder();
        serialized.append(enumValue);
        serialized.append(ENUM_ALIAS_SEPARATOR);
        serialized.append(enumName);
        if (aliases != null && aliases.length > 0) {
            for (final String alias : aliases) {
                if (alias != null && !alias.isEmpty()) {
                    serialized.append(ENUM_ALIAS_SEPARATOR).append(alias);
                }
            }
        }
        return serialized.toString();
    }
}
