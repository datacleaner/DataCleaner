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

import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.model.HistoricExecution;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.TriggerType;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.HeadingLabel;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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

    public SchedulePanel(TenantIdentifier tenant, ScheduleDefinition schedule, SchedulingServiceAsync service) {
        super();

        initWidget(uiBinder.createAndBindUi(this));

        headerPanel.add(new HeadingLabel("Job: " + schedule.getJob().getName()));

        if (schedule.isActive()) {
            final JobIdentifier scheduleAfterJob = schedule.getScheduleAfterJob();
            if (scheduleAfterJob == null) {
                scheduleLabel.setText(schedule.getScheduleExpression());
            } else {
                scheduleLabel.setText("Runs after " + scheduleAfterJob.getName());
            }
        } else {
            scheduleLabel.setText("Not scheduled");
            scheduleLabel.addStyleName("discrete");
        }

        service.getLatestExecution(tenant, schedule.getJob(), new DCAsyncCallback<HistoricExecution>() {
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

                    sb.append(result.getJobBeginDate());

                    latestExecutionLabel.setText(sb.toString());
                }
            }
        });
    }

}
