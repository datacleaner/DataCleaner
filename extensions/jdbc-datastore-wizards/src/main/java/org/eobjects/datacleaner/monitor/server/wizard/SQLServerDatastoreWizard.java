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

import org.eobjects.datacleaner.database.DatabaseDriverCatalog;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizard;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardSession;
import org.springframework.stereotype.Component;

/**
 * Datastore wizard for Microsoft SQL server database connection datastores
 */
@Component
public class SQLServerDatastoreWizard implements DatastoreWizard {

    @Override
    public String getDisplayName() {
        return "Microsoft SQL server";
    }

    @Override
    public int getExpectedPageCount() {
        return 2;
    }

    @Override
    public DatastoreWizardSession start(DatastoreWizardContext context) {
        final String url = "jdbc:jtds:sqlserver://<hostname>/<database>;useUnicode=true;characterEncoding=UTF-8";
        return new JdbcDatastoreWizardSession(context, "net.sourceforge.jtds.jdbc.Driver", url);
    }

    @Override
    public boolean isApplicableTo(DatastoreWizardContext context) {
        final DatabaseDriverCatalog catalog = new DatabaseDriverCatalog(null);
        final boolean installed = catalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS);
        return installed;
    }
}
