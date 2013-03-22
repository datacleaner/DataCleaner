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
package org.eobjects.datacleaner.monitor.server;

import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizard;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;
import org.eobjects.metamodel.util.Func;

/**
 * Default implementation of {@link DatastoreWizardContext}.
 */
public class DatastoreWizardContextImpl implements DatastoreWizardContext {

    private final DatastoreWizard _wizard;
    private final TenantContext _tenantContext;
    private final Func<String, Object> _sessionFunc;

    public DatastoreWizardContextImpl(DatastoreWizard wizard, TenantContext tenantContext, Func<String, Object> sessionFunc) {
        _wizard = wizard;
        _tenantContext = tenantContext;
        _sessionFunc = sessionFunc;
    }
    
    @Override
    public DatastoreWizard getDatastoreWizard() {
        return _wizard;
    }

    @Override
    public TenantContext getTenantContext() {
        return _tenantContext;
    }

    @Override
    public Func<String, Object> getHttpSession() {
        return _sessionFunc;
    }
}
