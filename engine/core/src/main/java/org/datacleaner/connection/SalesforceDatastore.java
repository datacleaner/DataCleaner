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

/**
 * A datastore that uses a Salesforce.com account as it's source.
 */
public class SalesforceDatastore extends UsageAwareDatastore<SalesforceDataContext> implements UpdateableDatastore, UsernameDatastore {

    private static final long serialVersionUID = 1L;

    private final String _username;
    private final String _password;
    private final String _securityToken;

    public SalesforceDatastore(String name, String username, String password, String securityToken) {
        super(name);
        _username = username;
        _password = password;
        _securityToken = securityToken;
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

    @Override
    public UpdateableDatastoreConnection openConnection() {
        DatastoreConnection connection = super.openConnection();
        return (UpdateableDatastoreConnection) connection;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(true, false);
    }

    @Override
    protected UsageAwareDatastoreConnection<SalesforceDataContext> createDatastoreConnection() {
        final SalesforceDataContext dataContext = new SalesforceDataContext(_username, _password, _securityToken);
        return new UpdateableDatastoreConnectionImpl<SalesforceDataContext>(dataContext, this);
    }

    @Override
    public String toString() {
        return "SalesforceDatastore[username=" + _username + "]";
    }

}
