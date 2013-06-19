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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;

/**
 * Page for entering salesforce credentials
 */
final class SalesforceDatastoreCredentialsPage extends AbstractFreemarkerWizardPage {

    private final SalesforceDatastoreWizardSession _session;

    private String _username = "";
    private String _password = "";

    public SalesforceDatastoreCredentialsPage(SalesforceDatastoreWizardSession session) {
        _session = session;
    }

    @Override
    public Integer getPageIndex() {
        return 0;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters) throws DCUserInputException {
        _username = formParameters.get("sfdc_username").get(0);
        _password = formParameters.get("sfdc_password").get(0);

        _session.setCredentials(_username, _password);

        if (StringUtils.isNullOrEmpty(_username)) {
            throw new DCUserInputException("Please provide a valid username");
        }
        if (StringUtils.isNullOrEmpty(_password)) {
            throw new DCUserInputException("Please provide a valid password");
        }

        return new SalesforceDatastoreSecurityTokenPage(_session);
    }

    @Override
    protected String getTemplateFilename() {
        return "SalesforceDatastoreCredentialsPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("sfdc_username", _username);
        model.put("sfdc_password", _password);
        return model;
    }

}
