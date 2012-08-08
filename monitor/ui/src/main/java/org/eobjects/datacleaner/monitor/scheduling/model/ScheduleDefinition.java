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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

/**
 * Represents information about a scheduled job execution.
 */
public class ScheduleDefinition implements Comparable<ScheduleDefinition>, Serializable {

    private static final long serialVersionUID = 1L;

    private TenantIdentifier _tenant;
    private JobIdentifier _job;
    private JobIdentifier _dependentJob;
    private String _cronExpression;
    private List<AlertDefinition> _alerts;
    private DatastoreIdentifier _datastore;

    // no-args constructor
    public ScheduleDefinition() {
    }

    public ScheduleDefinition(TenantIdentifier tenant, JobIdentifier job, DatastoreIdentifier datastoreIdentifier) {
        _tenant = tenant;
        _job = job;
        _datastore = datastoreIdentifier;
    }

    public TenantIdentifier getTenant() {
        return _tenant;
    }

    public void setTenant(TenantIdentifier tenant) {
        _tenant = tenant;
    }

    public JobIdentifier getJob() {
        return _job;
    }

    public void setJob(JobIdentifier job) {
        _job = job;
    }

    public void setCronExpression(String cronExpression) {
        _cronExpression = cronExpression;
    }

    public String getCronExpression() {
        return _cronExpression;
    }

    public DatastoreIdentifier getDatastore() {
        return _datastore;
    }

    public void setDatastore(DatastoreIdentifier datastore) {
        _datastore = datastore;
    }

    public void setDependentJob(JobIdentifier dependentJob) {
        _dependentJob = dependentJob;
    }

    public JobIdentifier getDependentJob() {
        return _dependentJob;
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

    public TriggerType getTriggerType() {
        if (_dependentJob != null) {
            return TriggerType.DEPENDENT;
        } else if (_cronExpression != null) {
            return TriggerType.PERIODIC;
        } else {
            return TriggerType.MANUAL;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_alerts == null) ? 0 : _alerts.hashCode());
        result = prime * result + ((_datastore == null) ? 0 : _datastore.hashCode());
        result = prime * result + ((_job == null) ? 0 : _job.hashCode());
        result = prime * result + ((_dependentJob == null) ? 0 : _dependentJob.hashCode());
        result = prime * result + ((_cronExpression == null) ? 0 : _cronExpression.hashCode());
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
        if (_alerts == null) {
            if (other._alerts != null)
                return false;
        } else if (!_alerts.equals(other._alerts))
            return false;
        if (_datastore == null) {
            if (other._datastore != null)
                return false;
        } else if (!_datastore.equals(other._datastore))
            return false;
        if (_job == null) {
            if (other._job != null)
                return false;
        } else if (!_job.equals(other._job))
            return false;
        if (_dependentJob == null) {
            if (other._dependentJob != null)
                return false;
        } else if (!_dependentJob.equals(other._dependentJob))
            return false;
        if (_cronExpression == null) {
            if (other._cronExpression != null)
                return false;
        } else if (!_cronExpression.equals(other._cronExpression))
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
