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
package org.eobjects.datacleaner.monitor.server.controllers;

import java.io.File;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.eobjects.analyzer.configuration.InjectionManagerFactoryImpl;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineIdentifier;
import org.eobjects.datacleaner.monitor.events.JobModificationEvent;
import org.eobjects.datacleaner.monitor.server.dao.ResultDao;
import org.eobjects.datacleaner.monitor.server.dao.ResultDaoImpl;
import org.eobjects.datacleaner.monitor.server.dao.TimelineDaoImpl;
import org.eobjects.datacleaner.monitor.server.listeners.JobModificationEventRenameResultsListener;
import org.eobjects.datacleaner.monitor.server.listeners.JobModificationEventUpdateTimelinesListener;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.repository.file.FileRepository;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

public class JobModificationControllerTest extends TestCase {

    private JobModificationController jobModificationController;
    private JobModificationEventRenameResultsListener jobModificationListener1;
    private JobModificationEventUpdateTimelinesListener jobModificationListener2;
    private ResultModificationController resultModificationController;
    private Repository repository;
    private TimelineDaoImpl timelineDao;

    protected void setUp() throws Exception {
        File targetDir = new File("target/repo_job_modification");
        FileUtils.deleteDirectory(targetDir);
        FileUtils.copyDirectory(new File("src/test/resources/example_repo"), targetDir);
        repository = new FileRepository(targetDir);

        final TenantContextFactoryImpl tenantContextFactory = new TenantContextFactoryImpl(repository,
                new InjectionManagerFactoryImpl());

        resultModificationController = new ResultModificationController();
        resultModificationController._contextFactory = tenantContextFactory;
        resultModificationController._eventPublisher = new ApplicationEventPublisher() {
            @Override
            public void publishEvent(ApplicationEvent event) {
                // do nothing, we'll not test that here
            }
        };

        final ResultDao resultDao = new ResultDaoImpl(tenantContextFactory);
        timelineDao = new TimelineDaoImpl(tenantContextFactory, repository);

        jobModificationController = new JobModificationController();
        jobModificationListener1 = new JobModificationEventRenameResultsListener(resultDao,
                resultModificationController);
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

        final Map<String, String> result = jobModificationController
                .modifyJob("tenant1", "product_profiling", input);
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
