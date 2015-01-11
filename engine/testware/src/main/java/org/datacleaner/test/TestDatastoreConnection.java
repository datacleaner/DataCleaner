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
package org.datacleaner.test;

import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.SchemaNavigator;
import org.datacleaner.connection.UpdateableDatastoreConnection;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.UpdateableDataContext;

final class TestDatastoreConnection implements UpdateableDatastoreConnection {

    private final UpdateableDataContext _dataContext;
    private final Datastore _datastore;

    public TestDatastoreConnection(TestDatastore datastore) throws Exception {
        _datastore = datastore;
        _dataContext = DataContextFactory.createJdbcDataContext(datastore.getDataSource());
    }

    @Override
    public DataContext getDataContext() {
        return _dataContext;
    }

    @Override
    public SchemaNavigator getSchemaNavigator() {
        return new SchemaNavigator(getDataContext());
    }

    @Override
    public Datastore getDatastore() {
        return _datastore;
    }

    @Override
    public void close() {
    }

    @Override
    public UpdateableDataContext getUpdateableDataContext() {
        return _dataContext;
    }

}
