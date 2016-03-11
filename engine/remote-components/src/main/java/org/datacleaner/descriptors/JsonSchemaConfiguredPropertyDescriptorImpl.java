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
package org.datacleaner.descriptors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.datacleaner.api.Converter;
import org.datacleaner.restclient.Serializator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.BooleanSchema;
import com.fasterxml.jackson.module.jsonSchema.types.IntegerSchema;
import com.fasterxml.jackson.module.jsonSchema.types.NumberSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema;

/**
 * Property descriptor for properties of RemoteTransformer, which has no
 * appropriate class on the client side. (Class is available only on server, but
 * is not part of standard DataCleaner installation). The data type of the
 * property is represented by Json Schema. Special care was taken to support
 * enumerations.
 *
 * @Since 9/1/15
 */
public class JsonSchemaConfiguredPropertyDescriptorImpl extends RemoteConfiguredPropertyDescriptor
        implements EnumerationProvider {

    private static final long serialVersionUID = 1L;

    private final JsonSchema schema;
    private final boolean isInputColumn;
    private boolean isArray;
    private Class<?> baseType;
    private EnumerationValue[] enumValues;

    public JsonSchemaConfiguredPropertyDescriptorImpl(String name, JsonSchema schema, boolean isInputColumn,
            String description, boolean required, ComponentDescriptor<?> component,
            Map<Class<? extends Annotation>, Annotation> annotations, JsonNode defaultValue) {
        super(name, description, required, component, annotations, defaultValue);
        this.schema = schema;
        this.isInputColumn = isInputColumn;
        init();
    }

    private void init() {
        isArray = schema.isArraySchema();
        JsonSchema baseSchema;

        if (isArray) {
            baseSchema = ((ArraySchema) schema).getItems().asSingleItems().getSchema();
        } else {
            baseSchema = schema;
        }

        enumValues = new EnumerationValue[0]; // default
        if (baseSchema instanceof ValueTypeSchema) {
            Set<String> enums = ((ValueTypeSchema) baseSchema).getEnums();
            if (enums != null && !enums.isEmpty()) {
                enumValues = new EnumerationValue[enums.size()];
                int i = 0;
                for (String value : enums) {

                    String enumValue, enumName;
                    String[] enumAliases;
                    String[] tokens = value.split(Serializator.ENUM_ALIAS_SEPARATOR);
                    if(tokens.length == 0) {
                        continue;
                    }
                    if(tokens.length == 1) {
                        enumValue = tokens[0];
                        enumName = tokens[0];
                        enumAliases = new String[0];
                    } else {
                        enumValue = tokens[0];
                        enumName = tokens[1];
                        enumAliases = Arrays.copyOfRange(tokens, 2, tokens.length);
                    }
                    enumValues[i++] = new EnumerationValue(enumValue, enumName, enumAliases);
                }
                Arrays.sort(enumValues);
            }
        }

        // must be called after enums are initialized
        baseType = schemaToJavaType(baseSchema);
    }

    @Override
    public Class<?> getType() {
        if (isArray()) {
            return Array.newInstance(getBaseType(), 0).getClass();
        }
        return baseType;
    }

    @Override
    public boolean isArray() {
        return isArray;
    }

    @Override
    public Class<?> getBaseType() {
        return baseType;
    }

    @Override
    public boolean isInputColumn() {
        return isInputColumn;
    }

    private Class<?> schemaToJavaType(JsonSchema schema) {
        // try to convert
        if (isEnum()) {
            return EnumerationValue.class;
        }
        if (schema instanceof StringSchema) {
            return String.class;
        }
        if (schema instanceof IntegerSchema) {
            return Integer.class;
        }
        if (schema instanceof BooleanSchema) {
            return Boolean.class;
        }
        if (schema instanceof NumberSchema) {
            return Double.class;
        }
        // fallback
        return JsonNode.class;
    }

    public boolean isEnum() {
        return enumValues != null && enumValues.length > 0;
    }

    @Override
    public EnumerationValue[] values() {
        return enumValues;
    }

    @Override
    public EnumerationValue forString(String value) {
        if(enumValues == null) {
            return null;
        }
        for(EnumerationValue candidate: enumValues) {
            if(value.equals(candidate.getValue()) || value.equals(candidate.getName())) {
                return candidate;
            }
            for(String alias: candidate.getAliases()) {
                if(value.equals(alias)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    @Override
    public Converter<?> createCustomConverter() {
        return isEnum() ? new EnumerationValueConverter() : null;
    }

    private class EnumerationValueConverter implements Converter<Object> {

        @Override
        public Object fromString(Class<?> type, String serializedForm) {
            for(EnumerationValue valueCandidate: enumValues) {
                if(valueCandidate.getValue().equals(serializedForm)) {
                    return valueCandidate;
                } else if(valueCandidate.getName().equals(serializedForm)) {
                    return valueCandidate;
                } else {
                    for(String alias: valueCandidate.getAliases()) {
                        if(alias.equals(serializedForm)) {
                            return valueCandidate;
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public String toString(Object instance) {
            if(instance == null) {
                return null;
            }
            if(instance instanceof EnumerationValue) {
                return ((EnumerationValue)instance).getValue();
            }
            if(instance instanceof Enum) {
                return ((Enum<?>)instance).name();
            }
            throw new IllegalArgumentException("Cannot serialize value of type " + instance.getClass());
        }

        @Override
        public boolean isConvertable(Class<?> type) {
            return type.isAssignableFrom(EnumerationValue.class) && isEnum();
        }
    }
}
