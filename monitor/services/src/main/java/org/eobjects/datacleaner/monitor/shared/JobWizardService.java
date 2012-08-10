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
package org.eobjects.datacleaner.monitor.shared;

import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardPage;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardSessionIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Defines a service for Job Wizards which are pluggable wizard components that
 * allow the user to create job as per some wizard UI.
 */
@RemoteServiceRelativePath("jobWizardService")
public interface JobWizardService extends RemoteService {

    @RolesAllowed(SecurityRoles.VIEWER)
    public List<DatastoreIdentifier> getAvailableDatastores(TenantIdentifier tenant);

    @RolesAllowed(SecurityRoles.VIEWER)
    public List<JobWizardIdentifier> getJobWizardIdentifiers(TenantIdentifier tenant);

    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    public JobWizardPage startWizard(TenantIdentifier tenant, JobWizardIdentifier wizard,
            DatastoreIdentifier selectedDatastore, String jobName) throws IllegalArgumentException;

    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    public JobWizardPage nextPage(TenantIdentifier tenant, JobWizardSessionIdentifier sessionIdentifier,
            Map<String, List<String>> formParameters);

    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    public Boolean cancelWizard(TenantIdentifier tenant, JobWizardSessionIdentifier sessionIdentifier);
}
