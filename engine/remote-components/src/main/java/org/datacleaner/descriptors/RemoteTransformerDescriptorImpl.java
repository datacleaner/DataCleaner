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
import java.util.Map;

import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.components.categories.TransformSuperCategory;
import org.datacleaner.components.remote.RemoteTransformer;
import org.datacleaner.configuration.RemoteServerData;
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
public class RemoteTransformerDescriptorImpl extends SimpleComponentDescriptor<RemoteTransformer>
        implements RemoteTransformerDescriptor<RemoteTransformer>, HasIcon {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(RemoteTransformerDescriptorImpl.class);
    
    private final String remoteDisplayName;
    private final Map<Class<? extends Annotation>, Annotation> annotations;
    private final byte[] iconData;
    private final RemoteDescriptorProvider remoteDescriptorProvider;

    public RemoteTransformerDescriptorImpl(RemoteDescriptorProvider remoteDescriptorProvider, String displayName,
            Map<Class<? extends Annotation>, Annotation> annotations, byte[] iconData) {
        super(RemoteTransformer.class, true);
        this.remoteDescriptorProvider = remoteDescriptorProvider;
        this.remoteDisplayName = displayName;
        this.annotations = annotations;
        this.iconData = iconData;
    }

    public RemoteDescriptorProvider getRemoteDescriptorProvider() {
        return remoteDescriptorProvider;
    }

    public void addPropertyDescriptor(ConfiguredPropertyDescriptor propertyDescriptor) {
        this._configuredProperties.add(propertyDescriptor);
    }

    @Override
    public String getDisplayName() {
        return remoteDisplayName;
    }

    private Class<?> classFromName(String className, Class<?> defaultClass) {
        Class<?> clazz = defaultClass;

        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            logger.warn("Class '" + className + "' was not found. \n" + e.getMessage());
        }

        return clazz;
    }

    @Override
    public Annotation getAnnotation(Class annotationClass) {
        return annotations.get(annotationClass);
    }

    @Override
    public RemoteTransformer newInstance() {
        final RemoteServerData serverData = getRemoteDescriptorProvider().getServerData();
        final RemoteTransformer remoteTransformer = new RemoteTransformer(serverData, remoteDisplayName);

        for (ConfiguredPropertyDescriptor propertyDescriptor : _configuredProperties) {
            if (propertyDescriptor instanceof RemoteConfiguredPropertyDescriptor) {
                ((RemoteConfiguredPropertyDescriptor) propertyDescriptor).setDefaultValue(remoteTransformer);
            }
        }

        return remoteTransformer;
    }

    @Override
    protected Class<? extends ComponentSuperCategory> getDefaultComponentSuperCategoryClass() {
        return TransformSuperCategory.class;
    }

    public byte[] getIconData() {
        return iconData;
    }
}
