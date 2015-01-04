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

import java.io.InputStream;

import javax.xml.datatype.XMLGregorianCalendar;

import org.datacleaner.monitor.jaxb.ExecutionType;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.scheduling.model.TriggerType;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;

/**
 * Reader of {@link ExecutionLog} objects.
 */
public class JaxbExecutionLogReader extends AbstractJaxbAdaptor<org.datacleaner.monitor.jaxb.ExecutionLog> {

    public JaxbExecutionLogReader() {
        super(org.datacleaner.monitor.jaxb.ExecutionLog.class);
    }

    public ExecutionLog read(InputStream inputStream, JobIdentifier jobIdentifier, TenantIdentifier tenant) {
        org.datacleaner.monitor.jaxb.ExecutionLog jaxbExecutionLog = unmarshal(inputStream);
        ExecutionLog executionLog = convert(jaxbExecutionLog, jobIdentifier, tenant);
        return executionLog;
    }

    private ExecutionLog convert(org.datacleaner.monitor.jaxb.ExecutionLog jaxbExecutionLog,
            JobIdentifier jobIdentifier, TenantIdentifier tenant) {

        final ExecutionLog executionLog = new ExecutionLog();
        executionLog.setResultId(jaxbExecutionLog.getResultId());

        final ExecutionType jaxbExecutionStatus = jaxbExecutionLog.getExecutionStatus();
        if (jaxbExecutionStatus != null) {
            executionLog.setExecutionStatus(ExecutionStatus.valueOf(jaxbExecutionStatus.toString()));
        }

        final org.datacleaner.monitor.jaxb.TriggerType jaxbTriggerType = jaxbExecutionLog.getTriggerType();
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
        
        Boolean resultPersisted = jaxbExecutionLog.getResultPersisted();
        resultPersisted = (resultPersisted == null) ? true : resultPersisted;
        executionLog.setResultPersisted(resultPersisted.booleanValue());

        final JaxbScheduleReader reader = new JaxbScheduleReader();
        final ScheduleDefinition schedule = reader.createSchedule(jaxbExecutionLog.getSchedule(), jobIdentifier, tenant, null,
                false);
        executionLog.setSchedule(schedule);
        executionLog.setJob(jobIdentifier);

        return executionLog;
    }
}
