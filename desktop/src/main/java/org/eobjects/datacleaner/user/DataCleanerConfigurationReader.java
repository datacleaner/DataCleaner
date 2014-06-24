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

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.eobjects.datacleaner.extensions.ExtensionReader;
import org.eobjects.datacleaner.util.FileFilters;
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

        loadExtensions(userPreferences);

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

    private void loadExtensions(UserPreferences userPreferences) {
        final String dumpInstallKey = "org.eobjects.datacleaner.extension.dumpinstall";
        final File extensionsDirectory = userPreferences.getExtensionsDirectory();

        final Set<String> extensionFilenames = new HashSet<String>();
        final List<ExtensionPackage> extensionPackages = userPreferences.getExtensionPackages();
        for (Iterator<ExtensionPackage> it = extensionPackages.iterator(); it.hasNext();) {
            final ExtensionPackage extensionPackage = (ExtensionPackage) it.next();

            // some extensions may be installed simply by "dumping" a JAR in the
            // extension folder. Such installs will have this key registered.
            final boolean dumpInstalled = "true".equals(extensionPackage.getAdditionalProperties().get(dumpInstallKey));
            boolean remove = false;

            final File[] files = extensionPackage.getFiles();
            for (File file : files) {
                if (dumpInstalled && !file.exists()) {
                    // file has been removed, we'll remove this extension
                    remove = true;
                    break;
                } else {
                    final File directory = file.getParentFile();
                    if (extensionsDirectory.equals(directory)) {
                        extensionFilenames.add(file.getName());
                    }
                }
            }

            if (remove) {
                it.remove();
            } else {
                extensionPackage.loadExtension();
            }
        }

        // Read all JAR files in the 'extensions' directory and register those
        // that are not already loaded.
        final File[] jarFiles = extensionsDirectory.listFiles(FileFilters.JAR);
        if (jarFiles != null) {
            for (File file : jarFiles) {
                final String filename = file.getName();
                if (!extensionFilenames.contains(filename)) {
                    logger.info("Adding extension from 'extension' folder: {}", file.getName());
                    ExtensionReader reader = new ExtensionReader();
                    ExtensionPackage extension = reader.readExternalExtension(file);
                    userPreferences.addExtensionPackage(extension);
                    extension.getAdditionalProperties().put(dumpInstallKey, "true");
                    extension.loadExtension();
                }
            }
        }

        // List directories and treat each sub directory with JAR files as
        // an extension
        final File[] subDirectories = extensionsDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        if (subDirectories != null) {
            for (File subDirectory : subDirectories) {
                final String directoryName = subDirectory.getName();
                if (!extensionFilenames.contains(directoryName)) {
                    logger.info("Adding extension from 'extension' folder: {}", directoryName);
                    ExtensionReader reader = new ExtensionReader();
                    ExtensionPackage extension = reader.readExternalExtension(subDirectory);
                    if (extension != null) {
                        userPreferences.addExtensionPackage(extension);
                        extension.getAdditionalProperties().put(dumpInstallKey, "true");
                        extension.loadExtension();
                    }
                }
            }
        }
    }

    private AnalyzerBeansConfiguration getConfigurationFromClasspath(JaxbConfigurationReader configurationReader) {
        logger.info("Reading conf.xml from classpath");
        try {
            return configurationReader.create(ResourceManager.get().getUrl("datacleaner-home/conf.xml")
                    .openStream());
        } catch (Exception ex2) {
            logger.warn("Unexpected error while reading conf.xml from classpath!", ex2);
            logger.warn("Creating a bare-minimum configuration because of previous errors!");
            return new AnalyzerBeansConfigurationImpl(new DatastoreCatalogImpl(), new ReferenceDataCatalogImpl(),
                    new SimpleDescriptorProvider(), new SingleThreadedTaskRunner(), new InMemoryStorageProvider());
        }
    }

}
