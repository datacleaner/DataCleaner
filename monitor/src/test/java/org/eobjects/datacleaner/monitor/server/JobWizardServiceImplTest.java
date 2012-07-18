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

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eobjects.datacleaner.monitor.configuration.JobContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizard;
import org.eobjects.datacleaner.monitor.jobwizard.quickanalysis.QuickAnalysisWizard;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardPage;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardSessionIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.file.FileRepository;
import org.eobjects.datacleaner.util.FileFilters;
import org.springframework.context.ApplicationContext;

public class JobWizardServiceImplTest extends TestCase {

    private JobWizardServiceImpl service;
    private ApplicationContext applicationContextMock;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Repository repository = new FileRepository("src/test/resources/example_repo");

        final Map<String, JobWizard> wizardMap = new TreeMap<String, JobWizard>();
        wizardMap.put("quick", new QuickAnalysisWizard());

        applicationContextMock = EasyMock.createMock(ApplicationContext.class);
        EasyMock.expect(applicationContextMock.getBeansOfType(JobWizard.class)).andReturn(wizardMap).anyTimes();
        EasyMock.replay(applicationContextMock);

        service = new JobWizardServiceImpl();
        service._tenantContextFactory = new TenantContextFactoryImpl(repository);
        service._applicationContext = applicationContextMock;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        EasyMock.verify(applicationContextMock);
    }

    public void testScenario() throws Exception {
        assertEquals(0, service.getOpenSessionCount());

        final TenantIdentifier tenant = new TenantIdentifier("tenant1");

        final List<JobWizardIdentifier> jobWizardIdentifiers = service.getJobWizardIdentifiers(tenant);
        assertEquals(1, jobWizardIdentifiers.size());

        final JobWizardIdentifier jobWizardIdentifier = jobWizardIdentifiers.get(0);
        assertEquals("JobWizardIdentifier[Quick analysis]", jobWizardIdentifier.toString());

        final List<DatastoreIdentifier> datastores = service.getAvailableDatastores(tenant);
        assertEquals(2, datastores.size());
        assertEquals("[DatastoreIdentifier[name=Vendors], DatastoreIdentifier[name=orderdb]]", datastores.toString());

        JobWizardPage wizardPage;
        Map<String, List<String>> formParameters;

        final String jobName = "JobWizardServiceImplTest-job1";

        // first page is the select table page.
        wizardPage = service.startWizard(tenant, jobWizardIdentifier, datastores.get(1), jobName);

        assertEquals(1, service.getOpenSessionCount());
        assertNotNull(wizardPage);
        assertEquals(0, wizardPage.getPageIndex().intValue());
        assertNotNull(wizardPage.getFormInnerHtml());

        final JobWizardSessionIdentifier wizardSession = wizardPage.getSessionIdentifier();
        assertNotNull(wizardSession.getSessionId());

        formParameters = new HashMap<String, List<String>>();
        formParameters.put("tableName", Arrays.asList("PUBLIC.CUSTOMERS"));

        // submit first page
        wizardPage = service.nextPage(tenant, wizardSession, formParameters);

        // second page is select columns page.
        assertEquals(1, service.getOpenSessionCount());
        assertNotNull(wizardPage);
        assertEquals(1, wizardPage.getPageIndex().intValue());
        assertNotNull(wizardPage.getFormInnerHtml());

        formParameters = new HashMap<String, List<String>>();
        formParameters.put("columns", Arrays.asList("CUSTOMERNUMBER", "CUSTOMERNAME"));

        // submit second page
        wizardPage = service.nextPage(tenant, wizardSession, formParameters);

        assertNull(wizardPage);

        // find the job and do assertions on it.

        final JobContext job = service._tenantContextFactory.getContext(tenant).getJob(jobName);
        assertNotNull(job);
        assertEquals("orderdb", job.getSourceDatastoreName());
        assertEquals("[PUBLIC.CUSTOMERS.CUSTOMERNUMBER, PUBLIC.CUSTOMERS.CUSTOMERNAME]", job.getSourceColumnPaths()
                .toString());

        File jobFile = new File("src/test/resources/example_repo/" + tenant.getId() + "/jobs/" + jobName
                + FileFilters.ANALYSIS_XML.getExtension());

        assertTrue(jobFile.exists());

        // clean up
        jobFile.delete();
    }
}
