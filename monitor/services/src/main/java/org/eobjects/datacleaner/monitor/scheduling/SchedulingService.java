/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.scheduling;

import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionIdentifier;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.shared.model.DCSecurityException;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Service interface for the scheduling module
 */
@RemoteServiceRelativePath("../gwtrpc/schedulingService")
public interface SchedulingService extends RemoteService {

    @RolesAllowed({ SecurityRoles.VIEWER, SecurityRoles.SCHEDULE_EDITOR })
    public List<ScheduleDefinition> getSchedules(TenantIdentifier tenant) throws DCSecurityException;

    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    public ScheduleDefinition updateSchedule(TenantIdentifier tenant, ScheduleDefinition scheduleDefinition)
            throws DCSecurityException;

    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    public void removeSchedule(TenantIdentifier tenant, JobIdentifier job) throws DCSecurityException;

    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    public ExecutionLog triggerExecution(TenantIdentifier tenant, JobIdentifier job) throws DCSecurityException;

    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    public boolean cancelExecution(TenantIdentifier tenant, ExecutionLog execution) throws DCSecurityException;

    @RolesAllowed({ SecurityRoles.VIEWER, SecurityRoles.SCHEDULE_EDITOR })
    public ExecutionLog getLatestExecution(TenantIdentifier tenant, JobIdentifier job) throws DCSecurityException;

    @RolesAllowed({ SecurityRoles.VIEWER, SecurityRoles.SCHEDULE_EDITOR })
    public ScheduleDefinition getSchedule(final TenantIdentifier tenant, final JobIdentifier jobIdentifier)
            throws DCSecurityException;
    
    @RolesAllowed({ SecurityRoles.VIEWER, SecurityRoles.SCHEDULE_EDITOR })
    public Date getServerDate();
    /**
     * Gets the full details about an {@link ExecutionIdentifier}.
     * 
     * @param tenant
     * @param executionLog
     * @return
     * @throws DCSecurityException
     */
    @RolesAllowed({ SecurityRoles.VIEWER, SecurityRoles.SCHEDULE_EDITOR })
    public ExecutionLog getExecution(TenantIdentifier tenant, ExecutionIdentifier executionIdentifier)
            throws DCSecurityException;

    /**
     * Gets all executions of a particular job.
     */
    @RolesAllowed({ SecurityRoles.VIEWER, SecurityRoles.SCHEDULE_EDITOR })
    public List<ExecutionIdentifier> getAllExecutions(TenantIdentifier tenant, JobIdentifier job)
            throws DCSecurityException;

    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    public List<JobIdentifier> getDependentJobCandidates(TenantIdentifier tenant, ScheduleDefinition schedule)
            throws DCSecurityException;
	
}
