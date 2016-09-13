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
package org.datacleaner.monitor.shared;

import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.monitor.shared.model.WizardIdentifier;
import org.datacleaner.monitor.shared.model.WizardPage;
import org.datacleaner.monitor.shared.model.WizardSessionIdentifier;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Defines a service for Job, Datastore and ReferenceData Wizards which are pluggable wizard
 * components that allow the user to create job, datastore and reference data as per some wizard UI.
 */
@RemoteServiceRelativePath("../gwtrpc/wizardService")
public interface WizardService extends WizardNavigationService, RemoteService {

    @RolesAllowed(SecurityRoles.JOB_EDITOR)
    List<WizardIdentifier> getNonDatastoreConsumingJobWizardIdentifiers(TenantIdentifier tenant, String locale);

    @RolesAllowed(SecurityRoles.JOB_EDITOR)
    List<WizardIdentifier> getJobWizardIdentifiers(TenantIdentifier tenant, DatastoreIdentifier selectedDatastore, String locale);

    @RolesAllowed(SecurityRoles.JOB_EDITOR)
    WizardPage startJobWizard(TenantIdentifier tenant, WizardIdentifier wizard,
            DatastoreIdentifier selectedDatastore, String locale) throws IllegalArgumentException;

    @RolesAllowed(SecurityRoles.CONFIGURATION_EDITOR)
    List<WizardIdentifier> getDatastoreWizardIdentifiers(TenantIdentifier tenant, String locale);

    @RolesAllowed(SecurityRoles.CONFIGURATION_EDITOR)
    WizardPage startDatastoreWizard(TenantIdentifier tenant, WizardIdentifier wizard, String locale)
            throws IllegalArgumentException;
    
    @RolesAllowed(SecurityRoles.CONFIGURATION_EDITOR)
    List<WizardIdentifier> getReferenceDataWizardIdentifiers(String referenceDataType, TenantIdentifier tenant, 
            String locale);
    
    @RolesAllowed(SecurityRoles.CONFIGURATION_EDITOR)
    WizardPage startReferenceDataWizard(TenantIdentifier tenant, WizardIdentifier wizard, String locale)
            throws IllegalArgumentException;

    @Override
    @RolesAllowed({ SecurityRoles.JOB_EDITOR, SecurityRoles.CONFIGURATION_EDITOR })
    WizardPage nextPage(TenantIdentifier tenant, WizardSessionIdentifier sessionIdentifier,
            Map<String, List<String>> formParameters) throws DCUserInputException;

    @Override
    @RolesAllowed({ SecurityRoles.JOB_EDITOR, SecurityRoles.CONFIGURATION_EDITOR })
    Boolean cancelWizard(TenantIdentifier tenant, WizardSessionIdentifier sessionIdentifier);
}
