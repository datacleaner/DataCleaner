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

import org.eobjects.datacleaner.monitor.shared.model.MetricGroup;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents a list of {@link ColumnParameterizedMetricPresenter}s which all
 * take the same columns as parameters. Because of this they are grouped in a
 * single presenter to save space.
 */
public class MultipleColumnParameterizedMetricsPresenter implements MetricPresenter {

    private final List<ColumnParameterizedMetricPresenter> _children;
    private final MetricGroup _metricGroup;

    public MultipleColumnParameterizedMetricsPresenter(MetricGroup metricGroup) {
        _metricGroup = metricGroup;
        _children = new ArrayList<ColumnParameterizedMetricPresenter>();
    }

    @Override
    public Widget asWidget() {
        final FlowPanel panel = new FlowPanel();
        panel.addStyleName("MultipleColumnParameterizedMetricsPresenter");

        panel.add(createColumnHeaderWidget());

        for (ColumnParameterizedMetricPresenter child : _children) {
            panel.add(child);
        }

        return panel;
    }

    private Widget createColumnHeaderWidget() {
        final FlowPanel panel = new FlowPanel();
        panel.addStyleName("ColumnHeaders");

        // add empty label
        {
            final Label label = new Label();
            label.addStyleName("ColumnHeader");
            panel.add(label);
        }

        // add label for each column
        final List<String> columnNames = _metricGroup.getColumnNames();
        for (String columnName : columnNames) {
            final Label label = new Label(columnName);
            label.addStyleName("ColumnHeader");
            panel.add(label);
        }

        return panel;
    }

    @Override
    public List<MetricIdentifier> getSelectedMetrics() {
        final List<MetricIdentifier> result = new ArrayList<MetricIdentifier>();
        for (ColumnParameterizedMetricPresenter child : _children) {
            result.addAll(child.getSelectedMetrics());
        }
        return result;
    }

    public boolean isEmpty() {
        return _children.isEmpty();
    }

    public void add(ColumnParameterizedMetricPresenter child) {
        _children.add(child);
    }

}
