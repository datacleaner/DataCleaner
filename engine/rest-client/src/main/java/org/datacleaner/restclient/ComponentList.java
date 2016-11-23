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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;

/**
 * List of component details.
 * @since 24. 07. 2015
 */
public class ComponentList {
    /**
     * Data storage class for particular component.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ComponentInfo {
        private String name = "";
        private String createURL = "";
        private JsonNode annotations;
        private Map<String, PropertyInfo> properties = new HashMap<>();
        private Boolean isEnabled = null;
        private byte[] iconData = null;

        public Map<String, PropertyInfo> getProperties() {
            return properties;
        }

        public ComponentInfo setProperties(final Map<String, PropertyInfo> properties) {
            this.properties = properties;
            return this;
        }

        public String getName() {
            return name;
        }

        public ComponentInfo setName(final String name) {
            this.name = name;
            return this;
        }

        public JsonNode getAnnotations() {
            return annotations;
        }

        public void setAnnotations(final JsonNode annotations) {
            this.annotations = annotations;
        }

        public String getCreateURL() {
            return createURL;
        }

        public ComponentInfo setCreateURL(final String createURL) {
            this.createURL = createURL;
            return this;
        }

        public byte[] getIconData() {
            return iconData;
        }

        public ComponentInfo setIconData(final byte[] iconData) {
            this.iconData = iconData;
            return this;
        }

        public Boolean isEnabled() {
            return isEnabled;
        }

        public ComponentInfo setEnabled(final Boolean enabled) {
            isEnabled = enabled;
            return this;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "name", "type", "description", "required", "inputColumn" })
    public static class PropertyInfo {
        private String name;
        /** More human-readable class name, which contains also info about generics. */
        private String classDetails;
        /** Class name that can be deserialized to Class object */
        private String className;
        private JsonSchema schema;
        private String description;
        private boolean required;
        private boolean isInputColumn;
        private JsonNode annotations;
        private JsonNode defaultValue;

        public void setIsInputColumn(final boolean inputColumn) {
            isInputColumn = inputColumn;
        }

        public boolean isInputColumn() {
            return isInputColumn;
        }

        public void setInputColumn(final boolean inputColumn) {
            isInputColumn = inputColumn;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(final boolean required) {
            this.required = required;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(final String className) {
            this.className = className;
        }

        public JsonSchema getSchema() {
            return schema;
        }

        public void setSchema(final JsonSchema schema) {
            this.schema = schema;
        }

        public String getClassDetails() {
            return classDetails;
        }

        public void setClassDetails(final String classDetails) {
            this.classDetails = classDetails;
        }

        public JsonNode getAnnotations() {
            return annotations;
        }

        public void setAnnotations(final JsonNode annotations) {
            this.annotations = annotations;
        }

        public JsonNode getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(final JsonNode defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

    private List<ComponentInfo> components = new ArrayList<>();

    public void add(final ComponentInfo componentInfo) {
        components.add(componentInfo);
    }

    public List<ComponentInfo> getComponents() {
        return components;
    }
}
