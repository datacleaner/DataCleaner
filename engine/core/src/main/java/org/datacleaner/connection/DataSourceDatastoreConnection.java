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
package org.datacleaner.connection;

import javax.sql.DataSource;

import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.TableType;

/**
 * A {@link DatastoreConnection} based on a {@link DataSource}.
 */
public class DataSourceDatastoreConnection extends UsageAwareDatastoreConnection<UpdateableDataContext> implements
        UpdateableDatastoreConnection {

    private final UpdateableDataContext _dataContext;
    private final SchemaNavigator _schemaNavigator;

    public DataSourceDatastoreConnection(DataSource ds, Datastore datastore) {
        this(ds, TableType.DEFAULT_TABLE_TYPES, null, datastore);
    }

    public DataSourceDatastoreConnection(DataSource ds, TableType[] tableTypes, String catalogName, Datastore datastore) {
        super(datastore);
        _dataContext = new JdbcDataContext(ds, tableTypes, catalogName);
        _schemaNavigator = new SchemaNavigator(_dataContext);
    }
    
    public DataSourceDatastoreConnection(UpdateableDataContext dataContext, Datastore datastore) {
        super(datastore);
        _dataContext = dataContext;
        _schemaNavigator = new SchemaNavigator(_dataContext);
    }

    @Override
    public UpdateableDataContext getDataContext() {
        return _dataContext;
    }

    @Override
    public UpdateableDataContext getUpdateableDataContext() {
        return _dataContext;
    }

    @Override
    public SchemaNavigator getSchemaNavigator() {
        return _schemaNavigator;
    }

    @Override
    protected void closeInternal() {
        // do nothing, the JdbcDataContext will automatically close pooled
        // connections in the datasource
    }
}
