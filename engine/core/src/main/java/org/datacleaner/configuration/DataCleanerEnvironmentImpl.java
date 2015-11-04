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

import java.util.ArrayList;
import java.util.List;

import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.storage.InMemoryStorageProvider;
import org.datacleaner.storage.StorageProvider;

/**
 * Default (immutable) implementation of {@link DataCleanerEnvironmentImpl}.
 */
public class DataCleanerEnvironmentImpl implements DataCleanerEnvironment {

    private final TaskRunner _taskRunner;
    private final DescriptorProvider _descriptorProvider;
    private final StorageProvider _storageProvider;
    private final InjectionManagerFactory _injectionManagerFactory;
    private final List<CredentialsProvider> _credentialsProviders;

    /**
     * Creates a {@link DataCleanerEnvironment}
     *
     * @param taskRunner
     * @param descriptorProvider
     * @param storageProvider
     * @param injectionManagerFactory
     */
    public DataCleanerEnvironmentImpl(TaskRunner taskRunner, DescriptorProvider descriptorProvider,
                                      StorageProvider storageProvider, InjectionManagerFactory injectionManagerFactory) {
        this(taskRunner, descriptorProvider, storageProvider, injectionManagerFactory, null);
    }

    /**
     * Creates a {@link DataCleanerEnvironment}
     * 
     * @param taskRunner
     * @param descriptorProvider
     * @param storageProvider
     * @param injectionManagerFactory
     * @param credentialsProviders
     */
    public DataCleanerEnvironmentImpl(TaskRunner taskRunner, DescriptorProvider descriptorProvider,
            StorageProvider storageProvider, InjectionManagerFactory injectionManagerFactory,
            List<CredentialsProvider> credentialsProviders) {
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

        if (credentialsProviders == null) {
            _credentialsProviders = defaultCredentialsProviders();
        }
        else {
            _credentialsProviders = credentialsProviders;
        }
    }

    /**
     * Creates a {@link DataCleanerEnvironment} based on defaults
     */
    public DataCleanerEnvironmentImpl() {
        this(defaultTaskRunner(), defaultDescriptorProvider(), defaultStorageProvider(),
                defaultInjectionManagerFactory(), defaultCredentialsProviders());
    }

    /**
     * Creates a copy of another {@link DataCleanerEnvironment}
     * 
     * @param e
     */
    public DataCleanerEnvironmentImpl(DataCleanerEnvironment e) {
        this(e.getTaskRunner(), e.getDescriptorProvider(), e.getStorageProvider(), e.getInjectionManagerFactory(),
                e.getCredentialsProviders());
    }

    public DataCleanerEnvironmentImpl withTaskRunner(TaskRunner taskRunner) {
        return new DataCleanerEnvironmentImpl(taskRunner, getDescriptorProvider(), getStorageProvider(),
                getInjectionManagerFactory(), getCredentialsProviders());
    }

    public DataCleanerEnvironmentImpl withDescriptorProvider(DescriptorProvider descriptorProvider) {
        return new DataCleanerEnvironmentImpl(getTaskRunner(), descriptorProvider, getStorageProvider(),
                getInjectionManagerFactory(), getCredentialsProviders());
    }

    public DataCleanerEnvironmentImpl withStorageProvider(StorageProvider storageProvider) {
        return new DataCleanerEnvironmentImpl(getTaskRunner(), getDescriptorProvider(), storageProvider,
                getInjectionManagerFactory(), getCredentialsProviders());
    }

    public DataCleanerEnvironmentImpl withInjectionManagerFactory(InjectionManagerFactory injectionManagerFactory) {
        return new DataCleanerEnvironmentImpl(getTaskRunner(), getDescriptorProvider(), getStorageProvider(),
                injectionManagerFactory, getCredentialsProviders());
    }

    @Override
    public List<CredentialsProvider> getCredentialsProviders() {
        return _credentialsProviders;
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

    public static List<CredentialsProvider> defaultCredentialsProviders() {
        return new ArrayList<CredentialsProvider>();
    }
}
