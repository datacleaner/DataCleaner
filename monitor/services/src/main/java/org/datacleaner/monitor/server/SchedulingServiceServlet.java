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
package org.datacleaner.monitor.server;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.datacleaner.monitor.scheduling.SchedulingService;
import org.datacleaner.monitor.scheduling.model.ExecutionIdentifier;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.shared.model.DCSecurityException;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * Servlet wrapper/proxy for the {@link SchedulingService}. Passes all service
 * requests on to a delegate, see {@link #setDelegate(SchedulingService)} and
 * {@link #getDelegate()}.
 */
public class SchedulingServiceServlet extends SecureGwtServlet implements SchedulingService {

    private static final long serialVersionUID = 1L;

    private SchedulingService _delegate;

    @Override
    public void init() throws ServletException {
        super.init();

        if (_delegate == null) {
            WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
            SchedulingService delegate = applicationContext.getBean(SchedulingService.class);
            if (delegate == null) {
                throw new ServletException("No delegate found in application context!");
            }
            _delegate = delegate;
        }
    }

    public SchedulingService getDelegate() {
        return _delegate;
    }

    public void setDelegate(SchedulingService delegate) {
        _delegate = delegate;
    }

    @Override
    public List<ScheduleDefinition> getSchedules(TenantIdentifier tenant) {
        return _delegate.getSchedules(tenant);
    }

    @Override
    public ScheduleDefinition updateSchedule(TenantIdentifier tenant, ScheduleDefinition scheduleDefinition) {
        return _delegate.updateSchedule(tenant, scheduleDefinition);
    }

    @Override
    public List<ExecutionIdentifier> getAllExecutions(TenantIdentifier tenant, JobIdentifier job) {
        return _delegate.getAllExecutions(tenant, job);
    }

    @Override
    public ExecutionLog getExecution(TenantIdentifier tenant, ExecutionIdentifier executionIdentifier)
            throws DCSecurityException {
        return _delegate.getExecution(tenant, executionIdentifier);
    }

    @Override
    public ExecutionLog getLatestExecution(TenantIdentifier tenant, JobIdentifier job) {
        return _delegate.getLatestExecution(tenant, job);
    }

    @Override
    public ExecutionLog triggerExecution(TenantIdentifier tenant, JobIdentifier job) {
        return _delegate.triggerExecution(tenant, job);
    }

    @Override
    public ExecutionLog triggerExecution(TenantIdentifier tenant, JobIdentifier job,
            Map<String, String> overrideProperties) {
        return _delegate.triggerExecution(tenant, job, overrideProperties);
    }

    @Override
    public List<JobIdentifier> getDependentJobCandidates(TenantIdentifier tenant, ScheduleDefinition schedule)
            throws DCSecurityException {
        return _delegate.getDependentJobCandidates(tenant, schedule);
    }

    @Override
    public void removeSchedule(TenantIdentifier tenant, JobIdentifier job) throws DCSecurityException {
        _delegate.removeSchedule(tenant, job);
    }

    @Override
    public boolean cancelExecution(TenantIdentifier tenant, ExecutionLog executionLog) throws DCSecurityException {
        return _delegate.cancelExecution(tenant, executionLog);
    }
    
    @Override
    public ScheduleDefinition getSchedule(TenantIdentifier tenant, JobIdentifier jobIdentifier)
            throws DCSecurityException {
        return _delegate.getSchedule(tenant, jobIdentifier);
    }

	@Override
	public String getServerDate() {
		return _delegate.getServerDate();
	}
}
