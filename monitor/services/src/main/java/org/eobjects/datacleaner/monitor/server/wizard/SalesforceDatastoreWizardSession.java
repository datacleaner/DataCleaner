/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.server.wizard;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.SalesforceDatastore;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardSession;

final class SalesforceDatastoreWizardSession implements DatastoreWizardSession {

    private final DatastoreWizardContext _context;
    private String _username;
    private String _password;
    private String _securityToken;

    public SalesforceDatastoreWizardSession(DatastoreWizardContext context) {
        _context = context;
    }

    @Override
    public WizardPageController firstPageController() {
        return new SalesforceDatastoreCredentialsPage(this);
    }

    @Override
    public Integer getPageCount() {
        return 2;
    }

    protected void setCredentials(String username, String password) {
        _username = username;
        _password = password;
    }
    
    protected void setSecurityToken(String securityToken) {
        _securityToken = securityToken;
    }

    @Override
    public Datastore createDatastore() {
        SalesforceDatastore datastore = new SalesforceDatastore(_context.getDatastoreName(), _username, _password,
                _securityToken);
        return datastore;
    }
}
