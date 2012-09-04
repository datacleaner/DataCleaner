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
package org.eobjects.datacleaner.monitor.alertnotification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eobjects.datacleaner.monitor.configuration.ResultContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.scheduling.model.AlertDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.server.MetricValueProducer;
import org.eobjects.datacleaner.monitor.server.MetricValues;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.metamodel.util.LazyRef;
import org.eobjects.metamodel.util.NumberComparator;
import org.eobjects.metamodel.util.Ref;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlertNotificationServiceImpl implements AlertNotificationService {

    @Autowired
    TenantContextFactory _tenantContextFactory;
    
    @Autowired
    MetricValueProducer _metricValueProducer;
    
    private List<AlertNotifier> alertNotifiers;
    
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

        final TenantContext context = _tenantContextFactory.getContext(execution.getSchedule().getTenant());
        final ResultContext result = context.getResult(execution.getResultId());

        final Ref<Map<AlertDefinition, Number>> activeAlerts = new LazyRef<Map<AlertDefinition, Number>>() {
            @Override
            protected Map<AlertDefinition, Number> fetch() {
                final List<MetricIdentifier> metricIdentifiers = new ArrayList<MetricIdentifier>();
                final List<AlertDefinition> allAlerts = execution.getSchedule().getAlerts();
                for (AlertDefinition alertDefinition : allAlerts) {
                    MetricIdentifier metricIdentifier = alertDefinition.getMetricIdentifier();
                    metricIdentifiers.add(metricIdentifier);
                }

                final TenantIdentifier tenantId = execution.getSchedule().getTenant();

                final MetricValues metricValues = _metricValueProducer.getMetricValues(metricIdentifiers,
                        result.getResultFile(), tenantId, execution.getJob());
                final List<Number> values = metricValues.getValues();

                final Map<AlertDefinition, Number> result = new TreeMap<AlertDefinition, Number>();
                for (int i = 0; i < allAlerts.size(); i++) {
                    final Number max = allAlerts.get(i).getMaximumValue();
                    final Number min = allAlerts.get(i).getMinimumValue();
                    final Number actual = values.get(i);
                    if (isBeyondThreshold(actual, min, max)) {
                        result.put(allAlerts.get(i), actual);
                    }
                }

                return result;
            }
        };

        for (AlertNotifier alertNotification : alertNotifiers) {
            alertNotification.onExecutionFinished(execution, activeAlerts, result);
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
