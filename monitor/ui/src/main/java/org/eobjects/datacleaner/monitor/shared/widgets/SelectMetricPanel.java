package org.eobjects.datacleaner.monitor.shared.widgets;

import org.eobjects.datacleaner.monitor.shared.model.JobMetrics;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A panel for selecting a metric.
 */
public class SelectMetricPanel extends FlowPanel {

    private final TextBox _displayNameBox;
    private final JobMetrics _metrics;
    private MetricIdentifier _metric;

    public SelectMetricPanel(JobMetrics metrics, MetricIdentifier metric, boolean displayNameVisible) {
        super();
        _displayNameBox = new TextBox();
        _displayNameBox.setVisible(displayNameVisible);
        _metric = metric;
        _metrics = metrics;

        add(new Label("Name:"));
        add(_displayNameBox);
    }

    public boolean isDisplayNameVisible() {
        return _displayNameBox.isVisible();
    }

    public void setDisplayNameVisible(boolean visible) {
        _displayNameBox.setVisible(visible);
    }

    public MetricIdentifier getMetric() {
        MetricIdentifier copy = _metric.copy();
        if (isDisplayNameVisible()) {
            String displayName = _displayNameBox.getValue();
            copy.setMetricDisplayName(displayName);
        }
        return copy;
    }

}
