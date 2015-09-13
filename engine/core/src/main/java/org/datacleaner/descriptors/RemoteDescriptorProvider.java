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

import org.apache.metamodel.util.LazyRef;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.tasks.Task;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @Since 9/8/15
 */
public class RemoteDescriptorProvider extends AbstractDescriptorProvider {

    private String url, username, password;

    LazyRef<Data> data = new LazyRef<Data>() {
        @Override
        protected Data fetch() throws Throwable {
            Data data = new Data();
            data.downloadDescriptors();
            return data;
        }
    };

    public RemoteDescriptorProvider(TaskRunner taskRunner, String url, String username, String password) {
        this.url = url;
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
            // TODO - load and fill the component descriptor collections
        }
    }
}
