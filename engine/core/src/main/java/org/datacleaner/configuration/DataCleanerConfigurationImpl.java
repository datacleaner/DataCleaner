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

import java.util.Collection;

import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.datacleaner.reference.ReferenceDataCatalogImpl;
import org.datacleaner.repository.file.FileRepository;

/**
 * Default (immutable) implementation of {@link DataCleanerConfiguration}
 */
public class DataCleanerConfigurationImpl implements DataCleanerConfiguration {

    private static final long serialVersionUID = 1L;

    /**
     * The default filename used to store the configuration file in DataCleaner
     * (without any directory specification, since it varies).
     */
    public static final String DEFAULT_FILENAME = "conf.xml";

    private final transient DataCleanerEnvironment _environment;
    private final transient DataCleanerHomeFolder _homeFolder;
    private final DatastoreCatalog _datastoreCatalog;
    private final ReferenceDataCatalog _referenceDataCatalog;
    private final ServerInformationCatalog _serverInformationCatalog;

    public DataCleanerConfigurationImpl() {
        this(defaultEnvironment(), defaultHomeFolder(), defaultDatastoreCatalog(), defaultReferenceDataCatalog(),
                defaultServerInformationCatalog());
    }

    public DataCleanerConfigurationImpl(DataCleanerEnvironment environment, DataCleanerHomeFolder homeFolder) {
        this(environment, homeFolder, defaultDatastoreCatalog(), defaultReferenceDataCatalog(),
                defaultServerInformationCatalog());
    }

    public DataCleanerConfigurationImpl(DataCleanerConfiguration c) {
        this(c.getEnvironment(), c.getHomeFolder(), c.getDatastoreCatalog(), c.getReferenceDataCatalog(),
                defaultServerInformationCatalog());
    }

    public DataCleanerConfigurationImpl(DataCleanerEnvironment environment, DataCleanerHomeFolder homeFolder,
            DatastoreCatalog datastoreCatalog, ReferenceDataCatalog referenceDataCatalog) {
        this(environment, homeFolder, datastoreCatalog, referenceDataCatalog, defaultServerInformationCatalog());
    }

    public DataCleanerConfigurationImpl(DataCleanerEnvironment environment, DataCleanerHomeFolder homeFolder,
            DatastoreCatalog datastoreCatalog, ReferenceDataCatalog referenceDataCatalog,
            ServerInformationCatalog serverInformationCatalog) {
        if (environment == null) {
            _environment = defaultEnvironment();
        } else {
            _environment = environment;
        }
        if (homeFolder == null) {
            _homeFolder = defaultHomeFolder();
        } else {
            _homeFolder = homeFolder;
        }
        if (datastoreCatalog == null) {
            _datastoreCatalog = defaultDatastoreCatalog();
        } else {
            _datastoreCatalog = datastoreCatalog;
        }
        if (referenceDataCatalog == null) {
            _referenceDataCatalog = defaultReferenceDataCatalog();
        } else {
            _referenceDataCatalog = referenceDataCatalog;
        }

        if(serverInformationCatalog == null) {
            _serverInformationCatalog = defaultServerInformationCatalog();
        } else {
            _serverInformationCatalog = serverInformationCatalog;
        }
    }

    public DataCleanerConfigurationImpl withDatastoreCatalog(DatastoreCatalog datastoreCatalog) {
        return new DataCleanerConfigurationImpl(getEnvironment(), getHomeFolder(), datastoreCatalog,
                getReferenceDataCatalog(), getServerInformationCatalog());
    }

    public DataCleanerConfigurationImpl withDatastores(Datastore... datastores) {
        return withDatastoreCatalog(new DatastoreCatalogImpl(datastores));
    }

    public DataCleanerConfigurationImpl withDatastores(Collection<Datastore> datastores) {
        return withDatastoreCatalog(new DatastoreCatalogImpl(datastores));
    }

    public DataCleanerConfigurationImpl withReferenceDataCatalog(ReferenceDataCatalog referenceDataCatalog) {
        return new DataCleanerConfigurationImpl(getEnvironment(), getHomeFolder(), getDatastoreCatalog(),
                referenceDataCatalog, getServerInformationCatalog());
    }

    public DataCleanerConfigurationImpl withHomeFolder(DataCleanerHomeFolder homeFolder) {
        return new DataCleanerConfigurationImpl(getEnvironment(), homeFolder, getDatastoreCatalog(),
                getReferenceDataCatalog(), getServerInformationCatalog());
    }

    public DataCleanerConfigurationImpl withEnvironment(DataCleanerEnvironment environment) {
        return new DataCleanerConfigurationImpl(environment, getHomeFolder(), getDatastoreCatalog(),
                getReferenceDataCatalog(), getServerInformationCatalog());
    }

    public DataCleanerConfigurationImpl withServerInformationCatalog(
            final ServerInformationCatalog serverInformationCatalog) {
        return new DataCleanerConfigurationImpl(getEnvironment(), getHomeFolder(), getDatastoreCatalog(),
                getReferenceDataCatalog(), serverInformationCatalog);
    }

    @Override
    public DatastoreCatalog getDatastoreCatalog() {
        return _datastoreCatalog;
    }

    @Override
    public ReferenceDataCatalog getReferenceDataCatalog() {
        return _referenceDataCatalog;
    }

    public ServerInformationCatalog getServerInformationCatalog() {
        return _serverInformationCatalog;
    }

    @Override
    public DataCleanerHomeFolder getHomeFolder() {
        return _homeFolder;
    }

    @Override
    public DataCleanerEnvironment getEnvironment() {
        return _environment;
    }

    public static ReferenceDataCatalog defaultReferenceDataCatalog() {
        return new ReferenceDataCatalogImpl();
    }

    public static DatastoreCatalog defaultDatastoreCatalog() {
        return new DatastoreCatalogImpl();
    }

    public static ServerInformationCatalog defaultServerInformationCatalog() {
        return new ServerInformationCatalogImpl();
    }

    public static DataCleanerHomeFolder defaultHomeFolder() {
        return new DataCleanerHomeFolderImpl(new FileRepository("."));
    }

    public static DataCleanerEnvironment defaultEnvironment() {
        return new DataCleanerEnvironmentImpl();
    }
}
