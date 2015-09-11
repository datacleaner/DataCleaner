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

import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;
import org.datacleaner.api.InputColumn;
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
    private CountDownLatch latch;

    private final Map<String, AnalyzerDescriptor<?>> _analyzerBeanDescriptors = new HashMap<String, AnalyzerDescriptor<?>>();
    private final Map<String, FilterDescriptor<?, ?>> _filterBeanDescriptors = new HashMap<String, FilterDescriptor<?, ?>>();
    private final Map<String, TransformerDescriptor<?>> _transformerBeanDescriptors = new HashMap<String, TransformerDescriptor<?>>();
    private final Map<String, RendererBeanDescriptor<?>> _rendererBeanDescriptors = new HashMap<String, RendererBeanDescriptor<?>>();

    public RemoteDescriptorProvider(TaskRunner taskRunner, String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        latch = new CountDownLatch(1);
        taskRunner.run(new Task() {
            @Override
            public void execute() throws Exception {
                try {
                    downloadDescriptors();
                } finally {
                    latch.countDown();
                }
            }
        }, null);
    }

    private void downloadDescriptors() {

        // TODO - this is a mocked remote transformer descriptor
        String serverUrl = "http://ubu:8888";
        String resourcePath = "/repository/demo/components/Concatenator";

        RemoteTransformerDescriptorImpl transformer = new RemoteTransformerDescriptorImpl(
                serverUrl + resourcePath,
                "Concatenator" + " (remote)");
        transformer.addPropertyDescriptor(new TypeBasedConfiguredPropertyDescriptorImpl(
                "Columns", "Input Columns", InputColumn[].class, true, transformer));
        transformer.addPropertyDescriptor(new JsonSchemaConfiguredPropertyDescriptorImpl(
                "Separator", new StringSchema(), false, "A string to separate the concatenated values"));

        _transformerBeanDescriptors.put(transformer.getDisplayName(), transformer);
    }

    @Override
    public Collection<FilterDescriptor<?, ?>> getFilterDescriptors() {
        awaitTasks();
        return Collections.unmodifiableCollection(_filterBeanDescriptors.values());
    }

    @Override
    public Collection<AnalyzerDescriptor<?>> getAnalyzerDescriptors() {
        awaitTasks();
        return Collections.unmodifiableCollection(_analyzerBeanDescriptors.values());
    }

    @Override
    public Collection<TransformerDescriptor<?>> getTransformerDescriptors() {
        awaitTasks();
        return Collections.unmodifiableCollection(_transformerBeanDescriptors.values());
    }

    @Override
    public Collection<RendererBeanDescriptor<?>> getRendererBeanDescriptors() {
        awaitTasks();
        return Collections.unmodifiableCollection(_rendererBeanDescriptors.values());
    }

    private void awaitTasks() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

}
