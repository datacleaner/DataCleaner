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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.*;
import org.datacleaner.api.Converter;
import org.datacleaner.components.remote.RemoteTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Since 9/1/15
 */
public class JsonSchemaConfiguredPropertyDescriptorImpl implements ConfiguredPropertyDescriptor, EnumerationProvider {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaConfiguredPropertyDescriptorImpl.class);

    private String name;
    private String description;
    private JsonSchema schema;
    private ComponentDescriptor component;
    private boolean isInputColumn;
    private boolean required;
    private boolean isArray;
    private Class baseType;
    private EnumerationValue[] enumValues;
    Map<Class<Annotation>, Annotation> annotations = new HashMap<>();

    public JsonSchemaConfiguredPropertyDescriptorImpl(String name, JsonSchema schema, boolean isInputColumn, String description, boolean required, ComponentDescriptor component, Map<Class<Annotation>, Annotation> annotations) {
        this.name = name;
        this.description = description;
        this.schema = schema;
        this.isInputColumn = isInputColumn;
        this.component = component;
        this.required = required;
        this.annotations = annotations;
        init();
    }

    private void init() {
        isArray = schema.isArraySchema();
        JsonSchema baseSchema;

        if(isArray) {
            baseSchema = ((ArraySchema)schema).getItems().asSingleItems().getSchema();
        } else {
            baseSchema = schema;
        }

        enumValues = new EnumerationValue[0]; // default
        if(baseSchema instanceof ValueTypeSchema) {
            Set<String> enums = ((ValueTypeSchema)baseSchema).getEnums();
            if(enums != null && !enums.isEmpty()) {
                enumValues = new EnumerationValue[enums.size()];
                int i = 0;
                for(String value: enums) {
                    enumValues[i++] = new EnumerationValue(value);
                }
            }
        }

        // must be called after enums are initialized
        baseType = schemaToJavaType(baseSchema);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setValue(Object component, Object value) throws IllegalArgumentException {
        if(!(component instanceof RemoteTransformer)) {
            throw new IllegalArgumentException("Cannot set remote property to non-remote transformer");
        }
        ((RemoteTransformer)component).setPropertyValue(this.getName(), value);
    }

    @Override
    public Object getValue(Object component) throws IllegalArgumentException {
        if(!(component instanceof RemoteTransformer)) {
            throw new IllegalArgumentException("Cannot set remote property to non-remote transformer");
        }
        return ((RemoteTransformer)component).getPropertyValue(this.getName());
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return new HashSet<>(annotations.values());
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return (A) annotations.get(annotationClass);
    }

    @Override
    public Class<?> getType() {
        if(isArray()) {
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
    public ComponentDescriptor<?> getComponentDescriptor() {
        return component;
    }

    @Override
    public int getTypeArgumentCount() {
        return 0;
    }

    @Override
    public Class<?> getTypeArgument(int i) throws IndexOutOfBoundsException {
        return null;
    }

    @Override
    public int compareTo(PropertyDescriptor o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public boolean isInputColumn() {
        return isInputColumn;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public Class<? extends Converter<?>> getCustomConverter() {
        return null;
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    private Class<?> schemaToJavaType(JsonSchema schema) {
        // try to convert
        if(isEnum()) {
            return EnumerationValue.class;
        }
        if(schema instanceof StringSchema) { return String.class; }
        if(schema instanceof IntegerSchema) { return Integer.class; }
        if(schema instanceof BooleanSchema) { return Boolean.class; }
        if(schema instanceof NumberSchema) { return Double.class; }
        // fallback
        return JsonNode.class;
    }

    public boolean isEnum() {
        return enumValues != null && enumValues.length > 0;
    }

    public EnumerationValue[] getEnumValues() {
        return enumValues;
    }

    @Override
    public EnumerationValue[] values() {
        return enumValues;
    }

}
