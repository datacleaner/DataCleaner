/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.user;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.configuration.JaxbConfigurationReader;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.descriptors.SimpleDescriptorProvider;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.reference.ReferenceDataCatalogImpl;
import org.eobjects.analyzer.storage.InMemoryStorageProvider;
import org.eobjects.datacleaner.util.ResourceManager;
import org.eobjects.metamodel.util.FileHelper;
import org.eobjects.metamodel.util.LazyRef;
import org.eobjects.metamodel.util.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads {@link AnalyzerBeansConfiguration} from conf.xml and decorates it with
 * additional configuration from {@link UserPreferences}.
 */
public class DataCleanerConfigurationReader extends LazyRef<AnalyzerBeansConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(DataCleanerConfigurationReader.class);

    private final FileObject _dataCleanerHome;
    private final FileObject _configurationFile;
    private final Ref<UserPreferences> _userPreferencesRef;

    public DataCleanerConfigurationReader(final FileObject dataCleanerHome, final FileObject configurationFile,
            final Ref<UserPreferences> userPreferencesRef) {
        _dataCleanerHome = dataCleanerHome;
        _configurationFile = configurationFile;
        _userPreferencesRef = userPreferencesRef;
    }

    @Override
    protected AnalyzerBeansConfiguration fetch() {
        // load user preferences first, since we need it while reading
        // the configuration (some custom elements may refer to classes
        // within the extensions)
        UserPreferences userPreferences = _userPreferencesRef.get();
        final List<ExtensionPackage> extensionPackages = userPreferences.getExtensionPackages();
        for (ExtensionPackage extensionPackage : extensionPackages) {
            extensionPackage.loadExtension();
        }

        // load the configuration file
        final JaxbConfigurationReader configurationReader = new JaxbConfigurationReader(
                new DataCleanerConfigurationReaderInterceptor(_dataCleanerHome));

        boolean exists;
        try {
            exists = _configurationFile != null && _configurationFile.exists();
        } catch (FileSystemException e1) {
            logger.debug("Could not determine if configuration file exists");
            exists = false;
        }

        final AnalyzerBeansConfiguration c;
        if (exists) {
            InputStream inputStream = null;
            try {
                inputStream = _configurationFile.getContent().getInputStream();
                
                c = configurationReader.create(inputStream);
                logger.info("Succesfully read configuration from {}", _configurationFile.getName().getPath());
            } catch (Exception e) {
                FileHelper.safeClose(inputStream);
                try {
                    inputStream = _configurationFile.getContent().getInputStream();
                    String content = FileHelper.readInputStreamAsString(inputStream, FileHelper.DEFAULT_ENCODING);
                    logger.error("Failed to read configuration file. File contents was:");
                    logger.error(content);
                } catch (Throwable t) {
                    logger.debug("Failed to re-open configuration file to determine file contents", e);
                }

                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new IllegalStateException("Unexpected error while reading configuration file: "
                        + _configurationFile, e);
            } finally {
                FileHelper.safeClose(inputStream);
            }
        } else {
            logger.info("Configuration file does not exist, reading built-in configuration.");
            c = getConfigurationFromClasspath(configurationReader);
        }

        return c;
    }

    private AnalyzerBeansConfiguration getConfigurationFromClasspath(JaxbConfigurationReader configurationReader) {
        logger.info("Reading conf.xml from classpath");
        try {
            return configurationReader.create(ResourceManager.getInstance().getUrl("datacleaner-home/conf.xml")
                    .openStream());
        } catch (Exception ex2) {
            logger.warn("Unexpected error while reading conf.xml from classpath!", ex2);
            logger.warn("Creating a bare-minimum configuration because of previous errors!");
            return new AnalyzerBeansConfigurationImpl(new DatastoreCatalogImpl(), new ReferenceDataCatalogImpl(),
                    new SimpleDescriptorProvider(), new SingleThreadedTaskRunner(), new InMemoryStorageProvider());
        }
    }

}
