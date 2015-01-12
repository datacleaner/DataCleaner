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

import org.datacleaner.monitor.wizard.datastore.AbstractDatastoreWizardSession;
import org.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;
import org.datacleaner.monitor.wizard.datastore.DatastoreWizardSession;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Abstract core {@link DatastoreWizardSession} for JDBC based datastores
 */
abstract class AbstractJdbcDatastoreWizardSession extends AbstractDatastoreWizardSession {

    private String _driverClassName;
    private String _url;
    private String _username;
    private String _password;
    private String _description;
    private String _name;

    public AbstractJdbcDatastoreWizardSession(DatastoreWizardContext context) {
        super(context);
    }

    public void setUrl(String url) {
        _url = url;
    }

    public String getUrl() {
        return _url;
    }

    public void setCredentials(String username, String password) {
        _username = username;
        _password = password;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public String getDescription() {
        return _description;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public void setDriverClassName(String driverClassName) {
        _driverClassName = driverClassName;
    }

    public String getDriverClassName() {
        return _driverClassName;
    }

    @Override
    public final Element createDatastoreElement(DocumentBuilder documentBuilder) {
        final Document document = documentBuilder.newDocument();

        final Element datastore = document.createElement("jdbc-datastore");
        datastore.setAttribute("name", _name);
        datastore.setAttribute("description", _description);

        final Element url = document.createElement("url");
        url.setTextContent(_url);
        datastore.appendChild(url);

        final Element driver = document.createElement("driver");
        driver.setTextContent(_driverClassName);
        datastore.appendChild(driver);

        if (_username != null) {
            final Element username = document.createElement("username");
            username.setTextContent(_username);
            datastore.appendChild(username);
        }

        if (_password != null) {
            final Element password = document.createElement("password");
            password.setTextContent(_password);
            datastore.appendChild(password);
        }

        return datastore;
    }
}
