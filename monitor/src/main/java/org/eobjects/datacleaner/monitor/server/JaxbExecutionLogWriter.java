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
package org.eobjects.datacleaner.monitor.server;

import java.io.OutputStream;

import org.eobjects.datacleaner.monitor.jaxb.ExecutionType;
import org.eobjects.datacleaner.monitor.jaxb.Schedule;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;

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

        result.setExecutionStatus(createExecutionStatus(executionLog.getExecutionStatus()));
        result.setJobBeginDate(createDate(executionLog.getJobBeginDate()));
        result.setJobEndDate(createDate(executionLog.getJobEndDate()));
        result.setSchedule(createSchedule(executionLog.getSchedule()));
        result.setLogOutput(executionLog.getLogOutput());
        return result;
    }

    private Schedule createSchedule(ScheduleDefinition scheduleDefinition) {
        JaxbScheduleWriter scheduleWriter = new JaxbScheduleWriter();
        Schedule schedule = scheduleWriter.createSchedule(scheduleDefinition);
        return schedule;
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
