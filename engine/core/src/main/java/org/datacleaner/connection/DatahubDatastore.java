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

import org.datacleaner.metamodel.datahub.DatahubDataContext;

public class DatahubDatastore extends UsageAwareDatastore<DatahubDataContext>
        implements UpdateableDatastore, UsernameDatastore {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final String _host;
    private final Integer _port;
    private final String _username;
    private final String _password;
    private final String _tenantName;
    private boolean _https;
    private boolean _acceptUnverifiedSslPeers;
    private final String _securityMode;

    public DatahubDatastore(String name, String host, Integer port,
            String username, String password, String tenantName,
            boolean https, boolean acceptUnverifiedSslPeers, String securityMode) {
        super(name);
        _host = host;
        _port = port;
        _username = username;
        _password = password;
        _tenantName = tenantName;
        _https = https;
        _acceptUnverifiedSslPeers = acceptUnverifiedSslPeers;
        _securityMode = securityMode;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(true, false);
    }

    public String getHost() {
        return _host;
    }

    public Integer getPort() {
        return _port;
    }

    @Override
    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }

    public String getTenantName() {
        return _tenantName;
    }

    public boolean https() {
        return _https;
    }

    public boolean acceptUnverifiedSslPeers() {
        return _acceptUnverifiedSslPeers;
    }

    public String getSecurityMode() {
        return _securityMode;
    }

    @Override
    public UpdateableDatastoreConnection openConnection() {
        DatastoreConnection connection = super.openConnection();
        return (UpdateableDatastoreConnection) connection;
    }

    @Override
    protected UsageAwareDatastoreConnection<DatahubDataContext> createDatastoreConnection() {
        final DatahubDataContext dataContext = new DatahubDataContext(_host,
                _port, _username, _password, _tenantName, _https, _acceptUnverifiedSslPeers, _securityMode);
        return new UpdateableDatastoreConnectionImpl<DatahubDataContext>(
                dataContext, this);
    }

    @Override
    public String toString() {
        return "DataHubDatastore[host= " + _host + ", port=" + _port
                + ", username=" + _username + ", tenant=" + _tenantName
                + ", https=" + (_https ? "true" : "false")
                + ", acceptUnverifiedSslPeers=" + (_acceptUnverifiedSslPeers ? "true" : "false")
                + ", securityMode=" + _securityMode
                + "]";
    }

}
