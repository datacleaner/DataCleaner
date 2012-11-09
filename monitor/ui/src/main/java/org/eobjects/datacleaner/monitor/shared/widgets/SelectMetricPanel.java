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
package org.eobjects.datacleaner.monitor.shared.widgets;

import java.util.List;

import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.shared.model.JobMetrics;
import org.eobjects.datacleaner.monitor.shared.model.MetricGroup;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A panel for selecting a metric.
 */
public class SelectMetricPanel extends FlowPanel {

    private final TenantIdentifier _tenant;
    private final Label _displayNameLabel;
    private final TextBox _displayNameBox;
    private final JobMetrics _jobMetrics;

    private final ListBox _metricGroupSelectionBox;
    private final ListBox _metricSelectionBox;
    private final ListBox _columnParameterSelectionBox;
    private final StringParameterizedMetricTextBox _queryParameterTextBox;

    public SelectMetricPanel(TenantIdentifier tenant, JobMetrics jobMetrics, MetricIdentifier existingMetric,
            boolean displayNameVisible) {
        super();
        addStyleName("SelectMetricPanel");
        _tenant = tenant;
        _displayNameLabel = new Label("Name:");
        _displayNameBox = new TextBox();
        _displayNameBox.setStyleName("DisplayNameBox");
        _jobMetrics = jobMetrics;

        setDisplayNameVisible(displayNameVisible);

        add(_displayNameLabel);
        add(_displayNameBox);

        _metricGroupSelectionBox = new ListBox();
        add(_metricGroupSelectionBox);

        _metricSelectionBox = new ListBox();
        _metricSelectionBox.setVisible(false);
        add(_metricSelectionBox);

        _columnParameterSelectionBox = new ListBox();
        _columnParameterSelectionBox.setVisible(false);
        add(_columnParameterSelectionBox);

        _queryParameterTextBox = new StringParameterizedMetricTextBox(_tenant, _jobMetrics.getJob(), existingMetric,
                "", null);
        _queryParameterTextBox.setVisible(false);
        add(_queryParameterTextBox);

        _metricGroupSelectionBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                _metricSelectionBox.setVisible(false);

                final MetricGroup group = getSelectedMetricGroup();
                if (group == null) {
                    return;
                }

                final List<MetricIdentifier> metrics = group.getMetrics();

                _metricSelectionBox.clear();
                _metricSelectionBox.addItem("(Select metric)");
                for (MetricIdentifier metric : metrics) {
                    _metricSelectionBox.addItem(metric.getMetricDescriptorName());
                }

                _metricSelectionBox.setVisible(true);
            }
        });

        _metricSelectionBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                _columnParameterSelectionBox.clear();
                _columnParameterSelectionBox.setVisible(false);
                _queryParameterTextBox.setVisible(false);

                final MetricIdentifier metric = getSelectedMetric();
                if (metric == null) {
                    return;
                }

                if (metric.isParameterizedByColumnName()) {
                    _columnParameterSelectionBox.setVisible(true);
                    List<String> columnNames = getSelectedMetricGroup().getColumnNames();
                    for (String columnName : columnNames) {
                        _columnParameterSelectionBox.addItem(columnName);
                    }
                } else if (metric.isParameterizedByQueryString()) {
                    _queryParameterTextBox.setMetric(_tenant, _jobMetrics.getJob(), metric);
                    _queryParameterTextBox.setVisible(true);
                }
            }
        });

        final List<MetricGroup> metricGroups = _jobMetrics.getMetricGroups();
        _metricGroupSelectionBox.addItem("(Select metric group)");
        for (MetricGroup metricGroup : metricGroups) {
            _metricGroupSelectionBox.addItem(metricGroup.getName());
        }

        if (existingMetric == null) {
            // initialize metric selection if possible
            if (metricGroups.size() == 1) {
                final MetricGroup group = metricGroups.get(0);
                selectItem(_metricGroupSelectionBox, group.getName());

                final List<MetricIdentifier> metrics = group.getMetrics();
                if (metrics.size() == 1) {
                    selectItem(_metricSelectionBox, metrics.get(0).getMetricDescriptorName());
                }
            }
        } else {
            // set input as the existing metric
            _displayNameBox.setText(existingMetric.getDisplayName());
            final MetricGroup group = _jobMetrics.getMetricGroup(existingMetric);
            if (group == null) {
                return;
            }
            selectItem(_metricGroupSelectionBox, group.getName());
            selectItem(_metricSelectionBox, existingMetric.getMetricDescriptorName());
            if (existingMetric.isParameterizedByColumnName()) {
                selectItem(_columnParameterSelectionBox, existingMetric.getParamColumnName());
            } else if (existingMetric.isParameterizedByQueryString()) {
                _queryParameterTextBox.setText(existingMetric.getParamQueryString());
            }
        }
    }

    private void selectItem(ListBox listBox, String itemText) {
        int itemCount = listBox.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            String text = listBox.getItemText(i);
            if (text.equals(itemText)) {
                listBox.setSelectedIndex(i);
                DomEvent.fireNativeEvent(Document.get().createChangeEvent(), listBox);
                return;
            }
        }
    }

    private MetricIdentifier getSelectedMetric() throws DCUserInputException {
        final MetricGroup group = getSelectedMetricGroup();

        final int index = _metricSelectionBox.getSelectedIndex();
        if (index == -1 || index == 0) {
            throw new DCUserInputException("No metric selected");
        }

        final String metricName = _metricSelectionBox.getItemText(index);
        final MetricIdentifier metric = group.getMetric(metricName);
        return metric;
    }

    public boolean isDisplayNameVisible() {
        return _displayNameBox.isVisible();
    }

    public void setDisplayNameVisible(boolean visible) {
        _displayNameBox.setVisible(visible);
        _displayNameLabel.setVisible(visible);
    }

    public void setDisplayName(String string) {
        _displayNameBox.setText(string);
    }

    public String getDisplayName() {
        return _displayNameBox.getText();
    }

    public MetricIdentifier getMetric() throws DCUserInputException {
        final MetricIdentifier selectedMetric = getSelectedMetric();
        final MetricIdentifier copy = selectedMetric.copy();

        if (isDisplayNameVisible()) {
            copy.setMetricDisplayName(getDisplayName());
        }

        if (copy.isParameterizedByColumnName()) {
            String columnName = _columnParameterSelectionBox.getItemText(_columnParameterSelectionBox
                    .getSelectedIndex());
            copy.setParamColumnName(columnName);
        } else if (copy.isParameterizedByQueryString()) {
            copy.setParamQueryString(_queryParameterTextBox.getText());
        }
        return copy;
    }

    private MetricGroup getSelectedMetricGroup() {
        int index = _metricGroupSelectionBox.getSelectedIndex();
        if (index == -1 || index == 0) {
            throw new DCUserInputException("No metric group selected");
        }
        final String groupName = _metricGroupSelectionBox.getItemText(index);
        final MetricGroup group = _jobMetrics.getMetricGroup(groupName);
        return group;
    }

}
