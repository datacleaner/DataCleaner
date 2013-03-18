/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import javax.xml.parsers.DocumentBuilder;

import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardSession;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Wizard session for Salesforce datastore wizard
 */
final class SalesforceDatastoreWizardSession implements DatastoreWizardSession {

    private final DatastoreWizardContext _context;
    private String _name;
    private String _description;
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
        return 3;
    }

    public DatastoreWizardContext getWizardContext() {
        return _context;
    }

    public void setName(String name) {
        _name = name;
    }

    public void setDescription(String description) {
        _description = description;
    }

    protected void setCredentials(String username, String password) {
        _username = username;
        _password = password;
    }

    protected void setSecurityToken(String securityToken) {
        _securityToken = securityToken;
    }

    @Override
    public Element createDatastoreElement(DocumentBuilder documentBuilder) {
        final Document doc = documentBuilder.newDocument();
        final Element username = doc.createElement("username");
        final Element password = doc.createElement("password");
        final Element securityToken = doc.createElement("security-token");

        username.setTextContent(_username);
        password.setTextContent(_password);
        securityToken.setTextContent(_securityToken);

        final Element element = doc.createElement("salesforce-datastore");
        element.setAttribute("name", _name);
        element.setAttribute("description", _description);
        element.appendChild(username);
        element.appendChild(password);
        element.appendChild(securityToken);
        return element;
    }
}
