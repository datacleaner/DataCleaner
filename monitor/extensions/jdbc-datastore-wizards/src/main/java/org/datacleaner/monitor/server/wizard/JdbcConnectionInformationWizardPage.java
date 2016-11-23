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

abstract class JdbcConnectionInformationWizardPage extends AbstractFreemarkerWizardPage {

    private final AbstractJdbcDatastoreWizardSession _session;
    private final String _templateUrl;

    public JdbcConnectionInformationWizardPage(final AbstractJdbcDatastoreWizardSession session, final String templateUrl) {
        _session = session;
        _templateUrl = templateUrl;
    }

    @Override
    public WizardPageController nextPageController(final Map<String, List<String>> formParameters)
            throws DCUserInputException {
        final String url = getString(formParameters, "url");

        if (StringUtils.isNullOrEmpty(url)) {
            throw new DCUserInputException("Connection string / URL cannot be empty");
        }

        _session.setUrl(url);

        final String username = getString(formParameters, "username");
        final String password = getString(formParameters, "password");
        _session.setCredentials(username, password);

        return new DatastoreNameAndDescriptionWizardPage(_session.getWizardContext(), getPageIndex() + 1) {
            @Override
            protected WizardPageController nextPageController(final String name, final String description) {
                _session.setDescription(description);
                _session.setName(name);
                return null;
            }
        };
    }

    @Override
    protected String getTemplateFilename() {
        return "JdbcConnectionInformationWizardPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> map = new HashMap<>();
        map.put("templateUrl", _templateUrl);
        return map;
    }

}
