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

import java.util.Collection;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.eobjects.datacleaner.monitor.shared.model.DCSecurityException;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.dashboard.model.JobMetrics;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineData;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineGroup;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineIdentifier;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Service interface for the timeline module
 */
@RemoteServiceRelativePath("dashboardService")
public interface DashboardService extends RemoteService {
    
    /**
     * Determines whether the current user is authorized to edit the dashboard.
     * @param tenant
     * @return
     */
    public boolean isDashboardEditor(TenantIdentifier tenant);

    /**
     * Gets the saved jobs of a particular tenant
     * 
     * @param tenant
     * @return
     */
    @RolesAllowed(SecurityRoles.VIEWER)
    public List<JobIdentifier> getJobs(TenantIdentifier tenant) throws DCSecurityException;

    /**
     * Gets all available metrics for a job
     * 
     * @param tenant
     * @param job
     * @return
     */
    @RolesAllowed(SecurityRoles.DASHBOARD_EDITOR)
    public JobMetrics getJobMetrics(TenantIdentifier tenant, JobIdentifier job) throws DCSecurityException;

    /**
     * Gets the available timeline groups
     * 
     * @param tenant
     * 
     * @return
     */
    @RolesAllowed(SecurityRoles.VIEWER)
    public List<TimelineGroup> getTimelineGroups(TenantIdentifier tenant) throws DCSecurityException;

    /**
     * Adds a timeline group to the tenant's repository.
     * 
     * @param tenant
     * @param name
     * @return
     */
    @RolesAllowed(SecurityRoles.DASHBOARD_EDITOR)
    public TimelineGroup addTimelineGroup(TenantIdentifier tenant, String name) throws DCSecurityException;

    /**
     * Removes a timeline group from a tenant's repository. All contained
     * timelines must be removed first, or else the operation will not succeed.
     * 
     * @param tenant
     * @param timelineGroup
     * @return a boolean indicating if the remove operation went well.
     */
    @RolesAllowed(SecurityRoles.DASHBOARD_EDITOR)
    public Boolean removeTimelineGroup(TenantIdentifier tenant, TimelineGroup timelineGroup) throws DCSecurityException;

    /**
     * Gets the saved timeline identifiers of a tenant
     * 
     * @param tenant
     * @param group
     *            optionally a timeline group to narrow the search
     * @return
     */
    @RolesAllowed(SecurityRoles.VIEWER)
    public List<TimelineIdentifier> getTimelines(TenantIdentifier tenant, TimelineGroup group)
            throws DCSecurityException;

    /**
     * Gets the definition (incl. metric references) of a timeline
     * 
     * @param tenant
     * @param timeline
     * @return
     */
    @RolesAllowed(SecurityRoles.VIEWER)
    public TimelineDefinition getTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline)
            throws DCSecurityException;

    /**
     * Updates a timeline definition
     * 
     * @param tenant
     * @param timeline
     * @param timelineDefinition
     * 
     * @return the persisted timeline identifier
     */
    @RolesAllowed(SecurityRoles.DASHBOARD_EDITOR)
    public TimelineIdentifier updateTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline,
            TimelineDefinition timelineDefinition) throws DCSecurityException;

    /**
     * Creates a new timeline definition
     * 
     * @param tenant
     * @param timeline
     * @param timelineDefinition
     * 
     * @return the persisted timeline identifier
     */
    @RolesAllowed(SecurityRoles.DASHBOARD_EDITOR)
    public TimelineIdentifier createTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline,
            TimelineDefinition timelineDefinition) throws DCSecurityException;

    /**
     * Materializes the data needed to draw a particular timeline
     * 
     * @param tenant
     * @param timeline
     * @return
     */
    @RolesAllowed(SecurityRoles.VIEWER)
    public TimelineData getTimelineData(TenantIdentifier tenant, TimelineDefinition timeline)
            throws DCSecurityException;

    /**
     * Gets suggestions for parameter values of a particular metric
     * 
     * @param tenant
     * @param metric
     * @return
     */
    @RolesAllowed(SecurityRoles.DASHBOARD_EDITOR)
    public Collection<String> getMetricParameterSuggestions(TenantIdentifier tenant, JobIdentifier jobIdentifier,
            MetricIdentifier metric) throws DCSecurityException;

    /**
     * Deletes a timeline from the repository
     * 
     * @param tenant
     * @param timeline
     * @return a boolean indicating if the remove operation went well.
     */
    @RolesAllowed(SecurityRoles.DASHBOARD_EDITOR)
    public Boolean removeTimeline(TenantIdentifier tenant, TimelineIdentifier timeline) throws DCSecurityException;
}
