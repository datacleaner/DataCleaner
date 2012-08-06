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

import java.io.InputStream;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eobjects.datacleaner.monitor.jaxb.ExecutionType;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.TriggerType;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

/**
 * Reader of {@link ExecutionLog} objects.
 */
public class JaxbExecutionLogReader extends AbstractJaxbAdaptor<org.eobjects.datacleaner.monitor.jaxb.ExecutionLog> {

    public ExecutionLog read(InputStream inputStream, TenantIdentifier tenant) {
        org.eobjects.datacleaner.monitor.jaxb.ExecutionLog jaxbExecutionLog = unmarshal(inputStream);
        ExecutionLog executionLog = convert(jaxbExecutionLog, tenant);
        return executionLog;
    }

    private ExecutionLog convert(org.eobjects.datacleaner.monitor.jaxb.ExecutionLog jaxbExecutionLog,
            TenantIdentifier tenant) {

        final ExecutionLog executionLog = new ExecutionLog();
        executionLog.setResultId(jaxbExecutionLog.getResultId());

        final ExecutionType jaxbExecutionStatus = jaxbExecutionLog.getExecutionStatus();
        if (jaxbExecutionStatus != null) {
            executionLog.setExecutionStatus(ExecutionStatus.valueOf(jaxbExecutionStatus.toString()));
        }

        final org.eobjects.datacleaner.monitor.jaxb.TriggerType jaxbTriggerType = jaxbExecutionLog.getTriggerType();
        if (jaxbTriggerType != null) {
            executionLog.setTriggerType(TriggerType.valueOf(jaxbTriggerType.toString()));
        }

        final XMLGregorianCalendar jaxbJobBeginDate = jaxbExecutionLog.getJobBeginDate();
        if (jaxbJobBeginDate != null) {
            executionLog.setJobBeginDate(jaxbJobBeginDate.toGregorianCalendar().getTime());
        }

        final XMLGregorianCalendar jaxbJobEndDate = jaxbExecutionLog.getJobEndDate();
        if (jaxbJobEndDate != null) {
            executionLog.setJobEndDate(jaxbJobEndDate.toGregorianCalendar().getTime());
        }

        executionLog.setTriggeredBy(jaxbExecutionLog.getTriggeredBy());
        executionLog.setLogOutput(jaxbExecutionLog.getLogOutput());

        final JaxbScheduleReader reader = new JaxbScheduleReader();
        final ScheduleDefinition schedule = reader.createSchedule(jaxbExecutionLog.getSchedule(), null, tenant, null,
                false);
        executionLog.setSchedule(schedule);

        return executionLog;
    }
}
