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

import org.datacleaner.connection.Datastore;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.wizard.job.JobWizard;
import org.datacleaner.monitor.wizard.job.JobWizardContext;
import org.apache.metamodel.util.Func;

/**
 * Default implementation of {@link JobWizardContext}.
 */
public final class JobWizardContextImpl implements JobWizardContext {

    private final TenantContext _tenantContext;
    private final Datastore _sourceDatastore;
    private final Func<String, Object> _sessionFunc;
    private final JobWizard _jobWizard;
    private final Locale _locale;

    public JobWizardContextImpl(JobWizard jobWizard, TenantContext tenantContext, Datastore sourceDatastore,
            Func<String, Object> sessionFunc, Locale locale) {
        _jobWizard = jobWizard;
        _tenantContext = tenantContext;
        _sourceDatastore = sourceDatastore;
        _sessionFunc = sessionFunc;
        _locale = locale;
    }

    @Override
    public JobWizard getJobWizard() {
        return _jobWizard;
    }

    @Override
    public Datastore getSourceDatastore() {
        return _sourceDatastore;
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
