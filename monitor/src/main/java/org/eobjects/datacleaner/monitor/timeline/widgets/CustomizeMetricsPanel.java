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
package org.eobjects.datacleaner.monitor.timeline.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.datacleaner.monitor.timeline.TimelineServiceAsync;
import org.eobjects.datacleaner.monitor.timeline.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.JobMetrics;
import org.eobjects.datacleaner.monitor.timeline.model.MetricGroup;
import org.eobjects.datacleaner.monitor.timeline.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;
import org.eobjects.datacleaner.monitor.util.LoadingIndicator;

import com.google.gwt.user.client.ui.FlowPanel;

public class CustomizeMetricsPanel extends FlowPanel {

    private final List<MetricPresenter> _metricPresenters;
    private TimelineDefinition _timelineDefinition;
    private TimelineServiceAsync _service;
    private TenantIdentifier _tenantIdentifier;

    public CustomizeMetricsPanel(TimelineServiceAsync service, TenantIdentifier tenantIdentifier,
            TimelineDefinition timelineDefinition) {
        super();
        _service = service;
        _tenantIdentifier = tenantIdentifier;
        _timelineDefinition = timelineDefinition;
        _metricPresenters = new ArrayList<MetricPresenter>();

        addStyleName("CustomizeMetricsPanel");
        add(new LoadingIndicator());

        _service.getJobMetrics(_tenantIdentifier, _timelineDefinition.getJobIdentifier(),
                new DCAsyncCallback<JobMetrics>() {

                    @Override
                    public void onSuccess(JobMetrics jobMetrics) {
                        setJobMetrics(jobMetrics);
                    }
                });
    }

    private void setJobMetrics(JobMetrics jobMetrics) {
        clear();
        final List<MetricGroup> metricGroups = jobMetrics.getMetricGroups();
        for (MetricGroup metricGroup : metricGroups) {
            add(createMetricGroupPanel(metricGroup));
        }
        onMetricsLoaded();
    }

    /**
     * Overrideable method invoked when metrics have been loaded
     */
    protected void onMetricsLoaded() {
    }

    private FlowPanel createMetricGroupPanel(MetricGroup metricGroup) {
        final FlowPanel panel = new FlowPanel();
        panel.addStyleName("MetricGroupPanel");

        panel.add(new HeadingLabel(metricGroup.getName()));

        final List<MetricIdentifier> activeMetrics = _timelineDefinition.getMetrics();
        final List<MetricIdentifier> availableMetrics = metricGroup.getMetrics();
        for (MetricIdentifier metricIdentifier : availableMetrics) {
            MetricPresenter presenter = createMetricPresenter(metricGroup, metricIdentifier, activeMetrics);
            _metricPresenters.add(presenter);
            panel.add(presenter);
        }

        return panel;
    }

    private MetricPresenter createMetricPresenter(MetricGroup metricGroup, MetricIdentifier metricIdentifier,
            List<MetricIdentifier> activeMetrics) {
        if (metricIdentifier.isParameterizedByColumnName()) {
            return new ColumnParameterizedMetricPresenter(metricIdentifier, activeMetrics, metricGroup);
        } else if (metricIdentifier.isParameterizedByQueryString()) {
            final JobIdentifier jobIdentifier = _timelineDefinition.getJobIdentifier();
            return new StringParameterizedMetricPresenter(_tenantIdentifier, jobIdentifier, metricIdentifier,
                    activeMetrics, _service);
        } else {
            return new UnparameterizedMetricPresenter(metricIdentifier, activeMetrics);
        }
    }

    public List<MetricIdentifier> getSelectedMetrics() {
        final List<MetricIdentifier> metrics = new ArrayList<MetricIdentifier>();

        for (MetricPresenter metricPresenter : _metricPresenters) {
            final List<MetricIdentifier> selectedMetrics = metricPresenter.getSelectedMetrics();
            metrics.addAll(selectedMetrics);
        }

        return metrics;
    }
}
