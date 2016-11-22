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
package org.datacleaner.monitor.server;

import java.util.Locale;

import org.apache.metamodel.util.Func;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.wizard.datastore.DatastoreWizard;
import org.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;

/**
 * Default implementation of {@link DatastoreWizardContext}.
 */
public class DatastoreWizardContextImpl implements DatastoreWizardContext {

    private final DatastoreWizard _wizard;
    private final TenantContext _tenantContext;
    private final Func<String, Object> _sessionFunc;
    private final Locale _locale;

    public DatastoreWizardContextImpl(final DatastoreWizard wizard, final TenantContext tenantContext,
            final Func<String, Object> sessionFunc, final Locale locale) {
        _wizard = wizard;
        _tenantContext = tenantContext;
        _sessionFunc = sessionFunc;
        _locale = locale;
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

    @Override
    public Locale getLocale() {
        return _locale;
    }
}
