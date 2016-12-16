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
package org.datacleaner.monitor.scheduling;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.datacleaner.monitor.scheduling.model.ExecutionIdentifier;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.shared.model.DCSecurityException;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Service interface for the scheduling module
 */
@RemoteServiceRelativePath("../gwtrpc/schedulingService")
public interface SchedulingService extends RemoteService {

    @RolesAllowed({ SecurityRoles.VIEWER, SecurityRoles.SCHEDULE_EDITOR })
    List<ScheduleDefinition> getSchedules(TenantIdentifier tenant, boolean loadProperties) throws DCSecurityException;

    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    ScheduleDefinition updateSchedule(TenantIdentifier tenant, ScheduleDefinition scheduleDefinition)
            throws DCSecurityException, DCUserInputException;

    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    void removeSchedule(TenantIdentifier tenant, JobIdentifier job) throws DCSecurityException;

    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    ExecutionLog triggerExecution(TenantIdentifier tenant, JobIdentifier job) throws DCSecurityException;

    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    ExecutionLog triggerExecution(TenantIdentifier tenant, JobIdentifier job, Map<String, String> overrideProperties)
            throws DCSecurityException;

    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    boolean cancelExecution(TenantIdentifier tenant, ExecutionLog execution) throws DCSecurityException;

    @RolesAllowed({ SecurityRoles.VIEWER, SecurityRoles.SCHEDULE_EDITOR })
    ExecutionLog getLatestExecution(TenantIdentifier tenant, JobIdentifier job) throws DCSecurityException;

    @RolesAllowed({ SecurityRoles.VIEWER, SecurityRoles.SCHEDULE_EDITOR })
    ScheduleDefinition getSchedule(TenantIdentifier tenant, JobIdentifier jobIdentifier) throws DCSecurityException;

    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    String getServerDate();

    /**
     * Gets the full details about an {@link ExecutionIdentifier}.
     *
     * @param tenant
     * @param executionLog
     * @return
     * @throws DCSecurityException
     */
    @RolesAllowed({ SecurityRoles.VIEWER, SecurityRoles.SCHEDULE_EDITOR })
    ExecutionLog getExecution(TenantIdentifier tenant, ExecutionIdentifier executionIdentifier)
            throws DCSecurityException;

    /**
     * Gets all executions of a particular job.
     * @throws FileNotFoundException
     */
    @RolesAllowed({ SecurityRoles.VIEWER, SecurityRoles.SCHEDULE_EDITOR })
    List<ExecutionIdentifier> getAllExecutions(TenantIdentifier tenant, JobIdentifier job)
            throws DCSecurityException, IllegalStateException;

    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    List<JobIdentifier> getDependentJobCandidates(TenantIdentifier tenant, ScheduleDefinition schedule)
            throws DCSecurityException;

}
