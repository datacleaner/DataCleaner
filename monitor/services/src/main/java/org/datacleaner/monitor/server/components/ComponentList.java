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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.monitor.configuration.ComponentConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * List of component details.
 * @author j.horcicka (GMC)
 * @since 24. 07. 2015
 */
public class ComponentList {
    private List<ComponentInfo> components = new ArrayList<>();
    @JsonIgnore
    private String tenant;
    @JsonIgnore
    private ComponentDescriptor descriptor;
    @JsonIgnore
    private List<String[]> propertyList;
    @JsonIgnore
    private ComponentConfiguration componentConfiguration;

    public void add(String tenant, ComponentDescriptor descriptor) {
        this.tenant = tenant;
        this.descriptor = descriptor;
        fillPropertyListAndComponentConfiguration();
        ComponentInfo componentInfo = new ComponentInfo()
                .setName(descriptor.getDisplayName())
                .setDescription(descriptor.getDescription())
                .setCreateURL(getURLForCreation())
                .setPropertyList(propertyList)
                .setConfiguration(componentConfiguration);
        components.add(componentInfo);
    }

    public List<ComponentInfo> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentInfo> components) {
        this.components = components;
    }

    private String getURLForCreation() {
        String name = descriptor.getDisplayName().replace("/", "%2F");
        String url = String.format("POST /repository/%s/components/%s", tenant, name);

        return url;
    }

    private void fillPropertyListAndComponentConfiguration() {
        propertyList = new ArrayList<>();
        componentConfiguration = new ComponentConfiguration();
        JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(false);
        JsonNode valuePlaceholder = jsonNodeFactory.textNode("<VALUE_PLACEHOLDER>");

        for (ConfiguredPropertyDescriptor propertyDescriptor : (Set<ConfiguredPropertyDescriptor>) descriptor.getConfiguredProperties()) {
            if (propertyDescriptor.isInputColumn()) {
                componentConfiguration.getColumns().add(propertyDescriptor.getName());
            }

            propertyList.add(new String[]{
                    propertyDescriptor.getName(),
                    (propertyDescriptor.getDescription() == null) ?
                            "" : propertyDescriptor.getDescription(),
                    propertyDescriptor.isRequired() ?
                            ComponentInfo.PROPERTY_IS_REQUIRED : ComponentInfo.PROPERTY_IS_NOT_REQUIRED,
            });

            componentConfiguration.getProperties().put(propertyDescriptor.getName(), valuePlaceholder);
        }
    }

    /**
     * Data storage class for particular component.
     */
    static class ComponentInfo {
        public static final String PROPERTY_IS_REQUIRED = "required";
        public static final String PROPERTY_IS_NOT_REQUIRED = "not required";

        private String name = "";
        private String description = "";
        private String createURL = "";
        private List<String[]> propertyList = null;
        private ComponentConfiguration configuration = null;

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

        public List<String[]> getPropertyList() {
            return propertyList;
        }

        public ComponentInfo setPropertyList(List<String[]> propertyList) {
            this.propertyList = propertyList;
            return this;
        }

        public ComponentConfiguration getConfiguration() {
            return configuration;
        }

        public ComponentInfo setConfiguration(ComponentConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }
    }
}
