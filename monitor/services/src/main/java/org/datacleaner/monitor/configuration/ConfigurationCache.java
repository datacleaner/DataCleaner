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

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.metamodel.util.Func;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DefaultConfigurationReaderInterceptor;
import org.datacleaner.configuration.JaxbConfigurationReader;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFileResourceTypeHandler;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.repository.file.FileRepositoryFolder;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;
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
    private final DataCleanerEnvironment _environment;
    private final Repository _repository;

    private volatile DataCleanerConfiguration _configuration;
    private volatile long _lastModifiedCache;

    public ConfigurationCache(DataCleanerEnvironment environment, TenantContext tenantContext,
            Repository repository) {
        _environment = environment;
        _tenantContext = tenantContext;
        _repository = repository;

        final RepositoryFolder tenantFolder = _tenantContext.getTenantRootFolder();

        RepositoryFile file = tenantFolder.getFile("conf.xml");
        if (file == null) {
            file = tenantFolder.createFile("conf.xml", new WriteDefaultTenantConfigurationAction());
        }
        _file = file;
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
        final JaxbConfigurationReader reader = new JaxbConfigurationReader(new DefaultConfigurationReaderInterceptor() {

            @Override
            protected File getRelativeParentDirectory() {
                if (_tenantContext.getTenantRootFolder() instanceof FileRepositoryFolder) {
                    FileRepositoryFolder tenantFolder = (FileRepositoryFolder) _tenantContext.getTenantRootFolder();
                    File file = tenantFolder.getFile();
                    return file;
                }

                final String userHome = System.getProperty("user.home");
                final String result = userHome + File.separator + ".datacleaner/repository/"
                        + _tenantContext.getTenantId();

                return new File(result);
            }

            @Override
            public String createFilename(String filename) {
                if (isAbsolute(filename)) {
                    return super.createFilename(filename);
                }

                if (_tenantContext.getTenantRootFolder() instanceof FileRepositoryFolder) {
                    // for FileRepository implementations, the super
                    // implementation will also "just work" because of the above
                    // getRelativeParentDirectory method.
                    return super.createFilename(filename);
                }

                final String userHome = System.getProperty("user.home");
                final String result = userHome + File.separator + ".datacleaner/repository/"
                        + _tenantContext.getTenantId() + File.separator + filename;

                logger.warn("File path is relative, but repository is not file-based: {}. Returning: {}", filename,
                        result);

                return result;
            }

            @Override
            protected List<ResourceTypeHandler<?>> getResourceTypeHandlers() {
                List<ResourceTypeHandler<?>> handlers = super.getResourceTypeHandlers();
                handlers.add(new RepositoryFileResourceTypeHandler(_repository, _tenantContext.getTenantId()));
                return handlers;
            }

            @Override
            public DataCleanerEnvironment createBaseEnvironment() {
                return _environment;
            }
            
            @Override
            public RepositoryFolder getHomeFolder() {
                return _tenantContext.getTenantRootFolder();
            }
        });

        final RepositoryFile configurationFile = getConfigurationFile();
        _lastModifiedCache = configurationFile.getLastModified();
        if (_lastModifiedCache < 0) {
            logger.warn(
                    "Last modified timestamp was negative ({})! Returning plain AnalyzerBeansConfiguration since this indicates that the file has been deleted.",
                    _lastModifiedCache);
            return new DataCleanerConfigurationImpl(_environment, _tenantContext.getTenantRootFolder());
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

    private boolean isAbsolute(String filename) {
        assert filename != null;

        File file = new File(filename);
        return file.isAbsolute();
    }

    public void clearCache() {
        _configuration = null;
        _lastModifiedCache = -1;
    }
}
