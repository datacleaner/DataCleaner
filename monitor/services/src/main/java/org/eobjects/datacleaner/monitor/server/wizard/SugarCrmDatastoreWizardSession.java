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

import javax.xml.parsers.DocumentBuilder;

import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardSession;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Wizard session for SugarCRM datastore wizard
 */
final class SugarCrmDatastoreWizardSession implements DatastoreWizardSession {

    private final DatastoreWizardContext _context;
    private String _username;
    private String _password;
    private String _baseUrl;

    public SugarCrmDatastoreWizardSession(DatastoreWizardContext context) {
        _context = context;
    }

    @Override
    public WizardPageController firstPageController() {
        return new SugarCrmBaseUrlWizardPage() {
            @Override
            protected WizardPageController nextPageController(String baseUrl) {
                _baseUrl = baseUrl;
                return new SugarCrmDatastoreCredentialsPage(SugarCrmDatastoreWizardSession.this);
            }
        };
    }

    @Override
    public Integer getPageCount() {
        return 2;
    }

    protected void setCredentials(String username, String password) {
        _username = username;
        _password = password;
    }

    @Override
    public Element createDatastoreElement(DocumentBuilder documentBuilder) {
        final Document doc = documentBuilder.newDocument();
        final Element username = doc.createElement("username");
        final Element password = doc.createElement("password");
        final Element baseUrl = doc.createElement("base-url");

        username.setTextContent(_username);
        password.setTextContent(_password);
        baseUrl.setTextContent(_baseUrl);

        final Element element = doc.createElement("sugar-crm-datastore");
        element.setAttribute("name", _context.getDatastoreName());
        element.setAttribute("description", "Connects to the web services of SugarCRM");
        element.appendChild(baseUrl);
        element.appendChild(username);
        element.appendChild(password);
        return element;
    }
}
