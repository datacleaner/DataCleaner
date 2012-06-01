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

import java.util.ArrayList;
import java.util.List;


import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Defines a timeline created by the user. A {@link TimelineDefinition} can
 * either be created ad-hoc or saved in the repository, in which case it will
 * have a {@link TimelineIdentifier}.
 */
public class TimelineDefinition implements IsSerializable {

    private List<MetricIdentifier> _metrics;
    private JobIdentifier _jobIdentifier;
    private ChartOptions _chartOptions;
    private boolean _changed;
    
    public TimelineDefinition(boolean changed) {
        _changed = changed;
    }
    
    public TimelineDefinition() {
        this(false);
    }

    public void setChanged(boolean changed) {
        _changed = changed;
    }
    
    public boolean isChanged() {
        return _changed;
    }
    
    public List<MetricIdentifier> getMetrics() {
        if (_metrics == null) {
            return new ArrayList<MetricIdentifier>(0);
        }
        return _metrics;
    }
    
    public void setMetrics(List<MetricIdentifier> metrics) {
        _metrics = metrics;
    }

    public JobIdentifier getJobIdentifier() {
        return _jobIdentifier;
    }

    public void setJobIdentifier(JobIdentifier jobIdentifier) {
        _jobIdentifier = jobIdentifier;
    }

    public ChartOptions getChartOptions() {
        if (_chartOptions == null) {
            return new ChartOptions();
        }
        return _chartOptions;
    }

    public void setChartOptions(ChartOptions chartOptions) {
        _chartOptions = chartOptions;
    }

    @Override
    public String toString() {
        return "TimelineDefinition[job=" + _jobIdentifier + ",metrics=" + _metrics + "]";
    }
}
