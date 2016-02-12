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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.LazyRef;
import org.apache.metamodel.util.SharedExecutorService;
import org.datacleaner.configuration.RemoteServerData;
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
public class RemoteDescriptorProviderImpl extends AbstractDescriptorProvider implements RemoteDescriptorProvider {

    private static final Logger logger = LoggerFactory.getLogger(RemoteDescriptorProviderImpl.class);
    private final RemoteServerData remoteServerData;
    private RemoteLazyRef dataLazyReference = new RemoteLazyRef();

    private static final int TEST_CONNECTION_TIMEOUT = 15 * 1000; // [ms]
    private static final int TEST_CONNECTION_INTERVAL = 2 * 1000; // [ms]
    /* for all remote transformer descriptors together */
    private long lastConnectionCheckTime = 0L;
    private boolean lastConnectionCheckResult = true;
    private boolean checkInProgress = false;

    public RemoteDescriptorProviderImpl(RemoteServerData remoteServerData) {
        super(false);
        this.remoteServerData = remoteServerData;
        dataLazyReference.requestLoad();
    }

    @Override
    public RemoteServerData getServerData() {
        return remoteServerData;
    }

    public boolean isServerUp() {
        final long now = System.currentTimeMillis();
        boolean runCheck = false;

        synchronized (this) { // not to start multiple checks at once
            if (lastConnectionCheckTime + TEST_CONNECTION_INTERVAL < now && checkInProgress == false) {
                runCheck = true;
                lastConnectionCheckTime = now;
                checkInProgress = true;
            }
        }

        if (runCheck) {
            SharedExecutorService.get().execute(new Runnable() {
                @Override
                public void run() {
                    checkServerAvailability();
                }
            });
        }

        return lastConnectionCheckResult;
    }

    private void checkServerAvailability() {
        Socket socket = new Socket();
        try {
            URL siteURL = new URL(remoteServerData.getUrl());
            int port = siteURL.getPort();
            if(port <= 0) {
                port = siteURL.getDefaultPort();
            }
            InetSocketAddress endpoint = new InetSocketAddress(siteURL.getHost(), port);
            socket.connect(endpoint, TEST_CONNECTION_TIMEOUT);
            lastConnectionCheckResult = socket.isConnected();
        } catch (IOException e) {
            lastConnectionCheckResult = false;
            logger.warn("Server '" + remoteServerData.getServerName() + "(" + remoteServerData.getUrl() + ")' is down: "
                    + e.getMessage());
        } finally {
            synchronized (this) {
                checkInProgress = false;
                FileHelper.safeClose(socket);
            }
        }
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
                        remoteServerData.getUsername(), remoteServerData.getPassword());
                final ComponentList components = client.getAllComponents(true);

                for (ComponentList.ComponentInfo component : components.getComponents()) {
                    try {
                        final RemoteTransformerDescriptorImpl transformerDescriptor = new RemoteTransformerDescriptorImpl(
                                RemoteDescriptorProviderImpl.this, component.getName(),
                                component.getSuperCategoryName(), component.getCategoryNames(),
                                component.getIconData());

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
                //Load actual status of remote server.
                isServerUp();
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

    @Override
    public Collection<DescriptorProviderStatus> getStatus() {
        final Set<DescriptorProviderStatus> statusSet = new HashSet<>();

        if (!isServerUp()) {
            DescriptorProviderStatus serverDownState = new DescriptorProviderStatus(
                    DescriptorProviderStatus.Level.ERROR, "Remote server is not available at the moment. ");
            statusSet.add(serverDownState);
        }

        return statusSet;
    }
}
