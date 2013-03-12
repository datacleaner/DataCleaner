/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import org.eobjects.datacleaner.monitor.shared.model.MetricGroup;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Presenter for metrics that are parameterizable by column names.
 */
public class ColumnParameterizedMetricPresenter implements MetricPresenter {

    private final MetricIdentifier _metricIdentifier;
    private final List<MetricIdentifier> _activeMetrics;
    private final MetricGroup _metricGroup;
    private final FlowPanel _panel;
    private final List<MetricIdentifier> _selectedMetrics;

    public ColumnParameterizedMetricPresenter(MetricIdentifier metricIdentifier, List<MetricIdentifier> activeMetrics,
            MetricGroup metricGroup) {
        _metricIdentifier = metricIdentifier;
        _activeMetrics = activeMetrics;
        _metricGroup = metricGroup;
        _selectedMetrics = new ArrayList<MetricIdentifier>();
        _panel = new FlowPanel();
        _panel.addStyleName("ColumnParameterizedMetricsPresenter");

        _panel.add(new Label(_metricIdentifier.getMetricDescriptorName() + ":"));

        final List<String> columnNames = _metricGroup.getColumnNames();
        for (String columnName : columnNames) {
            // create a copy of the metric for each column name
            MetricIdentifier clone = _metricIdentifier.copy();
            clone.setParamColumnName(columnName);
            Widget widget = createMetricWidget(clone);
            _panel.add(widget);
        }
    }

    private Widget createMetricWidget(final MetricIdentifier metric) {
        final MetricIdentifier activeMetric = isActiveMetric(metric);
        final MetricIdentifier metricToReturn;
        if (activeMetric == null) {
            metricToReturn = metric;
        } else {
            metricToReturn = activeMetric;
        }
        
        final CheckBox checkBox = new CheckBox();
        checkBox.setTitle(metric.getDisplayName());
        checkBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (checkBox.getValue().booleanValue()) {
                    _selectedMetrics.add(metricToReturn);
                } else {
                    _selectedMetrics.remove(metricToReturn);
                }
            }
        });
        
        if (activeMetric == null) {
            checkBox.setValue(false);
        } else {
            checkBox.setValue(true);
            _selectedMetrics.add(metricToReturn);
        }
        return checkBox;
    }

    private MetricIdentifier isActiveMetric(MetricIdentifier metric) {
        for (MetricIdentifier activeMetric : _activeMetrics) {
            if (activeMetric.equalsIgnoreCustomizedDetails(metric)) {
                return activeMetric;
            }
        }
        return null;
    }

    @Override
    public Widget asWidget() {
        return _panel;
    }

    @Override
    public List<MetricIdentifier> getSelectedMetrics() {
        return _selectedMetrics;
    }

}
