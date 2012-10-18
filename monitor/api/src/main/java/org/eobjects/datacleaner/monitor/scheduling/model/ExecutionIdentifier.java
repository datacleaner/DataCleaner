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
import java.util.Date;

/**
 * Represents the basic information about an execution. Can be used as a key for
 * retrieving more information in an {@link ExecutionLog} object.
 */
public class ExecutionIdentifier implements Serializable, Comparable<ExecutionIdentifier> {

    private static final long serialVersionUID = 1L;

    private String _resultId;
    private TriggerType _triggerType;
    private Date _jobBeginDate;
    private ExecutionStatus _executionStatus;

    public ExecutionIdentifier() {
        this(null, null);
    }

    protected ExecutionIdentifier(String resultId, TriggerType triggerType) {
        _resultId = resultId;
        _triggerType = triggerType;
        _executionStatus = ExecutionStatus.PENDING;
    }

    public boolean isFinished() {
        boolean runningOrUnkown = _executionStatus == null || _executionStatus == ExecutionStatus.RUNNING
                || _executionStatus == ExecutionStatus.PENDING || _executionStatus == ExecutionStatus.UNKNOWN;
        return !runningOrUnkown;
    }

    public String getResultId() {
        return _resultId;
    }

    public void setResultId(String resultId) {
        _resultId = resultId;
    }

    /**
     * Gets the trigger type of this execution. Note that this trigger type
     * MIGHT NOT be the same as the TriggerType in the Schedule of the job (
     * {@link ExecutionLog#getSchedule()}), since the execution might be
     * manually triggered while also having a defined schedule.
     * 
     * @return
     */
    public TriggerType getTriggerType() {
        return _triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        _triggerType = triggerType;
    }

    public Date getJobBeginDate() {
        return _jobBeginDate;
    }

    public void setJobBeginDate(Date jobBeginDate) {
        _jobBeginDate = jobBeginDate;
    }

    public ExecutionStatus getExecutionStatus() {
        return _executionStatus;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        _executionStatus = executionStatus;
    }

    @Override
    public String toString() {
        return "ExecutionIdentifier[begin=" + _jobBeginDate + ", status=" + _executionStatus + "]";
    }

    @Override
    public int compareTo(ExecutionIdentifier o) {
        if (o == null) {
            return 1;
        }
        if (o == this) {
            return 0;
        }

        if (_jobBeginDate != null) {
            int diff = _jobBeginDate.compareTo(o.getJobBeginDate());
            if (diff != 0) {
                return diff;
            }
        }

        if (_resultId != null) {
            int diff = _resultId.compareTo(o.getResultId());
            if (diff != 0) {
                return diff;
            }
        }

        if (_executionStatus != null) {
            int diff = _executionStatus.compareTo(o.getExecutionStatus());
            if (diff != 0) {
                return diff;
            }
        }

        if (_triggerType != null) {
            int diff = _triggerType.compareTo(o.getTriggerType());
            if (diff != 0) {
                return diff;
            }
        }

        return 0;
    }
}
