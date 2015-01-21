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
package org.datacleaner.monitor.server.listeners;

import java.util.List;

import org.datacleaner.monitor.events.JobModificationEvent;
import org.datacleaner.monitor.scheduling.SchedulingService;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Listener that refreshes the internal job scheduler of dependent jobs, when a
 * job is renamed.
 */
@Component
public class JobModificationEventUpdateSchedulesListener implements ApplicationListener<JobModificationEvent> {

    private static final Logger logger = LoggerFactory.getLogger(JobModificationEventUpdateSchedulesListener.class);

    private final SchedulingService _schedulingService;

    @Autowired
    public JobModificationEventUpdateSchedulesListener(SchedulingService schedulingService) {
        _schedulingService = schedulingService;
    }

    @Override
    public void onApplicationEvent(JobModificationEvent event) {
        final String oldJobName = event.getOldJobName();
        final String newJobName = event.getNewJobName();
        if (oldJobName.equals(newJobName)) {
            return;
        }
        
        final TenantIdentifier tenant = new TenantIdentifier(event.getTenant());
        _schedulingService.removeSchedule(tenant, new JobIdentifier(oldJobName));
        
        final List<ScheduleDefinition> schedules = _schedulingService.getSchedules(tenant);
        for (ScheduleDefinition schedule : schedules) {
            boolean update = false;
            if (schedule.getDependentJob() != null) {
                String dependentJob = schedule.getDependentJob().getName();
                if (oldJobName.equals(dependentJob)) {
                    logger.info("Updating dependent job schedule: {}", schedule);
                    schedule.setDependentJob(new JobIdentifier(newJobName));
                    update = true;
                }
            } else if (newJobName.equals(schedule.getJob().getName())) {
                // also update the new job schedule since the scheduler anyways
                // needs to re-define the schedule
                _schedulingService.updateSchedule(tenant, schedule);
                logger.info("Updating job schedule: {}", schedule);
                update = true;
            }

            if (update) {
                _schedulingService.updateSchedule(tenant, schedule);
            } else {
                logger.info("Not updating schedule: {}", schedule);
            }
        }
    }
}
