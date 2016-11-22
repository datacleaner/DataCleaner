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
package org.datacleaner.monitor.dashboard;

import java.util.List;

import org.datacleaner.monitor.dashboard.model.DashboardGroup;
import org.datacleaner.monitor.dashboard.model.TimelineData;
import org.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.datacleaner.monitor.dashboard.model.TimelineIdentifier;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async variant of {@link DashboardService}.
 */
public interface DashboardServiceAsync {

    void getTimelines(TenantIdentifier tenant, DashboardGroup group,
            AsyncCallback<List<TimelineIdentifier>> callback);

    void getTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier identifier,
            AsyncCallback<TimelineDefinition> callback);

    void getTimelineData(TenantIdentifier tenant, TimelineDefinition timeline,
            AsyncCallback<TimelineData> callback);

    void getJobs(TenantIdentifier tenant, AsyncCallback<List<JobIdentifier>> callback);

    void updateTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline,
            TimelineDefinition timelineDefinition, AsyncCallback<TimelineIdentifier> callback);

    void createTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline,
            TimelineDefinition timelineDefinition, AsyncCallback<TimelineIdentifier> callback);

    void removeTimeline(TenantIdentifier tenant, TimelineIdentifier timeline, AsyncCallback<Boolean> callback);

    void getDashboardGroups(TenantIdentifier tenant, AsyncCallback<List<DashboardGroup>> callback);

    void addDashboardGroup(TenantIdentifier tenant, String name, AsyncCallback<DashboardGroup> callback);

    void removeDashboardGroup(TenantIdentifier tenant, DashboardGroup timelineGroup,
            AsyncCallback<Boolean> callback);
}
