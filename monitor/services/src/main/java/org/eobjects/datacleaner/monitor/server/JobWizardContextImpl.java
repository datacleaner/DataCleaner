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

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.wizard.job.JobWizardContext;

/**
 * Default implementation of {@link JobWizardContext}.
 */
public final class JobWizardContextImpl implements JobWizardContext {

    private final TenantContext _tenantContext;
    private final Datastore _sourceDatastore;
    private final String _jobName;

    public JobWizardContextImpl(TenantContext tenantContext, Datastore sourceDatastore, String jobName) {
        _tenantContext = tenantContext;
        _sourceDatastore = sourceDatastore;
        _jobName = jobName;
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
    public String getJobName() {
        return _jobName;
    }

}
