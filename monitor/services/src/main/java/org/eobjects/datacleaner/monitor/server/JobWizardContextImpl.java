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

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.wizard.job.JobWizard;
import org.eobjects.datacleaner.monitor.wizard.job.JobWizardContext;
import org.eobjects.metamodel.util.Func;

/**
 * Default implementation of {@link JobWizardContext}.
 */
public final class JobWizardContextImpl implements JobWizardContext {

    private final TenantContext _tenantContext;
    private final Datastore _sourceDatastore;
    private final String _jobName;
    private final Func<String, Object> _sessionFunc;
    private final JobWizard _jobWizard;

    public JobWizardContextImpl(JobWizard jobWizard, TenantContext tenantContext, Datastore sourceDatastore,
            String jobName, Func<String, Object> sessionFunc) {
        _jobWizard = jobWizard;
        _tenantContext = tenantContext;
        _sourceDatastore = sourceDatastore;
        _jobName = jobName;
        _sessionFunc = sessionFunc;
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
    public String getJobName() {
        return _jobName;
    }

    @Override
    public Func<String, Object> getHttpSession() {
        return _sessionFunc;
    }
}
