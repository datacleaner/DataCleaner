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

import java.util.Date;

import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents a running (or finished) execution of a job.
 */
public class ExecutionLog implements IsSerializable {

    // metadata about the job and the trigger
    private ScheduleDefinition _schedule;
    private JobIdentifier _job;
    private TriggerType _triggerType;

    // execution level output
    private String _logOutput;
    private Date _jobBeginDate;
    private Date _jobEndDate;
    private ExecutionStatus _executionStatus;

    // no-args constructor
    public ExecutionLog() {
    }

    public ExecutionLog(ScheduleDefinition schedule, TriggerType triggerType) {
        _schedule = schedule;
        _job = schedule.getJob();
        _triggerType = triggerType;
        _executionStatus = ExecutionStatus.PENDING;
    }

    public ScheduleDefinition getSchedule() {
        return _schedule;
    }

    public String getLogOutput() {
        return _logOutput;
    }

    public void setLogOutput(String logOutput) {
        _logOutput = logOutput;
    }

    public Date getJobBeginDate() {
        return _jobBeginDate;
    }

    public void setJobBeginDate(Date jobBeginDate) {
        _jobBeginDate = jobBeginDate;
    }

    public Date getJobEndDate() {
        return _jobEndDate;
    }

    public void setJobEndDate(Date jobEndDate) {
        _jobEndDate = jobEndDate;
    }

    public JobIdentifier getJob() {
        return _job;
    }

    public TriggerType getTriggerType() {
        return _triggerType;
    }

    public ExecutionStatus getExecutionStatus() {
        return _executionStatus;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        _executionStatus = executionStatus;
    }
}
