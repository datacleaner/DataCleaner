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
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.eobjects.analyzer.configuration.InjectionManagerFactoryImpl;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.eobjects.datacleaner.monitor.events.JobCopyEvent;
import org.eobjects.datacleaner.monitor.events.JobDeletionEvent;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.file.FileRepository;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

public class JobCopyAndDeleteControllerTest extends TestCase {

    private Repository repository;
    private TenantContextFactoryImpl tenantContextFactory;

    protected void setUp() throws Exception {
        File targetDir = new File("target/repo_job_copy");
        FileUtils.deleteDirectory(targetDir);
        FileUtils.copyDirectory(new File("src/test/resources/example_repo"), targetDir);
        repository = new FileRepository(targetDir);

        tenantContextFactory = new TenantContextFactoryImpl(repository, new InjectionManagerFactoryImpl());
    }

    public void testRenameJobAndResult() throws Exception {
        assertNull(repository.getRepositoryNode("/tenant1/jobs/product_analysis.analysis.xml"));

        final AtomicBoolean copyEventReceived = new AtomicBoolean(false);

        final JobCopyController jobCopyController = new JobCopyController();
        jobCopyController._contextFactory = tenantContextFactory;
        jobCopyController._eventPublisher = new ApplicationEventPublisher() {
            @Override
            public void publishEvent(ApplicationEvent event) {
                copyEventReceived.set(true);

                assertTrue(event instanceof JobCopyEvent);
                JobCopyEvent copyEvent = (JobCopyEvent) event;

                assertEquals("product_profiling", copyEvent.getSourceJob().getName());
                assertEquals("product_analysis", copyEvent.getTargetJob().getName());
            }
        };

        final JobCopyPayload input = new JobCopyPayload();
        input.setName("product_analysis");

        Map<String, String> result = jobCopyController.copyJob("tenant1", "product_profiling", input);
        assertEquals("{repository_url=/tenant1/jobs/product_analysis.analysis.xml, "
                + "source_job=product_profiling, target_job=product_analysis}", result.toString());

        assertNotNull(repository.getRepositoryNode("/tenant1/jobs/product_analysis.analysis.xml"));

        assertTrue(copyEventReceived.get());

        final AtomicBoolean deleteEventReceived = new AtomicBoolean(false);
        final JobDeletionController jobDeleteController = new JobDeletionController();
        jobDeleteController._contextFactory = tenantContextFactory;
        jobDeleteController._eventPublisher = new ApplicationEventPublisher() {
            @Override
            public void publishEvent(ApplicationEvent event) {
                deleteEventReceived.set(true);

                assertTrue(event instanceof JobDeletionEvent);
                JobDeletionEvent copyEvent = (JobDeletionEvent) event;

                assertEquals("product_analysis", copyEvent.getJobName());
            }
        };

        result = jobDeleteController.deleteJob("tenant1", "product_analysis");
        assertEquals("{action=delete, job=product_analysis}", result.toString());

        assertTrue(deleteEventReceived.get());

        assertNull(repository.getRepositoryNode("/tenant1/jobs/product_analysis.analysis.xml"));
    }
}
