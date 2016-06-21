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
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.apache.metamodel.util.LazyRef;
import org.datacleaner.Version;
import org.datacleaner.configuration.RemoteServerConfiguration;
import org.datacleaner.configuration.RemoteServerData;
import org.datacleaner.configuration.RemoteServerState;
import org.datacleaner.configuration.RemoteServerStateListener;
import org.datacleaner.restclient.ComponentList;
import org.datacleaner.restclient.ComponentRESTClient;
import org.datacleaner.restclient.Serializator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Provides descriptors of components that are available for remote calls on a
 * DataCleaner Monitor server. The list of them is downloaded and appropriate
 * descriptors are created for them ({@link RemoteTransformerDescriptorImpl}).
 *
 * @Since 9/8/15
 */
public class RemoteDescriptorProviderImpl extends AbstractDescriptorProvider implements RemoteDescriptorProvider {

    private static final Logger logger = LoggerFactory.getLogger(RemoteDescriptorProviderImpl.class);
    private final RemoteServerData remoteServerData;
    private final RemoteServerConfiguration remoteServerConfiguration;
    private RemoteLazyRef dataLazyReference = new RemoteLazyRef();

    private RemoteServerState actualState = null;
    private RemoteServerStateListener remoteServerStateListener = new RemoteServerStateListenerImpl();

    public RemoteDescriptorProviderImpl(RemoteServerData remoteServerData,
            RemoteServerConfiguration remoteServerConfiguration) {
        super(false);
        this.remoteServerData = remoteServerData;
        this.remoteServerConfiguration = remoteServerConfiguration;
        dataLazyReference.requestLoad();
    }

    @Override
    public RemoteServerData getServerData() {
        return remoteServerData;
    }

    public RemoteServerState getServerState() {
        if (actualState == null) {
            remoteServerConfiguration.addListener(remoteServerStateListener);
            actualState = remoteServerConfiguration.getActualState(remoteServerData.getServerName());
        }
        return actualState;
    }

    public void refresh() {
        dataLazyReference = new RemoteLazyRef();
        notifyListeners();
    }

    @Override
    public Collection<FilterDescriptor<?, ?>> getFilterDescriptors() {
        return Collections.unmodifiableCollection(dataLazyReference.get()._filterBeanDescriptors.values());
    }

    @Override
    public Collection<AnalyzerDescriptor<?>> getAnalyzerDescriptors() {
        return Collections.unmodifiableCollection(dataLazyReference.get()._analyzerBeanDescriptors.values());
    }

    @Override
    public Collection<TransformerDescriptor<?>> getTransformerDescriptors() {
        return Collections.unmodifiableCollection(dataLazyReference.get()._transformerBeanDescriptors.values());
    }

    @Override
    public Collection<RendererBeanDescriptor<?>> getRendererBeanDescriptors() {
        return Collections.unmodifiableCollection(dataLazyReference.get()._rendererBeanDescriptors.values());
    }

    private final class RemoteLazyRef extends LazyRef<Data> {
        @Override
        public Data fetch() throws Throwable {
            Data data = new Data();
            data.downloadDescriptors();

            return data;
        }
    }

    private final class Data {
        final Map<String, AnalyzerDescriptor<?>> _analyzerBeanDescriptors = new HashMap<>();
        final Map<String, FilterDescriptor<?, ?>> _filterBeanDescriptors = new HashMap<>();
        final Map<String, TransformerDescriptor<?>> _transformerBeanDescriptors = new HashMap<>();
        final Map<String, RendererBeanDescriptor<?>> _rendererBeanDescriptors = new HashMap<>();

        private void downloadDescriptors() {
            try {
                logger.info("Loading remote components list from " + remoteServerData.getUrl());
                final ComponentRESTClient client = new ComponentRESTClient(remoteServerData.getUrl(),
                        remoteServerData.getUsername(), remoteServerData.getPassword(), Version.getVersion());
                final ComponentList components = client.getAllComponents(true);

                for (ComponentList.ComponentInfo component : components.getComponents()) {
                    try {
                        final RemoteTransformerDescriptorImpl transformerDescriptor = new RemoteTransformerDescriptorImpl(
                                RemoteDescriptorProviderImpl.this, component.getName(),
                                initAnnotations(component.getName(), null, component.getAnnotations()),
                                component.getIconData(), component.isEnabled());
                        for (Map.Entry<String, ComponentList.PropertyInfo> propE : component.getProperties()
                                .entrySet()) {
                            final String propertyName = propE.getKey();
                            final ComponentList.PropertyInfo propInfo = propE.getValue();
                            final String className = propInfo.getClassName();
                            try {
                                Class<?> cl = findClass(className);
                                transformerDescriptor.addPropertyDescriptor(
                                        new TypeBasedConfiguredPropertyDescriptorImpl(propertyName,
                                                propInfo.getDescription(), cl, propInfo.isRequired(),
                                                transformerDescriptor, initAnnotations(component.getName(),
                                                        propertyName, propInfo.getAnnotations()),
                                                propInfo.getDefaultValue()));
                            } catch (ClassNotFoundException e) {
                                logger.debug("Cannot initialize typed property descriptor '{}'.'{}' because of {}",
                                        component.getName(), propertyName, e.toString());
                                // class not available on this server.
                                transformerDescriptor.addPropertyDescriptor(
                                        new JsonSchemaConfiguredPropertyDescriptorImpl(propertyName,
                                                propInfo.getSchema(), propInfo.isInputColumn(),
                                                propInfo.getDescription(), propInfo.isRequired(),
                                                transformerDescriptor, initAnnotations(component.getName(),
                                                        propertyName, propInfo.getAnnotations()),
                                                propInfo.getDefaultValue()));
                            }
                        }
                        _transformerBeanDescriptors.put(transformerDescriptor.getDisplayName(), transformerDescriptor);
                        logger.info("Registered remote component {}", transformerDescriptor.getDisplayName());
                    } catch (Exception e) {
                        logger.error("Cannot create remote component representation for: " + component.getName(), e);
                    }
                }
            } catch (Exception e) {
                logger.error("Cannot get list of remote components on " + remoteServerData.getUrl(), e);
                // TODO: plan a task to try again after somw while. And then
                // notify listeners...
            }
        }
    }

    private Class<?> findClass(String name) throws ClassNotFoundException {
        return ClassUtils.getClass(getClass().getClassLoader(), name, false);
    }

    private Map<Class<? extends Annotation>, Annotation> initAnnotations(String componentName, String propertyName,
            JsonNode annotationsInfo) {
        final Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();
        if (annotationsInfo == null) {
            return annotations;
        }

        for(Iterator<Map.Entry<String, JsonNode>> it = annotationsInfo.fields(); it.hasNext();) {
            Map.Entry<String, JsonNode> annotationEntry = it.next();
            try {
                final String annotClassName = annotationEntry.getKey();
                @SuppressWarnings("unchecked")
                final Class<? extends Annotation> anClass = (Class<? extends Annotation>) Class.forName(annotClassName);

                Map<String, Object> annotationValues = new HashMap<>();
                JsonNode annProperties = annotationEntry.getValue();
                for (Iterator<Map.Entry<String, JsonNode>> annPropIter = annProperties.fields(); annPropIter.hasNext();) {
                    Map.Entry<String, JsonNode> annPropEntry = annPropIter.next();
                    String propName = annPropEntry.getKey();
                    JsonNode propValueNode = annPropEntry.getValue();
                    Method propMethod = anClass.getDeclaredMethod(propName, new Class[0]);
                    Class<?> propClass = propMethod.getReturnType();
                    Object propValue = Serializator.getJacksonObjectMapper().treeToValue(propValueNode, propClass);
                    annotationValues.put(propName, propValue);
                }

                final Annotation anProxy = AnnotationProxy.newAnnotation(anClass, annotationValues);
                annotations.put(anClass, anProxy);

            } catch (Exception e) {
                if(propertyName == null){
                    logger.warn("Cannot create annotation '{}' for component '{}' property '{}'", annotationEntry.getKey(),
                            componentName, propertyName, e);
                }else {
                    logger.warn("Cannot create annotation '{}' for component '{}'",annotationEntry.getKey(),
                            componentName, e);
                }
            }
        }
        return annotations;
    }

    private class RemoteServerStateListenerImpl implements RemoteServerStateListener{

        @Override
        public void onRemoteServerStateChange(final String remoteServerName, final RemoteServerState state) {
            if(remoteServerName.equals(remoteServerData.getServerName())){
                actualState = state;
            }
        }
    }
}
