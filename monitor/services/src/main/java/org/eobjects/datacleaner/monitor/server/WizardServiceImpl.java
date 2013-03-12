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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.JaxbJobWriter;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.server.dao.DatastoreDao;
import org.eobjects.datacleaner.monitor.shared.WizardService;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardPage;
import org.eobjects.datacleaner.monitor.shared.model.WizardSessionIdentifier;
import org.eobjects.datacleaner.monitor.wizard.WizardContext;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.WizardSession;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizard;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardSession;
import org.eobjects.datacleaner.monitor.wizard.job.JobWizard;
import org.eobjects.datacleaner.monitor.wizard.job.JobWizardContext;
import org.eobjects.datacleaner.monitor.wizard.job.JobWizardSession;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.Action;
import org.eobjects.metamodel.util.Func;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.w3c.dom.Element;

@Component("wizardService")
public class WizardServiceImpl implements WizardService {

    private static final Logger logger = LoggerFactory.getLogger(WizardServiceImpl.class);

    private final ConcurrentMap<String, WizardSession> _sessions;
    private final ConcurrentMap<String, WizardContext> _contexts;
    private final ConcurrentMap<String, WizardPageController> _currentControllers;

    @Autowired
    TenantContextFactory _tenantContextFactory;

    @Autowired
    ApplicationContext _applicationContext;

    @Autowired
    DatastoreDao _datastoreDao;

    public WizardServiceImpl() {
        _sessions = new ConcurrentHashMap<String, WizardSession>();
        _currentControllers = new ConcurrentHashMap<String, WizardPageController>();
        _contexts = new ConcurrentHashMap<String, WizardContext>();
    }

    private Collection<JobWizard> getAvailableJobWizards() {
        return _applicationContext.getBeansOfType(JobWizard.class).values();
    }

    private Collection<DatastoreWizard> getAvailableDatastoreWizards() {
        return _applicationContext.getBeansOfType(DatastoreWizard.class).values();
    }

    @Override
    public List<WizardIdentifier> getDatastoreWizardIdentifiers(TenantIdentifier tenant) {
        List<WizardIdentifier> result = new ArrayList<WizardIdentifier>();
        for (DatastoreWizard datastoreWizard : getAvailableDatastoreWizards()) {
            WizardIdentifier wizardIdentifier = createDatastoreWizardIdentifier(datastoreWizard);
            result.add(wizardIdentifier);
        }
        return result;
    }

    @Override
    public List<WizardIdentifier> getJobWizardIdentifiers(TenantIdentifier tenant,
            DatastoreIdentifier datastoreIdentifier) {

        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final Datastore datastore = tenantContext.getConfiguration().getDatastoreCatalog()
                .getDatastore(datastoreIdentifier.getName());

        final JobWizardContext context = new JobWizardContextImpl(tenantContext, datastore, null, createSessionFunc());

        final List<WizardIdentifier> result = new ArrayList<WizardIdentifier>();
        for (JobWizard jobWizard : getAvailableJobWizards()) {
            if (jobWizard.isApplicableTo(context)) {
                WizardIdentifier wizardIdentifier = createJobWizardIdentifier(jobWizard);
                result.add(wizardIdentifier);
            }
        }
        return result;
    }

    /**
     * Create a convenience function that wraps the http session.
     * 
     * @return
     */
    private Func<String, Object> createSessionFunc() {
        return new Func<String, Object>() {
            @Override
            public Object eval(String key) {
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes();
                HttpSession session = requestAttributes.getRequest().getSession(true);
                return session.getAttribute(key);
            }
        };
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
        return jobWizardIdentifier;
    }

    @Override
    public WizardPage startDatastoreWizard(TenantIdentifier tenant, WizardIdentifier wizardIdentifier)
            throws IllegalArgumentException {
        final DatastoreWizard wizard = instantiateDatastoreWizard(wizardIdentifier);

        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);

        final DatastoreWizardContext context = new DatastoreWizardContextImpl(tenantContext, createSessionFunc());

        final WizardSession session = wizard.start(context);

        return startSession(session, wizardIdentifier, context);
    }

    @Override
    public WizardPage startJobWizard(TenantIdentifier tenant, WizardIdentifier wizardIdentifier,
            DatastoreIdentifier selectedDatastore, String jobName) throws IllegalArgumentException {
        final JobWizard wizard = instantiateJobWizard(wizardIdentifier);

        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        if (tenantContext.containsJob(jobName)) {
            throw new IllegalArgumentException("A job with the name '" + jobName + "' already exist.");
        }

        final Datastore datastore = tenantContext.getConfiguration().getDatastoreCatalog()
                .getDatastore(selectedDatastore.getName());

        final JobWizardContext context = new JobWizardContextImpl(tenantContext, datastore, jobName,
                createSessionFunc());

        final WizardSession session = wizard.start(context);

        return startSession(session, wizardIdentifier, context);
    }

    private WizardPage startSession(WizardSession session, WizardIdentifier wizardIdentifier, WizardContext context) {
        final String sessionId = createSessionId();

        final WizardSessionIdentifier sessionIdentifier = new WizardSessionIdentifier();
        sessionIdentifier.setSessionId(sessionId);
        sessionIdentifier.setWizardIdentifier(wizardIdentifier);

        final WizardPageController firstPageController = session.firstPageController();

        createSession(sessionId, session, context, firstPageController);

        return createPage(sessionIdentifier, firstPageController, session);
    }

    @Override
    public WizardPage nextPage(TenantIdentifier tenant, WizardSessionIdentifier sessionIdentifier,
            Map<String, List<String>> formParameters) throws DCUserInputException {
        final String sessionId = sessionIdentifier.getSessionId();
        final WizardPageController controller = _currentControllers.get(sessionId);

        final WizardPageController nextPageController;

        try {
            nextPageController = controller.nextPageController(formParameters);
        } catch (DCUserInputException e) {
            logger.info("A user input exception was thrown by wizard controller - rethrowing to UI: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("An unexpected error occurred in the wizard controller, wizard will be closed", e);
            closeSession(sessionId);
            throw e;
        }

        if (nextPageController == null) {
            final WizardContext wizardContext = _contexts.get(sessionId);
            final WizardSession session = _sessions.get(sessionId);
            final String wizardResult;
            try {
                if (wizardContext instanceof JobWizardContext) {
                    wizardResult = finishJobWizard((JobWizardContext) wizardContext, (JobWizardSession) session);
                } else if (wizardContext instanceof DatastoreWizardContext) {
                    wizardResult = finishDatastoreWizard((DatastoreWizardContext) wizardContext,
                            (DatastoreWizardSession) session);
                } else {
                    throw new UnsupportedOperationException("Unexpected wizard type: " + wizardContext);
                }
            } finally {
                closeSession(sessionId);
            }

            // returning null signals that no more pages should be shown, the
            // wizard is done.
            return createFinishPage(sessionIdentifier, wizardResult);
        } else {
            final WizardSession session = _sessions.get(sessionId);
            _currentControllers.put(sessionId, nextPageController);
            return createPage(sessionIdentifier, nextPageController, session);
        }
    }

    /**
     * Creates a "page" that symbolizes a finished wizard.
     * 
     * @param sessionId
     * @param wizardResult
     * @return
     */
    private WizardPage createFinishPage(WizardSessionIdentifier sessionIdentifier, String wizardResult) {
        WizardPage page = new WizardPage();
        page.setPageIndex(WizardPage.PAGE_INDEX_FINISHED);
        page.setSessionIdentifier(sessionIdentifier);
        page.setWizardResult(wizardResult);
        return page;
    }

    private String finishDatastoreWizard(DatastoreWizardContext wizardContext, DatastoreWizardSession session) {
        final DocumentBuilder documentBuilder;
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        final TenantContext tenantContext = wizardContext.getTenantContext();
        final Element datastoreNode = session.createDatastoreElement(documentBuilder);

        String datastoreName = _datastoreDao.addDatastore(tenantContext, datastoreNode);
        return datastoreName;
    }

    private String finishJobWizard(final JobWizardContext wizardContext, final JobWizardSession session) {
        final TenantContext tenantContext = wizardContext.getTenantContext();
        final RepositoryFolder jobFolder = tenantContext.getJobFolder();
        final String jobName = wizardContext.getJobName();
        jobFolder.createFile(jobName + FileFilters.ANALYSIS_XML.getExtension(),
                new Action<OutputStream>() {
                    @Override
                    public void run(OutputStream out) throws Exception {

                        final AnalysisJobBuilder jobBuilder = session.createJob();
                        final AnalysisJob analysisJob = jobBuilder.toAnalysisJob();

                        final AnalyzerBeansConfiguration configuration = tenantContext.getConfiguration();
                        final JaxbJobWriter writer = new JaxbJobWriter(configuration);
                        writer.write(analysisJob, out);
                    }
                });
        return jobName;
    }

    private WizardPage createPage(WizardSessionIdentifier sessionIdentifier, WizardPageController pageController,
            WizardSession session) {
        final WizardPage page = new WizardPage();
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
    public Boolean cancelWizard(TenantIdentifier tenant, WizardSessionIdentifier sessionIdentifier) {
        if (sessionIdentifier == null) {
            return true;
        }
        String sessionId = sessionIdentifier.getSessionId();
        closeSession(sessionId);
        return true;
    }

    public void createSession(String sessionId, WizardSession session, WizardContext context,
            WizardPageController controller) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        _sessions.put(sessionId, session);
        _contexts.put(sessionId, context);
        _currentControllers.put(sessionId, controller);
    }

    private void closeSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        _sessions.remove(sessionId);
        _contexts.remove(sessionId);
        _currentControllers.remove(sessionId);
    }

    private JobWizard instantiateJobWizard(WizardIdentifier wizardIdentifier) {
        for (JobWizard jobWizard : getAvailableJobWizards()) {
            final String displayName = jobWizard.getDisplayName();
            if (displayName.equals(wizardIdentifier.getDisplayName())) {
                return jobWizard;
            }
        }
        return null;
    }

    private DatastoreWizard instantiateDatastoreWizard(WizardIdentifier wizardIdentifier) {
        for (DatastoreWizard jobWizard : getAvailableDatastoreWizards()) {
            final String displayName = jobWizard.getDisplayName();
            if (displayName.equals(wizardIdentifier.getDisplayName())) {
                return jobWizard;
            }
        }
        return null;
    }
}
