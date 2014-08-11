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
package org.eobjects.datacleaner.monitor.scheduling.command;

import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.model.AlertDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.scheduling.widgets.CustomizeAlertPanel;
import org.eobjects.datacleaner.monitor.shared.DescriptorService;
import org.eobjects.datacleaner.monitor.shared.DescriptorServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobMetrics;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.CancelPopupButton;
import org.eobjects.datacleaner.monitor.shared.widgets.DCPopupPanel;
import org.eobjects.datacleaner.monitor.shared.widgets.DefineMetricPanel;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;

public class AddAlertCommand implements Command {
 
	private ScheduleDefinition _schedule;
	private SchedulingServiceAsync _service;
	private static final DescriptorServiceAsync descriptorService = GWT.create(DescriptorService.class);
	
	public AddAlertCommand(ScheduleDefinition schedule,SchedulingServiceAsync service) {
		_schedule = schedule;
		_service = service;
	}
	
	@Override
	public void execute() {
		final JobIdentifier job = _schedule.getJob();
        final TenantIdentifier tenant = _schedule.getTenant();

        descriptorService.getJobMetrics(tenant, job, new DCAsyncCallback<JobMetrics>() {
            @Override
            public void onSuccess(final JobMetrics jobMetrics) {
                final DefineMetricPanel defineMetricPanel = new DefineMetricPanel(tenant, jobMetrics, null, false);

                final DCPopupPanel popup = new DCPopupPanel("Create alert: Define metric to monitor");
                final Button nextButton = new Button("Next");
                nextButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        final MetricIdentifier metric = defineMetricPanel.getMetric();

                        final AlertDefinition alert = new AlertDefinition();
                        alert.setMetricIdentifier(metric);
                        final CustomizeAlertPanel customizeAlertPanel = new CustomizeAlertPanel(tenant, job, alert,
                                jobMetrics);

                        final Button saveButton = new Button("Save");
                        saveButton.addClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                popup.setHeader("Create alert: Select alerting criteria");
                                AlertDefinition alert = customizeAlertPanel.updateAlert();
                                _schedule.getAlerts().add(alert);
                                _service.updateSchedule(tenant, _schedule, new DCAsyncCallback<ScheduleDefinition>() {
                                    @Override
                                    public void onSuccess(ScheduleDefinition result) {
                                        GWT.log("Succesfully added alert in schedule: " + result);
                                        Window.Location.reload();
                                    }
                                });
                            }
                        });

                        popup.removeButton(nextButton);
                        popup.getButtonPanel().insert(saveButton, 0);
                        popup.setWidget(customizeAlertPanel);
                        popup.center();
                    }
                });

                popup.setWidget(defineMetricPanel);
                popup.addButton(nextButton);
                popup.addButton(new CancelPopupButton(popup));
                popup.center();
                popup.show();
            }
        });

	}

}