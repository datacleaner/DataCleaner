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
package org.datacleaner.monitor.server.jaxb;

import java.io.OutputStream;

import org.datacleaner.monitor.jaxb.ExecutionType;
import org.datacleaner.monitor.jaxb.Schedule;
import org.datacleaner.monitor.jaxb.TriggerType;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.scheduling.model.ExecutionStatus;

/**
 * Component responsible for writing a {@link ExecutionLog} to an XML
 * outputstream.
 */
public class JaxbExecutionLogWriter extends AbstractJaxbAdaptor<org.datacleaner.monitor.jaxb.ExecutionLog> {

    public JaxbExecutionLogWriter() {
        super(org.datacleaner.monitor.jaxb.ExecutionLog.class);
    }

    public void write(final ExecutionLog executionLog, final OutputStream out) {
        final org.datacleaner.monitor.jaxb.ExecutionLog jaxbObj = createExecutionLog(executionLog);

        marshal(jaxbObj, out);
    }

    private org.datacleaner.monitor.jaxb.ExecutionLog createExecutionLog(final ExecutionLog executionLog) {
        final org.datacleaner.monitor.jaxb.ExecutionLog result = new org.datacleaner.monitor.jaxb.ExecutionLog();

        result.setResultId(executionLog.getResultId());
        result.setExecutionStatus(createExecutionStatus(executionLog.getExecutionStatus()));
        result.setJobBeginDate(createDate(executionLog.getJobBeginDate()));
        result.setJobEndDate(createDate(executionLog.getJobEndDate()));
        result.setTriggerType(createTriggerType(executionLog.getTriggerType()));
        result.setTriggeredBy(executionLog.getTriggeredBy());
        result.setLogOutput(executionLog.getLogOutput());
        result.setResultPersisted(executionLog.isResultPersisted());

        final JaxbScheduleWriter scheduleWriter = new JaxbScheduleWriter();
        final Schedule schedule = scheduleWriter.createSchedule(executionLog.getSchedule());

        result.setSchedule(schedule);
        return result;
    }

    private TriggerType createTriggerType(final org.datacleaner.monitor.scheduling.model.TriggerType triggerType) {
        switch (triggerType) {
        case PERIODIC:
            return TriggerType.PERIODIC;
        case DEPENDENT:
            return TriggerType.DEPENDENT;
        case MANUAL:
            return TriggerType.MANUAL;
        case ONETIME:
            return TriggerType.ONETIME;
        case HOTFOLDER:
            return TriggerType.HOTFOLDER;
        default:
            throw new UnsupportedOperationException("Unknown trigger type: " + triggerType);
        }
    }

    private ExecutionType createExecutionStatus(final ExecutionStatus executionStatus) {
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
