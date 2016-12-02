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

import javax.servlet.ServletException;

import org.datacleaner.monitor.dashboard.DashboardService;
import org.datacleaner.monitor.dashboard.model.DashboardGroup;
import org.datacleaner.monitor.dashboard.model.TimelineData;
import org.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.datacleaner.monitor.dashboard.model.TimelineIdentifier;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * Servlet wrapper/proxy for the {@link DashboardService}. Passes all service
 * requests on to a delegate, see {@link #setDelegate(DashboardService)} and
 * {@link #getDelegate()}.
 */
public class DashboardServiceServlet extends SecureGwtServlet implements DashboardService {

    private static final long serialVersionUID = 1L;

    private DashboardService _delegate;

    @Override
    public void init() throws ServletException {
        super.init();

        if (_delegate == null) {
            final WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
            final DashboardService delegate = applicationContext.getBean(DashboardService.class);
            if (delegate == null) {
                throw new ServletException("No delegate found in application context!");
            }
            _delegate = delegate;
        }
    }

    public DashboardService getDelegate() {
        return _delegate;
    }

    public void setDelegate(final DashboardService delegate) {
        _delegate = delegate;
    }

    @Override
    public List<JobIdentifier> getJobs(final TenantIdentifier tenant) {
        return _delegate.getJobs(tenant);
    }

    @Override
    public List<TimelineIdentifier> getTimelines(final TenantIdentifier tenant, final DashboardGroup group) {
        return _delegate.getTimelines(tenant, group);
    }

    @Override
    public TimelineDefinition getTimelineDefinition(final TenantIdentifier tenant, final TimelineIdentifier timeline) {
        return _delegate.getTimelineDefinition(tenant, timeline);
    }

    @Override
    public TimelineData getTimelineData(final TenantIdentifier tenant, final TimelineDefinition timeline) {
        return _delegate.getTimelineData(tenant, timeline);
    }

    @Override
    public TimelineIdentifier updateTimelineDefinition(final TenantIdentifier tenant, final TimelineIdentifier timeline,
            final TimelineDefinition timelineDefinition) {
        return _delegate.updateTimelineDefinition(tenant, timeline, timelineDefinition);
    }

    @Override
    public TimelineIdentifier createTimelineDefinition(final TenantIdentifier tenant, final TimelineIdentifier timeline,
            final TimelineDefinition timelineDefinition) {
        return _delegate.createTimelineDefinition(tenant, timeline, timelineDefinition);
    }

    @Override
    public Boolean removeTimeline(final TenantIdentifier tenant, final TimelineIdentifier timeline) {
        return _delegate.removeTimeline(tenant, timeline);
    }

    @Override
    public List<DashboardGroup> getDashboardGroups(final TenantIdentifier tenant) {
        return _delegate.getDashboardGroups(tenant);
    }

    @Override
    public DashboardGroup addDashboardGroup(final TenantIdentifier tenant, final String name) {
        return _delegate.addDashboardGroup(tenant, name);
    }

    @Override
    public Boolean removeDashboardGroup(final TenantIdentifier tenant, final DashboardGroup timelineGroup) {
        return _delegate.removeDashboardGroup(tenant, timelineGroup);
    }
}
