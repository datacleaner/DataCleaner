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

import org.eobjects.datacleaner.monitor.timeline.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.JobMetrics;
import org.eobjects.datacleaner.monitor.timeline.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineData;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineIdentifier;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Service interface for the timeline web app
 */
@RemoteServiceRelativePath("timelineService")
public interface TimelineService extends RemoteService {

    /**
     * Gets the saved jobs of a particular tenant
     * 
     * @param tenant
     * @return
     */
    public List<JobIdentifier> getSavedJobs(TenantIdentifier tenant);

    /**
     * Gets all available metrics for a job
     * 
     * @param tenant
     * @param job
     * @return
     */
    public JobMetrics getJobMetrics(TenantIdentifier tenant, JobIdentifier job);

    /**
     * Gets the saved timeline identifiers of a tenant
     * 
     * @param tenant
     * @return
     */
    public List<TimelineIdentifier> getSavedTimelines(TenantIdentifier tenant);

    /**
     * Gets the definition (incl. metric references) of a timeline
     * 
     * @param tenant
     * @param timeline
     * @return
     */
    public TimelineDefinition getTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline);

    /**
     * Updates a timeline definition
     * 
     * @param tenant
     * @param timeline
     * @param timelineDefinition
     * 
     * @return the persisted timeline identifier
     */
    public TimelineIdentifier updateTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline,
            TimelineDefinition timelineDefinition);

    /**
     * Creates a new timeline definition
     * 
     * @param tenant
     * @param timeline
     * @param timelineDefinition
     * 
     * @return the persisted timeline identifier
     */
    public TimelineIdentifier createTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline,
            TimelineDefinition timelineDefinition);

    /**
     * Materializes the data needed to draw a particular timeline
     * 
     * @param tenant
     * @param timeline
     * @return
     */
    public TimelineData getTimelineData(TenantIdentifier tenant, TimelineDefinition timeline);

    /**
     * Gets suggestions for parameter values of a particular metric
     * 
     * @param tenant
     * @param metric
     * @return
     */
    public Collection<String> getMetricParameterSuggestions(TenantIdentifier tenant, JobIdentifier jobIdentifier,
            MetricIdentifier metric);

    /**
     * Deletes a timeline from the repository
     * 
     * @param tenant
     * @param timeline
     * @return
     */
    public Boolean deleteTimeline(TenantIdentifier tenant, TimelineIdentifier timeline);
}
