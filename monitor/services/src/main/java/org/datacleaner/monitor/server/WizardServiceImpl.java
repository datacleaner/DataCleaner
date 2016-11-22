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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.LocaleUtils;
import org.apache.metamodel.util.Func;
import org.datacleaner.connection.Datastore;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.server.dao.DatastoreDao;
import org.datacleaner.monitor.server.dao.WizardDao;
import org.datacleaner.monitor.shared.WizardService;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.monitor.shared.model.WizardIdentifier;
import org.datacleaner.monitor.shared.model.WizardPage;
import org.datacleaner.monitor.shared.model.WizardSessionIdentifier;
import org.datacleaner.monitor.wizard.WizardSession;
import org.datacleaner.monitor.wizard.datastore.DatastoreWizard;
import org.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;
import org.datacleaner.monitor.wizard.job.JobWizard;
import org.datacleaner.monitor.wizard.job.JobWizardContext;
import org.datacleaner.monitor.wizard.referencedata.DictionaryWizard;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizard;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizardContext;
import org.datacleaner.monitor.wizard.referencedata.StringPatternWizard;
import org.datacleaner.monitor.wizard.referencedata.SynonymCatalogWizard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Main implementation of {@link WizardService}.
 */
@Component("wizardService")
public class WizardServiceImpl implements WizardService {

    @Autowired
    TenantContextFactory _tenantContextFactory;

    @Autowired
    DatastoreDao _datastoreDao;

    @Autowired
    WizardDao _wizardDao;

    @Override
    public List<WizardIdentifier> getNonDatastoreConsumingJobWizardIdentifiers(final TenantIdentifier tenant, final String locale) {
        final List<WizardIdentifier> result = new ArrayList<>();
        final Collection<JobWizard> jobWizards = _wizardDao.getWizardsOfType(JobWizard.class);
        for (final JobWizard jobWizard : jobWizards) {
            if (!jobWizard.isDatastoreConsumer()) {
                final WizardIdentifier jobWizardIdentifier = createJobWizardIdentifier(jobWizard);
                result.add(jobWizardIdentifier);
            }
        }
        return result;
    }

    @Override
    public List<WizardIdentifier> getDatastoreWizardIdentifiers(final TenantIdentifier tenant, final String localeString) {

        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);

        final Func<String, Object> sessionFunc = _wizardDao.createSessionFunc();
        final Locale locale = getLocale(localeString);
        final DatastoreWizardContext context = new DatastoreWizardContextImpl(null, tenantContext, sessionFunc, locale);

        final List<WizardIdentifier> result = new ArrayList<>();
        for (final DatastoreWizard datastoreWizard : _wizardDao.getWizardsOfType(DatastoreWizard.class)) {
            if (datastoreWizard.isApplicableTo(context)) {
                final WizardIdentifier wizardIdentifier = createDatastoreWizardIdentifier(datastoreWizard);
                result.add(wizardIdentifier);
            }
        }
        return result;
    }

    @Override
    public List<WizardIdentifier> getJobWizardIdentifiers(final TenantIdentifier tenant,
            final DatastoreIdentifier datastoreIdentifier, final String localeString) {

        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final Datastore datastore = tenantContext.getConfiguration().getDatastoreCatalog()
                .getDatastore(datastoreIdentifier.getName());

        final Func<String, Object> sessionFunc = _wizardDao.createSessionFunc();
        final Locale locale = getLocale(localeString);
        final JobWizardContext context = new JobWizardContextImpl(null, tenantContext, datastore, sessionFunc, locale);

        final List<WizardIdentifier> result = new ArrayList<>();
        for (final JobWizard jobWizard : _wizardDao.getWizardsOfType(JobWizard.class)) {
            if (jobWizard.isDatastoreConsumer() && jobWizard.isApplicableTo(context)) {
                final WizardIdentifier wizardIdentifier = createJobWizardIdentifier(jobWizard);
                result.add(wizardIdentifier);
            }
        }
        return result;
    }

    private WizardIdentifier createDatastoreWizardIdentifier(final DatastoreWizard datastoreWizard) {
        final String displayName = datastoreWizard.getDisplayName();
        final WizardIdentifier jobWizardIdentifier = new WizardIdentifier();
        jobWizardIdentifier.setDisplayName(displayName);
        jobWizardIdentifier.setExpectedPageCount(datastoreWizard.getExpectedPageCount());
        return jobWizardIdentifier;
    }

    private WizardIdentifier createJobWizardIdentifier(final JobWizard jobWizard) {
        final String displayName = jobWizard.getDisplayName();
        final WizardIdentifier jobWizardIdentifier = new WizardIdentifier();
        jobWizardIdentifier.setDisplayName(displayName);
        jobWizardIdentifier.setExpectedPageCount(jobWizard.getExpectedPageCount());
        jobWizardIdentifier.setDatastoreConsumer(jobWizard.isDatastoreConsumer());
        return jobWizardIdentifier;
    }

    @Override
    public WizardPage startDatastoreWizard(final TenantIdentifier tenant, final WizardIdentifier wizardIdentifier,
            final String localeString) throws IllegalArgumentException {
        final DatastoreWizard wizard = instantiateDatastoreWizard(wizardIdentifier);
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final Func<String, Object> sessionFunc = _wizardDao.createSessionFunc();
        final Locale locale = getLocale(localeString);
        final DatastoreWizardContext context = new DatastoreWizardContextImpl(wizard, tenantContext, sessionFunc,
                locale);
        final WizardSession session = wizard.start(context);

        return startSession(session, wizardIdentifier);
    }

    private Locale getLocale(final String localeString) {
        if (localeString == null) {
            return Locale.ENGLISH;
        }
        if ("".equals(localeString) || "default".equals(localeString)) {
            return Locale.ENGLISH;
        }
        return LocaleUtils.toLocale(localeString);
    }

    @Override
    public WizardPage startJobWizard(final TenantIdentifier tenant, final WizardIdentifier wizardIdentifier,
            final DatastoreIdentifier selectedDatastore, final String localeString) throws IllegalArgumentException {
        final JobWizard wizard = instantiateJobWizard(wizardIdentifier);
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final Datastore datastore;

        if (selectedDatastore == null) {
            datastore = null;
        } else {
            datastore = tenantContext.getConfiguration().getDatastoreCatalog()
                    .getDatastore(selectedDatastore.getName());
        }

        final Func<String, Object> sessionFunc = _wizardDao.createSessionFunc();
        final Locale locale = getLocale(localeString);
        final JobWizardContext context =
                new JobWizardContextImpl(wizard, tenantContext, datastore, sessionFunc, locale);
        final WizardSession session = wizard.start(context);

        return startSession(session, wizardIdentifier);
    }

    private WizardPage startSession(final WizardSession session, final WizardIdentifier wizardIdentifier) {
        return _wizardDao.startSession(wizardIdentifier, session);
    }

    @Override
    public WizardPage nextPage(final TenantIdentifier tenant, final WizardSessionIdentifier sessionIdentifier,
            final Map<String, List<String>> formParameters) throws DCUserInputException {
        return _wizardDao.nextPage(tenant, sessionIdentifier, formParameters);
    }

    @Override
    public WizardPage previousPage(final TenantIdentifier tenant, final WizardSessionIdentifier sessionIdentifier) {
        return _wizardDao.previousPage(tenant, sessionIdentifier);
    }

    @Override
    public Boolean cancelWizard(final TenantIdentifier tenant, final WizardSessionIdentifier sessionIdentifier) {
        if (sessionIdentifier == null) {
            return true;
        }
        final String sessionId = sessionIdentifier.getSessionId();
        _wizardDao.closeSession(sessionId);
        return true;
    }

    private JobWizard instantiateJobWizard(final WizardIdentifier wizardIdentifier) {
        for (final JobWizard jobWizard : _wizardDao.getWizardsOfType(JobWizard.class)) {
            final String displayName = jobWizard.getDisplayName();
            if (displayName.equals(wizardIdentifier.getDisplayName())) {
                return jobWizard;
            }
        }
        return null;
    }

    private DatastoreWizard instantiateDatastoreWizard(final WizardIdentifier wizardIdentifier) {
        for (final DatastoreWizard datastoreWizard : _wizardDao.getWizardsOfType(DatastoreWizard.class)) {
            final String displayName = datastoreWizard.getDisplayName();
            if (displayName.equals(wizardIdentifier.getDisplayName())) {
                return datastoreWizard;
            }
        }
        return null;
    }

    @Override
    public List<WizardIdentifier> getReferenceDataWizardIdentifiers(final String referenceDataType,
            final TenantIdentifier tenant, final String localeString) {
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final Func<String, Object> sessionFunc = _wizardDao.createSessionFunc();
        final Locale locale = getLocale(localeString);
        final ReferenceDataWizardContext context = new ReferenceDataWizardContextImpl(null, tenantContext, sessionFunc,
                locale);
        final List<WizardIdentifier> result = new ArrayList<>();
        final Collection wizards;

        if (referenceDataType.equals("dictionary")) {
            wizards = _wizardDao.getWizardsOfType(DictionaryWizard.class);
        } else if (referenceDataType.equals("synonym-catalog")) {
            wizards = _wizardDao.getWizardsOfType(SynonymCatalogWizard.class);
        } else if (referenceDataType.equals("string-pattern")) {
            wizards = _wizardDao.getWizardsOfType(StringPatternWizard.class);
        } else {
            wizards = _wizardDao.getWizardsOfType(ReferenceDataWizard.class);
        }

        for (final ReferenceDataWizard datastoreWizard : ((Collection<ReferenceDataWizard>) wizards)) {
            if (datastoreWizard.isApplicableTo(context)) {
                final WizardIdentifier wizardIdentifier = createReferenceDataWizardIdentifier(datastoreWizard);
                result.add(wizardIdentifier);
            }
        }

        return result;
    }

    private WizardIdentifier createReferenceDataWizardIdentifier(final ReferenceDataWizard referenceDataWizard) {
        final String displayName = referenceDataWizard.getDisplayName();
        final WizardIdentifier referenceDataWizardIdentifier = new WizardIdentifier();
        referenceDataWizardIdentifier.setDisplayName(displayName);
        referenceDataWizardIdentifier.setExpectedPageCount(referenceDataWizard.getExpectedPageCount());

        return referenceDataWizardIdentifier;
    }

    @Override
    public WizardPage startReferenceDataWizard(final TenantIdentifier tenant, final WizardIdentifier wizardIdentifier,
            final String localeString) throws IllegalArgumentException {
        final ReferenceDataWizard wizard = instantiateReferenceDataWizard(wizardIdentifier);
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final Func<String, Object> sessionFunc = _wizardDao.createSessionFunc();
        final Locale locale = getLocale(localeString);
        final ReferenceDataWizardContext context = new ReferenceDataWizardContextImpl(wizard, tenantContext,
                sessionFunc, locale);
        final WizardSession session = wizard.start(context);

        return startSession(session, wizardIdentifier);
    }

    private ReferenceDataWizard instantiateReferenceDataWizard(final WizardIdentifier wizardIdentifier) {
        for (final ReferenceDataWizard referenceDataWizard : _wizardDao.getWizardsOfType(ReferenceDataWizard.class)) {
            final String displayName = referenceDataWizard.getDisplayName();

            if (displayName.equals(wizardIdentifier.getDisplayName())) {
                return referenceDataWizard;
            }
        }

        return null;
    }
}
