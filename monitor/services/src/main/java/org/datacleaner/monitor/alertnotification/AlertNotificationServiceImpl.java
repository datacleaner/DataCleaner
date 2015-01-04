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
package org.datacleaner.monitor.alertnotification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.datacleaner.monitor.configuration.ResultContext;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.job.MetricValues;
import org.datacleaner.monitor.scheduling.model.AlertDefinition;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.server.MetricValueProducer;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.repository.RepositoryFile;
import org.apache.metamodel.util.LazyRef;
import org.apache.metamodel.util.NumberComparator;
import org.apache.metamodel.util.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default alert notification service
 */
@Component
public class AlertNotificationServiceImpl implements AlertNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AlertNotificationServiceImpl.class);

    private final TenantContextFactory _tenantContextFactory;
    private final MetricValueProducer _metricValueProducer;

    private List<AlertNotifier> alertNotifiers;

    @Autowired
    public AlertNotificationServiceImpl(TenantContextFactory tenantContextFactory,
            MetricValueProducer metricValueProducer) {
        _tenantContextFactory = tenantContextFactory;
        _metricValueProducer = metricValueProducer;
    }

    @Override
    public void notifySubscribers(final ExecutionLog execution) {
        if (alertNotifiers == null || alertNotifiers.isEmpty()) {
            // no notifiers to invoke
            return;
        }

        if (_tenantContextFactory == null) {
            throw new IllegalStateException("TenantContextFactory cannot be null");
        }
        if (_metricValueProducer == null) {
            throw new IllegalStateException("MetricValueProducer cannot be null");
        }

        final TenantContext tenantContext = _tenantContextFactory.getContext(execution.getSchedule().getTenant());
        final ResultContext resultContext = tenantContext.getResult(execution.getResultId());
        if (resultContext == null) {
            // nothing to tell about
            return;
        }

        final Ref<Map<AlertDefinition, Number>> activeAlerts = new LazyRef<Map<AlertDefinition, Number>>() {
            @Override
            protected Map<AlertDefinition, Number> fetch() {
                final Map<AlertDefinition, Number> result = new TreeMap<AlertDefinition, Number>();
                final List<MetricIdentifier> metricIdentifiers = new ArrayList<MetricIdentifier>();
                final List<AlertDefinition> allAlerts = execution.getSchedule().getAlerts();
                if (allAlerts.isEmpty()) {
                    // no alerts at all
                    return result;
                }
                for (AlertDefinition alertDefinition : allAlerts) {
                    MetricIdentifier metricIdentifier = alertDefinition.getMetricIdentifier();
                    metricIdentifiers.add(metricIdentifier);
                }

                final TenantIdentifier tenantId = execution.getSchedule().getTenant();
                final RepositoryFile resultFile = resultContext.getResultFile();

                final MetricValues metricValues = _metricValueProducer.getMetricValues(metricIdentifiers, resultFile,
                        tenantId, execution.getJob());
                final List<Number> values = metricValues.getValues();

                for (int i = 0; i < allAlerts.size(); i++) {
                    AlertDefinition alertDef = allAlerts.get(i);
                    final Number max = alertDef.getMaximumValue();
                    final Number min = alertDef.getMinimumValue();
                    final Number actual = values.get(i);
                    if (isBeyondThreshold(actual, min, max)) {
                        result.put(alertDef, actual);
                    }
                }

                return result;
            }
        };

        for (AlertNotifier alertNotifier : alertNotifiers) {
            try {
                alertNotifier.onExecutionFinished(execution, activeAlerts, resultContext);
            } catch (Exception e) {
                logger.error("AlertNotifier (" + alertNotifier + ") threw unexpected exception", e);
            }
        }
    }

    protected boolean isBeyondThreshold(Number actual, Number min, Number max) {
        if (min == null && max == null) {
            return false;
        }

        final Comparable<Object> comparable = NumberComparator.getComparable(actual);
        if (max != null && comparable.compareTo(max) > 0) {
            return true;
        }

        if (min != null && comparable.compareTo(min) < 0) {
            return true;
        }

        return false;
    }

    public void setAlertNotifiers(List<AlertNotifier> alertNotifiers) {
        this.alertNotifiers = alertNotifiers;
    }

    public List<AlertNotifier> getAlertNotifiers() {
        return alertNotifiers;
    }
}
