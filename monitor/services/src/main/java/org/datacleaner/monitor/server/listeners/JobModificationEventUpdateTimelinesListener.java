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
import org.datacleaner.monitor.events.JobModificationEvent;
import org.datacleaner.monitor.server.dao.TimelineDao;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Listener that handles updates of result timelines, when a job has been
 * renamed.
 */
@Component
public class JobModificationEventUpdateTimelinesListener implements ApplicationListener<JobModificationEvent> {

    private final TimelineDao _timelineDao;

    @Autowired
    public JobModificationEventUpdateTimelinesListener(TimelineDao timelineDao) {
        _timelineDao = timelineDao;
    }

    @Override
    public void onApplicationEvent(JobModificationEvent event) {
        final String oldJobName = event.getOldJobName();
        final String newJobName = event.getNewJobName();
        if (oldJobName.equals(newJobName)) {
            return;
        }

        JobIdentifier oldJobIdentifier = new JobIdentifier(oldJobName);
        JobIdentifier newJobIdentifier = new JobIdentifier(newJobName);

        final Map<TimelineIdentifier, TimelineDefinition> timelines = _timelineDao.getTimelinesForJob(
                new TenantIdentifier(event.getTenant()), oldJobIdentifier);

        for (Map.Entry<TimelineIdentifier, TimelineDefinition> entry : timelines.entrySet()) {
            final TimelineIdentifier identifier = entry.getKey();
            final TimelineDefinition definition = entry.getValue();
            definition.setJobIdentifier(newJobIdentifier);

            _timelineDao.updateTimeline(identifier, definition);
        }
    }

}
