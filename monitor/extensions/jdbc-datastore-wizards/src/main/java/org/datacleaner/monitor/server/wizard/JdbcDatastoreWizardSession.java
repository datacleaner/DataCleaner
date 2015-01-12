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

import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;
import org.datacleaner.monitor.wizard.datastore.DatastoreWizardSession;

/**
 * Wizard session for JDBC datastores.
 */
public class JdbcDatastoreWizardSession extends AbstractJdbcDatastoreWizardSession implements DatastoreWizardSession {

    public JdbcDatastoreWizardSession(DatastoreWizardContext context, String driverClassName, String url) {
        super(context);
        setDriverClassName(driverClassName);
        setUrl(url);
    }

    @Override
    public WizardPageController firstPageController() {
        return new JdbcConnectionInformationWizardPage(this, getUrl()) {
            @Override
            public Integer getPageIndex() {
                return 0;
            }
        };
    }

    @Override
    public Integer getPageCount() {
        return 2;
    }

}
