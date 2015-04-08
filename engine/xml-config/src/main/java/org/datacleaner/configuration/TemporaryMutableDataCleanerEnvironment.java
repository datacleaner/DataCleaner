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
package org.datacleaner.configuration;

import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.storage.StorageProvider;

/**
 * A temporary and mutable implementation of {@link DataCleanerEnvironment} -
 * used only while building a {@link DataCleanerConfiguration} in
 * {@link JaxbConfigurationReader}.
 */
final class TemporaryMutableDataCleanerEnvironment implements DataCleanerEnvironment {

    private InjectionManagerFactory _injectionManagerFactory;
    private StorageProvider _storageProvider;
    private DescriptorProvider _descriptorProvider;
    private TaskRunner _taskRunner;

    public TemporaryMutableDataCleanerEnvironment(DataCleanerEnvironment baseEnvironment) {
        _injectionManagerFactory = baseEnvironment.getInjectionManagerFactory();
        _storageProvider = baseEnvironment.getStorageProvider();
        _descriptorProvider = baseEnvironment.getDescriptorProvider();
        _taskRunner = baseEnvironment.getTaskRunner();
    }

    @Override
    public TaskRunner getTaskRunner() {
        return _taskRunner;
    }

    @Override
    public DescriptorProvider getDescriptorProvider() {
        return _descriptorProvider;
    }

    @Override
    public StorageProvider getStorageProvider() {
        return _storageProvider;
    }

    @Override
    public InjectionManagerFactory getInjectionManagerFactory() {
        return _injectionManagerFactory;
    }

    public void setDescriptorProvider(DescriptorProvider descriptorProvider) {
        _descriptorProvider = descriptorProvider;
    }

    public void setInjectionManagerFactory(InjectionManagerFactory injectionManagerFactory) {
        _injectionManagerFactory = injectionManagerFactory;
    }

    public void setStorageProvider(StorageProvider storageProvider) {
        _storageProvider = storageProvider;
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        _taskRunner = taskRunner;
    }
}
