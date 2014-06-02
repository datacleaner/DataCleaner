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
package org.eobjects.datacleaner.monitor.scheduling.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    private String _groupName;
    private VariableProviderDefinition _variableProvider;
    private boolean _distributedExecution;
    private String _dateForOneTimeSchedule;

    // no-args constructor
    public ScheduleDefinition() {
    }

    public ScheduleDefinition(TenantIdentifier tenant, JobIdentifier job, String groupName) {
        _tenant = tenant;
        _job = job;
        _groupName = groupName;
    }

    public String getDateForOneTimeSchedule() {
		return _dateForOneTimeSchedule;
	}

	public void setDateForOneTimeSchedule(String _dateForOneTimeSchedule) {
		this._dateForOneTimeSchedule = _dateForOneTimeSchedule;
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

    public void setGroupName(String groupName) {
        _groupName = groupName;
    }

    public String getGroupName() {
        return _groupName;
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

    public boolean isDistributedExecution() {
        return _distributedExecution;
    }

    public void setDistributedExecution(boolean distributedExecution) {
        _distributedExecution = distributedExecution;
    }

    public TriggerType getTriggerType() {
        if (_dependentJob != null) {
            return TriggerType.DEPENDENT;
        } else if (_cronExpression != null) {
            return TriggerType.PERIODIC;
        } else if(_dateForOneTimeSchedule!= null){
        	return TriggerType.ONETIME;
        }
        else {
            return TriggerType.MANUAL;
        }
    }

    public VariableProviderDefinition getVariableProvider() {
        return _variableProvider;
    }

    public void setVariableProvider(VariableProviderDefinition variableProvider) {
        _variableProvider = variableProvider;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_alerts == null) ? 0 : _alerts.hashCode());
        result = prime * result + ((_cronExpression == null) ? 0 : _cronExpression.hashCode());
        result = prime * result + ((_dateForOneTimeSchedule == null) ? 0 : _dateForOneTimeSchedule.hashCode());
        result = prime * result + ((_groupName == null) ? 0 : _groupName.hashCode());
        result = prime * result + ((_dependentJob == null) ? 0 : _dependentJob.hashCode());
        result = prime * result + ((_job == null) ? 0 : _job.hashCode());
        result = prime * result + ((_tenant == null) ? 0 : _tenant.hashCode());
        result = prime * result + ((_variableProvider == null) ? 0 : _variableProvider.hashCode());
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
        if (_cronExpression == null) {
            if (other._cronExpression != null)
                return false;
        } else if (!_cronExpression.equals(other._cronExpression))
            return false;
        if (_dateForOneTimeSchedule == null) {
            if (other._dateForOneTimeSchedule != null)
                return false;
        } else if (!_dateForOneTimeSchedule.equals(other._dateForOneTimeSchedule))
            return false;
        if (_groupName == null) {
            if (other._groupName != null)
                return false;
        } else if (!_groupName.equals(other._groupName))
            return false;
        if (_dependentJob == null) {
            if (other._dependentJob != null)
                return false;
        } else if (!_dependentJob.equals(other._dependentJob))
            return false;
        if (_job == null) {
            if (other._job != null)
                return false;
        } else if (!_job.equals(other._job))
            return false;
        if (_tenant == null) {
            if (other._tenant != null)
                return false;
        } else if (!_tenant.equals(other._tenant))
            return false;
        if (_variableProvider == null) {
            if (other._variableProvider != null)
                return false;
        } else if (!_variableProvider.equals(other._variableProvider))
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

	@Override
	public String toString() {
		return "ScheduleDefinition[_tenant=" + _tenant + ", _job=" + _job + ", _dependentJob=" + _dependentJob + ", _cronExpression=" + _cronExpression + ", _alerts=" + _alerts + ", _groupName="+ _groupName + ", _variableProvider=" + _variableProvider+ ", _distributedExecution=" + _distributedExecution+ ", _dateForOneTimeSchedule="+ _dateForOneTimeSchedule + "]";
	}

   
}
