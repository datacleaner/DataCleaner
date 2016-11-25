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
import java.util.List;

import org.datacleaner.monitor.jaxb.Alert;
import org.datacleaner.monitor.jaxb.AlertSeverityType;
import org.datacleaner.monitor.jaxb.MetricType;
import org.datacleaner.monitor.jaxb.Schedule;
import org.datacleaner.monitor.jaxb.Schedule.Alerts;
import org.datacleaner.monitor.jaxb.VariableProvider;
import org.datacleaner.monitor.scheduling.model.AlertDefinition;
import org.datacleaner.monitor.scheduling.model.AlertSeverity;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.scheduling.model.VariableProviderDefinition;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;

/**
 * JAXB based reader of .schedule.xml files
 */
public class JaxbScheduleReader extends AbstractJaxbAdaptor<Schedule> {

    public JaxbScheduleReader() {
        super(Schedule.class);
    }

    public ScheduleDefinition read(final InputStream inputStream, final JobIdentifier job,
            final TenantIdentifier tenant, final String groupName) {
        final Schedule schedule = unmarshal(inputStream);
        return createSchedule(schedule, job, tenant, groupName, true);
    }

    public AlertDefinition createAlert(final Alert alert) {
        final MetricType metricType = alert.getMetric();

        final MetricIdentifier metricIdentifier = new JaxbMetricAdaptor().deserialize(metricType);

        final AlertSeverity severity = createSeverity(alert.getSeverity());

        return new AlertDefinition(alert.getDescription(), metricIdentifier, alert.getMinimumValue(),
                alert.getMaximumValue(), severity);
    }

    private AlertSeverity createSeverity(final AlertSeverityType severity) {
        if (severity == null) {
            return null;
        }
        switch (severity) {
        case INTELLIGENCE:
            return AlertSeverity.INTELLIGENCE;
        case SURVEILLANCE:
            return AlertSeverity.SURVEILLANCE;
        case WARNING:
            return AlertSeverity.WARNING;
        case FATAL:
            return AlertSeverity.FATAL;
        default:
            throw new UnsupportedOperationException("Unsupported severity: " + severity);
        }
    }

    public ScheduleDefinition createSchedule(final Schedule schedule, final JobIdentifier job,
            final TenantIdentifier tenant, final String groupName, final boolean includeAlerts) {
        final ScheduleDefinition scheduleDefinition = new ScheduleDefinition();
        if (schedule != null) {
            scheduleDefinition.setDateForOneTimeSchedule(schedule.getOneTime());
            scheduleDefinition.setCronExpression(schedule.getCronExpression());
            final String jaxbDependentJob = schedule.getDependentJob();
            if (jaxbDependentJob != null) {
                scheduleDefinition.setDependentJob(new JobIdentifier(jaxbDependentJob));
            }

            final VariableProvider variableProvider = schedule.getVariableProvider();
            if (variableProvider != null) {
                final VariableProviderDefinition variableProviderDef = new VariableProviderDefinition();
                variableProviderDef.setClassName(variableProvider.getClassName());
                scheduleDefinition.setVariableProvider(variableProviderDef);
            }

            scheduleDefinition.setHotFolder(schedule.getHotFolder());

            final Boolean runOnHadoop = schedule.isRunOnHadoop();
            if (runOnHadoop != null && runOnHadoop.booleanValue()) {
                scheduleDefinition.setRunOnHadoop(runOnHadoop);
            }
            final Boolean distributedExecution = schedule.isDistributedExecution();
            if (distributedExecution != null && distributedExecution.booleanValue()) {
                scheduleDefinition.setDistributedExecution(distributedExecution.booleanValue());
            }

        }

        scheduleDefinition.setJob(job);
        scheduleDefinition.setTenant(tenant);
        scheduleDefinition.setGroupName(groupName);

        if (includeAlerts && schedule != null) {
            final Alerts jaxbAlerts = schedule.getAlerts();
            if (jaxbAlerts != null) {
                final List<Alert> alertList = jaxbAlerts.getAlert();
                for (final Alert jaxbAlert : alertList) {
                    final AlertDefinition alert = createAlert(jaxbAlert);
                    scheduleDefinition.getAlerts().add(alert);
                }
            }
        }
        return scheduleDefinition;
    }

}
