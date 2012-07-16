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
package org.eobjects.datacleaner.monitor.dashboard.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.datacleaner.monitor.dashboard.DashboardServiceAsync;
import org.eobjects.datacleaner.monitor.dashboard.model.JobMetrics;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.MetricGroup;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.HeadingLabel;
import org.eobjects.datacleaner.monitor.shared.widgets.LoadingIndicator;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;

public class CustomizeMetricsPanel extends FlowPanel {

    private final List<MetricPresenter> _metricPresenters;
    private TimelineDefinition _timelineDefinition;
    private DashboardServiceAsync _service;
    private TenantIdentifier _tenantIdentifier;

    public CustomizeMetricsPanel(DashboardServiceAsync service, TenantIdentifier tenantIdentifier,
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
        final FlowPanel innerPanel = new FlowPanel();

        final List<MetricIdentifier> activeMetrics = _timelineDefinition.getMetrics();
        final List<MetricIdentifier> availableMetrics = metricGroup.getMetrics();
        final MultipleColumnParameterizedMetricsPresenter columnParameterizedMetrics = new MultipleColumnParameterizedMetricsPresenter(metricGroup);
        for (MetricIdentifier metricIdentifier : availableMetrics) {
            final MetricPresenter presenter = createMetricPresenter(columnParameterizedMetrics, metricGroup,
                    metricIdentifier, activeMetrics);
            if (presenter != null) {
                _metricPresenters.add(presenter);
                innerPanel.add(presenter);
            }
        }

        if (!columnParameterizedMetrics.isEmpty()) {
            _metricPresenters.add(columnParameterizedMetrics);
            innerPanel.add(columnParameterizedMetrics);
        }

        
        HeadingLabel heading = new HeadingLabel(metricGroup.getName());
        heading.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // toggle visibility
                innerPanel.setVisible(!innerPanel.isVisible());
            }
        });
        final FlowPanel panel = new FlowPanel();
        panel.addStyleName("MetricGroupPanel");
        panel.add(heading);
        panel.add(innerPanel);
        return panel;
    }

    private MetricPresenter createMetricPresenter(
            MultipleColumnParameterizedMetricsPresenter columnParameterizedMetrics, MetricGroup metricGroup,
            MetricIdentifier metricIdentifier, List<MetricIdentifier> activeMetrics) {
        if (metricIdentifier.isParameterizedByColumnName()) {
            final ColumnParameterizedMetricPresenter presenter = new ColumnParameterizedMetricPresenter(
                    metricIdentifier, activeMetrics, metricGroup);
            columnParameterizedMetrics.add(presenter);
            // null will be treated as a presenter not added immediately
            return null;
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
