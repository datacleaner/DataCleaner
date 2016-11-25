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
package org.datacleaner.monitor.shared.widgets;

import org.datacleaner.monitor.shared.model.JobMetrics;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * An anchor used to select or define a metric (either single or formula based),
 * eg. for alerting or inclusion in a timeline.
 */
public class MetricAnchor extends DropDownAnchor implements ClickHandler {

    private final TenantIdentifier _tenant;
    private JobMetrics _jobMetrics;
    private MetricIdentifier _metric;

    public MetricAnchor(final TenantIdentifier tenant) {
        this(tenant, null, null);
    }

    public MetricAnchor(final TenantIdentifier tenant, final JobMetrics jobMetrics, final MetricIdentifier metric) {
        super();
        _tenant = tenant;
        addStyleName("MetricAnchor");
        addClickHandler(this);
        setJobMetrics(jobMetrics);
        setMetric(metric);
    }

    public MetricIdentifier getMetric() {
        return _metric;
    }

    public void setMetric(final MetricIdentifier metric) {
        _metric = metric;
        updateText();
    }

    public JobMetrics getJobMetrics() {
        return _jobMetrics;
    }

    public void setJobMetrics(final JobMetrics jobMetrics) {
        _jobMetrics = jobMetrics;
    }

    private void updateText() {
        if (_metric == null || _metric.getDisplayName() == null || "".equals(_metric.getDisplayName())) {
            setText("(select metric)");
        } else {
            setText(_metric.getDisplayName());
        }
    }

    @Override
    public void onClick(final ClickEvent event) {
        if (_jobMetrics == null) {
            GWT.log("No JobMetrics available");
            return;
        }

        final DefineMetricPopup popup =
                new DefineMetricPopup(_tenant, _jobMetrics, _metric, false, new DefineMetricPopup.Handler() {
                    @Override
                    public void onMetricDefined(final MetricIdentifier metric) {
                        setMetric(metric);
                    }
                });
        popup.show();
    }
}
