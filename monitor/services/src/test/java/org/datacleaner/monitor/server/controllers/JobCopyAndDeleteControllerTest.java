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
import java.util.concurrent.atomic.AtomicBoolean;

import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.events.JobCopyEvent;
import org.datacleaner.monitor.events.JobDeletionEvent;
import org.datacleaner.monitor.server.job.DefaultJobEngineManager;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class JobCopyAndDeleteControllerTest extends TestCase {

    private Repository repository;
    private TenantContextFactoryImpl tenantContextFactory;

    protected void setUp() throws Exception {
        final ApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("context/application-context.xml");
        repository = applicationContext.getBean(FileRepository.class);

        tenantContextFactory = new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(),
                new DefaultJobEngineManager(applicationContext));
    }

    public void testRenameJobAndResult() throws Exception {
        assertNull(repository.getRepositoryNode("/tenant1/jobs/product_analysis.analysis.xml"));

        final AtomicBoolean copyEventReceived = new AtomicBoolean(false);

        final JobCopyController jobCopyController = new JobCopyController();
        jobCopyController._contextFactory = tenantContextFactory;
        jobCopyController._eventPublisher = event -> {
            copyEventReceived.set(true);

            assertTrue(event instanceof JobCopyEvent);
            final JobCopyEvent copyEvent = (JobCopyEvent) event;

            assertEquals("product_profiling", copyEvent.getSourceJob().getName());
            assertEquals("product_analysis", copyEvent.getTargetJob().getName());
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
        jobDeleteController._eventPublisher = event -> {
            deleteEventReceived.set(true);

            assertTrue(event instanceof JobDeletionEvent);
            final JobDeletionEvent copyEvent = (JobDeletionEvent) event;

            assertEquals("product_analysis", copyEvent.getJobName());
        };

        result = jobDeleteController.deleteJob("tenant1", "product_analysis");
        assertEquals("{action=delete, job=product_analysis}", result.toString());

        assertTrue(deleteEventReceived.get());

        assertNull(repository.getRepositoryNode("/tenant1/jobs/product_analysis.analysis.xml"));
    }
}
