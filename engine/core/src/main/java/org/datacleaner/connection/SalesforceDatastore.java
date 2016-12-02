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

import org.apache.metamodel.salesforce.SalesforceDataContext;

import com.google.common.base.Strings;

/**
 * A datastore that uses a Salesforce.com account as it's source.
 */
public class SalesforceDatastore extends UsageAwareDatastore<SalesforceDataContext>
        implements UpdateableDatastore, UsernameDatastore {

    private static final long serialVersionUID = 1L;

    private final String _username;
    private final String _password;
    private final String _securityToken;
    private final String _endpointUrl;

    public SalesforceDatastore(final String name, final String username, final String password,
            final String securityToken) {
        this(name, username, password, securityToken, null);
    }

    public SalesforceDatastore(final String name, final String username, final String password,
            final String securityToken, final String endpointUrl) {
        super(name);
        _username = username;
        _password = password;
        _securityToken = securityToken;
        _endpointUrl = endpointUrl;
    }

    /**
     * Gets the username of the salesforce account
     *
     * @return
     */
    @Override
    public String getUsername() {
        return _username;
    }

    /**
     * Gets the password of the salesforce account
     *
     * @return
     */
    public String getPassword() {
        return _password;
    }

    /**
     * Gets the security token of the salesforce account
     *
     * @return
     */
    public String getSecurityToken() {
        return _securityToken;
    }

    /**
     * Gets the endpoint URL to use for Salesforce.com web services, or null if
     * the default/production URL should be used.
     *
     * @return
     */
    public String getEndpointUrl() {
        return _endpointUrl;
    }

    @Override
    public UpdateableDatastoreConnection openConnection() {
        final DatastoreConnection connection = super.openConnection();
        return (UpdateableDatastoreConnection) connection;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(true, false);
    }

    @Override
    protected UsageAwareDatastoreConnection<SalesforceDataContext> createDatastoreConnection() {
        final SalesforceDataContext dataContext;
        if (Strings.isNullOrEmpty(_endpointUrl)) {
            dataContext = new SalesforceDataContext(_username, _password, _securityToken);
        } else {
            dataContext = new SalesforceDataContext(_endpointUrl, _username, _password, _securityToken);
        }
        return new UpdateableDatastoreConnectionImpl<>(dataContext, this);
    }

    @Override
    public String toString() {
        return "SalesforceDatastore[username=" + _username + "]";
    }

}
