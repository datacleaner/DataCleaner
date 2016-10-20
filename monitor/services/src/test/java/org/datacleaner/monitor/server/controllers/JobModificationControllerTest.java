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
package org.datacleaner.monitor.server.controllers;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.datacleaner.monitor.dashboard.model.TimelineIdentifier;
import org.datacleaner.monitor.events.JobModificationEvent;
import org.datacleaner.monitor.server.dao.ResultDao;
import org.datacleaner.monitor.server.dao.ResultDaoImpl;
import org.datacleaner.monitor.server.dao.TimelineDaoImpl;
import org.datacleaner.monitor.server.job.DefaultJobEngineManager;
import org.datacleaner.monitor.server.listeners.JobModificationEventRenameResultsListener;
import org.datacleaner.monitor.server.listeners.JobModificationEventUpdateTimelinesListener;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.repository.file.FileRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletResponse;

public class JobModificationControllerTest extends TestCase {

    private JobModificationController jobModificationController;
    private JobModificationEventRenameResultsListener jobModificationListener1;
    private JobModificationEventUpdateTimelinesListener jobModificationListener2;
    private Repository repository;
    private TimelineDaoImpl timelineDao;

    protected void setUp() throws Exception {
        final ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "context/application-context.xml");
        repository = applicationContext.getBean(FileRepository.class);

        final TenantContextFactoryImpl tenantContextFactory = new TenantContextFactoryImpl(repository,
                new DataCleanerEnvironmentImpl(), new DefaultJobEngineManager(applicationContext));

        final ResultDao resultDao = new ResultDaoImpl(tenantContextFactory, null);
        timelineDao = new TimelineDaoImpl(tenantContextFactory, repository);

        jobModificationController = new JobModificationController();
        jobModificationListener1 = new JobModificationEventRenameResultsListener(resultDao);
        jobModificationListener2 = new JobModificationEventUpdateTimelinesListener(timelineDao);
        jobModificationController._contextFactory = tenantContextFactory;
        jobModificationController._eventPublisher = new ApplicationEventPublisher() {
            @Override
            public void publishEvent(ApplicationEvent event) {
                jobModificationListener1.onApplicationEvent((JobModificationEvent) event);
                jobModificationListener2.onApplicationEvent((JobModificationEvent) event);
            }
        };
    }

    public void testRenameJobAndResult() throws Exception {
        final JobModificationPayload input = new JobModificationPayload();
        input.setName("renamed_job");

        HttpServletResponse response = new MockHttpServletResponse(); 
        final Map<String, String> result = jobModificationController.modifyJob("tenant1", "product_profiling", input, response);
        assertEquals("{new_job_name=renamed_job, old_job_name=product_profiling, "
                + "repository_url=/tenant1/jobs/renamed_job.analysis.xml}", result.toString());

        final RepositoryFolder resultsFolder = repository.getFolder("tenant1").getFolder("results");

        // check that files have been renamed
        assertEquals(0, resultsFolder.getFiles("product_profiling", ".result.dat").size());
        assertEquals(6, resultsFolder.getFiles("renamed_job", ".result.dat").size());

        final TimelineDefinition timelineDefinition = timelineDao.getTimelineDefinition(new TimelineIdentifier(
                "Product types", "/tenant1/timelines/Product data/Product types.analysis.timeline.xml", null));

        assertEquals("renamed_job", timelineDefinition.getJobIdentifier().getName());
    }
}
