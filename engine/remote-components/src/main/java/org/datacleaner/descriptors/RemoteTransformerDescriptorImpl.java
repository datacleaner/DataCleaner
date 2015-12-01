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

import java.util.HashSet;
import java.util.Set;

import org.datacleaner.api.ComponentCategory;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.components.categories.TransformSuperCategory;
import org.datacleaner.components.remote.RemoteTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transformer descriptor that represents a remote transformer sitting on a
 * DataCleaner Monitor server. This descriptor is created by
 * {@link RemoteDescriptorProvider} when it downloads a transformers list from
 * the server.
 * 
 * @Since 9/1/15
 */
public class RemoteTransformerDescriptorImpl extends SimpleComponentDescriptor implements TransformerDescriptor,
        HasIcon {
    private static final Logger logger = LoggerFactory.getLogger(RemoteTransformerDescriptorImpl.class);
    private String remoteDisplayName;
    private String baseUrl;
    private String superCategoryName;
    private Set<String> categoryNames;
    private String username;
    private String password;
    private byte[] iconData;
    private String serverName;
    private RemoteDescriptorProvider remoteDescriptorProvider;
    private Integer serverPriority = 0;

    public RemoteTransformerDescriptorImpl(String baseUrl, String displayName, String superCategoryName,
            Set<String> categoryNames, byte[] iconData, String username, String password) {
        super(RemoteTransformer.class, true);
        this.remoteDisplayName = displayName;
        this.superCategoryName = superCategoryName;
        this.categoryNames = categoryNames;
        this.iconData = iconData;
        this.username = username;
        this.password = password;
        this.baseUrl = baseUrl;
    }

    public void setRemoteDescriptorProvider(RemoteDescriptorProvider remoteDescriptorProvider) {
        this.remoteDescriptorProvider = remoteDescriptorProvider;
    }

    public RemoteDescriptorProvider getRemoteDescriptorProvider() {
        return remoteDescriptorProvider;
    }

    public void addPropertyDescriptor(ConfiguredPropertyDescriptor propertyDescriptor) {
        this._configuredProperties.add(propertyDescriptor);
    }

    public void setServerPriority(Integer serverPriority) {
        this.serverPriority = serverPriority;
    }

    public Integer getServerPriority() {
        return serverPriority;
    }

    @Override
    public String getDisplayName() {
        return remoteDisplayName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }

    @Override
    protected Class<? extends ComponentSuperCategory> getDefaultComponentSuperCategoryClass() {
        return classFromName(superCategoryName, TransformSuperCategory.class);
    }

    private Class classFromName(String className, Class defaultClass) {
        Class clazz = defaultClass;

        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            logger.warn("Class '" + className + "' was not found. \n" + e.getMessage());
        }

        return clazz;
    }

    @Override
    public Set<ComponentCategory> getComponentCategories() {
        Set<ComponentCategory> componentCategories = new HashSet<>();

        try {
            for (String name : categoryNames) {
                Class categoryClass = classFromName(name, null);

                if (categoryClass == null) {
                    continue;
                }

                ComponentCategory category = (ComponentCategory) categoryClass.newInstance();
                componentCategories.add(category);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            logger.warn("New instance of a component category could not have been created. \n" + e.getMessage());
        }

        return componentCategories;
    }

    @Override
    public Object newInstance() {
        RemoteTransformer t = new RemoteTransformer(baseUrl, remoteDisplayName, username, password);
        for (ConfiguredPropertyDescriptor prop : (Set<ConfiguredPropertyDescriptor>) _configuredProperties) {
            if (prop instanceof RemoteConfiguredPropertyDescriptor) {
                ((RemoteConfiguredPropertyDescriptor) prop).setDefaultValue(t);
            }
        }
        return t;
    }

    public byte[] getIconData() {
        return iconData;
    }
}
