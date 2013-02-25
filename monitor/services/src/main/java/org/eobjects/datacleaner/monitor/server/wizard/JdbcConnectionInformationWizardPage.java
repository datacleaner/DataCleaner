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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;

final class JdbcConnectionInformationWizardPage extends AbstractFreemarkerWizardPage {

    private final JdbcDatastoreWizardSession _session;
    private final String _templateUrl;

    public JdbcConnectionInformationWizardPage(JdbcDatastoreWizardSession session, String templateUrl) {
        _session = session;
        _templateUrl = templateUrl;
    }

    @Override
    public Integer getPageIndex() {
        return 0;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException {
        final String url = formParameters.get("url").get(0);
        _session.setUrl(url);

        final String username = formParameters.get("username").get(0);
        final String password = formParameters.get("password").get(0);
        _session.setCredentials(username, password);

        return new DatastoreDescriptionWizardPage(1, new DatastoreDescriptionCallback() {
            @Override
            public WizardPageController nextPageController(String description) {
                _session.setDescription(description);
                return null;
            }
        });
    }

    @Override
    protected String getTemplateFilename() {
        return "JdbcConnectionInformationWizardPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("templateUrl", _templateUrl);
        return map;
    }

}
