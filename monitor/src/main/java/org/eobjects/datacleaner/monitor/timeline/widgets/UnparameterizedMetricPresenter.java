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

import org.eobjects.datacleaner.monitor.timeline.model.MetricIdentifier;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Metric presenters for simple, unparameterized metrics.
 */
public class UnparameterizedMetricPresenter implements MetricPresenter {

    private final MetricIdentifier _metricIdentifier;
    private final List<MetricIdentifier> _activeMetrics;
    private final CheckBox _checkBox;

    public UnparameterizedMetricPresenter(MetricIdentifier metricIdentifier, List<MetricIdentifier> activeMetrics) {
        _metricIdentifier = metricIdentifier;
        _activeMetrics = activeMetrics;

        _checkBox = new CheckBox(metricIdentifier.getDisplayName());
        if (isActiveMetric()) {
            _checkBox.setValue(true);
        } else {
            _checkBox.setValue(false);
        }
    }

    @Override
    public Widget asWidget() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName("UnparameterizedMetricsPresenter");
        panel.add(_checkBox);
        return panel;
    }

    @Override
    public List<MetricIdentifier> getSelectedMetrics() {
        List<MetricIdentifier> metrics = new ArrayList<MetricIdentifier>();
        if (_checkBox.getValue().booleanValue()) {
            metrics.add(_metricIdentifier);
        }
        return metrics;
    }

    private boolean isActiveMetric() {
        for (MetricIdentifier activeMetric : _activeMetrics) {
            if (activeMetric.equals(_metricIdentifier)) {
                return true;
            }
        }
        return false;
    }
}
