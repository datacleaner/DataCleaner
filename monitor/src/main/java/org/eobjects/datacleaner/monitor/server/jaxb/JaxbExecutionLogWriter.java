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
package org.eobjects.datacleaner.monitor.server.jaxb;

import java.io.OutputStream;

import org.eobjects.datacleaner.monitor.jaxb.ExecutionType;
import org.eobjects.datacleaner.monitor.jaxb.Schedule;
import org.eobjects.datacleaner.monitor.jaxb.TriggerType;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionStatus;

/**
 * Component responsible for writing a {@link ExecutionLog} to an XML
 * outputstream.
 */
public class JaxbExecutionLogWriter extends JaxbWriter<org.eobjects.datacleaner.monitor.jaxb.ExecutionLog> {

    public void write(ExecutionLog executionLog, OutputStream out) {
        final org.eobjects.datacleaner.monitor.jaxb.ExecutionLog jaxbObj = createExecutionLog(executionLog);

        marshal(jaxbObj, out);
    }

    private org.eobjects.datacleaner.monitor.jaxb.ExecutionLog createExecutionLog(ExecutionLog executionLog) {
        final org.eobjects.datacleaner.monitor.jaxb.ExecutionLog result = getObjectFactory().createExecutionLog();

        result.setResultId(executionLog.getResultId());
        result.setExecutionStatus(createExecutionStatus(executionLog.getExecutionStatus()));
        result.setJobBeginDate(createDate(executionLog.getJobBeginDate()));
        result.setJobEndDate(createDate(executionLog.getJobEndDate()));
        result.setTriggerType(createTriggerType(executionLog.getTriggerType()));
        result.setLogOutput(executionLog.getLogOutput());
        
        final JaxbScheduleWriter scheduleWriter = new JaxbScheduleWriter();
        final Schedule schedule = scheduleWriter.createSchedule(executionLog.getSchedule());
        
        result.setSchedule(schedule);
        return result;
    }

    private TriggerType createTriggerType(org.eobjects.datacleaner.monitor.scheduling.model.TriggerType triggerType) {
        switch (triggerType) {
        case PERIODIC:
            return TriggerType.PERIODIC;
        case DEPENDENT:
            return TriggerType.DEPENDENT;
        case MANUAL:
            return TriggerType.MANUAL;
        default:
            throw new UnsupportedOperationException("Unknown trigger type: " + triggerType);
        }
    }

    private ExecutionType createExecutionStatus(ExecutionStatus executionStatus) {
        switch (executionStatus) {
        case PENDING:
            return ExecutionType.PENDING;
        case RUNNING:
            return ExecutionType.RUNNING;
        case SUCCESS:
            return ExecutionType.SUCCESS;
        case FAILURE:
            return ExecutionType.FAILURE;
        default:
            throw new UnsupportedOperationException("Unknown execution status: " + executionStatus);
        }
    }
}
