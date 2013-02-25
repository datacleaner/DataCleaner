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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.JaxbJobWriter;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizard;
import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizardContext;
import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizardPageController;
import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizardSession;
import org.eobjects.datacleaner.monitor.shared.JobWizardService;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardPage;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardSessionIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.Action;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.Func;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component("jobWizardService")
public class JobWizardServiceImpl implements JobWizardService {

    private final ConcurrentMap<String, JobWizardContext> _contexts;
    private final ConcurrentMap<String, JobWizardSession> _sessions;
    private final ConcurrentMap<String, JobWizardPageController> _currentControllers;

    @Autowired
    TenantContextFactory _tenantContextFactory;

    @Autowired
    ApplicationContext _applicationContext;

    public JobWizardServiceImpl() {
        _sessions = new ConcurrentHashMap<String, JobWizardSession>();
        _currentControllers = new ConcurrentHashMap<String, JobWizardPageController>();
        _contexts = new ConcurrentHashMap<String, JobWizardContext>();
    }

    private Collection<JobWizard> getAvailableWizards() {
        return _applicationContext.getBeansOfType(JobWizard.class).values();
    }

    @Override
    public List<DatastoreIdentifier> getAvailableDatastores(TenantIdentifier tenant) {
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final String[] datastoreNames = tenantContext.getConfiguration().getDatastoreCatalog().getDatastoreNames();

        return CollectionUtils.map(datastoreNames, new Func<String, DatastoreIdentifier>() {
            @Override
            public DatastoreIdentifier eval(String name) {
                return new DatastoreIdentifier(name);
            }
        });
    }

    @Override
    public List<JobWizardIdentifier> getJobWizardIdentifiers(TenantIdentifier tenant) {
        List<JobWizardIdentifier> result = new ArrayList<JobWizardIdentifier>();
        for (JobWizard jobWizard : getAvailableWizards()) {
            JobWizardIdentifier jobWizardIdentifier = createJobWizardIdentifier(jobWizard);
            result.add(jobWizardIdentifier);
        }
        return result;
    }

    private JobWizardIdentifier createJobWizardIdentifier(JobWizard jobWizard) {
        final String displayName = jobWizard.getDisplayName();
        final JobWizardIdentifier jobWizardIdentifier = new JobWizardIdentifier();
        jobWizardIdentifier.setDisplayName(displayName);
        jobWizardIdentifier.setExpectedPageCount(jobWizard.getExpectedPageCount());
        return jobWizardIdentifier;
    }

    @Override
    public JobWizardPage startWizard(TenantIdentifier tenant, JobWizardIdentifier wizardIdentifier,
            DatastoreIdentifier selectedDatastore, String jobName) throws IllegalArgumentException {
        final String sessionId = createSessionId();

        final JobWizard wizard = instantiateWizard(wizardIdentifier);

        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        if (tenantContext.containsJob(jobName)) {
            throw new IllegalArgumentException("A job with the name '" + jobName + "' already exist.");
        }

        final Datastore datastore = tenantContext.getConfiguration().getDatastoreCatalog()
                .getDatastore(selectedDatastore.getName());

        final JobWizardContext context = new JobWizardContextImpl(tenantContext, datastore, jobName);

        final JobWizardSession session = wizard.start(context);

        final JobWizardSessionIdentifier sessionIdentifier = new JobWizardSessionIdentifier();
        sessionIdentifier.setSessionId(sessionId);
        sessionIdentifier.setWizardIdentifier(wizardIdentifier);

        final JobWizardPageController firstPageController = session.firstPageController();

        createSession(sessionId, session, context, firstPageController);

        return createPage(sessionIdentifier, firstPageController, session);
    }

    @Override
    public JobWizardPage nextPage(TenantIdentifier tenant, JobWizardSessionIdentifier sessionIdentifier,
            Map<String, List<String>> formParameters) {
        final String sessionId = sessionIdentifier.getSessionId();
        final JobWizardPageController controller = _currentControllers.get(sessionId);
        final JobWizardPageController nextPageController = controller.nextPageController(formParameters);
        if (nextPageController == null) {
            final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);

            final JobWizardContext jobWizardContext = _contexts.get(sessionId);

            final RepositoryFolder jobFolder = tenantContext.getJobFolder();
            jobFolder.createFile(jobWizardContext.getJobName() + FileFilters.ANALYSIS_XML.getExtension(),
                    new Action<OutputStream>() {
                        @Override
                        public void run(OutputStream out) throws Exception {
                            final JobWizardSession session = _sessions.get(sessionId);
                            final AnalysisJobBuilder jobBuilder = session.createJob();
                            final AnalysisJob analysisJob = jobBuilder.toAnalysisJob();

                            final AnalyzerBeansConfiguration configuration = tenantContext.getConfiguration();
                            final JaxbJobWriter writer = new JaxbJobWriter(configuration);
                            writer.write(analysisJob, out);
                        }
                    });

            closeSession(sessionId);

            // returning null signals that no more pages should be shown, the
            // wizard is done.
            return null;
        } else {
            final JobWizardSession session = _sessions.get(sessionId);
            _currentControllers.put(sessionId, nextPageController);
            return createPage(sessionIdentifier, nextPageController, session);
        }
    }

    private JobWizardPage createPage(JobWizardSessionIdentifier sessionIdentifier,
            JobWizardPageController pageController, JobWizardSession session) {
        final JobWizardPage page = new JobWizardPage();
        page.setSessionIdentifier(sessionIdentifier);
        page.setFormInnerHtml(pageController.getFormInnerHtml());
        page.setPageIndex(pageController.getPageIndex());
        if (session != null) {
            page.setExpectedPageCount(session.getPageCount());
        }
        return page;
    }

    private String createSessionId() {
        return UUID.randomUUID().toString();
    }

    public int getOpenSessionCount() {
        return _sessions.size();
    }

    @Override
    public Boolean cancelWizard(TenantIdentifier tenant, JobWizardSessionIdentifier sessionIdentifier) {
        if (sessionIdentifier == null) {
            return true;
        }
        String sessionId = sessionIdentifier.getSessionId();
        closeSession(sessionId);
        return true;
    }

    public void createSession(String sessionId, JobWizardSession session, JobWizardContext context,
            JobWizardPageController controller) {
        _sessions.put(sessionId, session);
        _contexts.put(sessionId, context);
        _currentControllers.put(sessionId, controller);
    }

    private void closeSession(String sessionId) {
        _sessions.remove(sessionId);
        _contexts.remove(sessionId);
        _currentControllers.remove(sessionId);
    }

    private JobWizard instantiateWizard(JobWizardIdentifier wizardIdentifier) {
        for (JobWizard jobWizard : getAvailableWizards()) {
            final String displayName = jobWizard.getDisplayName();
            if (displayName.equals(wizardIdentifier.getDisplayName())) {
                return jobWizard;
            }
        }
        return null;
    }
}
