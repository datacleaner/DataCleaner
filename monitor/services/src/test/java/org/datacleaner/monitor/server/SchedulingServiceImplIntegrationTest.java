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
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.datacleaner.configuration.InjectionManagerFactoryImpl;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.server.job.DefaultJobEngineManager;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Slightly heavier tests than those in {@link SchedulingServiceImplTest}. To
 * avoid the penalty of the {@link #setUp()} here we've separated it from the
 * regular unittest.
 */
public class SchedulingServiceImplIntegrationTest extends TestCase {

    private SchedulingServiceImpl service;
    private File resultDirectory;
    private TenantContextFactory tenantContextFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (service == null) {
            final File targetDir = new File("target/example_repo");
            FileUtils.deleteDirectory(targetDir);
            FileUtils.copyDirectory(new File("src/test/resources/example_repo"), targetDir);

            final ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                    "context/application-context.xml");

            final Repository repository = new FileRepository(targetDir);
            tenantContextFactory = new TenantContextFactoryImpl(repository, new InjectionManagerFactoryImpl(),
                    new DefaultJobEngineManager(applicationContext));

            service = new SchedulingServiceImpl(repository, tenantContextFactory);
            service.setApplicationContext(applicationContext);

            service.initialize();

            resultDirectory = new File(targetDir, "tenant1/results");
        }
    }

    public void testCancellation() throws Exception {
        final TenantIdentifier tenant = new TenantIdentifier("tenant1");

        final ScheduleDefinition schedule = service.getSchedule(tenant, new JobIdentifier("waiting_job"));
        ExecutionLog execution = service.triggerExecution(tenant, schedule.getJob());

        assertEquals(ExecutionStatus.PENDING, execution.getExecutionStatus());
        assertNotNull(execution.getJob());

        while (execution.getExecutionStatus() == ExecutionStatus.PENDING) {
            execution = service.getExecution(tenant, execution);
            assertNotNull(execution.getJob());
        }

        boolean result = service.cancelExecution(tenant, execution);
        if (!result) {
            final String logOutput = execution.getLogOutput();
            System.err.println(logOutput);
            fail("Expected positive result");
        }

        execution = service.getExecution(tenant, execution);
        assertEquals(ExecutionStatus.FAILURE, execution.getExecutionStatus());

        final String logOutput = execution.getLogOutput();
        assertTrue(logOutput, logOutput.indexOf("org.datacleaner.job.runner.AnalysisJobCancellation") != -1);
    }

    public void testActiveQuartzTriggersInScenario() throws Exception {
        Scheduler scheduler = service.getScheduler();

        assertTrue(scheduler.isStarted());

        final FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("random_number");
            }
        };

        try {
            final List<String> triggerGroupNames = scheduler.getTriggerGroupNames();
            Collections.sort(triggerGroupNames);

            assertEquals("[tenant1, tenant2]", triggerGroupNames.toString());
            assertEquals("[tenant1.random_number_generation]",
                    scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals("tenant1")).toString());
            assertEquals("[tenant2.another_random_job]",
                    scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals("tenant2")).toString());

            final List<String> jobGroupNames = scheduler.getJobGroupNames();
            Collections.sort(jobGroupNames);

            assertEquals("[tenant1, tenant2]", jobGroupNames.toString());
            assertEquals("[tenant1.random_number_generation]",
                    scheduler.getJobKeys(GroupMatcher.jobGroupEndsWith("tenant1")).toString());
            assertEquals("[tenant2.another_random_job]", scheduler.getJobKeys(GroupMatcher.jobGroupEndsWith("tenant2"))
                    .toString());

            final TenantIdentifier tenant = new TenantIdentifier("tenant1");

            final List<ScheduleDefinition> schedules = service.getSchedules(tenant);

            // sort to make it deterministic
            Collections.sort(schedules);

            assertEquals(9, schedules.size());
            assertEquals(null, schedules.get(1).getCronExpression());
            ScheduleDefinition randomNumberGenerationSchedule = schedules.get(7);
            assertEquals("@hourly", randomNumberGenerationSchedule.getCronExpression());

            final CronTrigger trigger = (CronTrigger) scheduler.getTrigger(new TriggerKey("random_number_generation",
                    "tenant1"));
            assertEquals("0 0 * * * ?", trigger.getCronExpression());

            File[] files = resultDirectory.listFiles(filenameFilter);
            assertEquals("Unexpected files in " + resultDirectory + ": " + Arrays.toString(files), 0, files.length);

            ExecutionLog execution = service.triggerExecution(tenant, randomNumberGenerationSchedule.getJob());

            assertEquals(ExecutionStatus.PENDING, execution.getExecutionStatus());
            assertNull(execution.getJobEndDate());

            for (int i = 0; i < 100; i++) {
                // spend max 10 seconds waiting for execution
                if (execution.getExecutionStatus() == ExecutionStatus.RUNNING
                        || execution.getExecutionStatus() == ExecutionStatus.PENDING) {
                    Thread.sleep(100);
                }
            }

            final String logOutput = execution.getLogOutput();
            assertEquals("Got " + execution + ":\n" + logOutput, ExecutionStatus.SUCCESS,
                    execution.getExecutionStatus());
            assertNotNull(execution.getJobBeginDate());
            assertNotNull(execution.getJobEndDate());
            assertTrue("Unexpected log output was: " + logOutput, logOutput.indexOf("Job execution BEGIN") != -1);
            assertTrue("Unexpected log output was: " + logOutput, logOutput.indexOf("Job execution SUCCESS") != -1);

            scheduler.shutdown(true);

            files = resultDirectory.listFiles(filenameFilter);
            assertEquals("Expected 2 files: analysis result and log file, but found: " + Arrays.toString(files), 2,
                    files.length);

        } finally {
            scheduler.shutdown();

            File[] files = resultDirectory.listFiles(filenameFilter);
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
        }
    }
}
