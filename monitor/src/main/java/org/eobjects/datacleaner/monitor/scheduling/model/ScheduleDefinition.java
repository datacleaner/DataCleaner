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
package org.eobjects.datacleaner.monitor.scheduling.model;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents information about a scheduled job execution.
 */
public class ScheduleDefinition implements IsSerializable, Comparable<ScheduleDefinition> {

    private TenantIdentifier _tenant;
    private JobIdentifier _job;
    private JobIdentifier _scheduleAfterJob;
    private String _scheduleExpression;
    private boolean _active;
    private List<AlertDefinition> _alerts;

    // no-args constructor
    public ScheduleDefinition() {
    }

    public ScheduleDefinition(TenantIdentifier tenant, JobIdentifier job, String scheduleExpression, boolean active) {
        _tenant = tenant;
        _job = job;
        _scheduleExpression = scheduleExpression;
        _active = active;
    }

    public ScheduleDefinition(TenantIdentifier tenant, JobIdentifier job, JobIdentifier scheduleAfterJob, boolean active) {
        _tenant = tenant;
        _job = job;
        _scheduleAfterJob = scheduleAfterJob;
        _active = active;
    }

    public TenantIdentifier getTenant() {
        return _tenant;
    }

    public JobIdentifier getJob() {
        return _job;
    }

    public String getScheduleExpression() {
        return _scheduleExpression;
    }

    public boolean isActive() {
        return _active;
    }

    public void setActive(boolean active) {
        _active = active;
    }

    public JobIdentifier getScheduleAfterJob() {
        return _scheduleAfterJob;
    }
    
    public List<AlertDefinition> getAlerts() {
        if (_alerts == null) {
            _alerts = new ArrayList<AlertDefinition>();
        }
        return _alerts;
    }
    
    public void setAlerts(List<AlertDefinition> alerts) {
        _alerts = alerts;
    }

    public String getScheduleSummary() {
        if (!isActive()) {
            return "Not scheduled";
        }

        if (_scheduleAfterJob == null) {
            return getScheduleExpression();
        } else {
            return "Run after " + _scheduleAfterJob.getName();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (_active ? 1231 : 1237);
        result = prime * result + ((_alerts == null) ? 0 : _alerts.hashCode());
        result = prime * result + ((_job == null) ? 0 : _job.hashCode());
        result = prime * result + ((_scheduleAfterJob == null) ? 0 : _scheduleAfterJob.hashCode());
        result = prime * result + ((_scheduleExpression == null) ? 0 : _scheduleExpression.hashCode());
        result = prime * result + ((_tenant == null) ? 0 : _tenant.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScheduleDefinition other = (ScheduleDefinition) obj;
        if (_active != other._active)
            return false;
        if (_alerts == null) {
            if (other._alerts != null)
                return false;
        } else if (!_alerts.equals(other._alerts))
            return false;
        if (_job == null) {
            if (other._job != null)
                return false;
        } else if (!_job.equals(other._job))
            return false;
        if (_scheduleAfterJob == null) {
            if (other._scheduleAfterJob != null)
                return false;
        } else if (!_scheduleAfterJob.equals(other._scheduleAfterJob))
            return false;
        if (_scheduleExpression == null) {
            if (other._scheduleExpression != null)
                return false;
        } else if (!_scheduleExpression.equals(other._scheduleExpression))
            return false;
        if (_tenant == null) {
            if (other._tenant != null)
                return false;
        } else if (!_tenant.equals(other._tenant))
            return false;
        return true;
    }

    @Override
    public int compareTo(ScheduleDefinition o) {
        int diff = _job.compareTo(o.getJob());
        if (diff == 0) {
            diff = hashCode() - o.hashCode();
        }
        return diff;
    }
}
