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
package org.eobjects.datacleaner.monitor.scheduling.widgets;

import java.util.List;

import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.model.AlertDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.HistoricExecution;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.TriggerType;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.CancelPopupButton;
import org.eobjects.datacleaner.monitor.shared.widgets.DCPopupPanel;
import org.eobjects.datacleaner.monitor.shared.widgets.HeadingLabel;
import org.eobjects.datacleaner.monitor.shared.widgets.LoadingIndicator;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel which presents a schedule
 */
public class SchedulePanel extends Composite {

    interface MyUiBinder extends UiBinder<Widget, SchedulePanel> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField
    FlowPanel headerPanel;

    @UiField
    Label scheduleLabel;

    @UiField
    Label latestExecutionLabel;

    @UiField
    Button triggerNowButton;
    
    @UiField
    FlowPanel alertsPanel;

    public SchedulePanel(final TenantIdentifier tenant, final ScheduleDefinition schedule,
            final SchedulingServiceAsync service) {
        super();

        initWidget(uiBinder.createAndBindUi(this));

        final JobIdentifier job = schedule.getJob();
        headerPanel.add(new HeadingLabel(job.getName()));

        scheduleLabel.setText(schedule.getScheduleSummary());
        if (!schedule.isActive()) {
            scheduleLabel.addStyleName("discrete");
        }

        triggerNowButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final DCPopupPanel popupPanel = new DCPopupPanel("Execute job");
                popupPanel.setWidget(new LoadingIndicator());
                popupPanel.addButton(new CancelPopupButton(popupPanel, "Close"));
                popupPanel.center();
                popupPanel.show();

                service.triggerExecution(tenant, job, new DCAsyncCallback<HistoricExecution>() {
                    @Override
                    public void onSuccess(HistoricExecution result) {
                        HistoricExecutionPanel panel = new HistoricExecutionPanel(result);
                        popupPanel.setWidget(panel);
                        popupPanel.center();
                    }
                });
            }
        });

        service.getLatestExecution(tenant, job, new DCAsyncCallback<HistoricExecution>() {
            @Override
            public void onSuccess(HistoricExecution result) {
                if (result == null) {
                    latestExecutionLabel.setText("Not available");
                    latestExecutionLabel.addStyleName("discrete");
                } else {
                    StringBuilder sb = new StringBuilder();

                    TriggerType triggerType = result.getTriggerType();
                    switch (triggerType) {
                    case MANUAL:
                        sb.append("Manually triggered: ");
                        break;
                    case SCHEDULED:
                        sb.append("Scheduled run: ");
                        break;
                    }

                    final DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);

                    sb.append(format.format(result.getJobBeginDate()));

                    latestExecutionLabel.setText(sb.toString());
                }
            }
        });
        
        final List<AlertDefinition> alerts = schedule.getAlerts();
        for (AlertDefinition alert : alerts) {
            AlertPanel alertPanel = new AlertPanel(alert);
            alertsPanel.add(alertPanel);
        }
    }

}
