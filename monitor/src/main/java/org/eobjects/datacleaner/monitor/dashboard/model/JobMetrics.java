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
package org.eobjects.datacleaner.monitor.timeline.model;

import java.util.List;

import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.MetricGroup;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents the available metrics of a specific job.
 */
public class JobMetrics implements IsSerializable {

    private JobIdentifier _job;
    private List<MetricGroup> _metricGroups;

    public JobIdentifier getJob() {
        return _job;
    }

    public void setJob(JobIdentifier job) {
        _job = job;
    }

    public List<MetricGroup> getMetricGroups() {
        return _metricGroups;
    }

    public void setMetricGroups(List<MetricGroup> metricGroups) {
        _metricGroups = metricGroups;
    }

    public String getName() {
        return _job.getName() + " metrics";
    }

    @Override
    public String toString() {
        return "JobMetrics[" + getName() + "]";
    }
}
