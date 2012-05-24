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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Presenter for metrics that are parameterizable by a user defined string.
 */
public class StringParameterizedMetricPresenter implements MetricPresenter {

    private final MetricIdentifier _metricIdentifier;
    private final List<MetricIdentifier> _activeMetrics;
    private final FlowPanel _panel;
    private final List<MetricPanel> _selectedMetricPanels;

    public final class MetricPanel extends FlowPanel {
        private final CheckBox _checkBox;
        private final StringParameterizedMetricTextBox _suggestBox;

        public MetricPanel(final MetricIdentifier metric) {
            super();
            addStyleName("StringParameterizedMetricPresenterMetricPanel");
            _checkBox = new CheckBox();
            _checkBox.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (_checkBox.getValue().booleanValue()) {
                        _selectedMetricPanels.add(MetricPanel.this);
                    } else {
                        _selectedMetricPanels.remove(MetricPanel.this);
                    }
                }
            });
            if (isActiveMetric(metric)) {
                _checkBox.setValue(true);
                _selectedMetricPanels.add(MetricPanel.this);
            } else {
                _checkBox.setValue(false);
            }
            _suggestBox = new StringParameterizedMetricTextBox(metric.getParamQueryString());
            add(_checkBox);
            add(_suggestBox);
        }

        public MetricIdentifier createMetricIdentifier() {
            MetricIdentifier copy = _metricIdentifier.copy();
            copy.setParamQueryString(_suggestBox.getText());
            return copy;
        }
    }

    public StringParameterizedMetricPresenter(MetricIdentifier metricIdentifier, List<MetricIdentifier> activeMetrics) {
        _metricIdentifier = metricIdentifier;
        _activeMetrics = activeMetrics;
        _selectedMetricPanels = new ArrayList<MetricPanel>();
        _panel = new FlowPanel();
        _panel.addStyleName("StringParameterizedMetricsPresenter");

        final Button addButton = new Button("Add");
        addButton.addStyleName("StringParameterizedMetricPresenterAddButton");
        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addMetricPanel(_metricIdentifier);
            }
        });

        _panel.add(new Label(_metricIdentifier.getMetricDescriptorName() + ":"));
        _panel.add(addButton);

        for (MetricIdentifier activeMetric : activeMetrics) {
            if (activeMetric.equalsIgnoreParameterValues(_metricIdentifier)) {
                addMetricPanel(activeMetric);
            }
        }

        if (_selectedMetricPanels.isEmpty()) {
            addMetricPanel(_metricIdentifier);
        }
    }

    private void addMetricPanel(MetricIdentifier metric) {
        Widget widget = new MetricPanel(metric);
        _panel.add(widget);
    }

    private boolean isActiveMetric(MetricIdentifier metric) {
        for (MetricIdentifier activeMetric : _activeMetrics) {
            if (activeMetric.equals(metric)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Widget asWidget() {
        return _panel;
    }

    @Override
    public List<MetricIdentifier> getSelectedMetrics() {
        List<MetricIdentifier> result = new ArrayList<MetricIdentifier>();
        for (MetricPanel panel : _selectedMetricPanels) {
            MetricIdentifier metricIdentifier = panel.createMetricIdentifier();
            result.add(metricIdentifier);
        }
        return result;
    }

}
