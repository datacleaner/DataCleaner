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

import java.util.Map;

import org.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.datacleaner.monitor.dashboard.model.TimelineIdentifier;
import org.datacleaner.monitor.events.JobDeletionEvent;
import org.datacleaner.monitor.server.dao.TimelineDao;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Listener that deletes timelines pertaining to a deleted job from the
 * repository.
 */
@Component
public class JobDeletionEventDeleteTimelinesListener implements ApplicationListener<JobDeletionEvent> {

    @Autowired
    TimelineDao timelineDao;

    @Override
    public void onApplicationEvent(JobDeletionEvent event) {
        final TenantIdentifier tenantIdentifier = new TenantIdentifier(event.getTenant());
        final JobIdentifier jobIdentifier = new JobIdentifier(event.getJobName());

        final Map<TimelineIdentifier, TimelineDefinition> timelines = timelineDao.getTimelinesForJob(tenantIdentifier,
                jobIdentifier);
        for (TimelineIdentifier timeline : timelines.keySet()) {
            timelineDao.removeTimeline(timeline);
        }
    }
}
