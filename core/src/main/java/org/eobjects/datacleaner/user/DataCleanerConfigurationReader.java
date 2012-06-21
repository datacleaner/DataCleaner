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
package org.eobjects.datacleaner.user;

import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.configuration.JaxbConfigurationReader;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.descriptors.SimpleDescriptorProvider;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.reference.ReferenceDataCatalogImpl;
import org.eobjects.analyzer.storage.InMemoryStorageProvider;
import org.eobjects.datacleaner.util.ResourceManager;
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

        AnalyzerBeansConfiguration c;
        try {
            c = configurationReader.create(_configurationFile.getContent().getInputStream());
            logger.info("Succesfully read configuration from {}", _configurationFile.getName().getPath());
        } catch (Exception ex1) {
            logger.warn("Unexpected error while reading conf.xml from DataCleanerHome!", ex1);
            logger.info("Reading conf.xml from classpath");
            try {
                c = configurationReader.create(ResourceManager.getInstance().getUrl("datacleaner-home/conf.xml")
                        .openStream());
            } catch (Exception ex2) {
                logger.warn("Unexpected error while reading conf.xml from classpath!", ex2);
                logger.warn("Creating a bare-minimum configuration because of previous errors!");
                c = new AnalyzerBeansConfigurationImpl(new DatastoreCatalogImpl(), new ReferenceDataCatalogImpl(),
                        new SimpleDescriptorProvider(), new SingleThreadedTaskRunner(), new InMemoryStorageProvider());
            }
        }

        return c;
    }

}
