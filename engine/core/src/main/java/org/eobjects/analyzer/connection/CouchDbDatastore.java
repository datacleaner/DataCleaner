/**
 * AnalyzerBeans
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

import java.util.List;

import org.ektorp.http.StdHttpClient;
import org.apache.metamodel.couchdb.CouchDbDataContext;
import org.apache.metamodel.util.SimpleTableDef;

public class CouchDbDatastore extends UsageAwareDatastore<CouchDbDataContext> implements UpdateableDatastore,
        UsernameDatastore {

    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_PORT = CouchDbDataContext.DEFAULT_PORT;

    /**
     * 20 second connection timeout - due to batching nature of the connections,
     * CouchDB may take a bit of time to respond to new connections.
     */
    private static final int HTTP_TIMEOUT = 20000;

    private final String _hostname;
    private final Integer _port;
    private final String _username;
    private final String _password;
    private final boolean _sslEnabled;
    private final SimpleTableDef[] _tableDefs;

    public CouchDbDatastore(String name, String hostname, Integer port, String username, String password,
            boolean sslEnabled, SimpleTableDef[] tableDefs) {
        super(name);
        _hostname = hostname;
        _port = port;
        _username = username;
        _password = password;
        _sslEnabled = sslEnabled;
        _tableDefs = tableDefs;
    }

    @Override
    public UpdateableDatastoreConnection openConnection() {
        return (UpdateableDatastoreConnection) super.openConnection();
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(true, false);
    }

    @Override
    protected UsageAwareDatastoreConnection<CouchDbDataContext> createDatastoreConnection() {
        final StdHttpClient.Builder httpClient = new StdHttpClient.Builder();
        httpClient.host(getHostname());
        if (_port != null) {
            httpClient.port(_port);
        }
        if (_username != null) {
            httpClient.username(_username);
        }
        if (_password != null) {
            httpClient.password(_password);
        }
        httpClient.enableSSL(_sslEnabled);

        httpClient.connectionTimeout(HTTP_TIMEOUT);
        httpClient.socketTimeout(HTTP_TIMEOUT);

        final CouchDbDataContext dataContext;
        if (_tableDefs != null && _tableDefs.length > 0) {
            dataContext = new CouchDbDataContext(httpClient, _tableDefs);
        } else {
            dataContext = new CouchDbDataContext(httpClient);
        }
        return new UpdateableDatastoreConnectionImpl<CouchDbDataContext>(dataContext, this);
    }

    public String getHostname() {
        if (_hostname == null) {
            return "localhost";
        }
        return _hostname;
    }

    public int getPort() {
        if (_port == null) {
            return DEFAULT_PORT;
        }
        return _port;
    }

    @Override
    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }

    public SimpleTableDef[] getTableDefs() {
        return _tableDefs;
    }

    public boolean isSslEnabled() {
        return _sslEnabled;
    }

    @Override
    protected void decorateIdentity(List<Object> identifiers) {
        super.decorateIdentity(identifiers);
        identifiers.add(_hostname);
        identifiers.add(getPort());
        identifiers.add(_username);
        identifiers.add(_password);
        identifiers.add(_sslEnabled);
        identifiers.add(_tableDefs);
    }

}
