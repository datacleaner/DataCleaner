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

import java.io.File;

import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.datacleaner.reference.ReferenceDataCatalogImpl;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.repository.file.FileRepository;
import org.datacleaner.storage.StorageProvider;

/**
 * 
 *
 * @deprecated use {@link DataCleanerConfigurationImpl} instead
 */
@Deprecated
public final class AnalyzerBeansConfigurationImpl implements AnalyzerBeansConfiguration {

    private static final long serialVersionUID = 1L;

    private final transient DataCleanerEnvironment _environment;
    private final transient RepositoryFolder _homeFolder;
    private final DatastoreCatalog _datastoreCatalog;
    private final ReferenceDataCatalog _referenceDataCatalog;

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
        this(defaultDatastoreCatalog(), defaultReferenceDataCatalog(), DataCleanerEnvironmentImpl
                .defaultDescriptorProvider(), DataCleanerEnvironmentImpl.defaultTaskRunner(),
                DataCleanerEnvironmentImpl.defaultStorageProvider());
    }

    /**
     * Creates a minimalistic configuration object with a specific
     * {@link InjectionManagerFactory}, mostly suitable for stubbing and
     * testing.
     * 
     * @param injectionManagerFactory
     */
    public AnalyzerBeansConfigurationImpl(InjectionManagerFactory injectionManagerFactory) {
        this(defaultDatastoreCatalog(), defaultReferenceDataCatalog(), DataCleanerEnvironmentImpl
                .defaultDescriptorProvider(), DataCleanerEnvironmentImpl.defaultTaskRunner(),
                DataCleanerEnvironmentImpl.defaultStorageProvider(), injectionManagerFactory, defaultHomeFolder());
    }

    private static RepositoryFolder defaultHomeFolder() {
        return new FileRepository(new File("."));
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
        this(datastoreCatalog, referenceDataCatalog, descriptorProvider, taskRunner, storageProvider, null,
                defaultHomeFolder());
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
            InjectionManagerFactory injectionManagerFactory, RepositoryFolder homeFolder) {
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
        _homeFolder = homeFolder;
        _environment = new DataCleanerEnvironmentImpl(taskRunner, descriptorProvider, storageProvider,
                injectionManagerFactory);
    }

    public AnalyzerBeansConfigurationImpl replace(TaskRunner taskRunner) {
        return new AnalyzerBeansConfigurationImpl(_datastoreCatalog, _referenceDataCatalog, getDescriptorProvider(),
                taskRunner, getStorageProvider(), getInjectionManagerFactory(), _homeFolder);
    }

    public AnalyzerBeansConfigurationImpl replace(DescriptorProvider descriptorProvider) {
        return new AnalyzerBeansConfigurationImpl(_datastoreCatalog, _referenceDataCatalog, descriptorProvider,
                getTaskRunner(), getStorageProvider(), getInjectionManagerFactory(), _homeFolder);
    }

    public AnalyzerBeansConfigurationImpl replace(RepositoryFolder homeFolder) {
        return new AnalyzerBeansConfigurationImpl(_datastoreCatalog, _referenceDataCatalog, getDescriptorProvider(),
                getTaskRunner(), getStorageProvider(), getInjectionManagerFactory(), homeFolder);
    }

    public AnalyzerBeansConfigurationImpl replace(DatastoreCatalog datastoreCatalog) {
        return new AnalyzerBeansConfigurationImpl(datastoreCatalog, _referenceDataCatalog, getDescriptorProvider(),
                getTaskRunner(), getStorageProvider(), getInjectionManagerFactory(), _homeFolder);
    }

    public AnalyzerBeansConfigurationImpl replace(ReferenceDataCatalog referenceDataCatalog) {
        return new AnalyzerBeansConfigurationImpl(_datastoreCatalog, referenceDataCatalog, getDescriptorProvider(),
                getTaskRunner(), getStorageProvider(), getInjectionManagerFactory(), _homeFolder);
    }

    public AnalyzerBeansConfigurationImpl replace(StorageProvider storageProvider) {
        return new AnalyzerBeansConfigurationImpl(_datastoreCatalog, _referenceDataCatalog, getDescriptorProvider(),
                getTaskRunner(), storageProvider, getInjectionManagerFactory(), _homeFolder);
    }

    public AnalyzerBeansConfigurationImpl replace(InjectionManagerFactory injectionManagerFactory) {
        return new AnalyzerBeansConfigurationImpl(_datastoreCatalog, _referenceDataCatalog, getDescriptorProvider(),
                getTaskRunner(), getStorageProvider(), injectionManagerFactory, _homeFolder);
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
        return _environment.getDescriptorProvider();
    }

    @Override
    public StorageProvider getStorageProvider() {
        return _environment.getStorageProvider();
    }

    @Override
    public TaskRunner getTaskRunner() {
        return _environment.getTaskRunner();
    }

    @Override
    public InjectionManager getInjectionManager(AnalysisJob job) {
        return getInjectionManagerFactory().getInjectionManager(this, job);
    }

    @Override
    public InjectionManagerFactory getInjectionManagerFactory() {
        return _environment.getInjectionManagerFactory();
    }

    @Override
    public DataCleanerEnvironment getEnvironment() {
        return _environment;
    }

    @Override
    public RepositoryFolder getHomeFolder() {
        return _homeFolder;
    }
}
