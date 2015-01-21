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
package org.datacleaner.monitor.server.wizard;

import javax.xml.parsers.DocumentBuilder;

import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.datastore.AbstractDatastoreWizardSession;
import org.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Wizard session for SugarCRM datastore wizard
 */
final class SugarCrmDatastoreWizardSession extends AbstractDatastoreWizardSession {

    private String _username;
    private String _password;
    private String _baseUrl;
    private String _name;
    private String _description;

    public SugarCrmDatastoreWizardSession(DatastoreWizardContext context) {
        super(context);
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
        return 3;
    }

    protected void setCredentials(String username, String password) {
        _username = username;
        _password = password;
    }

    public void setName(String name) {
        _name = name;
    }

    public void setDescription(String description) {
        _description = description;
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
        element.setAttribute("name", _name);
        element.setAttribute("description", _description);
        element.appendChild(baseUrl);
        element.appendChild(username);
        element.appendChild(password);
        return element;
    }
}
