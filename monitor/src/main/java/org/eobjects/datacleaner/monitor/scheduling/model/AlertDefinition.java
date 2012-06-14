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
package org.eobjects.datacleaner.monitor.scheduling.model;

import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Defines the rules of an alert that the user has configured.
 */
public class AlertDefinition implements IsSerializable {

    private MetricIdentifier _metricIdentifier;
    private Number _minimumValue;
    private Number _maximumValue;

    // no-args constructor
    public AlertDefinition() {
        this(null, null, null);
    }

    public AlertDefinition(MetricIdentifier metricIdentifier, Number minimumValue, Number maximumValue) {
        _metricIdentifier = metricIdentifier;
        _minimumValue = minimumValue;
        _maximumValue = maximumValue;
    }

    public Number getMaximumValue() {
        return _maximumValue;
    }

    public Number getMinimumValue() {
        return _minimumValue;
    }

    public MetricIdentifier getMetricIdentifier() {
        return _metricIdentifier;
    }
}
