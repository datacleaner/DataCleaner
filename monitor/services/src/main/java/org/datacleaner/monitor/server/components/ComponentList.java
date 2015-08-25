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
import org.datacleaner.api.WSPrivateProperty;
import org.datacleaner.api.WSStatelessComponent;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.monitor.configuration.ComponentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * List of component details.
 * @since 24. 07. 2015
 */
public class ComponentList {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentList.class);
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
        if (descriptor.getAnnotation(WSStatelessComponent.class) == null) {
            LOGGER.info("Component {} is skipped (not annotated by {})",
                    descriptor.getDisplayName(), WSStatelessComponent.class.toString());
            return;
        }

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
        String name = descriptor.getDisplayName();

        try {
            name = URLEncoder.encode(name, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            LOGGER.warn(e.getMessage());
        }

        return String.format("POST /repository/%s/components/%s", tenant, name);
    }

    private void fillPropertyListAndComponentConfiguration() {
        propertyList = new ArrayList<>();
        componentConfiguration = new ComponentConfiguration();
        JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(false);
        JsonNode valuePlaceholder = jsonNodeFactory.textNode("<VALUE_PLACEHOLDER>");

        for (ConfiguredPropertyDescriptor propertyDescriptor : (Set<ConfiguredPropertyDescriptor>) descriptor.getConfiguredProperties()) {
            if (propertyDescriptor.getAnnotation(WSPrivateProperty.class) != null) {
                continue;
            }

            if (propertyDescriptor.isInputColumn()) {
                // TODO: Do we really want to provide info about expected columns? For some cases could work,
                // e.g. when transformer has simple InputColumn properties.
                // But when it has e.g. InputColumn array? Or Mapped property? What to provide here?
                // componentConfiguration.getColumns().add(propertyDescriptor.getName());
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
