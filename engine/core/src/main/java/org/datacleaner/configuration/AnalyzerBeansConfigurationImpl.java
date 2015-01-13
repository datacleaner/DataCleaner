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

import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.datacleaner.reference.ReferenceDataCatalogImpl;
import org.datacleaner.storage.InMemoryStorageProvider;
import org.datacleaner.storage.StorageProvider;

public final class AnalyzerBeansConfigurationImpl implements AnalyzerBeansConfiguration {

    private static final long serialVersionUID = 1L;

    private final transient DescriptorProvider _descriptorProvider;
    private final transient StorageProvider _storageProvider;
    private final transient TaskRunner _taskRunner;
    private final DatastoreCatalog _datastoreCatalog;
    private final ReferenceDataCatalog _referenceDataCatalog;
    private final InjectionManagerFactory _injectionManagerFactory;

    private static StorageProvider defaultStorageProvider() {
        return new InMemoryStorageProvider();
    }

    private static TaskRunner defaultTaskRunner() {
        return new SingleThreadedTaskRunner();
    }

    private static DescriptorProvider defaultDescriptorProvider() {
        return new SimpleDescriptorProvider();
    }

    private static ReferenceDataCatalog defaultReferenceDataCatalog() {
        return new ReferenceDataCatalogImpl();
    }

    private static DatastoreCatalog defaultDatastoreCatalog() {
        return new DatastoreCatalogImpl();
    }

    /**
     * Creates a minimalistic configuration object, mostly suitable for stubbing
     * and testing.
     */
    public AnalyzerBeansConfigurationImpl() {
        this(defaultDatastoreCatalog(), defaultReferenceDataCatalog(), defaultDescriptorProvider(),
                defaultTaskRunner(), defaultStorageProvider());
    }

    /**
     * Creates a minimalistic configuration object with a specific
     * {@link InjectionManagerFactory}, mostly suitable for stubbing and testing.
     * 
     * @param injectionManagerFactory
     */
    public AnalyzerBeansConfigurationImpl(InjectionManagerFactory injectionManagerFactory) {
        this(defaultDatastoreCatalog(), defaultReferenceDataCatalog(), defaultDescriptorProvider(),
                defaultTaskRunner(), defaultStorageProvider(), injectionManagerFactory);
    }

    /**
     * Creates a fully specified configuration object (with a default
     * {@link InjectionManagerFactory}).
     * 
     * @param datastoreCatalog
     * @param referenceDataCatalog
     * @param descriptorProvider
     * @param taskRunner
     * @param storageProvider
     */
    public AnalyzerBeansConfigurationImpl(DatastoreCatalog datastoreCatalog, ReferenceDataCatalog referenceDataCatalog,
            DescriptorProvider descriptorProvider, TaskRunner taskRunner, StorageProvider storageProvider) {
        this(datastoreCatalog, referenceDataCatalog, descriptorProvider, taskRunner, storageProvider, null);
    }

    /**
     * Creates a fully specified configuration object.
     * 
     * @param datastoreCatalog
     * @param referenceDataCatalog
     * @param descriptorProvider
     * @param taskRunner
     * @param storageProvider
     * @param injectionManagerFactory
     */
    public AnalyzerBeansConfigurationImpl(DatastoreCatalog datastoreCatalog, ReferenceDataCatalog referenceDataCatalog,
            DescriptorProvider descriptorProvider, TaskRunner taskRunner, StorageProvider storageProvider,
            InjectionManagerFactory injectionManagerFactory) {
        if (datastoreCatalog == null) {
            throw new IllegalArgumentException("datastoreCatalog cannot be null");
        }
        if (referenceDataCatalog == null) {
            throw new IllegalArgumentException("referenceDataCatalog cannot be null");
        }
        if (descriptorProvider == null) {
            throw new IllegalArgumentException("descriptorProvider cannot be null");
        }
        if (taskRunner == null) {
            throw new IllegalArgumentException("taskRunner cannot be null");
        }
        if (storageProvider == null) {
            throw new IllegalArgumentException("storageProvider cannot be null");
        }
        _datastoreCatalog = datastoreCatalog;
        _referenceDataCatalog = referenceDataCatalog;
        _descriptorProvider = descriptorProvider;
        _taskRunner = taskRunner;
        _storageProvider = storageProvider;

        if (injectionManagerFactory == null) {
            injectionManagerFactory = new InjectionManagerFactoryImpl();
        }
        _injectionManagerFactory = injectionManagerFactory;
    }

    /**
     * Creates a new {@link AnalyzerBeansConfiguration} with a different
     * {@link TaskRunner}
     * 
     * @param taskRunner
     * @return
     */
    public AnalyzerBeansConfigurationImpl replace(TaskRunner taskRunner) {
        return new AnalyzerBeansConfigurationImpl(_datastoreCatalog, _referenceDataCatalog, _descriptorProvider,
                taskRunner, _storageProvider, _injectionManagerFactory);
    }

    public AnalyzerBeansConfigurationImpl replace(DescriptorProvider descriptorProvider) {
        return new AnalyzerBeansConfigurationImpl(_datastoreCatalog, _referenceDataCatalog, descriptorProvider,
                _taskRunner, _storageProvider, _injectionManagerFactory);
    }

    public AnalyzerBeansConfigurationImpl replace(DatastoreCatalog datastoreCatalog) {
        return new AnalyzerBeansConfigurationImpl(datastoreCatalog, _referenceDataCatalog, _descriptorProvider,
                _taskRunner, _storageProvider, _injectionManagerFactory);
    }

    public AnalyzerBeansConfigurationImpl replace(ReferenceDataCatalog referenceDataCatalog) {
        return new AnalyzerBeansConfigurationImpl(_datastoreCatalog, referenceDataCatalog, _descriptorProvider,
                _taskRunner, _storageProvider, _injectionManagerFactory);
    }

    public AnalyzerBeansConfigurationImpl replace(StorageProvider storageProvider) {
        return new AnalyzerBeansConfigurationImpl(_datastoreCatalog, _referenceDataCatalog, _descriptorProvider,
                _taskRunner, storageProvider, _injectionManagerFactory);
    }

    public AnalyzerBeansConfigurationImpl replace(InjectionManagerFactory injectionManagerFactory) {
        return new AnalyzerBeansConfigurationImpl(_datastoreCatalog, _referenceDataCatalog, _descriptorProvider,
                _taskRunner, _storageProvider, injectionManagerFactory);
    }

    @Override
    public DatastoreCatalog getDatastoreCatalog() {
        return _datastoreCatalog;
    }

    @Override
    public ReferenceDataCatalog getReferenceDataCatalog() {
        return _referenceDataCatalog;
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
    public TaskRunner getTaskRunner() {
        return _taskRunner;
    }

    @Override
    public InjectionManager getInjectionManager(AnalysisJob job) {
        return _injectionManagerFactory.getInjectionManager(this, job);
    }

}
