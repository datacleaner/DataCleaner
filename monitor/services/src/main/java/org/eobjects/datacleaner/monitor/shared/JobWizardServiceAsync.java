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

import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardPage;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardSessionIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface JobWizardServiceAsync {

    void getJobWizardIdentifiers(TenantIdentifier tenant, AsyncCallback<List<JobWizardIdentifier>> callback);

    void startWizard(TenantIdentifier tenant, JobWizardIdentifier wizard, DatastoreIdentifier selectedDatastore,
            String jobName, AsyncCallback<JobWizardPage> callback);

    void cancelWizard(TenantIdentifier tenant, JobWizardSessionIdentifier sessionIdentifier,
            AsyncCallback<Boolean> callback);

    void nextPage(TenantIdentifier tenant, JobWizardSessionIdentifier sessionIdentifier,
            Map<String, List<String>> formParameters, AsyncCallback<JobWizardPage> callback);

    void getAvailableDatastores(TenantIdentifier tenant, AsyncCallback<List<DatastoreIdentifier>> callback);

}
