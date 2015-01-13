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

import java.util.List;

import org.datacleaner.monitor.dashboard.DashboardServiceAsync;
import org.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.monitor.shared.widgets.CancelPopupButton;
import org.datacleaner.monitor.shared.widgets.DCPopupPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Handler of the user interaction when customizing a timeline
 */
public class CustomizeTimelineHandler implements ClickHandler {

    private final DashboardServiceAsync _service;
    private final TimelinePanel _timelinePanel;

    private final DCPopupPanel _popup;

    private CustomizeMetricsPanel _customizeMetricsPanel;
    private CustomizeChartOptionsPanel _customizeChartOptionsPanel;

    public CustomizeTimelineHandler(DashboardServiceAsync service, TimelinePanel timelinePanel) {
        _service = service;
        _timelinePanel = timelinePanel;

        _popup = new DCPopupPanel("Customize timeline");
        _popup.addStyleName("CustomizeTimelinePopupPanel");
        _popup.addButton(createSaveButton());
        _popup.addButton(new CancelPopupButton(_popup));
    }

    @Override
    public void onClick(ClickEvent event) {
        showPopup();
    }

    public void showPopup() {
        _popup.setWidget(createPopupContent());
        _popup.center();
        _popup.show();
    }

    private Button createSaveButton() {
        final Button saveButton = new Button("Save");
        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final List<MetricIdentifier> selectedMetrics = _customizeMetricsPanel.getSelectedMetrics();
                if (selectedMetrics == null || selectedMetrics.isEmpty()) {
                    Window.alert("Please select one or more metrics to plot in the timeline");
                    return;
                }

                final TimelineDefinition timelineDefinition = new TimelineDefinition(true);
                timelineDefinition.setJobIdentifier(_timelinePanel.getTimelineDefinition().getJobIdentifier());
                timelineDefinition.setMetrics(selectedMetrics);
                timelineDefinition.setChartOptions(_customizeChartOptionsPanel.getChartOptions());

                _timelinePanel.setTimelineDefinition(timelineDefinition);
                _popup.hide();
            }
        });
        return saveButton;
    }

    private Widget createPopupContent() {
        _customizeMetricsPanel = new CustomizeMetricsPanel(_service, _timelinePanel.getTenantIdentifier(),
                _timelinePanel.getTimelineDefinition()) {
            @Override
            protected void onMetricsLoaded() {
                super.onMetricsLoaded();
                _popup.center();
            }
        };
        _customizeChartOptionsPanel = new CustomizeChartOptionsPanel(_timelinePanel.getTimelineDefinition()
                .getChartOptions());

        final TabPanel tabPanel = new TabPanel();
        tabPanel.add(_customizeMetricsPanel, "Metrics");
        tabPanel.add(_customizeChartOptionsPanel, "Chart options");
        tabPanel.selectTab(0);

        return tabPanel;
    }

}
