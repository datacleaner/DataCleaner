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

import java.util.Map;

import org.datacleaner.configuration.ConfigurationReaderInterceptor;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.configuration.DefaultConfigurationReaderInterceptor;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.storage.InMemoryStorageProvider;
import org.datacleaner.storage.StorageProvider;

/**
 * {@link ConfigurationReaderInterceptor} for conf.xml in a Spark environment
 */
public class SparkConfigurationReaderInterceptor extends DefaultConfigurationReaderInterceptor {

    private static final TaskRunner TASK_RUNNER = new SingleThreadedTaskRunner();
    private static final DescriptorProvider DESCRIPTOR_PROVIDER = new ClasspathScanDescriptorProvider(TASK_RUNNER)
            .scanPackage("org.datacleaner", true).scanPackage("com.hi", true).scanPackage("com.neopost", true);
    private static final StorageProvider STORAGE_PROVIDER = new InMemoryStorageProvider(500, 20);

    private static final DataCleanerEnvironment BASE_ENVIRONMENT = new DataCleanerEnvironmentImpl()
            .withTaskRunner(TASK_RUNNER).withDescriptorProvider(DESCRIPTOR_PROVIDER)
            .withStorageProvider(STORAGE_PROVIDER);

    public SparkConfigurationReaderInterceptor(Map<String, String> customProperties) {
        super(customProperties, BASE_ENVIRONMENT);
    }

}
