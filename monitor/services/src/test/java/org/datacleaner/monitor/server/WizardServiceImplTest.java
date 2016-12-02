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

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.jobwizard.common.MockAnalysisWizard;
import org.datacleaner.monitor.server.dao.WizardDaoImpl;
import org.datacleaner.monitor.server.job.DataCleanerJobContext;
import org.datacleaner.monitor.server.job.MockJobEngineManager;
import org.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.monitor.shared.model.WizardIdentifier;
import org.datacleaner.monitor.shared.model.WizardPage;
import org.datacleaner.monitor.shared.model.WizardSessionIdentifier;
import org.datacleaner.monitor.wizard.job.JobWizard;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;
import org.datacleaner.util.FileFilters;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;

import junit.framework.TestCase;

public class WizardServiceImplTest extends TestCase {

    private WizardServiceImpl service;
    private WizardDaoImpl wizardDao;
    private ApplicationContext applicationContextMock;
    private DatastoreServiceImpl datastoreService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Repository repository = new FileRepository("src/test/resources/example_repo");

        final Map<String, JobWizard> wizardMap = new TreeMap<>();
        wizardMap.put("quick", new MockAnalysisWizard());

        applicationContextMock = EasyMock.createMock(ApplicationContext.class);
        EasyMock.expect(applicationContextMock.getBeansOfType(JobWizard.class)).andReturn(wizardMap).anyTimes();
        EasyMock.replay(applicationContextMock);

        wizardDao = new WizardDaoImpl(applicationContextMock);

        final TenantContextFactoryImpl tenantContextFactory =
                new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(), new MockJobEngineManager());
        service = new WizardServiceImpl();
        service._tenantContextFactory = tenantContextFactory;
        service._wizardDao = wizardDao;

        datastoreService = new DatastoreServiceImpl(tenantContextFactory);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        EasyMock.verify(applicationContextMock);
    }

    public void testScenario() throws Exception {
        assertEquals(0L, wizardDao.getOpenSessionCount());

        final TenantIdentifier tenant = new TenantIdentifier("tenant1");

        final List<DatastoreIdentifier> datastores = datastoreService.getAvailableDatastores(tenant);
        assertEquals(2, datastores.size());
        assertEquals("[DatastoreIdentifier[name=Vendors], DatastoreIdentifier[name=orderdb]]", datastores.toString());

        final DatastoreIdentifier selectedDatastore = datastores.get(1);

        final List<WizardIdentifier> jobWizardIdentifiers =
                service.getJobWizardIdentifiers(tenant, selectedDatastore, "en");
        assertEquals(1, jobWizardIdentifiers.size());

        final WizardIdentifier jobWizardIdentifier = jobWizardIdentifiers.get(0);
        assertEquals("JobWizardIdentifier[Mock wizard]", jobWizardIdentifier.toString());

        WizardPage wizardPage;

        final String jobName = "JobWizardServiceImplTest-job1";

        // first page is the select table page.
        wizardPage = service.startJobWizard(tenant, jobWizardIdentifier, selectedDatastore, "en");

        assertEquals(1L, wizardDao.getOpenSessionCount());
        assertNotNull(wizardPage);
        assertEquals(0, wizardPage.getPageIndex().intValue());
        assertNotNull(wizardPage.getFormInnerHtml());

        final WizardSessionIdentifier wizardSession = wizardPage.getSessionIdentifier();
        assertNotNull(wizardSession.getSessionId());

        Map<String, List<String>> formParameters;
        formParameters = new HashMap<>();
        formParameters.put("tableName", Arrays.asList("PUBLIC.CUSTOMERS"));

        // submit first page
        wizardPage = service.nextPage(tenant, wizardSession, formParameters);

        // second page is select columns page.
        assertEquals(1L, wizardDao.getOpenSessionCount());
        assertNotNull(wizardPage);
        assertEquals(1, wizardPage.getPageIndex().intValue());
        assertNotNull(wizardPage.getFormInnerHtml());

        formParameters = new HashMap<>();
        formParameters.put("columns", Arrays.asList("CUSTOMERNUMBER", "CUSTOMERNAME"));

        // submit second page
        wizardPage = service.nextPage(tenant, wizardSession, formParameters);
        assertEquals(2, wizardPage.getPageIndex().intValue());

        // go back and forth
        wizardPage = service.previousPage(tenant, wizardSession);
        assertEquals(1, wizardPage.getPageIndex().intValue());
        wizardPage = service.nextPage(tenant, wizardSession, formParameters);
        assertEquals(2, wizardPage.getPageIndex().intValue());

        // now we submit a name for the job
        formParameters = new HashMap<>();
        formParameters.put("name", Arrays.asList(jobName));
        wizardPage = service.nextPage(tenant, wizardSession, formParameters);

        assertTrue(wizardPage.isFinished());

        // find the job and do assertions on it.
        final File jobFile = new File(
                "src/test/resources/example_repo/" + tenant.getId() + "/jobs/" + jobName + FileFilters.ANALYSIS_XML
                        .getExtension());
        try {
            final DataCleanerJobContext job =
                    (DataCleanerJobContext) service._tenantContextFactory.getContext(tenant).getJob(jobName);
            assertNotNull(job);
            assertEquals("orderdb", job.getSourceDatastoreName());
            assertEquals("[CUSTOMERS.CUSTOMERNUMBER, CUSTOMERS.CUSTOMERNAME]", job.getSourceColumnPaths().toString());

            assertTrue(jobFile.exists());
        } finally {
            // clean up
            jobFile.delete();
        }
    }
}
