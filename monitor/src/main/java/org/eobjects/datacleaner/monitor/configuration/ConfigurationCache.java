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
import org.eobjects.analyzer.configuration.DefaultConfigurationReaderInterceptor;
import org.eobjects.analyzer.configuration.JaxbConfigurationReader;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.repository.file.FileRepositoryFolder;
import org.eobjects.metamodel.util.FileHelper;

/**
 * Caches configuration objects for a tenant in order to avoid recreating it
 * every time it is needed.
 */
final class ConfigurationCache {

    private final Repository _repository;
    private final RepositoryFolder _tenantFolder;
    private final RepositoryFile _file;

    private volatile AnalyzerBeansConfiguration _configuration;
    private volatile long _lastModifiedCache;

    public ConfigurationCache(String tenantId, Repository repository) {
        _repository = repository;

        _tenantFolder = _repository.getFolder(tenantId);
        if (_tenantFolder == null) {
            throw new IllegalStateException("No tenant folder: " + tenantId);
        }

        _file = _tenantFolder.getFile("conf.xml");
        if (_file == null) {
            throw new IllegalStateException("No conf.xml file found for tenant: " + tenantId);
        }
    }

    public AnalyzerBeansConfiguration getAnalyzerBeansConfiguration() {
        long lastModified = _file.getLastModified();
        if (_configuration == null || lastModified != _lastModifiedCache) {
            synchronized (this) {
                lastModified = _file.getLastModified();
                if (_configuration == null || lastModified != _lastModifiedCache) {
                    _configuration = readConfiguration();
                }
            }
        }
        return _configuration;
    }

    private AnalyzerBeansConfiguration readConfiguration() {
        final JaxbConfigurationReader reader = new JaxbConfigurationReader(new DefaultConfigurationReaderInterceptor() {
            @Override
            public String createFilename(String filename) {
                if (_tenantFolder instanceof FileRepositoryFolder) {
                    File file = ((FileRepositoryFolder) _tenantFolder).getFile();
                    return file.getAbsolutePath() + File.separatorChar + filename;
                }
                // TODO: What about other non-file based repos?
                return super.createFilename(filename);
            }
        });
        final InputStream inputStream = _file.readFile();
        try {
            final AnalyzerBeansConfiguration conf = reader.read(inputStream);
            return conf;
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }
}
