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
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.shared.DescriptorService;
import org.eobjects.datacleaner.monitor.shared.DescriptorServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobMetrics;
import org.eobjects.datacleaner.monitor.shared.model.MetricGroup;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.DefineMetricPopup;
import org.eobjects.datacleaner.monitor.shared.widgets.HeadingLabel;
import org.eobjects.datacleaner.monitor.shared.widgets.LoadingIndicator;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

public class CustomizeMetricsPanel extends FlowPanel {

    private static final DescriptorServiceAsync descriptorService = GWT.create(DescriptorService.class);

    private final List<MetricPresenter> _metricPresenters;
    private FlowPanel _formulaMetricsPanel;
    private TimelineDefinition _timelineDefinition;
    private DashboardServiceAsync _service;
    private TenantIdentifier _tenantIdentifier;

    public CustomizeMetricsPanel(DashboardServiceAsync service, TenantIdentifier tenantIdentifier,
            TimelineDefinition timelineDefinition) {
        super();
        _service = service;
        _tenantIdentifier = tenantIdentifier;
        _timelineDefinition = timelineDefinition;
        _formulaMetricsPanel = null;
        _metricPresenters = new ArrayList<MetricPresenter>();

        addStyleName("CustomizeMetricsPanel");
        add(new LoadingIndicator());

        descriptorService.getJobMetrics(_tenantIdentifier, _timelineDefinition.getJobIdentifier(),
                new DCAsyncCallback<JobMetrics>() {
                    @Override
                    public void onSuccess(JobMetrics jobMetrics) {
                        setJobMetrics(jobMetrics);
                    }
                });
    }

    private void setJobMetrics(final JobMetrics jobMetrics) {
        clear();

        final Button formulaMetricButton = new Button("Add metric formula");
        formulaMetricButton.setTitle("Add a formula of metrics, comprising multiple child metrics in a calculation?");
        formulaMetricButton.addStyleDependentName("ImageTextButton");
        formulaMetricButton.addStyleName("MetricFormulaButton");
        formulaMetricButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final DefineMetricPopup popup = new DefineMetricPopup(_tenantIdentifier, jobMetrics, true,
                        new DefineMetricPopup.Handler() {
                            @Override
                            public void onMetricDefined(MetricIdentifier metric) {
                                addFormulaMetric(jobMetrics, metric);
                            }
                        });
                popup.show();
            }
        });
        add(formulaMetricButton);

        final Label jobLabel = new Label("Showing available metrics from job '"
                + _timelineDefinition.getJobIdentifier().getName() + "':");
        jobLabel.setStyleName("JobInformationLabel");
        add(jobLabel);

        final List<MetricGroup> metricGroups = jobMetrics.getMetricGroups();

        // show only metric groups if there are not too many
        final boolean isMetricGroupVisibleByDefault;
        if (metricGroups.size() > 2) {
            isMetricGroupVisibleByDefault = false;
        } else {
            isMetricGroupVisibleByDefault = true;
        }

        for (MetricGroup metricGroup : metricGroups) {
            final FlowPanel metricGroupPanel = createMetricGroupPanel(metricGroup, isMetricGroupVisibleByDefault);
            add(metricGroupPanel);
        }

        // add formula metrics
        final List<MetricIdentifier> metrics = _timelineDefinition.getMetrics();
        for (MetricIdentifier metric : metrics) {
            if (metric.isFormulaBased()) {
                addFormulaMetric(jobMetrics, metric);
            }
        }

        onMetricsLoaded();
    }

    private void addFormulaMetric(JobMetrics jobMetrics, MetricIdentifier metric) {
        if (_formulaMetricsPanel == null) {
            _formulaMetricsPanel = new FlowPanel();
            _formulaMetricsPanel.addStyleName("FormulaMetricsPanel");
            final FlowPanel metricGroupPanel = createMetricGroupPanel("Metric formulas", _formulaMetricsPanel, null,
                    true);
            metricGroupPanel.addStyleName("FormulaMetricsGroupPanel");
            add(metricGroupPanel);
        }

        FormulaMetricPresenter presenter = new FormulaMetricPresenter(_tenantIdentifier, jobMetrics, metric);
        _metricPresenters.add(presenter);
        _formulaMetricsPanel.add(presenter);
    }

    /**
     * Overrideable method invoked when metrics have been loaded
     */
    protected void onMetricsLoaded() {
    }

    private FlowPanel createMetricGroupPanel(MetricGroup metricGroup, boolean isMetricGroupVisible) {
        final FlowPanel innerPanel = new FlowPanel();
        innerPanel.setStyleName("contents");

        final List<MetricIdentifier> activeMetrics = _timelineDefinition.getMetrics();
        final List<MetricIdentifier> availableMetrics = metricGroup.getMetrics();
        final MultipleColumnParameterizedMetricsPresenter columnParameterizedMetrics = new MultipleColumnParameterizedMetricsPresenter(
                metricGroup);
        for (MetricIdentifier metricIdentifier : availableMetrics) {
            final MetricPresenter presenter = createMetricPresenter(columnParameterizedMetrics, metricGroup,
                    metricIdentifier, activeMetrics);
            if (presenter != null) {
                final List<MetricIdentifier> selectedMetrics = presenter.getSelectedMetrics();
                if (selectedMetrics.size() > 0) {
                    isMetricGroupVisible = true;
                }
                _metricPresenters.add(presenter);
                innerPanel.add(presenter);
            }
        }

        if (!columnParameterizedMetrics.isEmpty()) {
            _metricPresenters.add(columnParameterizedMetrics);
            innerPanel.add(columnParameterizedMetrics);
        }

        final String title = metricGroup.getName();
        return createMetricGroupPanel(title, innerPanel, metricGroup, isMetricGroupVisible);
    }

    private FlowPanel createMetricGroupPanel(String title, final Panel innerPanel, MetricGroup metricGroup,
            boolean isMetricGroupVisibleByDefault) {
        final FlowPanel metricGroupPanel = new FlowPanel();
        metricGroupPanel.setStyleName("MetricGroupPanel");

        final HeadingLabel heading = new HeadingLabel(title);

        heading.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // toggle visibility
                boolean expanded = metricGroupPanel.getStyleName().indexOf("expanded") != -1;
                setExpanded(metricGroupPanel, !expanded);
            }
        });
        setExpanded(metricGroupPanel, isMetricGroupVisibleByDefault);
        metricGroupPanel.add(heading);
        metricGroupPanel.add(innerPanel);
        return metricGroupPanel;
    }

    private void setExpanded(FlowPanel metricGroupPanel, boolean visible) {
        if (visible) {
            metricGroupPanel.removeStyleDependentName("collapsed");
            metricGroupPanel.addStyleDependentName("expanded");
        } else {
            metricGroupPanel.removeStyleDependentName("expanded");
            metricGroupPanel.addStyleDependentName("collapsed");
        }
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

        // add single metrics
        for (MetricPresenter metricPresenter : _metricPresenters) {
            final List<MetricIdentifier> selectedMetrics = metricPresenter.getSelectedMetrics();
            metrics.addAll(selectedMetrics);
        }

        return metrics;
    }
}
