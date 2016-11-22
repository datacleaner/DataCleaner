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
package org.datacleaner.monitor.server.dao;

import java.util.List;
import java.util.Map;

import org.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.datacleaner.monitor.dashboard.model.TimelineIdentifier;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;

/**
 * DAO (Data Access Object) interface for interactions with timelines in the
 * repository.
 */
public interface TimelineDao {

    Map<TimelineIdentifier, TimelineDefinition> getTimelinesForJob(TenantIdentifier tenant, JobIdentifier job);

    List<TimelineIdentifier> getTimelinesForTenant(TenantIdentifier tenant);

    TimelineIdentifier updateTimeline(TimelineIdentifier identifier, TimelineDefinition definition);

    TimelineDefinition getTimelineDefinition(TimelineIdentifier timeline);

    boolean removeTimeline(TimelineIdentifier timeline);
}
