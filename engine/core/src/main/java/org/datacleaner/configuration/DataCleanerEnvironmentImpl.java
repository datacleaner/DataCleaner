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
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.storage.InMemoryStorageProvider;
import org.datacleaner.storage.StorageProvider;

/**
 * Default (immutable) implementation of {@link DataCleanerEnvironment}.
 */
public class DataCleanerEnvironmentImpl implements DataCleanerEnvironment {

    private final TaskRunner _taskRunner;
    private final DescriptorProvider _descriptorProvider;
    private final StorageProvider _storageProvider;
    private final InjectionManagerFactory _injectionManagerFactory;

    public DataCleanerEnvironmentImpl(TaskRunner taskRunner, DescriptorProvider descriptorProvider,
            StorageProvider storageProvider, InjectionManagerFactory injectionManagerFactory) {
        if (taskRunner == null) {
            _taskRunner = defaultTaskRunner();
        } else {
            _taskRunner = taskRunner;
        }
        if (descriptorProvider == null) {
            _descriptorProvider = defaultDescriptorProvider();
        } else {
            _descriptorProvider = descriptorProvider;
        }
        if (storageProvider == null) {
            _storageProvider = defaultStorageProvider();
        } else {
            _storageProvider = storageProvider;
        }
        if (injectionManagerFactory == null) {
            _injectionManagerFactory = defaultInjectionManagerFactory();
        } else {
            _injectionManagerFactory = injectionManagerFactory;
        }
    }

    public DataCleanerEnvironmentImpl() {
        this(defaultTaskRunner(), defaultDescriptorProvider(), defaultStorageProvider(),
                defaultInjectionManagerFactory());
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

    public static InjectionManagerFactory defaultInjectionManagerFactory() {
        return new InjectionManagerFactoryImpl();
    }

    public static StorageProvider defaultStorageProvider() {
        return new InMemoryStorageProvider();
    }

    public static DescriptorProvider defaultDescriptorProvider() {
        return new SimpleDescriptorProvider();
    }

    public static TaskRunner defaultTaskRunner() {
        return new SingleThreadedTaskRunner();
    }
}
