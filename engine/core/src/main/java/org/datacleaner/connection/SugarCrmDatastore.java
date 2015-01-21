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

import org.apache.metamodel.sugarcrm.SugarCrmDataContext;

/**
 * A datastore that connects to a SugarCRM instance to fetch data
 */
public class SugarCrmDatastore extends UsageAwareDatastore<SugarCrmDataContext> implements UsernameDatastore {

    private static final long serialVersionUID = 1L;

    private final String _baseUrl;
    private final String _username;
    private final String _password;

    public SugarCrmDatastore(String name, String baseUrl, String username, String password) {
        super(name);
        _baseUrl = baseUrl;
        _username = username;
        _password = password;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(false, true);
    }

    @Override
    protected UsageAwareDatastoreConnection<SugarCrmDataContext> createDatastoreConnection() {
        final SugarCrmDataContext dataContext = new SugarCrmDataContext(_baseUrl, _username, _password, "AnalyzerBeans");
        return new DatastoreConnectionImpl<SugarCrmDataContext>(dataContext, this, dataContext);

    }

    public String getBaseUrl() {
        return _baseUrl;
    }

    @Override
    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }

    @Override
    public String toString() {
        return "SugarCrmDatastore[baseUrl=" + _baseUrl + ",username=" + _username + "]";
    }
}
