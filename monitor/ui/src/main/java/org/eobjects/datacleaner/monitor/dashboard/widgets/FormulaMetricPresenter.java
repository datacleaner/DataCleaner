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

import org.eobjects.datacleaner.monitor.shared.model.JobMetrics;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.MetricAnchor;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Metric presenter for formula metrics
 */
public class FormulaMetricPresenter implements MetricPresenter {

    private final MetricAnchor _anchor;
    private final CheckBox _checkBox;

    public FormulaMetricPresenter(TenantIdentifier tenantIdentifier, JobMetrics jobMetrics, MetricIdentifier metric) {
        _checkBox = new CheckBox();
        _checkBox.setValue(true);
        _anchor = new MetricAnchor(tenantIdentifier, jobMetrics, metric);
    }

    @Override
    public Widget asWidget() {
        final FlowPanel panel = new FlowPanel();
        panel.addStyleName("FormulaMetricPresenter");
        panel.add(_checkBox);
        panel.add(_anchor);
        return panel;
    }

    @Override
    public List<MetricIdentifier> getSelectedMetrics() {
        final List<MetricIdentifier> metrics = new ArrayList<MetricIdentifier>();
        if (_checkBox.getValue().booleanValue()) {
            metrics.add(_anchor.getMetric());
        }
        return metrics;
    }

}
