/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.user;

import java.io.InputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.LazyRef;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.JaxbConfigurationReader;
import org.datacleaner.util.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads {@link DataCleanerConfiguration} from conf.xml and decorates it with
 * additional configuration from {@link UserPreferences}.
 */
public class DataCleanerConfigurationReader extends LazyRef<DataCleanerConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(DataCleanerConfigurationReader.class);

    private final FileObject _dataCleanerHome;
    private final FileObject _configurationFile;

    public DataCleanerConfigurationReader(final FileObject dataCleanerHome, final FileObject configurationFile) {
        _dataCleanerHome = dataCleanerHome;
        _configurationFile = configurationFile;
    }

    /**
     * Gets the configuration file, if any. Note that in embedded mode or during
     * tests etc. there might not be a configuration file, and this method may
     * return null!
     *
     * @return
     */
    public FileObject getConfigurationFile() {
        return _configurationFile;
    }

    @Override
    protected DataCleanerConfiguration fetch() {
        // load the configuration file
        final JaxbConfigurationReader configurationReader =
                new JaxbConfigurationReader(new DesktopConfigurationReaderInterceptor(_dataCleanerHome));

        boolean exists;
        try {
            exists = _configurationFile != null && _configurationFile.exists();
        } catch (final FileSystemException e) {
            logger.debug("Could not determine if configuration file exists");
            exists = false;
        }

        final DataCleanerConfiguration c;
        if (exists) {
            InputStream inputStream = null;
            try {
                inputStream = _configurationFile.getContent().getInputStream();

                c = configurationReader.create(inputStream);
                logger.info("Succesfully read configuration from {}", _configurationFile.getName().getPath());
            } catch (final Exception e) {
                FileHelper.safeClose(inputStream);
                try {
                    inputStream = _configurationFile.getContent().getInputStream();
                    final String content = FileHelper.readInputStreamAsString(inputStream, FileHelper.DEFAULT_ENCODING);
                    logger.error("Failed to read configuration file {}. File contents was:", _configurationFile);
                    logger.error(content);
                } catch (final Throwable t) {
                    logger.debug("Failed to re-open configuration file to determine file contents", e);
                }

                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new IllegalStateException(
                        "Unexpected error while reading configuration file: " + _configurationFile, e);
            } finally {
                FileHelper.safeClose(inputStream);
            }
        } else {
            logger.info("Configuration file does not exist, reading built-in configuration.");
            c = getConfigurationFromClasspath(configurationReader);
        }

        return c;
    }

    private DataCleanerConfiguration getConfigurationFromClasspath(final JaxbConfigurationReader configurationReader) {
        logger.info("Reading conf.xml from classpath");
        try {
            return configurationReader.create(ResourceManager.get().getUrl("datacleaner-home/conf.xml").openStream());
        } catch (final Exception e) {
            logger.warn("Unexpected error while reading conf.xml from classpath!", e);
            logger.warn("Creating a bare-minimum configuration because of previous errors!");
            return new DataCleanerConfigurationImpl();
        }
    }

}
