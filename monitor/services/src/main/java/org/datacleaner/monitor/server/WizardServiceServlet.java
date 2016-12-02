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

import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.datacleaner.monitor.shared.WizardService;
import org.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.monitor.shared.model.WizardIdentifier;
import org.datacleaner.monitor.shared.model.WizardPage;
import org.datacleaner.monitor.shared.model.WizardSessionIdentifier;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

public class WizardServiceServlet extends SecureGwtServlet implements WizardService {

    private static final long serialVersionUID = 1L;

    private WizardService _delegate;

    @Override
    public void init() throws ServletException {
        super.init();

        if (_delegate == null) {
            final WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
            final WizardService delegate = applicationContext.getBean(WizardService.class);
            if (delegate == null) {
                throw new ServletException("No delegate found in application context!");
            }
            _delegate = delegate;
        }
    }

    public WizardService getDelegate() {
        return _delegate;
    }

    @Override
    public List<WizardIdentifier> getNonDatastoreConsumingJobWizardIdentifiers(final TenantIdentifier tenant,
            final String locale) {
        return _delegate.getNonDatastoreConsumingJobWizardIdentifiers(tenant, locale);
    }

    @Override
    public List<WizardIdentifier> getJobWizardIdentifiers(final TenantIdentifier tenant,
            final DatastoreIdentifier selectedDatastore, final String locale) {
        return _delegate.getJobWizardIdentifiers(tenant, selectedDatastore, locale);
    }

    @Override
    public WizardPage startJobWizard(final TenantIdentifier tenant, final WizardIdentifier wizardIdentifier,
            final DatastoreIdentifier selectedDatastore, final String locale) {
        return _delegate.startJobWizard(tenant, wizardIdentifier, selectedDatastore, locale);
    }

    @Override
    public Boolean cancelWizard(final TenantIdentifier tenant, final WizardSessionIdentifier sessionIdentifier) {
        return _delegate.cancelWizard(tenant, sessionIdentifier);
    }

    @Override
    public WizardPage nextPage(final TenantIdentifier tenant, final WizardSessionIdentifier sessionIdentifier,
            final Map<String, List<String>> formParameters) {
        return _delegate.nextPage(tenant, sessionIdentifier, formParameters);
    }

    @Override
    public List<WizardIdentifier> getDatastoreWizardIdentifiers(final TenantIdentifier tenant, final String locale) {
        return _delegate.getDatastoreWizardIdentifiers(tenant, locale);
    }

    @Override
    public WizardPage startDatastoreWizard(final TenantIdentifier tenant, final WizardIdentifier wizard,
            final String locale) throws IllegalArgumentException {
        return _delegate.startDatastoreWizard(tenant, wizard, locale);
    }

    @Override
    public List<WizardIdentifier> getReferenceDataWizardIdentifiers(final String referenceDataType,
            final TenantIdentifier tenant, final String locale) {
        return _delegate.getReferenceDataWizardIdentifiers(referenceDataType, tenant, locale);
    }

    @Override
    public WizardPage startReferenceDataWizard(final TenantIdentifier tenant, final WizardIdentifier wizard,
            final String locale) throws IllegalArgumentException {
        return _delegate.startReferenceDataWizard(tenant, wizard, locale);
    }

    @Override
    public WizardPage previousPage(final TenantIdentifier tenant, final WizardSessionIdentifier sessionIdentifier) {
        return _delegate.previousPage(tenant, sessionIdentifier);
    }
}
