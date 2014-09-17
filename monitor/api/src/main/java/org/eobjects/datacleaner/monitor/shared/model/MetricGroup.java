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
package org.eobjects.datacleaner.monitor.shared.model;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a grouping of related {@link MetricIdentifier}s. Typically these
 * are grouped according to the analysis job components that expose the metrics.
 */
public class MetricGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private String _name;
    private List<MetricIdentifier> _metrics;
    private List<String> _columnNames;

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public List<MetricIdentifier> getMetrics() {
        return _metrics;
    }

    public void setMetrics(List<MetricIdentifier> metrics) {
        _metrics = metrics;
    }

    public List<String> getColumnNames() {
        return _columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        _columnNames = columnNames;
    }

    @Override
    public String toString() {
        return "MetricGroup[" + getName() + "]";
    }

    public MetricIdentifier getMetric(String metricDescriptorName) {
        for (MetricIdentifier metric : _metrics) {
            if (metricDescriptorName.equals(metric.getMetricDescriptorName())) {
                return metric;
            }
        }
        return null;
    }

    public boolean containsMetric(MetricIdentifier metricIdentifier) {
        for (MetricIdentifier metric : _metrics) {
            if (metric.equalsIgnoreParameterValues(metricIdentifier)) {
                return true;
            }
        }
        return false;
    }
}
