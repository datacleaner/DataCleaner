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
package org.eobjects.datacleaner.monitor.scheduling;

import java.util.List;

import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async variant of {@link SchedulingService}.
 */
public interface SchedulingServiceAsync {

    void getSchedules(TenantIdentifier tenant, AsyncCallback<List<ScheduleDefinition>> callback);

    void updateSchedule(TenantIdentifier tenant, ScheduleDefinition scheduleDefinition, AsyncCallback<ScheduleDefinition> callback);

    void getLatestExecution(TenantIdentifier tenant, JobIdentifier job, AsyncCallback<ExecutionLog> callback);

    void getAllExecutions(TenantIdentifier tenant, JobIdentifier job, AsyncCallback<List<ExecutionLog>> callback);

    void triggerExecution(TenantIdentifier tenant, JobIdentifier job, AsyncCallback<ExecutionLog> callback);

    void getDependentJobCandidates(TenantIdentifier tenant, ScheduleDefinition schedule,
            AsyncCallback<List<JobIdentifier>> callback);

}
