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

import java.util.List;

import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.util.StringUtils;

public class MongoDbDatastore extends UsageAwareDatastore<UpdateableDataContext>
        implements UpdateableDatastore, UsernameDatastore {

    private static final long serialVersionUID = 1L;

    private final String _hostname;
    private final int _port;
    private final String _databaseName;
    private final String _username;
    private final char[] _password;
    private final SimpleTableDef[] _tableDefs;

    public MongoDbDatastore(final String name, final String databaseName) {
        this(name, null, null, databaseName);
    }

    public MongoDbDatastore(final String name, final String hostname, final Integer port, final String databaseName) {
        this(name, hostname, port, databaseName, null, (char[]) null);
    }

    public MongoDbDatastore(final String name, final String hostname, final Integer port, final String databaseName,
            final String username, final String password) {
        this(name, hostname, port, databaseName, username, password == null ? null : password.toCharArray());
    }

    public MongoDbDatastore(final String name, final String hostname, final Integer port, final String databaseName,
            final String username, final char[] password) {
        this(name, hostname, port, databaseName, username, password, null);
    }

    public MongoDbDatastore(final String name, final String hostname, final Integer port, final String databaseName,
            final String username, final String password, final SimpleTableDef[] tableDefs) {
        this(name, hostname, port, databaseName, username, password == null ? null : password.toCharArray(), tableDefs);
    }

    public MongoDbDatastore(final String name, String hostname, Integer port, final String databaseName,
            final String username, final char[] password, final SimpleTableDef[] tableDefs) {
        super(name);
        if (StringUtils.isNullOrEmpty(databaseName)) {
            throw new IllegalArgumentException("Database name cannot be null");
        }
        if (StringUtils.isNullOrEmpty(hostname)) {
            // default Mongo host
            hostname = "localhost";
        }
        if (port == null) {
            // default Mongo port
            port = 27017;
        }
        _hostname = hostname;
        _port = port;
        _databaseName = databaseName;
        _username = username;
        _password = password;
        _tableDefs = tableDefs;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(true, false);
    }

    @Override
    protected UsageAwareDatastoreConnection<UpdateableDataContext> createDatastoreConnection() {
        try {
            final UpdateableDataContext dataContext;
            if (_tableDefs == null || _tableDefs.length == 0) {
                dataContext = DataContextFactory
                        .createMongoDbDataContext(_hostname, _port, _databaseName, _username, _password);
            } else {
                dataContext = DataContextFactory
                        .createMongoDbDataContext(_hostname, _port, _databaseName, _hostname, _password, _tableDefs);
            }
            return new UpdateableDatastoreConnectionImpl<>(dataContext, this);
        } catch (final Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Failed to connect to MongoDB instance: " + e.getMessage(), e);
        }
    }

    @Override
    public UpdateableDatastoreConnection openConnection() {
        final DatastoreConnection connection = super.openConnection();
        return (UpdateableDatastoreConnection) connection;
    }

    public String getHostname() {
        return _hostname;
    }

    public int getPort() {
        return _port;
    }

    public String getDatabaseName() {
        return _databaseName;
    }

    @Override
    public String getUsername() {
        return _username;
    }

    public char[] getPassword() {
        return _password;
    }

    public SimpleTableDef[] getTableDefs() {
        return _tableDefs;
    }

    @Override
    protected void decorateIdentity(final List<Object> identifiers) {
        super.decorateIdentity(identifiers);
        identifiers.add(_databaseName);
        identifiers.add(_hostname);
        identifiers.add(_port);
        identifiers.add(_username);
        identifiers.add(_password);
        identifiers.add(_tableDefs);
    }
}
