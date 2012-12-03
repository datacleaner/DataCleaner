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
package org.eobjects.datacleaner.monitor.scheduling.quartz;

import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.listeners.JobListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A job listener which is used to fire the execution of a job when a dependent
 * job has been executed.
 */
public class ExecuteJobListener extends JobListenerSupport {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteJobListener.class);

    private final String _name;
    private final ScheduleDefinition _schedule;

    public ExecuteJobListener(String name, ScheduleDefinition schedule) {
        _name = name;
        _schedule = schedule;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        final JobKey jobKey = context.getJobDetail().getKey();
        final String jobName = jobKey.getName();
        final String expectedJobName = _schedule.getDependentJob().getName();
        if (jobName.equals(expectedJobName)) {
            logger.debug("Looked for job '{}', found it!", expectedJobName);
            final String tenantId = jobKey.getGroup();
            final String expectedTenantId = _schedule.getTenant().getId();
            if (tenantId.equals(expectedTenantId)) {
                logger.debug("Looked for tenant '{}', found it!", expectedTenantId);
                Scheduler scheduler = context.getScheduler();
                scheduleExecution(scheduler);
            } else {
                logger.debug("Looked for tenant '{}', found tenant '{}'", expectedTenantId, tenantId);
            }
        } else {
            logger.debug("Looked for job '{}', found job '{}'", expectedJobName, jobName);
        }
    }

    private void scheduleExecution(Scheduler scheduler) {
        final String jobName = _schedule.getJob().getName();
        final String tenantId = _schedule.getTenant().getId();
        try {
            scheduler.triggerJob(new JobKey(jobName, tenantId));
        } catch (SchedulerException e) {
            logger.error("Failed to trigger job " + jobName + " for tenant " + tenantId, e);
        }
    }
}
