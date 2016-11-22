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
package org.datacleaner.monitor.scheduling.model;

import java.io.Serializable;
import java.util.Date;

import org.datacleaner.monitor.shared.model.JobIdentifier;

/**
 * Represents a running (or finished) execution of a job.
 */
public class ExecutionLog extends ExecutionIdentifier implements Serializable {

    private static final long serialVersionUID = 1L;

    // metadata about the job and the trigger
    private ScheduleDefinition _schedule;
    private JobIdentifier _job;

    // execution level output
    private String _logOutput;
    private Date _jobEndDate;

    private String _triggeredBy;
    private boolean _resultPersisted;

    // no-args constructor
    public ExecutionLog() {
        super();
        _resultPersisted = false;
    }

    public ExecutionLog(final ScheduleDefinition schedule, final TriggerType triggerType) {
        super(createResultId(schedule), triggerType);
        _schedule = schedule;
        _job = (schedule == null ? null : schedule.getJob());
        _resultPersisted = false;
    }

    private static String createResultId(final ScheduleDefinition schedule) {
        if (schedule == null) {
            return null;
        }
        final JobIdentifier job = schedule.getJob();
        return createResultId(job);
    }

    private static String createResultId(final JobIdentifier job) {
        if (job == null) {
            return null;
        }
        return job.getName() + "-" + new Date().getTime();
    }

    public ScheduleDefinition getSchedule() {
        return _schedule;
    }

    public void setSchedule(final ScheduleDefinition schedule) {
        _schedule = schedule;
    }

    public String getLogOutput() {
        return _logOutput;
    }

    public void setLogOutput(final String logOutput) {
        _logOutput = logOutput;
    }

    public Date getJobEndDate() {
        return _jobEndDate;
    }

    public void setJobEndDate(final Date jobEndDate) {
        _jobEndDate = jobEndDate;
    }

    public JobIdentifier getJob() {
        return _job;
    }

    public void setJob(final JobIdentifier job) {
        _job = job;
    }

    public String getTriggeredBy() {
        return _triggeredBy;
    }

    public void setTriggeredBy(final String triggeredBy) {
        _triggeredBy = triggeredBy;
    }

    public boolean isResultPersisted() {
        return _resultPersisted;
    }

    public void setResultPersisted(final boolean resultPersisted) {
        _resultPersisted = resultPersisted;
    }

    @Override
    public String toString() {
        return "ExecutionLog[job=" + _job + ", " + super.toString() + "]";
    }
}
