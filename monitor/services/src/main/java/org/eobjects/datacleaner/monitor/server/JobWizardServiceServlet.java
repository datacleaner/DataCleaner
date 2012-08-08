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

import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.eobjects.datacleaner.monitor.shared.JobWizardService;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardPage;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardSessionIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

public class JobWizardServiceServlet extends SecureGwtServlet implements JobWizardService {

    private static final long serialVersionUID = 1L;

    private JobWizardService _delegate;

    @Override
    public void init() throws ServletException {
        super.init();

        if (_delegate == null) {
            WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
            JobWizardService delegate = applicationContext.getBean(JobWizardService.class);
            if (delegate == null) {
                throw new ServletException("No delegate found in application context!");
            }
            _delegate = delegate;
        }
    }

    public JobWizardService getDelegate() {
        return _delegate;
    }

    @Override
    public List<DatastoreIdentifier> getAvailableDatastores(TenantIdentifier tenant) {
        return _delegate.getAvailableDatastores(tenant);
    }

    @Override
    public List<JobWizardIdentifier> getJobWizardIdentifiers(TenantIdentifier tenant) {
        return _delegate.getJobWizardIdentifiers(tenant);
    }

    @Override
    public JobWizardPage startWizard(TenantIdentifier tenant, JobWizardIdentifier wizardIdentifier,
            DatastoreIdentifier selectedDatastore, String jobName) {
        return _delegate.startWizard(tenant, wizardIdentifier, selectedDatastore, jobName);
    }

    @Override
    public Boolean cancelWizard(TenantIdentifier tenant, JobWizardSessionIdentifier sessionIdentifier) {
        return _delegate.cancelWizard(tenant, sessionIdentifier);
    }

    @Override
    public JobWizardPage nextPage(TenantIdentifier tenant, JobWizardSessionIdentifier sessionIdentifier,
            Map<String, List<String>> formParameters) {
        return _delegate.nextPage(tenant, sessionIdentifier, formParameters);
    }
}
