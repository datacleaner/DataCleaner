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

import org.datacleaner.api.Converter;
import org.datacleaner.api.InputColumn;
import org.datacleaner.components.remote.RemoteTransformer;
import org.datacleaner.restclient.Serializator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Representation of configured property of remote transformer, in case that the property value class is available on
 * the client side like on the server. For example property of type String.class can be represented by a String.class
 * on server as well as on the client. But if server has some exotic class for property values, that is not
 * available on client classpath, such a property must be represented by {@link JsonSchemaConfiguredPropertyDescriptorImpl}.
 *
 * @see RemoteTransformer
 * @Since 9/9/15
 */
public class TypeBasedConfiguredPropertyDescriptorImpl extends RemoteConfiguredPropertyDescriptor {

    private static final Logger logger = LoggerFactory.getLogger(TypeBasedConfiguredPropertyDescriptorImpl.class);
    private Class type;

    public TypeBasedConfiguredPropertyDescriptorImpl(String name, String description, Class type, boolean required, ComponentDescriptor component, Map<Class<Annotation>, Annotation> annotations, JsonNode defaultValue) {
        super(name, description, required, component, annotations, defaultValue);
        this.type = type;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public boolean isArray() {
        return type.isArray();
    }

    @Override
    public Class<?> getBaseType() {
        if (type.isArray()) {
            return type.getComponentType();
        }
        return type;
    }

}
