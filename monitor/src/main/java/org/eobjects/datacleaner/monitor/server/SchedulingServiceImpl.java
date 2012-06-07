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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eobjects.datacleaner.monitor.scheduling.ExecuteJob;
import org.eobjects.datacleaner.monitor.scheduling.SchedulingService;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.timeline.TimelineService;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.schedule.jaxb.Schedule;
import org.eobjects.metamodel.util.FileHelper;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;

public class SchedulingServiceImpl implements SchedulingService {

    private static final String EXTENSION_SCHEDULE_XML = ".schedule.xml";

    private final TimelineService _timelineService;
    private final Repository _repository;
    private final SchedulerFactory _schedulerFactory;

    public SchedulingServiceImpl(TimelineService timelineService, Repository repository,
            SchedulerFactory schedulerFactory) {
        _timelineService = timelineService;
        _repository = repository;
        _schedulerFactory = schedulerFactory;
    }

    @Override
    public List<ScheduleDefinition> getSchedules(TenantIdentifier tenant) {
        final List<JobIdentifier> jobs = _timelineService.getJobs(tenant);
        final List<ScheduleDefinition> schedules = new ArrayList<ScheduleDefinition>(jobs.size());
        for (JobIdentifier jobIdentifier : jobs) {
            schedules.add(getSchedule(tenant, jobIdentifier));
        }
        return schedules;
    }

    private ScheduleDefinition getSchedule(TenantIdentifier tenant, JobIdentifier jobIdentifier) {
        final RepositoryFolder tenantFolder = _repository.getFolder(tenant.getId());
        final RepositoryFolder jobsFolder = tenantFolder.getFolder(TimelineServiceImpl.PATH_JOBS);
        final RepositoryFile scheduleFile = jobsFolder.getFile(jobIdentifier.getName() + EXTENSION_SCHEDULE_XML);

        final String scheduleExpression;
        final boolean active;

        if (scheduleFile == null) {
            scheduleExpression = null;
            active = false;
        } else {
            JaxbScheduleReader reader = new JaxbScheduleReader();
            InputStream inputStream = scheduleFile.readFile();
            final Schedule schedule;
            try {
                schedule = reader.unmarshallSchedule(inputStream);
            } finally {
                FileHelper.safeClose(inputStream);
            }

            scheduleExpression = schedule.getScheduleExpression();
            active = schedule.isActive();
        }

        return new ScheduleDefinition(tenant, jobIdentifier, scheduleExpression, active);
    }

    @Override
    public ScheduleDefinition updateSchedule(TenantIdentifier tenant, ScheduleDefinition scheduleDefinition) {

        final String quartzJobName = scheduleDefinition.getJob().getName();
        final String quartzJobGroupName = tenant.getId();

        try {
            final Scheduler scheduler = _schedulerFactory.getScheduler();
            scheduler.deleteJob(quartzJobName, quartzJobGroupName);

            if (scheduleDefinition.isActive()) {
                final JobDetail jobDetail = new JobDetail(quartzJobName, quartzJobGroupName, ExecuteJob.class);
                final CronTrigger trigger = new CronTrigger();
                trigger.setCronExpression(scheduleDefinition.getScheduleExpression());

                scheduler.scheduleJob(jobDetail, trigger);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not configure job scheduling", e);
        }
        
        // TODO: Write to schedule file

        return scheduleDefinition;
    }

}
