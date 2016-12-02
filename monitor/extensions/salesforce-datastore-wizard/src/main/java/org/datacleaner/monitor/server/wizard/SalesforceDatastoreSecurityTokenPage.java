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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;

/**
 * Page for entering salesforce security token
 */
final class SalesforceDatastoreSecurityTokenPage extends AbstractFreemarkerWizardPage {

    private final SalesforceDatastoreWizardSession _session;

    public SalesforceDatastoreSecurityTokenPage(final SalesforceDatastoreWizardSession session) {
        _session = session;
    }

    @Override
    public Integer getPageIndex() {
        return 1;
    }

    @Override
    public WizardPageController nextPageController(final Map<String, List<String>> formParameters)
            throws DCUserInputException {

        final String securityToken = formParameters.get("sfdc_security_token").get(0);
        _session.setSecurityToken(securityToken);

        final boolean verifyConnection = getBoolean(formParameters, "verify_connection");
        if (verifyConnection) {
            _session.testConnection();
        }

        return new DatastoreNameAndDescriptionWizardPage(_session.getWizardContext(), getPageIndex() + 1,
                "Salesforce.com", "Connects to the web services of Salesforce.com") {

            @Override
            protected WizardPageController nextPageController(final String name, final String description) {
                _session.setName(name);
                _session.setDescription(description);

                return null;
            }
        };
    }

    @Override
    protected String getTemplateFilename() {
        return "SalesforceDatastoreSecurityTokenPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        return new HashMap<>();
    }

}
