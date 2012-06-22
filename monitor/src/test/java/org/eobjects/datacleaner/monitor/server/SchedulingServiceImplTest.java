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
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.datacleaner.monitor.configuration.ConfigurationCache;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.timeline.TimelineService;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.file.FileRepository;
import org.eobjects.metamodel.util.DateUtils;
import org.eobjects.metamodel.util.Month;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;

import com.ibm.icu.text.SimpleDateFormat;

public class SchedulingServiceImplTest extends TestCase {

    public void testScenario() throws Exception {
        Repository repository = new FileRepository("src/test/resources/example_repo");
        ConfigurationCache configurationCache = new ConfigurationCache(repository);
        TimelineService timelineService = new TimelineServiceImpl(repository, configurationCache);

        SchedulingServiceImpl service = new SchedulingServiceImpl(timelineService, repository, configurationCache);

        Scheduler scheduler = service.getScheduler();
        assertFalse(scheduler.isStarted());

        service.initialize();

        assertTrue(scheduler.isStarted());
        scheduler.pauseAll();

        final File directory = new File("src/test/resources/example_repo/tenant1/results");
        final FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("random_number");
            }
        };

        try {
            assertEquals("[tenant1, tenant2]", Arrays.toString(scheduler.getTriggerGroupNames()));
            assertEquals("[random_number_generation]", Arrays.toString(scheduler.getTriggerNames("tenant1")));
            assertEquals("[another_random_job]", Arrays.toString(scheduler.getTriggerNames("tenant2")));

            assertEquals("[tenant1, tenant2]", Arrays.toString(scheduler.getJobGroupNames()));
            assertEquals("[random_number_generation]", Arrays.toString(scheduler.getJobNames("tenant1")));
            assertEquals("[another_random_job]", Arrays.toString(scheduler.getJobNames("tenant2")));

            final TenantIdentifier tenant = new TenantIdentifier("tenant1");

            final List<ScheduleDefinition> schedules = service.getSchedules(tenant);

            assertEquals(2, schedules.size());
            assertEquals(null, schedules.get(0).getScheduleExpression());
            ScheduleDefinition randomNumberGenerationSchedule = schedules.get(1);
            assertEquals("@hourly", randomNumberGenerationSchedule.getScheduleExpression());

            final CronTrigger trigger = (CronTrigger) scheduler.getTrigger("random_number_generation", "tenant1");
            assertEquals("0 0 * * * ?", trigger.getCronExpression());

            File[] files = directory.listFiles(filenameFilter);
            assertEquals("Unexpected files in " + directory + ": " + Arrays.toString(files), 0, files.length);

            final ExecutionLog execution = service.triggerExecution(tenant, randomNumberGenerationSchedule.getJob());
            assertEquals(ExecutionStatus.RUNNING, execution.getExecutionStatus());
            assertNull(execution.getJobEndDate());
            assertNotNull(execution.getJobBeginDate());

            for (int i = 0; i < 100; i++) {
                // spend max 10 seconds waiting for execution
                if (execution.getExecutionStatus() == ExecutionStatus.RUNNING) {
                    Thread.sleep(100);
                }
            }

            assertNotNull(execution.getJobEndDate());
            assertEquals(ExecutionStatus.SUCCESS, execution.getExecutionStatus());
            final String logOutput = execution.getLogOutput();
            assertTrue("Unexpected log output was: " + logOutput, logOutput.indexOf("Job execution BEGIN") != -1);
            assertTrue("Unexpected log output was: " + logOutput, logOutput.indexOf("Job execution SUCCESS") != -1);

            files = directory.listFiles(filenameFilter);
            assertEquals("Expected 2 files: analysis result and log file, but found: " + Arrays.toString(files), 2,
                    files.length);

        } finally {
            scheduler.shutdown();

            File[] files = directory.listFiles(filenameFilter);
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
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            cal.add(Calendar.DATE, 1);
        }

        assertEquals(cal.getTime(), dailyExpr.getNextValidTimeAfter(callTime));

        callTime = DateUtils.get(2012, Month.MARCH, 21);
        assertEquals("2012-03-25", new SimpleDateFormat("yyyy-MM-dd").format(dailyExpr.getNextValidTimeAfter(callTime)));

        callTime = DateUtils.get(2012, Month.MARCH, 24);
        assertEquals("2012-03-25", new SimpleDateFormat("yyyy-MM-dd").format(dailyExpr.getNextValidTimeAfter(callTime)));

        callTime = DateUtils.get(2012, Month.MARCH, 25);
        assertEquals("2012-03-25", new SimpleDateFormat("yyyy-MM-dd").format(dailyExpr.getNextValidTimeAfter(callTime)));

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
