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

import org.apache.metamodel.neo4j.Neo4jDataContext;

/**
 * Datastore providing access to Neo4j graph database
 */
public class Neo4jDatastore extends UsageAwareDatastore<Neo4jDataContext> {

    private static final long serialVersionUID = 1L;
    public static final int DEFAULT_PORT = 7474;

    private final String _hostname;
    private final int _port;
    private final String _username;
    private final String _password;

    public Neo4jDatastore(String name, String hostname, String username, String password) {
        this(name, hostname, DEFAULT_PORT, username, password);
    }

    public Neo4jDatastore(String name, String hostname, int port, String username, String password) {
        super(name);
        _hostname = hostname;
        _port = port;
        _username = username;
        _password = password;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(false, true);
    }

    @Override
    protected UsageAwareDatastoreConnection<Neo4jDataContext> createDatastoreConnection() {
        if (_hostname != null & _username != null & _password != null) {
            final Neo4jDataContext dataContext = new Neo4jDataContext(_hostname, _port, _username, _password);
            return new DatastoreConnectionImpl<Neo4jDataContext>(dataContext, this);
        }
        return null;
    }

    public String getHostname() {
        return _hostname;
    }

    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }

    public int getPort() {
        return _port;
    }

    @Override
    public String toString() {
        return "Neo4jDatastore[name=" + getName() + ", hostname=" + _hostname + ", port=" + _port + ", _username="
                + _username + "]";
    }

}
