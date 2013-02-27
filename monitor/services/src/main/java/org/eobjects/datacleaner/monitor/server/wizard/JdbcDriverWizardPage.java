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

import org.eobjects.datacleaner.database.DatabaseDriverCatalog;
import org.eobjects.datacleaner.database.DatabaseDriverDescriptor;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;

/**
 * Page for entering JDBC driver class
 */
final class JdbcDriverWizardPage extends AbstractFreemarkerWizardPage {

    private final GenericJdbcDatastoreWizardSession _session;

    public JdbcDriverWizardPage(GenericJdbcDatastoreWizardSession session) {
        _session = session;
    }

    @Override
    public Integer getPageIndex() {
        return 0;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException {
        String driverClassName = formParameters.get("driverClassName").get(0);

        _session.setDriverClassName(driverClassName);

        final DatabaseDriverDescriptor driver = DatabaseDriverCatalog
                .getDatabaseDriverByDriverClassName(driverClassName);
        final String templateUrl;
        if (driver == null) {
            templateUrl = "jdbc:<vendor>://<hostname>/<database>";
        } else {
            String[] connectionUrlTemplates = driver.getConnectionUrlTemplates();
            if (connectionUrlTemplates == null || connectionUrlTemplates.length == 0) {
                templateUrl = "jdbc:<vendor>://<hostname>/<database>";
            } else {
                templateUrl = connectionUrlTemplates[0];
            }
        }

        return new JdbcConnectionInformationWizardPage(_session, templateUrl) {
            @Override
            public Integer getPageIndex() {
                return JdbcDriverWizardPage.this.getPageIndex() + 1;
            }
        };
    }

    @Override
    protected String getTemplateFilename() {
        return "JdbcDriverWizardPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        return new HashMap<String, Object>();
    }

}
