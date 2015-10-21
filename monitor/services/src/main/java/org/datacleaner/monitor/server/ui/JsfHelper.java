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
package org.datacleaner.monitor.server.ui;

import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.Version;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.server.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * Component with convenience methods primarily intended to aid JSF/EL code
 * which is not always as expressive as Java.
 */
@Component("jsfHelper")
@Scope(WebApplicationContext.SCOPE_REQUEST)
public class JsfHelper {

    @Autowired
    TenantContextFactory tenantContextFactory;

    @Autowired
    User user;

    public String getVersion() {
        return Version.getDistributionVersion();
    }

    public DatastoreBeanWrapper[] getDatastores() {
        final DatastoreCatalog datastoreCatalog = tenantContextFactory.getContext(user.getTenant()).getConfiguration()
                .getDatastoreCatalog();

        DatastoreBeanWrapper[] datastoreBeanWrapper = prepareDatastoreWrappers(datastoreCatalog);
        return datastoreBeanWrapper;
    }

    private DatastoreBeanWrapper[] prepareDatastoreWrappers(DatastoreCatalog datastoreCatalog) {
        final String[] datastoreNames = datastoreCatalog.getDatastoreNames();

        final DatastoreBeanWrapper[] beanWrapperArray = new DatastoreBeanWrapper[datastoreNames.length];

        for (int i = 0; i < beanWrapperArray.length; i++) {
            final String datastoreName = datastoreNames[i];
            final Datastore datastore = datastoreCatalog.getDatastore(datastoreName);
            beanWrapperArray[i] = new DatastoreBeanWrapper(datastore);
        }

        return beanWrapperArray;
    }
}
