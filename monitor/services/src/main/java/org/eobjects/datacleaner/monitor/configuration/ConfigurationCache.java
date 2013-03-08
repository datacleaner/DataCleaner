/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.configuration;

import java.io.File;
import java.io.InputStream;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.configuration.DefaultConfigurationReaderInterceptor;
import org.eobjects.analyzer.configuration.InjectionManagerFactory;
import org.eobjects.analyzer.configuration.JaxbConfigurationReader;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.repository.file.FileRepositoryFolder;
import org.eobjects.metamodel.util.Func;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caches configuration objects for a tenant in order to avoid recreating it
 * every time it is needed.
 */
final class ConfigurationCache {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationCache.class);

    private final InjectionManagerFactory _injectionManagerFactory;
    private final RepositoryFolder _tenantFolder;
    private final RepositoryFile _file;
    private final String _tenantId;

    private volatile AnalyzerBeansConfiguration _configuration;
    private volatile long _lastModifiedCache;

    public ConfigurationCache(String tenantId, RepositoryFolder tenantFolder,
            InjectionManagerFactory injectionManagerFactory) {
        _tenantId = tenantId;
        _tenantFolder = tenantFolder;
        _injectionManagerFactory = injectionManagerFactory;

        RepositoryFile file = _tenantFolder.getFile("conf.xml");
        if (file == null) {
            file = _tenantFolder.createFile("conf.xml", new WriteDefaultTenantConfigurationAction());
        }
        _file = file;
    }

    public RepositoryFile getConfigurationFile() {
        return _file;
    }

    public AnalyzerBeansConfiguration getAnalyzerBeansConfiguration() {
        long lastModified = getConfigurationFile().getLastModified();
        if (_configuration == null || lastModified != _lastModifiedCache) {
            synchronized (this) {
                lastModified = _file.getLastModified();
                if (_configuration == null || lastModified != _lastModifiedCache) {
                    AnalyzerBeansConfiguration readConfiguration = readConfiguration();
                    AnalyzerBeansConfiguration decoratedConfiguration = decorateConfiguration(readConfiguration);
                    _configuration = decoratedConfiguration;
                }
            }
        }
        return _configuration;
    }

    protected AnalyzerBeansConfiguration decorateConfiguration(AnalyzerBeansConfiguration conf) {
        // set the injection manager factory on the configuration
        return conf;
    }

    protected AnalyzerBeansConfiguration readConfiguration() {
        final JaxbConfigurationReader reader = new JaxbConfigurationReader(new DefaultConfigurationReaderInterceptor() {
            @Override
            public String createFilename(String filename) {
                if (isAbsolute(filename)) {
                    return filename;
                }

                if (_tenantFolder instanceof FileRepositoryFolder) {
                    File file = ((FileRepositoryFolder) _tenantFolder).getFile();
                    return file.getAbsolutePath() + File.separatorChar + filename;
                }

                final String userHome = System.getProperty("user.home");
                final String result = userHome + File.separator + ".datacleaner/repository/" + _tenantId
                        + File.separator + filename;

                logger.warn("File path is relative, but repository is not file-based: {}. Returning: {}", filename,
                        result);

                return result;
            }

            @Override
            public AnalyzerBeansConfigurationImpl createBaseConfiguration() {
                return new AnalyzerBeansConfigurationImpl(_injectionManagerFactory);
            }
        });

        final RepositoryFile configurationFile = getConfigurationFile();
        _lastModifiedCache = configurationFile.getLastModified();

        logger.info("Reading configuration from file: {}", configurationFile);

        final AnalyzerBeansConfiguration readConfiguration = configurationFile
                .readFile(new Func<InputStream, AnalyzerBeansConfiguration>() {
                    @Override
                    public AnalyzerBeansConfiguration eval(InputStream inputStream) {
                        final AnalyzerBeansConfiguration readConfiguration = reader.read(inputStream);
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
}
