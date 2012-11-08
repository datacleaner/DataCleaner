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
package org.eobjects.datacleaner.monitor.dashboard;

import java.util.List;

import org.eobjects.datacleaner.monitor.dashboard.model.DashboardGroup;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineData;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async variant of {@link DashboardService}.
 */
public interface DashboardServiceAsync {

    public void getTimelines(TenantIdentifier tenant, DashboardGroup group,
            AsyncCallback<List<TimelineIdentifier>> callback);

    public void getTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier identifier,
            AsyncCallback<TimelineDefinition> callback);

    public void getTimelineData(TenantIdentifier tenant, TimelineDefinition timeline,
            AsyncCallback<TimelineData> callback);

    public void getJobs(TenantIdentifier tenant, AsyncCallback<List<JobIdentifier>> callback);

    public void updateTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline,
            TimelineDefinition timelineDefinition, AsyncCallback<TimelineIdentifier> callback);

    public void createTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline,
            TimelineDefinition timelineDefinition, AsyncCallback<TimelineIdentifier> callback);

    public void removeTimeline(TenantIdentifier tenant, TimelineIdentifier timeline, AsyncCallback<Boolean> callback);

    public void getDashboardGroups(TenantIdentifier tenant, AsyncCallback<List<DashboardGroup>> callback);

    public void addDashboardGroup(TenantIdentifier tenant, String name, AsyncCallback<DashboardGroup> callback);

    public void removeDashboardGroup(TenantIdentifier tenant, DashboardGroup timelineGroup,
            AsyncCallback<Boolean> callback);

    void isDashboardEditor(TenantIdentifier tenant, AsyncCallback<Boolean> callback);

}
