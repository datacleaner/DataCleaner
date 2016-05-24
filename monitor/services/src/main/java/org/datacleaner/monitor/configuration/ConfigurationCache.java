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
package org.datacleaner.monitor.configuration;

import java.io.InputStream;
import java.util.Map;

import org.apache.metamodel.util.Func;
import org.datacleaner.configuration.ConfigurationReaderInterceptor;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.configuration.DataCleanerHomeFolder;
import org.datacleaner.configuration.DataCleanerHomeFolderImpl;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.configuration.JaxbConfigurationReader;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caches configuration objects for a tenant in order to avoid recreating it
 * every time it is needed.
 */
final class ConfigurationCache {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationCache.class);

    private final RepositoryFile _file;
    private final TenantContext _tenantContext;
    private final InjectionManagerFactory _injectionManagerFactory;
    private final Repository _repository;

    private volatile DataCleanerConfiguration _configuration;
    private volatile long _lastModifiedCache;
    
    public ConfigurationCache(InjectionManagerFactory injectionManagerFactory, TenantContext tenantContext, Repository repository) {
        _injectionManagerFactory = injectionManagerFactory;
        _tenantContext = tenantContext;
        _repository = repository;
        
        final RepositoryFolder tenantFolder = _tenantContext.getTenantRootFolder();
        
        RepositoryFile file = tenantFolder.getFile(DataCleanerConfigurationImpl.DEFAULT_FILENAME);
        if (file == null) {
            file = tenantFolder.createFile(DataCleanerConfigurationImpl.DEFAULT_FILENAME,
                    new WriteDefaultTenantConfigurationAction());
        }
        _file = file;
    }

    /**
     * 
     * @param environment
     * @param tenantContext
     * @param repository
     * 
     * @deprecated use {@link #ConfigurationCache(InjectionManagerFactory, TenantContext, Repository)} instead
     */
    @Deprecated
    public ConfigurationCache(DataCleanerEnvironment environment, TenantContext tenantContext, Repository repository) {
        this(environment.getInjectionManagerFactory(), tenantContext, repository);
    }

    public RepositoryFile getConfigurationFile() {
        return _file;
    }

    public DataCleanerConfiguration getAnalyzerBeansConfiguration() {
        long lastModified = getConfigurationFile().getLastModified();
        if (_configuration == null || lastModified != _lastModifiedCache) {
            synchronized (this) {
                lastModified = _file.getLastModified();
                if (_configuration == null || lastModified != _lastModifiedCache) {
                    DataCleanerConfiguration readConfiguration = readConfiguration();
                    DataCleanerConfiguration decoratedConfiguration = decorateConfiguration(readConfiguration);
                    _configuration = decoratedConfiguration;
                }
            }
        }
        return _configuration;
    }

    protected DataCleanerConfiguration decorateConfiguration(DataCleanerConfiguration conf) {
        // set the injection manager factory on the configuration
        return conf;
    }

    protected DataCleanerConfiguration readConfiguration() {
        return readConfiguration(null);
    }
    
    DataCleanerConfiguration readConfiguration(Map<String, String> overrideProperties) {
        final ConfigurationReaderInterceptor interceptor = new MonitorConfigurationReaderInterceptor(_repository,
                _tenantContext, overrideProperties, _injectionManagerFactory);
        final JaxbConfigurationReader reader = new JaxbConfigurationReader(interceptor);

        final RepositoryFile configurationFile = getConfigurationFile();
        _lastModifiedCache = configurationFile.getLastModified();
        if (_lastModifiedCache < 0) {
            logger.warn(
                    "Last modified timestamp was negative ({})! Returning plain DataCleanerConfiguration since this indicates that the file has been deleted.",
                    _lastModifiedCache);
            final RepositoryFolder tenantRootFolder = _tenantContext.getTenantRootFolder();
            final DataCleanerHomeFolder homeFolder = new DataCleanerHomeFolderImpl(tenantRootFolder);
            final DataCleanerEnvironmentImpl baseEnvironment = MonitorConfigurationReaderInterceptor.createBaseEnvironment(_injectionManagerFactory);
            return new DataCleanerConfigurationImpl(baseEnvironment, homeFolder);
        }

        logger.info("Reading configuration from file: {}", configurationFile);

        final DataCleanerConfiguration readConfiguration = configurationFile
                .readFile(new Func<InputStream, DataCleanerConfiguration>() {
                    @Override
                    public DataCleanerConfiguration eval(InputStream inputStream) {
                        final DataCleanerConfiguration readConfiguration = reader.read(inputStream);
                        return readConfiguration;
                    }
                });

        return readConfiguration;
    }

    public void clearCache() {
        _configuration = null;
        _lastModifiedCache = -1;
    }
}
