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
package org.eobjects.datacleaner.monitor.shared.widgets;

import org.eobjects.datacleaner.monitor.shared.model.JobMetrics;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

/**
 * Popup for defining a metric
 */
public class DefineMetricPopup extends DCPopupPanel {

    public static interface Handler {
        public void onMetricDefined(MetricIdentifier metric);
    }

    public DefineMetricPopup(final TenantIdentifier tenant, final JobMetrics jobMetrics, final boolean formulaOnly,
            final Handler handler) {
        this(tenant, jobMetrics, null, formulaOnly, handler);
    }

    public DefineMetricPopup(final TenantIdentifier tenant, final JobMetrics jobMetrics, final MetricIdentifier metric,
            final boolean formulaOnly, final Handler handler) {
        super("Define metric");

        final DefineMetricPanel panel = new DefineMetricPanel(tenant, jobMetrics, metric, formulaOnly);

        final Button saveButton = new Button("Save");
        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                MetricIdentifier metric = panel.getMetric();
                handler.onMetricDefined(metric);
                hide();
            }
        });

        setWidget(panel);

        getButtonPanel().add(saveButton);
        getButtonPanel().add(new CancelPopupButton(this));

        center();
        show();
    }

}
