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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.server.dao.DatastoreDao;
import org.eobjects.datacleaner.monitor.server.dao.WizardDao;
import org.eobjects.datacleaner.monitor.shared.WizardService;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardPage;
import org.eobjects.datacleaner.monitor.shared.model.WizardSessionIdentifier;
import org.eobjects.datacleaner.monitor.wizard.WizardSession;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizard;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;
import org.eobjects.datacleaner.monitor.wizard.job.JobWizard;
import org.eobjects.datacleaner.monitor.wizard.job.JobWizardContext;
import org.eobjects.metamodel.util.Func;
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
    public List<WizardIdentifier> getNonDatastoreConsumingJobWizardIdentifiers(TenantIdentifier tenant) {
        final List<WizardIdentifier> result = new ArrayList<WizardIdentifier>();
        final Collection<JobWizard> jobWizards = _wizardDao.getWizardsOfType(JobWizard.class);
        for (JobWizard jobWizard : jobWizards) {
            if (!jobWizard.isDatastoreConsumer()) {
                final WizardIdentifier jobWizardIdentifier = createJobWizardIdentifier(jobWizard);
                result.add(jobWizardIdentifier);
            }
        }
        return result;
    }

    @Override
    public List<WizardIdentifier> getDatastoreWizardIdentifiers(TenantIdentifier tenant) {

        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);

        final Func<String, Object> sessionFunc = _wizardDao.createSessionFunc();
        final DatastoreWizardContext context = new DatastoreWizardContextImpl(null, tenantContext, sessionFunc);

        final List<WizardIdentifier> result = new ArrayList<WizardIdentifier>();
        for (DatastoreWizard datastoreWizard : _wizardDao.getWizardsOfType(DatastoreWizard.class)) {
            if (datastoreWizard.isApplicableTo(context)) {
                WizardIdentifier wizardIdentifier = createDatastoreWizardIdentifier(datastoreWizard);
                result.add(wizardIdentifier);
            }
        }
        return result;
    }

    @Override
    public List<WizardIdentifier> getJobWizardIdentifiers(TenantIdentifier tenant,
            DatastoreIdentifier datastoreIdentifier) {

        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final Datastore datastore = tenantContext.getConfiguration().getDatastoreCatalog()
                .getDatastore(datastoreIdentifier.getName());

        final Func<String, Object> sessionFunc = _wizardDao.createSessionFunc();
        final JobWizardContext context = new JobWizardContextImpl(null, tenantContext, datastore, sessionFunc);

        final List<WizardIdentifier> result = new ArrayList<WizardIdentifier>();
        for (JobWizard jobWizard : _wizardDao.getWizardsOfType(JobWizard.class)) {
            if (jobWizard.isDatastoreConsumer() && jobWizard.isApplicableTo(context)) {
                WizardIdentifier wizardIdentifier = createJobWizardIdentifier(jobWizard);
                result.add(wizardIdentifier);
            }
        }
        return result;
    }

    private WizardIdentifier createDatastoreWizardIdentifier(DatastoreWizard datastoreWizard) {
        final String displayName = datastoreWizard.getDisplayName();
        final WizardIdentifier jobWizardIdentifier = new WizardIdentifier();
        jobWizardIdentifier.setDisplayName(displayName);
        jobWizardIdentifier.setExpectedPageCount(datastoreWizard.getExpectedPageCount());
        return jobWizardIdentifier;
    }

    private WizardIdentifier createJobWizardIdentifier(JobWizard jobWizard) {
        final String displayName = jobWizard.getDisplayName();
        final WizardIdentifier jobWizardIdentifier = new WizardIdentifier();
        jobWizardIdentifier.setDisplayName(displayName);
        jobWizardIdentifier.setExpectedPageCount(jobWizard.getExpectedPageCount());
        jobWizardIdentifier.setDatastoreConsumer(jobWizard.isDatastoreConsumer());
        return jobWizardIdentifier;
    }

    @Override
    public WizardPage startDatastoreWizard(TenantIdentifier tenant, WizardIdentifier wizardIdentifier)
            throws IllegalArgumentException {
        final DatastoreWizard wizard = instantiateDatastoreWizard(wizardIdentifier);

        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);

        final Func<String, Object> sessionFunc = _wizardDao.createSessionFunc();
        final DatastoreWizardContext context = new DatastoreWizardContextImpl(wizard, tenantContext, sessionFunc);

        final WizardSession session = wizard.start(context);

        return startSession(session, wizardIdentifier);
    }

    @Override
    public WizardPage startJobWizard(TenantIdentifier tenant, WizardIdentifier wizardIdentifier,
            DatastoreIdentifier selectedDatastore) throws IllegalArgumentException {
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
        final JobWizardContext context = new JobWizardContextImpl(wizard, tenantContext, datastore, sessionFunc);

        final WizardSession session = wizard.start(context);

        return startSession(session, wizardIdentifier);
    }

    private WizardPage startSession(WizardSession session, WizardIdentifier wizardIdentifier) {
        final WizardPage page = _wizardDao.startSession(wizardIdentifier, session);
        return page;
    }

    @Override
    public WizardPage nextPage(TenantIdentifier tenant, WizardSessionIdentifier sessionIdentifier,
            Map<String, List<String>> formParameters) throws DCUserInputException {
        return _wizardDao.nextPage(tenant, sessionIdentifier, formParameters);
    }

    @Override
    public Boolean cancelWizard(TenantIdentifier tenant, WizardSessionIdentifier sessionIdentifier) {
        if (sessionIdentifier == null) {
            return true;
        }
        String sessionId = sessionIdentifier.getSessionId();
        _wizardDao.closeSession(sessionId);
        return true;
    }

    private JobWizard instantiateJobWizard(WizardIdentifier wizardIdentifier) {
        for (JobWizard jobWizard : _wizardDao.getWizardsOfType(JobWizard.class)) {
            final String displayName = jobWizard.getDisplayName();
            if (displayName.equals(wizardIdentifier.getDisplayName())) {
                return jobWizard;
            }
        }
        return null;
    }

    private DatastoreWizard instantiateDatastoreWizard(WizardIdentifier wizardIdentifier) {
        for (DatastoreWizard jobWizard : _wizardDao.getWizardsOfType(DatastoreWizard.class)) {
            final String displayName = jobWizard.getDisplayName();
            if (displayName.equals(wizardIdentifier.getDisplayName())) {
                return jobWizard;
            }
        }
        return null;
    }
}
