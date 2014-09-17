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
package org.eobjects.datacleaner.monitor.dashboard.widgets;

import java.util.List;

import org.eobjects.datacleaner.monitor.dashboard.DashboardServiceAsync;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.CancelPopupButton;
import org.eobjects.datacleaner.monitor.shared.widgets.DCPopupPanel;
import org.eobjects.datacleaner.monitor.shared.widgets.HeadingLabel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Handles the action that the user wants to create a new timeline.
 */
public class CreateTimelineHandler implements ClickHandler {

    private final DashboardGroupPanel _groupPanel;
    private final DashboardServiceAsync _service;
    private final TenantIdentifier _tenant;

    public CreateTimelineHandler(DashboardServiceAsync service, TenantIdentifier tenant,
            DashboardGroupPanel groupPanel) {
        _service = service;
        _tenant = tenant;
        _groupPanel = groupPanel;
    }

    @Override
    public void onClick(ClickEvent event) {
        final DCPopupPanel popup = createPopup();

        final SelectJobPanel selectJobPanel = new SelectJobPanel(_service, _tenant) {
            @Override
            public void onJobSelected(JobIdentifier job) {
                setJob(popup, job);
            }
        };

        popup.setWidget(selectJobPanel);
        popup.addButton(selectJobPanel.createSelectButton());
        popup.addButton(new CancelPopupButton(popup));
        popup.center();
        popup.show();
    }

    protected DCPopupPanel createPopup() {
        final DCPopupPanel popup = new DCPopupPanel("Create timeline");
        popup.addStyleName("CreateTimelinePopupPanel");
        return popup;
    }

    protected void setJob(final DCPopupPanel popup, final JobIdentifier job) {
        final TimelineDefinition timelineDefinition = new TimelineDefinition();
        timelineDefinition.setJobIdentifier(job);

        final TimelinePanel timelinePanel = new TimelinePanel(_tenant, _service, null, _groupPanel, true);
        timelinePanel.setTimelineDefinition(timelineDefinition, false);

        final CustomizeMetricsPanel customizeMetricsPanel = new CustomizeMetricsPanel(_service, _tenant,
                timelineDefinition) {
            @Override
            protected void onMetricsLoaded() {
                super.onMetricsLoaded();
                popup.center();
            }
        };

        final Button saveButton = new Button("Save");
        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final List<MetricIdentifier> selectedMetrics = customizeMetricsPanel.getSelectedMetrics();
                timelineDefinition.setMetrics(selectedMetrics);
                timelinePanel.setTimelineDefinition(timelineDefinition);
                _groupPanel.addTimelinePanel(timelinePanel);
                popup.hide();
            }
        });

        final FlowPanel mainPanel = new FlowPanel();
        mainPanel.add(new HeadingLabel("Select metrics to monitor"));
        mainPanel.add(customizeMetricsPanel);
        popup.setWidget(mainPanel);
        popup.removeButtons();
        popup.addButton(saveButton);
        popup.addButton(new CancelPopupButton(popup));

        popup.center();
    }
}
