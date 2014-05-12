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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.eobjects.analyzer.configuration.InjectionManagerFactoryImpl;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.server.job.DefaultJobEngineManager;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.file.FileRepository;
import org.eobjects.metamodel.util.DateUtils;
import org.eobjects.metamodel.util.Month;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ibm.icu.text.SimpleDateFormat;

public class SchedulingServiceImplTest extends TestCase {

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
        assertTrue(logOutput, logOutput.indexOf("org.eobjects.analyzer.job.runner.AnalysisJobCancellation") != -1);
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
            assertEquals("[tenant1, tenant2]", scheduler.getTriggerGroupNames().toString());
            assertEquals("[tenant1.random_number_generation]",
                    scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals("tenant1")).toString());
            assertEquals("[tenant2.another_random_job]",
                    scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals("tenant2")).toString());

            assertEquals("[tenant1, tenant2]", scheduler.getJobGroupNames().toString());
            assertEquals("[tenant1.random_number_generation]",
                    scheduler.getJobKeys(GroupMatcher.jobGroupEndsWith("tenant1")).toString());
            assertEquals("[tenant2.another_random_job]", scheduler.getJobKeys(GroupMatcher.jobGroupEndsWith("tenant2"))
                    .toString());

            final TenantIdentifier tenant = new TenantIdentifier("tenant1");

            final List<ScheduleDefinition> schedules = service.getSchedules(tenant);

            // sort to make it deterministic
            Collections.sort(schedules);

            assertEquals(6, schedules.size());
            assertEquals(null, schedules.get(1).getCronExpression());
            ScheduleDefinition randomNumberGenerationSchedule = schedules.get(4);
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

    public void testToCronExpressionYearly() throws Exception {
        CronExpression dailyExpr = SchedulingServiceImpl.toCronExpression("@yearly");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.add(Calendar.YEAR, 1);

        assertEquals(cal.getTime(), dailyExpr.getNextValidTimeAfter(new Date()));
    }

    public void testToCronExpressionMonthly() throws Exception {
        CronExpression dailyExpr = SchedulingServiceImpl.toCronExpression("@monthly");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.DATE, 1);
        cal.add(Calendar.MONTH, 1);

        assertEquals(cal.getTime(), dailyExpr.getNextValidTimeAfter(new Date()));
    }

    public void testToCronExpressionWeekly() throws Exception {
        CronExpression dailyExpr = SchedulingServiceImpl.toCronExpression("@weekly");
        Date callTime = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(callTime);
        if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
            Date time = cal.getTime();
            assertEquals(time, dailyExpr.getNextValidTimeAfter(callTime));
        }

        callTime = DateUtils.get(2012, Month.MARCH, 21);
        assertEquals("2012-03-25", new SimpleDateFormat("yyyy-MM-dd").format(dailyExpr.getNextValidTimeAfter(callTime)));

        callTime = DateUtils.get(2012, Month.MARCH, 24);
        assertEquals("2012-03-25", new SimpleDateFormat("yyyy-MM-dd").format(dailyExpr.getNextValidTimeAfter(callTime)));

        callTime = DateUtils.get(2012, Month.MARCH, 25);
        assertEquals("2012-04-01", new SimpleDateFormat("yyyy-MM-dd").format(dailyExpr.getNextValidTimeAfter(callTime)));

        callTime = DateUtils.get(2012, Month.MARCH, 26);
        assertEquals("2012-04-01", new SimpleDateFormat("yyyy-MM-dd").format(dailyExpr.getNextValidTimeAfter(callTime)));
    }

    public void testToCronExpressionDaily() throws Exception {
        CronExpression dailyExpr = SchedulingServiceImpl.toCronExpression("@daily");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.add(Calendar.DATE, 1);

        assertEquals(cal.getTime(), dailyExpr.getNextValidTimeAfter(new Date()));
    }

    public void testToCronExpressionHourly() throws Exception {
        CronExpression dailyExpr = SchedulingServiceImpl.toCronExpression("@hourly");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.add(Calendar.HOUR_OF_DAY, 1);

        assertEquals(cal.getTime(), dailyExpr.getNextValidTimeAfter(new Date()));
    }

    public void testToCronExpressionMinutely() throws Exception {
        CronExpression dailyExpr = SchedulingServiceImpl.toCronExpression("@minutely");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.MINUTE, 1);

        assertEquals(cal.getTime(), dailyExpr.getNextValidTimeAfter(new Date()));
    }
}
