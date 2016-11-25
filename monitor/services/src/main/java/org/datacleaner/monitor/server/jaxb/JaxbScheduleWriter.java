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
import java.util.List;

import org.datacleaner.monitor.jaxb.Alert;
import org.datacleaner.monitor.jaxb.AlertSeverityType;
import org.datacleaner.monitor.jaxb.Schedule;
import org.datacleaner.monitor.jaxb.Schedule.Alerts;
import org.datacleaner.monitor.jaxb.VariableProvider;
import org.datacleaner.monitor.scheduling.model.AlertDefinition;
import org.datacleaner.monitor.scheduling.model.AlertSeverity;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.scheduling.model.TriggerType;
import org.datacleaner.monitor.scheduling.model.VariableProviderDefinition;
import org.datacleaner.monitor.shared.model.JobIdentifier;

/**
 * Jaxb based Schedule writer for .schedule.xml files.
 */
public class JaxbScheduleWriter extends AbstractJaxbAdaptor<Schedule> {

    public JaxbScheduleWriter() {
        super(Schedule.class);
    }

    public void write(final ScheduleDefinition scheduleDefinition, final OutputStream outputStream) {
        final Schedule schedule = createSchedule(scheduleDefinition);

        marshal(schedule, outputStream);
    }

    public Schedule createSchedule(final ScheduleDefinition scheduleDefinition) {
        final Schedule schedule = new Schedule();
        if (scheduleDefinition == null) {
            return schedule;
        }

        if (scheduleDefinition.getTriggerType() == TriggerType.DEPENDENT) {
            final JobIdentifier scheduleAfterJob = scheduleDefinition.getDependentJob();
            if (scheduleAfterJob != null) {
                schedule.setDependentJob(scheduleAfterJob.getName());
            }
        } else if (scheduleDefinition.getTriggerType() == TriggerType.PERIODIC) {
            schedule.setCronExpression(scheduleDefinition.getCronExpression());
        } else if (scheduleDefinition.getTriggerType() == TriggerType.ONETIME) {
            schedule.setOneTime(scheduleDefinition.getDateForOneTimeSchedule());
        } else if (scheduleDefinition.getTriggerType() == TriggerType.HOTFOLDER) {
            schedule.setHotFolder(scheduleDefinition.getHotFolder());
        } else {
            schedule.setManualTrigger(true);
        }

        final VariableProviderDefinition variableProviderDef = scheduleDefinition.getVariableProvider();
        if (variableProviderDef != null) {
            final VariableProvider variableProvider = new VariableProvider();
            variableProvider.setClassName(variableProviderDef.getClassName());
            schedule.setVariableProvider(variableProvider);
        }

        final Boolean runOnHadoop = scheduleDefinition.isRunOnHadoop();
        if (runOnHadoop != null && runOnHadoop.booleanValue()) {
            schedule.setRunOnHadoop(runOnHadoop);
        }
        final boolean distributedExecution = scheduleDefinition.isDistributedExecution();
        schedule.setDistributedExecution(distributedExecution);


        final Alerts alerts = new Alerts();
        final List<AlertDefinition> alertDefinitions = scheduleDefinition.getAlerts();
        for (final AlertDefinition alertDefinition : alertDefinitions) {
            final Alert alert = createAlert(alertDefinition);
            alerts.getAlert().add(alert);
        }
        schedule.setAlerts(alerts);

        return schedule;
    }

    private Alert createAlert(final AlertDefinition alertDefinition) {
        final Alert alert = new Alert();
        alert.setDescription(alertDefinition.getDescription());
        alert.setMinimumValue(
                (alertDefinition.getMinimumValue() == null ? null : alertDefinition.getMinimumValue().intValue()));
        alert.setMaximumValue(
                (alertDefinition.getMaximumValue() == null ? null : alertDefinition.getMaximumValue().intValue()));
        alert.setMetric(new JaxbMetricAdaptor().serialize(alertDefinition.getMetricIdentifier()));
        alert.setSeverity(createSeverity(alertDefinition.getSeverity()));
        return alert;
    }

    private AlertSeverityType createSeverity(final AlertSeverity severity) {
        if (severity == null) {
            return null;
        }
        switch (severity) {
        case INTELLIGENCE:
            return AlertSeverityType.INTELLIGENCE;
        case SURVEILLANCE:
            return AlertSeverityType.SURVEILLANCE;
        case WARNING:
            return AlertSeverityType.WARNING;
        case FATAL:
            return AlertSeverityType.FATAL;
        default:
            throw new UnsupportedOperationException("Unsupported severity: " + severity);
        }
    }

}
