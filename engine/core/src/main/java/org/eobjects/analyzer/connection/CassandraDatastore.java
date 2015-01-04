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
package org.eobjects.analyzer.connection;

import org.apache.metamodel.cassandra.CassandraDataContext;
import org.apache.metamodel.util.SimpleTableDef;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;

/**
 * Datastore providing access to Apache Cassandra.
 */
public class CassandraDatastore extends UsageAwareDatastore<CassandraDataContext> {

    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_PORT = 9042;

    private final String _keySpace;
    private final SimpleTableDef[] _tableDefs;
    private final String _hostname;
    private final int _port;
    private final String _username;
    private final String _password;
    private final boolean _ssl;

    public CassandraDatastore(String name, String hostname, String keySpace) {
        this(name, hostname, DEFAULT_PORT, keySpace);
    }

    public CassandraDatastore(String name, String hostname, int port, String keySpace) {
        this(name, hostname, port, keySpace, null, null, false, null);
    }

    public CassandraDatastore(String name, String hostname, int port, String keySpace, String username,
            String password, boolean ssl, SimpleTableDef[] tableDefs) {
        super(name);
        _hostname = hostname;
        _port = port;
        _keySpace = keySpace;
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
            dataContext = new CassandraDataContext(cluster, _keySpace);
        } else {
            dataContext = new CassandraDataContext(cluster, _keySpace, _tableDefs);
        }
        return new DatastoreConnectionImpl<CassandraDataContext>(dataContext, this);
    }

    public String getHostname() {
        return _hostname;
    }

    public int getPort() {
        return _port;
    }

    public String getKeySpace() {
        return _keySpace;
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
