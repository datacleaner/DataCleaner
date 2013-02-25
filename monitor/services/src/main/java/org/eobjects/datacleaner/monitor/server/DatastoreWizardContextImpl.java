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
package org.eobjects.datacleaner.monitor.server;

import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;

/**
 * Default implementation of {@link DatastoreWizardContext}.
 */
public class DatastoreWizardContextImpl implements DatastoreWizardContext {

    private final TenantContext _tenantContext;
    private final String _datastoreName;

    public DatastoreWizardContextImpl(TenantContext tenantContext, String datastoreName) {
        _tenantContext = tenantContext;
        _datastoreName = datastoreName;
    }

    @Override
    public TenantContext getTenantContext() {
        return _tenantContext;
    }

    @Override
    public String getDatastoreName() {
        return _datastoreName;
    }

}
