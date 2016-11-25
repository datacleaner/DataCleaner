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

import javax.annotation.security.RolesAllowed;

import org.datacleaner.monitor.dashboard.model.DashboardGroup;
import org.datacleaner.monitor.dashboard.model.TimelineData;
import org.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.datacleaner.monitor.dashboard.model.TimelineIdentifier;
import org.datacleaner.monitor.shared.model.DCSecurityException;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Service interface for the timeline module
 */
@RemoteServiceRelativePath("../gwtrpc/dashboardService")
public interface DashboardService extends RemoteService {

    /**
     * Gets the saved jobs of a particular tenant
     *
     * @param tenant
     * @return
     */
    @RolesAllowed(SecurityRoles.VIEWER)
    List<JobIdentifier> getJobs(TenantIdentifier tenant) throws DCSecurityException;

    /**
     * Gets the available dashboard groups
     *
     * @param tenant
     *
     * @return
     */
    @RolesAllowed(SecurityRoles.VIEWER)
    List<DashboardGroup> getDashboardGroups(TenantIdentifier tenant) throws DCSecurityException;

    /**
     * Adds a dashboard group to the tenant's repository.
     *
     * @param tenant
     * @param name
     * @return
     */
    @RolesAllowed(SecurityRoles.DASHBOARD_EDITOR)
    DashboardGroup addDashboardGroup(TenantIdentifier tenant, String name) throws DCSecurityException;

    /**
     * Removes a timeline group from a tenant's repository. All contained
     * timelines must be removed first, or else the operation will not succeed.
     *
     * @param tenant
     * @param dashboardGroup
     * @return a boolean indicating if the remove operation went well.
     */
    @RolesAllowed(SecurityRoles.DASHBOARD_EDITOR)
    Boolean removeDashboardGroup(TenantIdentifier tenant, DashboardGroup dashboardGroup) throws DCSecurityException;

    /**
     * Gets the saved timeline identifiers of a tenant
     *
     * @param tenant
     * @param group
     *            optionally a timeline group to narrow the search
     * @return
     */
    @RolesAllowed(SecurityRoles.VIEWER)
    List<TimelineIdentifier> getTimelines(TenantIdentifier tenant, DashboardGroup group) throws DCSecurityException;

    /**
     * Gets the definition (incl. metric references) of a timeline
     *
     * @param tenant
     * @param timeline
     * @return
     */
    @RolesAllowed(SecurityRoles.VIEWER)
    TimelineDefinition getTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline)
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
    TimelineIdentifier updateTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline,
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
    TimelineIdentifier createTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline,
            TimelineDefinition timelineDefinition) throws DCSecurityException;

    /**
     * Materializes the data needed to draw a particular timeline
     *
     * @param tenant
     * @param timeline
     * @return
     */
    @RolesAllowed(SecurityRoles.VIEWER)
    TimelineData getTimelineData(TenantIdentifier tenant, TimelineDefinition timeline) throws DCSecurityException;

    /**
     * Deletes a timeline from the repository
     *
     * @param tenant
     * @param timeline
     * @return a boolean indicating if the remove operation went well.
     */
    @RolesAllowed(SecurityRoles.DASHBOARD_EDITOR)
    Boolean removeTimeline(TenantIdentifier tenant, TimelineIdentifier timeline) throws DCSecurityException;
}
