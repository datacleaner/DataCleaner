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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.metamodel.util.LazyRef;
import org.datacleaner.restclient.ComponentList;
import org.datacleaner.restclient.ComponentRESTClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Since 9/8/15
 */
public class RemoteDescriptorProvider extends AbstractDescriptorProvider {
    private static final Logger logger = LoggerFactory.getLogger(RemoteDescriptorProvider.class);
    private String url, username, password;
    private String tenant = "test"; // TODO

    LazyRef<Data> data = new LazyRef<Data>() {
        @Override
        protected Data fetch() throws Throwable {
            Data data = new Data();
            data.downloadDescriptors();
            return data;
        }
    };

    public RemoteDescriptorProvider(String url, String username, String password) {
        this.url = url.replaceAll("/+$", "");
        this.username = username;
        this.password = password;
        data.requestLoad();
    }

    @Override
    public Collection<FilterDescriptor<?, ?>> getFilterDescriptors() {
        return Collections.unmodifiableCollection(data.get()._filterBeanDescriptors.values());
    }

    @Override
    public Collection<AnalyzerDescriptor<?>> getAnalyzerDescriptors() {
        return Collections.unmodifiableCollection(data.get()._analyzerBeanDescriptors.values());
    }

    @Override
    public Collection<TransformerDescriptor<?>> getTransformerDescriptors() {
        return Collections.unmodifiableCollection(data.get()._transformerBeanDescriptors.values());
    }

    @Override
    public Collection<RendererBeanDescriptor<?>> getRendererBeanDescriptors() {
        return Collections.unmodifiableCollection(data.get()._rendererBeanDescriptors.values());
    }

    class Data {
        final Map<String, AnalyzerDescriptor<?>> _analyzerBeanDescriptors = new HashMap<String, AnalyzerDescriptor<?>>();
        final Map<String, FilterDescriptor<?, ?>> _filterBeanDescriptors = new HashMap<String, FilterDescriptor<?, ?>>();
        final Map<String, TransformerDescriptor<?>> _transformerBeanDescriptors = new HashMap<String, TransformerDescriptor<?>>();
        final Map<String, RendererBeanDescriptor<?>> _rendererBeanDescriptors = new HashMap<String, RendererBeanDescriptor<?>>();

        private void downloadDescriptors() {
            try {
                logger.info("Loading remote components list from " + url);
                // TODO: There is currently no "close" method in client, although Jersey client has "destroy" method.
                ComponentRESTClient client = new ComponentRESTClient(url, username, password);
                ComponentList components = client.getAllComponents(tenant, true);
                for(ComponentList.ComponentInfo component: components.getComponents()) {
                    try {
                        RemoteTransformerDescriptorImpl transformer = new RemoteTransformerDescriptorImpl(
                                url,
                                component.getName(),
                                tenant,
                                component.getSuperCategoryName(),
                                component.getCategoryNames(),
                                component.getIconData(),
                                username,
                                password);
                        for(Map.Entry<String, ComponentList.PropertyInfo> propE: component.getProperties().entrySet()) {
                            String name = propE.getKey();
                            ComponentList.PropertyInfo propInfo = propE.getValue();
                            String className = propInfo.getClassName();
                            try {
                                Class cl = Class.forName(className, false, getClass().getClassLoader());
                                transformer.addPropertyDescriptor(new TypeBasedConfiguredPropertyDescriptorImpl(
                                        name,
                                        propInfo.getDescription(),
                                        cl,
                                        propInfo.isRequired(),
                                        transformer));
                            } catch(Exception e) {
                                // class not available on this server.
                                transformer.addPropertyDescriptor(new JsonSchemaConfiguredPropertyDescriptorImpl(
                                        name,
                                        propInfo.getSchema(),
                                        propInfo.isInputColumn(),
                                        propInfo.getDescription(),
                                        propInfo.isRequired(),
                                        transformer));
                            }
                        }
                        _transformerBeanDescriptors.put(transformer.getDisplayName(), transformer);
                    } catch(Exception e) {
                        logger.error("Cannot create remote component representation for: " + component.getName(), e);
                    }
                }
            } catch(Exception e) {
                logger.error("Cannot get list of remote components on " + url, e);
                // TODO: plan a task to try again after somw while. And then notify listeners...
            }
        }
    }
}
