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
package org.datacleaner.monitor.shared.model;

import java.io.Serializable;
import java.util.List;


/**
 * Represents the available metrics of a specific job.
 */
public class JobMetrics implements Serializable {

    private static final long serialVersionUID = 1L;

    private JobIdentifier _job;
    private List<MetricGroup> _metricGroups;

    public JobMetrics(final JobIdentifier job, final List<MetricGroup> metricGroups) {
        _job = job;
        _metricGroups = metricGroups;
    }

    public JobMetrics() {
    }

    public JobIdentifier getJob() {
        return _job;
    }

    public void setJob(final JobIdentifier job) {
        _job = job;
    }

    public List<MetricGroup> getMetricGroups() {
        return _metricGroups;
    }

    public void setMetricGroups(final List<MetricGroup> metricGroups) {
        _metricGroups = metricGroups;
    }

    public String getName() {
        return _job.getName() + " metrics";
    }

    @Override
    public String toString() {
        return "JobMetrics[" + getName() + "]";
    }

    public MetricGroup getMetricGroup(final String groupName) {
        for (final MetricGroup group : _metricGroups) {
            if (groupName.equals(group.getName())) {
                return group;
            }
        }
        return null;
    }

    public MetricGroup getMetricGroup(final MetricIdentifier metric) {
        for (final MetricGroup group : _metricGroups) {
            if (group.containsMetric(metric)) {
                return group;
            }
        }
        return null;
    }
}
