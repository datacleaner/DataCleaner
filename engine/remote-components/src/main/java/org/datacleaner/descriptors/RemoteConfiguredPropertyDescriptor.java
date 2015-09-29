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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.datacleaner.api.Converter;
import org.datacleaner.api.InputColumn;
import org.datacleaner.components.remote.RemoteTransformer;
import org.datacleaner.restclient.Serializator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A Base class for property descriptors of remote transformers, implementing common functions. See the child classes for details.
 *
 * @Since 25.9.15
 */
public abstract class RemoteConfiguredPropertyDescriptor implements ConfiguredPropertyDescriptor {

    private static final Logger logger = LoggerFactory.getLogger(RemoteConfiguredPropertyDescriptor.class);

    private final String name;
    private final String description;
    private final boolean required;
    private final ComponentDescriptor component;
    private final Map<Class<Annotation>, Annotation> annotations;
    private final JsonNode defaultValue;

    RemoteConfiguredPropertyDescriptor(String name, String description, boolean required, ComponentDescriptor component, Map<Class<Annotation>, Annotation> annotations, JsonNode defaultValue) {
        this.name = name;
        this.annotations = annotations;
        this.defaultValue = defaultValue;
        this.description = description;
        this.required = required;
        this.component = component;
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
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setValue(Object component, Object value) throws IllegalArgumentException {
        ((RemoteTransformer)component).setPropertyValue(getName(), value);
    }

    public void setDefaultValue(Object component) {
        if(defaultValue != null) {
            ((RemoteTransformer) component).setPropertyValue(getName(), createDefaultValue());
        }
    }

    private Object createDefaultValue() {
        // TODO: this is code duplicate with ComponentHandler.convertPropertyValue
        // (which is used to deserialize properties values on the server side).
        // TODO: But on server side a StringConverter is used for string values,
        // which is not fully available on client
        // side (some extenstions providing custom converters may be not available on client classpath).
        // We must unify how values are serialized on client as well as server side. Maybe use pure JSON string?
        // Maybe it is enough to support JsonNode in StringConverter? That should fit the unknown values...

        try {
            return Serializator.getJacksonObjectMapper().treeToValue(defaultValue, getType());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isInputColumn() {
        return InputColumn.class.isAssignableFrom(getBaseType());
    }


    @Override
    public Object getValue(Object component) throws IllegalArgumentException {
        return ((RemoteTransformer)component).getPropertyValue(getName());
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

}
