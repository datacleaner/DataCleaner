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
import org.datacleaner.util.StringUtils;

/**
 * Page for entering SugarCRM credentials
 */
final class SugarCrmDatastoreCredentialsPage extends AbstractFreemarkerWizardPage {

    private final SugarCrmDatastoreWizardSession _session;

    public SugarCrmDatastoreCredentialsPage(final SugarCrmDatastoreWizardSession session) {
        _session = session;
    }

    @Override
    public Integer getPageIndex() {
        return 1;
    }

    @Override
    public WizardPageController nextPageController(final Map<String, List<String>> formParameters)
            throws DCUserInputException {
        final String username = formParameters.get("sugarcrm_username").get(0);
        final String password = formParameters.get("sugarcrm_password").get(0);

        if (StringUtils.isNullOrEmpty(username)) {
            throw new DCUserInputException("Please provide a valid username.");
        }
        if (StringUtils.isNullOrEmpty(password)) {
            throw new DCUserInputException("Please provide a valid password.");
        }

        _session.setCredentials(username, password);

        return new DatastoreNameAndDescriptionWizardPage(_session.getWizardContext(), getPageIndex() + 1, "SugarCRM",
                "Connects to the web services of SugarCRM") {
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
        return "SugarCrmDatastoreCredentialsPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        return new HashMap<>();
    }

}
