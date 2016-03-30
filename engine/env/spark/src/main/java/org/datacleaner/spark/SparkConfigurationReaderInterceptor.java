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
package org.datacleaner.spark;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.metamodel.util.Resource;
import org.datacleaner.api.RenderingFormat;
import org.datacleaner.configuration.ConfigurationReaderInterceptor;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.configuration.DefaultConfigurationReaderInterceptor;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.panels.ComponentBuilderPresenterRenderingFormat;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.spark.utils.HdfsHelper;
import org.datacleaner.storage.InMemoryStorageProvider;
import org.datacleaner.storage.StorageProvider;
import org.datacleaner.util.convert.HadoopResourceBuilder;

/**
 * {@link ConfigurationReaderInterceptor} for conf.xml in a Spark environment
 */
public class SparkConfigurationReaderInterceptor extends DefaultConfigurationReaderInterceptor {

    private static final TaskRunner TASK_RUNNER = new SingleThreadedTaskRunner();
    private static final Collection<Class<? extends RenderingFormat<?>>> EXCLUDED_RENDERER_FORMATS = Arrays.asList(
            SwingRenderingFormat.class, ComponentBuilderPresenterRenderingFormat.class);
    private static final DescriptorProvider DESCRIPTOR_PROVIDER = new ClasspathScanDescriptorProvider(TASK_RUNNER,
            EXCLUDED_RENDERER_FORMATS).scanPackage("org.datacleaner", true).scanPackage("com.hi", true).scanPackage(
                    "com.neopost", true);
    private static final StorageProvider STORAGE_PROVIDER = new InMemoryStorageProvider(500, 20);
    
    private static final DataCleanerEnvironment BASE_ENVIRONMENT = new DataCleanerEnvironmentImpl()
            .withTaskRunner(TASK_RUNNER).withDescriptorProvider(DESCRIPTOR_PROVIDER)
            .withStorageProvider(STORAGE_PROVIDER);

    private final HdfsHelper _hdfsHelper;

    public SparkConfigurationReaderInterceptor(Map<String, String> customProperties) {
        super(customProperties, BASE_ENVIRONMENT);
        _hdfsHelper = HdfsHelper.createHelper();
    }

    @Override
    public Resource createResource(String resourceUrl, DataCleanerConfiguration tempConfiguration) {
        final Matcher matcher = HadoopResourceBuilder.RESOURCE_SCHEME_PATTERN.matcher(resourceUrl);
        if (matcher.find()) {
            resourceUrl = matcher.group(1) + "://" + matcher.group(3);
        }
        final URI uri = URI.create(resourceUrl);
        return _hdfsHelper.getResourceToUse(uri);
    }

}
