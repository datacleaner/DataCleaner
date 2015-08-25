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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.datacleaner.api.WSPrivateProperty;
import org.datacleaner.descriptors.AbstractPropertyDescriptor;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * List of component details.
 * @since 24. 07. 2015
 */
public class ComponentList {

    private List<ComponentInfo> components = new ArrayList<>();

    public void add(String tenant, ComponentDescriptor descriptor) {
        components.add(createComponentInfo(tenant, descriptor));
    }

    public ComponentInfo createComponentInfo(String tenant, ComponentDescriptor descriptor) {
        return new ComponentInfo()
                .setName(descriptor.getDisplayName())
                .setDescription(descriptor.getDescription())
                .setCreateURL(getURLForCreation(tenant, descriptor))
                .setProperties(createPropertiesInfo(descriptor));
    }

    public List<ComponentInfo> getComponents() {
        return components;
    }

    private String getURLForCreation(String tenant, ComponentDescriptor descriptor) {
        try {
            return String.format(
                    "/repository/%s/components/%s",
                    UriUtils.encodePathSegment(tenant, "UTF8"),
                    UriUtils.encodePathSegment(descriptor.getDisplayName().replace("/", "_@_"), "UTF8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, PropertyInfo> createPropertiesInfo(ComponentDescriptor descriptor) {
        Map<String, PropertyInfo> result = new HashMap<>();
        for (ConfiguredPropertyDescriptor propertyDescriptor : (Set<ConfiguredPropertyDescriptor>) descriptor.getConfiguredProperties()) {
            if (propertyDescriptor.getAnnotation(WSPrivateProperty.class) != null) {
                continue;
            }


            PropertyInfo propInfo = new PropertyInfo();
            propInfo.setName(propertyDescriptor.getName());
            propInfo.setDescription(propertyDescriptor.getDescription());
            propInfo.setRequired(propertyDescriptor.isRequired());
            propInfo.setIsInputColumn(propertyDescriptor.isInputColumn());
            propInfo.setType(getPropertyType(descriptor, propertyDescriptor));
            if(propertyDescriptor.getType().isEnum()) {
                propInfo.setEnumValues(toStringArray(propertyDescriptor.getType().getEnumConstants()));
            }
            result.put(propInfo.getName(), propInfo);
        }
        return result;
    }

    private String[] toStringArray(Object[] array) {
        String[] result = new String[array.length];
        for(int i = 0; i < array.length; i++) {
            result[i] = String.valueOf(array[i]);
        }
        return result;
    }

    private String getPropertyType(ComponentDescriptor descriptor, ConfiguredPropertyDescriptor propertyDescriptor) {
        // TODO: move the "getField" to ComponentDescriptor interface to avoid retyping
        if(propertyDescriptor instanceof AbstractPropertyDescriptor) {
            Field f = ((AbstractPropertyDescriptor)propertyDescriptor).getField();
            return f.getGenericType().getTypeName();
        } else {
            return propertyDescriptor.getType().getCanonicalName();
        }
    }

    /**
     * Data storage class for particular component.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ComponentInfo {

        private String name = "";
        private String description = "";
        private String createURL = "";
        private Map<String, PropertyInfo> properties = new HashMap<>();

        public ComponentInfo setProperties(Map<String, PropertyInfo> properties) {
            this.properties = properties;
            return this;
        }

        public Map<String, PropertyInfo> getProperties() {
            return properties;
        }

        public String getName() {
            return name;
        }

        public ComponentInfo setName(String name) {
            this.name = name;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public ComponentInfo setDescription(String description) {
            this.description = description;
            return this;
        }

        public String getCreateURL() {
            return createURL;
        }

        public ComponentInfo setCreateURL(String createURL) {
            this.createURL = createURL;
            return this;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "name", "type", "description", "required", "inputColumn", "enumValues" })
    public static class PropertyInfo {
        private String name;
        private String type;
        private String description;
        private boolean required;
        private boolean isInputColumn;
        private String[] enumValues;

        public void setIsInputColumn(boolean inputColumn) {
            isInputColumn = inputColumn;
        }

        public boolean isInputColumn() {
            return isInputColumn;
        }

        public void setInputColumn(boolean inputColumn) {
            isInputColumn = inputColumn;
        }

        public void setName(String name) {
            this.name = name;
        }

        // We don't need name in the property info, since it is used a a key in the properties map.
        @JsonIgnore
        public String getName() {
            return name;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public boolean isRequired() {
            return required;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String[] getEnumValues() {
            return enumValues;
        }

        public void setEnumValues(String[] enumValues) {
            this.enumValues = enumValues;
        }
    }

}
