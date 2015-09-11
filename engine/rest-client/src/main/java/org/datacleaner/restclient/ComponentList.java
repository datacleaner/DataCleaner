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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;

/**
 * List of component details.
 * @since 24. 07. 2015
 */
public class ComponentList {

    private List<ComponentInfo> components = new ArrayList<>();

    public void add(ComponentInfo componentInfo) {
        components.add(componentInfo);
    }

    public List<ComponentInfo> getComponents() {
        return components;
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
    @JsonPropertyOrder({ "name", "type", "description", "required", "inputColumn"})
    public static class PropertyInfo {
        private String name;
        /** More human-readable class name, which contains also info about generics. */
        private String classDetails;
        /** Class name that can can be deserialized to Class object */
        private String className;
        private JsonSchema schema;
        private String description;
        private boolean required;
        private boolean isInputColumn;

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

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public JsonSchema getSchema() {
            return schema;
        }

        public void setSchema(JsonSchema schema) {
            this.schema = schema;
        }

        public String getClassDetails() {
            return classDetails;
        }

        public void setClassDetails(String classDetails) {
            this.classDetails = classDetails;
        }
    }

}
