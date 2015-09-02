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
import org.datacleaner.api.Converter;
import org.datacleaner.components.remote.RemoteTransformer;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/**
 * @Since 9/1/15
 */
public class RemoteConfiguredPropertyDescriptorImpl implements ConfiguredPropertyDescriptor {
    
    private String name;
    private JsonSchema schema;
    private ComponentDescriptor component;
    private boolean isInputColumn;

    public RemoteConfiguredPropertyDescriptorImpl(String name, JsonSchema schema) {
        this.name = name;
        this.schema = schema;
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
        ((RemoteTransformer)component).setPropertyValue(this, value);
    }

    @Override
    public Object getValue(Object component) throws IllegalArgumentException {
        if(!(component instanceof RemoteTransformer)) {
            throw new IllegalArgumentException("Cannot set remote property to non-remote transformer");
        }
        return ((RemoteTransformer)component).getProperty(this);
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
        return JsonNode.class;
    }

    @Override
    public boolean isArray() {
        return schema.isArraySchema();
    }

    @Override
    public Class<?> getBaseType() {
        return JsonNode.class;
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
        return 0;
    }

    @Override
    public boolean isInputColumn() {
        return isInputColumn;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getDescription() {
        return "DESCRIPTION";
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
}
