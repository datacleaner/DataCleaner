/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.monitor.dashboard.widgets;

import java.util.ArrayList;
import java.util.List;

import org.datacleaner.monitor.dashboard.DashboardServiceAsync;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.monitor.shared.widgets.DCButtons;
import org.datacleaner.monitor.shared.widgets.StringParameterizedMetricTextBox;

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

    public final class MetricPanel extends FlowPanel {

        private final MetricIdentifier _metricToReturn;
        private final CheckBox _checkBox;
        private final StringParameterizedMetricTextBox _suggestBox;

        public MetricPanel(final MetricIdentifier metric) {
            super();
            addStyleName("StringParameterizedMetricPresenterMetricPanel");
            addStyleName("input-group");
            _checkBox = new CheckBox();
            _checkBox.addStyleName("input-group-addon");
            final MetricIdentifier activeMetric = isActiveMetric(metric);
            if (activeMetric == null) {
                _metricToReturn = _metricIdentifier.copy();
                _checkBox.setValue(false);
            } else {
                _metricToReturn = activeMetric;
                _checkBox.setValue(true);
            }
            _suggestBox = new StringParameterizedMetricTextBox(_tenantIdentifier, _jobIdentifier, _metricToReturn,
                    _metricToReturn.getParamQueryString(), _checkBox);
            add(_checkBox);
            add(_suggestBox);
        }

        public MetricIdentifier createMetricIdentifier() {
            _metricToReturn.setParamQueryString(_suggestBox.getText());
            return _metricToReturn;
        }

        public boolean isSelected() {
            return _checkBox.getValue().booleanValue();
        }
    }

    private final MetricIdentifier _metricIdentifier;
    private final List<MetricIdentifier> _activeMetrics;
    private final FlowPanel _panel;
    private final List<MetricPanel> _metricPanels;
    private final TenantIdentifier _tenantIdentifier;
    private final JobIdentifier _jobIdentifier;

    public StringParameterizedMetricPresenter(final TenantIdentifier tenantIdentifier,
            final JobIdentifier jobIdentifier, final MetricIdentifier metricIdentifier,
            final List<MetricIdentifier> activeMetrics, final DashboardServiceAsync service) {
        _tenantIdentifier = tenantIdentifier;
        _jobIdentifier = jobIdentifier;
        _metricIdentifier = metricIdentifier;
        _activeMetrics = activeMetrics;
        _metricPanels = new ArrayList<>();
        _panel = new FlowPanel();
        _panel.addStyleName("StringParameterizedMetricsPresenter");

        final Button addButton = DCButtons.defaultButton("glyphicon-plus", "Add");
        addButton.addStyleName("StringParameterizedMetricPresenterAddButton");
        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                addMetricPanel(_metricIdentifier);
            }
        });

        _panel.add(new Label(_metricIdentifier.getMetricDescriptorName() + ":"));
        _panel.add(addButton);

        for (final MetricIdentifier activeMetric : activeMetrics) {
            if (activeMetric.equalsIgnoreParameterValues(_metricIdentifier)) {
                addMetricPanel(activeMetric);
            }
        }

        if (_metricPanels.isEmpty()) {
            addMetricPanel(_metricIdentifier);
        }
    }

    private void addMetricPanel(final MetricIdentifier metric) {
        final MetricPanel widget = new MetricPanel(metric);
        _panel.add(widget);
        _metricPanels.add(widget);
    }

    private MetricIdentifier isActiveMetric(final MetricIdentifier metric) {
        for (final MetricIdentifier activeMetric : _activeMetrics) {
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
        final List<MetricIdentifier> result = new ArrayList<>();
        for (final MetricPanel panel : _metricPanels) {
            if (panel.isSelected()) {
                final MetricIdentifier metricIdentifier = panel.createMetricIdentifier();
                result.add(metricIdentifier);
            }
        }
        return result;
    }

}
