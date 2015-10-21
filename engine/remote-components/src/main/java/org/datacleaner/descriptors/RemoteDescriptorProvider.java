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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.apache.metamodel.util.LazyRef;
import org.datacleaner.configuration.CredentialsProvider;
import org.datacleaner.restclient.ComponentList;
import org.datacleaner.restclient.ComponentRESTClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides descriptors of components that are available for remote calls on a
 * DataCleaner Monitor server. The list of them is downloaded and appropriate
 * descriptors are created for them ({@link RemoteTransformerDescriptorImpl}).
 *
 * @Since 9/8/15
 */
public class RemoteDescriptorProvider extends AbstractDescriptorProvider {

    private static final Logger logger = LoggerFactory.getLogger(RemoteDescriptorProvider.class);
    private final CredentialsProvider credentialsProvider;
    private RemoteLazyRef<Data> dataLazyReference = new RemoteLazyRef<>();

    public RemoteDescriptorProvider(CredentialsProvider credentialsProvider) {
        super(false);
        this.credentialsProvider = credentialsProvider;
        dataLazyReference.requestLoad();
    }

    public void refresh() {
        dataLazyReference = new RemoteLazyRef<>();
        notifyComponentDescriptorsUpdatedListeners();
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

    private final class RemoteLazyRef<E> extends LazyRef<E> {
        @Override
        public E fetch() throws Throwable {
            Data data = new Data();
            data.downloadDescriptors();

            return (E) data;
        }
    }

    private final class Data {
        final Map<String, AnalyzerDescriptor<?>> _analyzerBeanDescriptors = new HashMap<>();
        final Map<String, FilterDescriptor<?, ?>> _filterBeanDescriptors = new HashMap<>();
        final Map<String, TransformerDescriptor<?>> _transformerBeanDescriptors = new HashMap<>();
        final Map<String, RendererBeanDescriptor<?>> _rendererBeanDescriptors = new HashMap<>();

        private void downloadDescriptors() {
            try {
                logger.info("Loading remote components list from " + credentialsProvider.getHost());
                final ComponentRESTClient client = new ComponentRESTClient(
                        credentialsProvider.getHost(), credentialsProvider.getUsername(), credentialsProvider.getPassword());
                final ComponentList components = client.getAllComponents(true);
                
                for (ComponentList.ComponentInfo component : components.getComponents()) {
                    try {
                        final RemoteTransformerDescriptorImpl transformer = new RemoteTransformerDescriptorImpl(
                                credentialsProvider.getHost(), component.getName(),
                                component.getSuperCategoryName(), component.getCategoryNames(), component.getIconData(),
                                credentialsProvider.getUsername(), credentialsProvider.getPassword());

                        for (Map.Entry<String, ComponentList.PropertyInfo> propE : component.getProperties()
                                .entrySet()) {
                            final String propertyName = propE.getKey();
                            final ComponentList.PropertyInfo propInfo = propE.getValue();
                            final String className = propInfo.getClassName();
                            try {
                                Class<?> cl = findClass(className);
                                transformer.addPropertyDescriptor(new TypeBasedConfiguredPropertyDescriptorImpl(
                                        propertyName, propInfo.getDescription(), cl, propInfo.isRequired(), transformer,
                                        initAnnotations(component.getName(), propertyName, propInfo.getAnnotations()),
                                        propInfo.getDefaultValue()));
                            } catch (ClassNotFoundException e) {
                                logger.debug("Cannot initialize typed property descriptor '{}'.'{}' because of {}",
                                        component.getName(), propertyName, e.toString());
                                // class not available on this server.
                                transformer.addPropertyDescriptor(new JsonSchemaConfiguredPropertyDescriptorImpl(
                                        propertyName, propInfo.getSchema(), propInfo.isInputColumn(),
                                        propInfo.getDescription(), propInfo.isRequired(), transformer,
                                        initAnnotations(component.getName(), propertyName, propInfo.getAnnotations()),
                                        propInfo.getDefaultValue()));
                            }
                        }
                        _transformerBeanDescriptors.put(transformer.getDisplayName(), transformer);
                    } catch (Exception e) {
                        logger.error("Cannot create remote component representation for: " + component.getName(), e);
                    }
                }
            } catch (Exception e) {
                logger.error("Cannot get list of remote components on " + credentialsProvider.getHost(), e);
                // TODO: plan a task to try again after somw while. And then
                // notify listeners...
            }
        }
    }

    private Class<?> findClass(String name) throws ClassNotFoundException {
        return ClassUtils.getClass(getClass().getClassLoader(), name, false);
    }

    private Map<Class<? extends Annotation>, Annotation> initAnnotations(String componentName, String propertyName,
            Map<String, Map<String, Object>> annotationsInfo) {
        final Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();
        if (annotationsInfo == null) {
            return annotations;
        }
        for (Map.Entry<String, Map<String, Object>> annInfoE : annotationsInfo.entrySet()) {
            try {
                @SuppressWarnings("unchecked")
                final Class<? extends Annotation> anClass = (Class<? extends Annotation>) Class
                        .forName(annInfoE.getKey());
                final Map<String, Object> anProperties = annInfoE.getValue();
                final Annotation anProxy = AnnotationProxy.newAnnotation(anClass, anProperties);
                annotations.put(anClass, anProxy);
            } catch (Exception e) {
                logger.warn("Cannot create annotation '{}' for component '{}' property '{}'", annInfoE.getKey(),
                        componentName, propertyName);
            }
        }
        return annotations;
    }
}
