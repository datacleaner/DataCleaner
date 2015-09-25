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
package org.datacleaner.monitor.configuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.datacleaner.api.Component;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.PropertyDescriptor;
import org.datacleaner.job.ComponentConfiguration;
import org.datacleaner.job.ImmutableComponentConfiguration;
import org.datacleaner.lifecycle.AssignConfiguredPropertiesHelper;
import org.datacleaner.util.convert.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Class RemoteComponentsConfigurationImpl
 * 
 */
public class RemoteComponentsConfigurationImpl implements RemoteComponentsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteComponentsConfigurationImpl.class);

    /**
     * included components have priority
     */
    private Set<String> includeSet = new HashSet<>();
    private Set<String> excludeSet = new HashSet<>();

    // Key is component name a value is list of values
    private Map<String, List<Property>> properties = new HashMap<>();

    public RemoteComponentsConfigurationImpl() {
    }

    public RemoteComponentsConfigurationImpl(Set<String> includeSet, Set<String> excludeSet,
            Map<String, List<Property>> properties) {
        this.includeSet = includeSet;
        this.excludeSet = excludeSet;
        this.properties = properties;
    }

    @Override
    public boolean isAllowed(String componentDisplayName) {
        if (includeSet.isEmpty() && excludeSet.isEmpty()) {
            return true;
        }
        if (includeSet.isEmpty()) {
            if (excludeSet.contains(componentDisplayName)) {
                return false;
            }
            return true;
        } else {
            if (includeSet.contains(componentDisplayName)) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean isAllowed(ComponentDescriptor componentDescriptor) {
        return isAllowed(componentDescriptor.getDisplayName());
    }

    @Override
    public void setDefaultValues(ComponentDescriptor componentDescriptor, Component component) {
        List<Property> defaultProperties = properties.get(componentDescriptor.getDisplayName());
        if (defaultProperties == null || !isAllowed(componentDescriptor)) {
            return;
        }
        Map<PropertyDescriptor, Object> configuredProperties = new HashMap<>();
        for (Property defaultProperty : defaultProperties) {
            ConfiguredPropertyDescriptor propDesc = componentDescriptor
                    .getConfiguredProperty(defaultProperty.getName());
            if (propDesc == null) {
                LOGGER.warn("Component '{}' does not contain property '{}'.", componentDescriptor.getDisplayName(),
                        defaultProperty.getName());
                continue;
            }
            Class type = propDesc.getType();
            String inputValue = defaultProperty.getValue();
            Object objectProperty;
            if (defaultProperty.isSimpleString()) {
                objectProperty = StringConverter.simpleInstance().deserialize(inputValue, type);
            } else {
                // Value is XML
                if (defaultProperty.getDeserialObject() == null) {
                    try {
                        JAXBContext context = JAXBContext.newInstance(type);
                        Unmarshaller unmarshaller = context.createUnmarshaller();
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        dbFactory.setNamespaceAware(false);
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.parse(IOUtils.toInputStream(defaultProperty.getValue(), "UTF-8"));
                        doc.normalizeDocument();
                        objectProperty = unmarshaller.unmarshal(doc, type).getValue();
                        defaultProperty.setDeserialObject(objectProperty);
                    } catch (Exception e) {
                        LOGGER.warn("Problem with parsing '{}' property. Input value: {}", defaultProperty.getName(),
                                inputValue);
                        continue;
                    }
                } else {
                    objectProperty = defaultProperty.getDeserialObject();
                }
            }
            configuredProperties.put(propDesc, objectProperty);
        }
        ComponentConfiguration componentConfiguration = new ImmutableComponentConfiguration(configuredProperties);
        final AssignConfiguredPropertiesHelper helper = new AssignConfiguredPropertiesHelper();
        helper.assignProperties(component, componentDescriptor, componentConfiguration, true);
    }

    public static class Property {
        private String name;
        private String value;
        private boolean isSimpleString;
        private Object deserialObject = null;

        public Property(String name, String value, boolean simpleString) {
            this.name = name;
            this.isSimpleString = simpleString;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public boolean isSimpleString() {
            return isSimpleString;
        }

        public Object getDeserialObject() {
            return deserialObject;
        }

        public void setDeserialObject(Object deserialObject) {
            this.deserialObject = deserialObject;
        }
    }
}
