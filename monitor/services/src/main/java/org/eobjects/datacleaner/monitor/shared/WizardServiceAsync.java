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
package org.eobjects.datacleaner.monitor.shared;

import java.util.List;
import java.util.Map;

import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardPage;
import org.eobjects.datacleaner.monitor.shared.model.WizardSessionIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async variant of {@link WizardService}
 */
public interface WizardServiceAsync {

	void getJobWizardIdentifiers(TenantIdentifier tenant,
			DatastoreIdentifier selectedDatastore,
			AsyncCallback<List<WizardIdentifier>> callback);

	void getDatastoreWizardIdentifiers(TenantIdentifier tenant,
			AsyncCallback<List<WizardIdentifier>> callback);

	void startJobWizard(TenantIdentifier tenant, WizardIdentifier wizard,
			DatastoreIdentifier selectedDatastore, String jobName,
			AsyncCallback<WizardPage> callback);

	void startDatastoreWizard(TenantIdentifier tenant, WizardIdentifier wizard,
			String datastoreName, AsyncCallback<WizardPage> callback);

	void cancelWizard(TenantIdentifier tenant,
			WizardSessionIdentifier sessionIdentifier,
			AsyncCallback<Boolean> callback);

	void nextPage(TenantIdentifier tenant,
			WizardSessionIdentifier sessionIdentifier,
			Map<String, List<String>> formParameters,
			AsyncCallback<WizardPage> callback);

}
