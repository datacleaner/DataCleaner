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
package org.eobjects.datacleaner.monitor.server;

import java.util.List;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.shared.DatastoreConnectionException;
import org.eobjects.datacleaner.monitor.shared.DatastoreService;
import org.eobjects.datacleaner.monitor.shared.model.ColumnIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.SchemaIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TableIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.Func;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link DatastoreService}
 */
@Component
public class DatastoreServiceImpl implements DatastoreService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final TenantContextFactory _tenantContextFactory;

    @Autowired
    public DatastoreServiceImpl(TenantContextFactory tenantContextFactory) {
        _tenantContextFactory = tenantContextFactory;
    }

    @Override
    public List<DatastoreIdentifier> getAvailableDatastores(TenantIdentifier tenant) {
        final DatastoreCatalog datastoreCatalog = getDatastoreCatalog(tenant);
        final String[] datastoreNames = datastoreCatalog.getDatastoreNames();

        return CollectionUtils.map(datastoreNames, new Func<String, DatastoreIdentifier>() {
            @Override
            public DatastoreIdentifier eval(String name) {
                return new DatastoreIdentifier(name);
            }
        });
    }

    @Override
    public SchemaIdentifier getDefaultSchema(TenantIdentifier tenant, DatastoreIdentifier datastoreId)
            throws DatastoreConnectionException {
        final DatastoreCatalog datastoreCatalog = getDatastoreCatalog(tenant);
        final Datastore datastore = datastoreCatalog.getDatastore(datastoreId.getName());
        if (datastore == null) {
            return null;
        }

        try {
            final DatastoreConnection con = datastore.openConnection();
            try {
                final Schema schema = con.getDataContext().getDefaultSchema();
                return new SchemaIdentifier(datastoreId, schema.getName());
            } finally {
                con.close();
            }
        } catch (Exception e) {
            logger.warn("Failed to open connection to datastore: " + datastoreId.getName(), e);
            throw new DatastoreConnectionException(e.getMessage());
        }
    }

    @Override
    public List<SchemaIdentifier> getSchemas(final TenantIdentifier tenant, final DatastoreIdentifier datastoreId)
            throws DatastoreConnectionException {
        final DatastoreCatalog datastoreCatalog = getDatastoreCatalog(tenant);
        final Datastore datastore = datastoreCatalog.getDatastore(datastoreId.getName());
        if (datastore == null) {
            return null;
        }

        try {
            final DatastoreConnection con = datastore.openConnection();
            try {
                final String[] schemaNames = con.getDataContext().getSchemaNames();
                final List<SchemaIdentifier> schemaIdentifiers = CollectionUtils.map(schemaNames,
                        new Func<String, SchemaIdentifier>() {
                            @Override
                            public SchemaIdentifier eval(String schemaName) {
                                return new SchemaIdentifier(datastoreId, schemaName);
                            }
                        });
                return schemaIdentifiers;
            } finally {
                con.close();
            }
        } catch (Exception e) {
            logger.warn("Failed to open connection to datastore: " + datastoreId.getName(), e);
            throw new DatastoreConnectionException(e.getMessage());
        }
    }

    @Override
    public List<TableIdentifier> getTables(final TenantIdentifier tenant, final SchemaIdentifier schemaId) {
        final DatastoreCatalog datastoreCatalog = getDatastoreCatalog(tenant);
        final String datastoreName = schemaId.getDatastore().getName();
        final Datastore datastore = datastoreCatalog.getDatastore(datastoreName);
        if (datastore == null) {
            return null;
        }
        final DatastoreConnection con = datastore.openConnection();
        try {
            final Schema schema = con.getDataContext().getSchemaByName(schemaId.getName());
            final String[] tableNames = schema.getTableNames();
            final List<TableIdentifier> tableIdentifiers = CollectionUtils.map(tableNames,
                    new Func<String, TableIdentifier>() {
                        @Override
                        public TableIdentifier eval(String tableName) {
                            return new TableIdentifier(schemaId, tableName);
                        }
                    });
            return tableIdentifiers;
        } finally {
            con.close();
        }
    }

    @Override
    public List<ColumnIdentifier> getColumns(final TenantIdentifier tenant, final TableIdentifier tableId) {
        final DatastoreCatalog datastoreCatalog = getDatastoreCatalog(tenant);
        final SchemaIdentifier schemaId = tableId.getSchema();
        final String datastoreName = schemaId.getDatastore().getName();
        final Datastore datastore = datastoreCatalog.getDatastore(datastoreName);
        if (datastore == null) {
            return null;
        }
        final DatastoreConnection con = datastore.openConnection();
        try {
            final Schema schema = con.getDataContext().getSchemaByName(schemaId.getName());
            final Table table = schema.getTableByName(tableId.getName());
            final String[] columnNames = table.getColumnNames();
            final List<ColumnIdentifier> columnIdentifiers = CollectionUtils.map(columnNames,
                    new Func<String, ColumnIdentifier>() {
                        @Override
                        public ColumnIdentifier eval(String columnName) {
                            return new ColumnIdentifier(tableId, columnName);
                        }
                    });
            return columnIdentifiers;
        } finally {
            con.close();
        }
    }

    private DatastoreCatalog getDatastoreCatalog(TenantIdentifier tenant) {
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final DatastoreCatalog datastoreCatalog = tenantContext.getConfiguration().getDatastoreCatalog();
        return datastoreCatalog;
    }

}
