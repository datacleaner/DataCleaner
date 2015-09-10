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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.IntegerSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;
import org.datacleaner.api.Converter;
import org.datacleaner.components.remote.RemoteTransformer;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/**
 * @Since 9/1/15
 */
public class JsonSchemaConfiguredPropertyDescriptorImpl implements ConfiguredPropertyDescriptor {
    
    private String name;
    private String description;
    private JsonSchema schema;
    private ComponentDescriptor component;
    private boolean isInputColumn;

    public JsonSchemaConfiguredPropertyDescriptorImpl(String name, JsonSchema schema, boolean isInputColumn, String description) {
        this.name = name;
        this.description = description;
        this.schema = schema;
        this.isInputColumn = isInputColumn;
    }

    public void setComponent(ComponentDescriptor component) {
        this.component = component;
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
        return ((RemoteTransformer)component).getProperty(this.getName());
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return Collections.emptySet();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return null;
    }

    @Override
    public Class<?> getType() {
        return schemaToJavaType(schema);
    }

    @Override
    public boolean isArray() {
        return schema.isArraySchema();
    }

    @Override
    public Class<?> getBaseType() {
        if(schema.isArraySchema()) {
            return schemaToJavaType(((ArraySchema)schema).getItems().asSingleItems().getSchema());
        } else {
            return schemaToJavaType(schema);
        }
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
        return false;
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
        if(schema instanceof StringSchema) { return String.class; }
        if(schema instanceof IntegerSchema) { return Integer.class; }
        // fallback
        return JsonNode.class;
    }
}
