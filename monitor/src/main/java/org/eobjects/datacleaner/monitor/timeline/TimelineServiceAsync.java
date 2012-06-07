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
package org.eobjects.datacleaner.monitor.timeline;

import java.util.Collection;
import java.util.List;

import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.JobMetrics;
import org.eobjects.datacleaner.monitor.timeline.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineData;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineGroup;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineIdentifier;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async variant of {@link TimelineService}.
 */
public interface TimelineServiceAsync {

    public void getTimelines(TenantIdentifier tenant, TimelineGroup group,
            AsyncCallback<List<TimelineIdentifier>> callback);

    public void getTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier identifier,
            AsyncCallback<TimelineDefinition> callback);

    public void getTimelineData(TenantIdentifier tenant, TimelineDefinition timeline,
            AsyncCallback<TimelineData> callback);

    public void getJobs(TenantIdentifier tenant, AsyncCallback<List<JobIdentifier>> callback);

    public void getJobMetrics(TenantIdentifier tenant, JobIdentifier job, AsyncCallback<JobMetrics> callback);

    public void updateTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline,
            TimelineDefinition timelineDefinition, AsyncCallback<TimelineIdentifier> callback);

    public void createTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline,
            TimelineDefinition timelineDefinition, AsyncCallback<TimelineIdentifier> callback);

    public void getMetricParameterSuggestions(TenantIdentifier tenant, JobIdentifier jobIdentifier,
            MetricIdentifier metric, AsyncCallback<Collection<String>> callback);

    public void deleteTimeline(TenantIdentifier tenant, TimelineIdentifier timeline, AsyncCallback<Boolean> callback);

    public void getTimelineGroups(TenantIdentifier tenant, AsyncCallback<List<TimelineGroup>> callback);

}
