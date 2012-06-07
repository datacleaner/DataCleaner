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
import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.DefaultConfigurationReaderInterceptor;
import org.eobjects.analyzer.configuration.JaxbConfigurationReader;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.repository.file.FileRepositoryFolder;
import org.eobjects.metamodel.util.FileHelper;

/**
 * Caches configuration objects per tenant in order to avoid recreating it every
 * time it is needed.
 */
public class ConfigurationCache {

    private final Map<String, AnalyzerBeansConfiguration> _analyzerBeansConfigurations;
    private final Repository _repository;

    public ConfigurationCache(Repository repository) {
        _repository = repository;
        _analyzerBeansConfigurations = new HashMap<String, AnalyzerBeansConfiguration>();
    }

    public AnalyzerBeansConfiguration getAnalyzerBeansConfiguration(String tenantId) {
        AnalyzerBeansConfiguration conf = _analyzerBeansConfigurations.get(tenantId);
        if (conf == null) {
            synchronized (_analyzerBeansConfigurations) {
                conf = _analyzerBeansConfigurations.get(tenantId);
                if (conf == null) {
                    conf = readConfiguration(tenantId);
                }
            }
        }
        return conf;
    }

    public AnalyzerBeansConfiguration getAnalyzerBeansConfiguration(TenantIdentifier tenant) {
        final String tenantId = tenant.getId();
        return getAnalyzerBeansConfiguration(tenantId);
    }

    private AnalyzerBeansConfiguration readConfiguration(final String tenantId) {
        final RepositoryFolder tenantFolder = _repository.getFolder(tenantId);
        if (tenantFolder == null) {
            throw new IllegalStateException("No tenant folder: " + tenantId);
        }

        final RepositoryFile configurationFile = tenantFolder.getFile("conf.xml");
        if (configurationFile == null) {
            throw new IllegalStateException("No conf.xml file found for tenant: " + tenantId);
        }

        final JaxbConfigurationReader reader = new JaxbConfigurationReader(new DefaultConfigurationReaderInterceptor() {
            @Override
            public String createFilename(String filename) {
                if (tenantFolder instanceof FileRepositoryFolder) {
                    File file = ((FileRepositoryFolder) tenantFolder).getFile();
                    return file.getAbsolutePath() + File.separatorChar + filename;
                }
                // TODO: What about other repos?
                return super.createFilename(filename);
            }
        });
        final InputStream inputStream = configurationFile.readFile();
        try {
            final AnalyzerBeansConfiguration conf = reader.read(inputStream);
            _analyzerBeansConfigurations.put(tenantId, conf);
            return conf;
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }
}
