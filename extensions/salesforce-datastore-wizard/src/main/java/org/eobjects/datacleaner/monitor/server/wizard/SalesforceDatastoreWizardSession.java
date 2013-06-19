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

import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.SalesforceDatastore;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.datastore.AbstractDatastoreWizardSession;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Wizard session for Salesforce datastore wizard
 */
final class SalesforceDatastoreWizardSession extends AbstractDatastoreWizardSession {

    private static final Logger logger = LoggerFactory.getLogger(SalesforceDatastoreWizardSession.class);

    private String _name;
    private String _description;
    private String _username;
    private String _password;
    private String _securityToken;

    public SalesforceDatastoreWizardSession(DatastoreWizardContext context) {
        super(context);
    }

    @Override
    public WizardPageController firstPageController() {
        return new SalesforceDatastoreCredentialsPage(this);
    }

    @Override
    public Integer getPageCount() {
        return 3;
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

    protected void testConnection() throws DCUserInputException {
        String[] tableNames;
        try {
            SalesforceDatastore ds = new SalesforceDatastore("test_salesforce_datastore", _username, _password, _securityToken);
            DatastoreConnection connection = ds.openConnection();
            logger.debug("Test connection for Salesforce.com established: {}", connection);
            tableNames = connection.getDataContext().getDefaultSchema().getTableNames();
        } catch (Exception e) {
            logger.warn("Test connection for Salesforce.com failed", e);
            String message = StringUtils.isNullOrEmpty(e.getMessage()) ? "Unknown error" : e.getMessage();
            throw new DCUserInputException("Failed to verify connection: " + message);
        }
        if (tableNames.length == 0) {
            throw new DCUserInputException("No tables/entities found in this Salesforce.com instance. This suggests an error.");
        }
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
