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

import org.apache.metamodel.cassandra.CassandraDataContext;
import org.apache.metamodel.util.SimpleTableDef;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;

/**
 * Datastore providing access to Apache Cassandra.
 */
public class CassandraDatastore extends UsageAwareDatastore<CassandraDataContext> {

    public static final int DEFAULT_PORT = 9042;
    private static final long serialVersionUID = 1L;
    private final String _keyspace;
    private final SimpleTableDef[] _tableDefs;
    private final String _hostname;
    private final int _port;
    private final String _username;
    private final String _password;
    private final boolean _ssl;

    public CassandraDatastore(final String name, final String hostname, final String keyspace) {
        this(name, hostname, DEFAULT_PORT, keyspace);
    }

    public CassandraDatastore(final String name, final String hostname, final int port, final String keyspace) {
        this(name, hostname, port, keyspace, null, null, false, null);
    }

    public CassandraDatastore(final String name, final String hostname, final int port, final String keyspace,
            final String username, final String password, final boolean ssl, final SimpleTableDef[] tableDefs) {
        super(name);
        _hostname = hostname;
        _port = port;
        _keyspace = keyspace;
        _username = username;
        _password = password;
        _ssl = ssl;
        _tableDefs = tableDefs;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(true, false);
    }

    @Override
    protected UsageAwareDatastoreConnection<CassandraDataContext> createDatastoreConnection() {
        Builder clusterBuilder = Cluster.builder();
        clusterBuilder = clusterBuilder.addContactPoint(_hostname);
        clusterBuilder = clusterBuilder.withPort(_port);
        if (_ssl) {
            clusterBuilder = clusterBuilder.withSSL();
        }
        if (_username != null && _password != null) {
            clusterBuilder = clusterBuilder.withCredentials(_username, _password);
        }

        final Cluster cluster = clusterBuilder.build();
        final CassandraDataContext dataContext;
        if (_tableDefs == null || _tableDefs.length == 0) {
            dataContext = new CassandraDataContext(cluster, _keyspace);
        } else {
            dataContext = new CassandraDataContext(cluster, _keyspace, _tableDefs);
        }
        return new DatastoreConnectionImpl<>(dataContext, this);
    }

    public String getHostname() {
        return _hostname;
    }

    public int getPort() {
        return _port;
    }

    public String getKeyspace() {
        return _keyspace;
    }

    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }

    public SimpleTableDef[] getTableDefs() {
        return _tableDefs;
    }

    public boolean isSsl() {
        return _ssl;
    }

    @Override
    public String toString() {
        return "CassandraDatastore[name=" + getName() + "]";
    }
}
