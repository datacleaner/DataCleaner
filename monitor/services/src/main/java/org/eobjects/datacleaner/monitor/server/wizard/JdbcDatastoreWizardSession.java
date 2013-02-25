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
 * Wizard session for JDBC datastores.
 */
public class JdbcDatastoreWizardSession implements DatastoreWizardSession {

    private final DatastoreWizardContext _context;
    private final String _driverClassName;

    private String _url;
    private String _username;
    private String _password;
    private String _description;

    public JdbcDatastoreWizardSession(DatastoreWizardContext context, String driverClassName, String url) {
        _context = context;
        _driverClassName = driverClassName;
        _url = url;
    }

    @Override
    public WizardPageController firstPageController() {
        return new JdbcConnectionInformationWizardPage(this, _url);
    }

    @Override
    public Integer getPageCount() {
        return 2;
    }

    @Override
    public Element createDatastoreElement(DocumentBuilder documentBuilder) {
        final Document document = documentBuilder.newDocument();

        final Element datastore = document.createElement("jdbc-datastore");
        datastore.setAttribute("name", _context.getDatastoreName());
        datastore.setAttribute("description", _description);

        final Element url = document.createElement("url");
        url.setTextContent(_url);
        datastore.appendChild(url);

        final Element driver = document.createElement("driver");
        driver.setTextContent(_driverClassName);
        datastore.appendChild(driver);

        final Element username = document.createElement("username");
        username.setTextContent(_username);
        datastore.appendChild(username);

        final Element password = document.createElement("password");
        password.setTextContent(_password);
        datastore.appendChild(password);

        return datastore;
    }

    public void setUrl(String url) {
        _url = url;
    }

    public void setCredentials(String username, String password) {
        _username = username;
        _password = password;
    }

    public void setDescription(String description) {
        _description = description;
    }

}
