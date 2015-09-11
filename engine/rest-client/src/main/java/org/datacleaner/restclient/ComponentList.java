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
