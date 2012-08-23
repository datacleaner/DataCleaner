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
import java.util.List;

import org.eobjects.datacleaner.monitor.jaxb.Alert;
import org.eobjects.datacleaner.monitor.jaxb.AlertSeverityType;
import org.eobjects.datacleaner.monitor.jaxb.MetricType;
import org.eobjects.datacleaner.monitor.jaxb.Schedule;
import org.eobjects.datacleaner.monitor.jaxb.Schedule.Alerts;
import org.eobjects.datacleaner.monitor.scheduling.model.AlertDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.AlertSeverity;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

/**
 * JAXB based reader of .schedule.xml files
 */
public class JaxbScheduleReader extends AbstractJaxbAdaptor<Schedule> {
    
    public JaxbScheduleReader() {
        super(Schedule.class);
    }

    public ScheduleDefinition read(InputStream inputStream, JobIdentifier job, TenantIdentifier tenant,
            DatastoreIdentifier datastore) {
        final Schedule schedule = unmarshal(inputStream);
        final ScheduleDefinition scheduleDefinition = createSchedule(schedule, job, tenant, datastore, true);
        return scheduleDefinition;
    }

    public AlertDefinition createAlert(Alert alert) {
        final MetricIdentifier metricIdentifier = new MetricIdentifier();
        final MetricType metricType = alert.getMetric();
        metricIdentifier.setAnalyzerDescriptorName(metricType.getAnalyzerDescriptorName());
        metricIdentifier.setAnalyzerInputName(metricType.getAnalyzerInput());
        metricIdentifier.setAnalyzerName(metricType.getAnalyzerName());
        metricIdentifier.setMetricDescriptorName(metricType.getMetricDescriptorName());
        metricIdentifier.setParamColumnName(metricType.getMetricParamColumnName());
        metricIdentifier.setParamQueryString(metricType.getMetricParamQueryString());
        metricIdentifier.setParameterizedByColumnName(metricType.getMetricParamColumnName() != null);
        metricIdentifier.setParameterizedByQueryString(metricType.getMetricParamQueryString() != null);

        AlertSeverity severity = createSeverity(alert.getSeverity());

        return new AlertDefinition(alert.getDescription(), metricIdentifier, alert.getMinimumValue(),
                alert.getMaximumValue(), severity);
    }

    private AlertSeverity createSeverity(AlertSeverityType severity) {
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

    public ScheduleDefinition createSchedule(Schedule schedule, JobIdentifier job, TenantIdentifier tenant,
            DatastoreIdentifier datastore, boolean includeAlerts) {
        final ScheduleDefinition scheduleDefinition = new ScheduleDefinition();
        if (schedule != null) {
            scheduleDefinition.setCronExpression(schedule.getCronExpression());
            final String jaxbDependentJob = schedule.getDependentJob();
            if (jaxbDependentJob != null) {
                scheduleDefinition.setDependentJob(new JobIdentifier(jaxbDependentJob));
            }
        }
        scheduleDefinition.setJob(job);
        scheduleDefinition.setTenant(tenant);
        scheduleDefinition.setDatastore(datastore);

        if (includeAlerts && schedule != null) {
            final Alerts jaxbAlerts = schedule.getAlerts();
            if (jaxbAlerts != null) {
                List<Alert> alertList = jaxbAlerts.getAlert();
                for (Alert jaxbAlert : alertList) {
                    AlertDefinition alert = createAlert(jaxbAlert);
                    scheduleDefinition.getAlerts().add(alert);
                }
            }
        }
        return scheduleDefinition;
    }

}
