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
package org.eobjects.datacleaner.monitor.scheduling.widgets;

import java.util.List;

import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.model.AlertDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.TriggerType;
import org.eobjects.datacleaner.monitor.shared.ClientConfig;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.DropDownAnchor;
import org.eobjects.datacleaner.monitor.util.Urls;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
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

    private final ScheduleDefinition _schedule;
    private final ClientConfig _clientConfig;

    @UiField
    DropDownAnchor jobLabel;

    @UiField
    Anchor scheduleAnchor;

    @UiField
    Button triggerNowButton;

    @UiField
    Button launchButton;

    @UiField
    Button historyButton;

    @UiField
    FlowPanel alertsPanel;

    public SchedulePanel(final ClientConfig clientConfig, final ScheduleDefinition schedule, final SchedulingServiceAsync service) {
        super();

        _clientConfig = clientConfig;
        _schedule = schedule;

        initWidget(uiBinder.createAndBindUi(this));

        // add the job type as a style name
        final String jobType = schedule.getJob().getType();
        if (jobType != null) {
            addStyleName(jobType);
        }

        final boolean analysisJob = JobIdentifier.JOB_TYPE_ANALYSIS_JOB.equals(jobType);

        updateScheduleWidgets();

        final TenantIdentifier tenant = _clientConfig.getTenant();

        final String encodedJobName = URL.encodeQueryString(schedule.getJob().getName());

        if (_clientConfig.isJobEditor()) {
            jobLabel.addClickHandler(new CustomizeJobClickHandler(this, tenant));
        }

        if (_clientConfig.isScheduleEditor()) {
            CustomizeScheduleClickHandler handler = new CustomizeScheduleClickHandler(this, service, tenant, schedule);
            scheduleAnchor.addClickHandler(handler);

            final String token = History.getToken();
            GWT.log("Encoded job name: " + encodedJobName);
            if (("schedule_" + encodedJobName).equals(token)) {
                History.newItem("");
                handler.showSchedulingPopup();
            }
        }

        if (!_clientConfig.isJobEditor()) {
            alertsPanel.setVisible(false);
        }

        if (_clientConfig.isScheduleEditor()) {
            TriggerJobClickHandler handler = new TriggerJobClickHandler(service, tenant, _schedule);
            triggerNowButton.addClickHandler(handler);

            final String token = History.getToken();
            if (("trigger_" + encodedJobName).equals(token)) {
                History.newItem("");
                handler.showExecutionPopup();
            }
        }

        if (_clientConfig.isJobEditor() && analysisJob) {
            launchButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    String url = Urls.createRepositoryUrl(tenant, "jobs/" + schedule.getJob().getName() + ".launch.jnlp");
                    Window.open(url, "_blank", null);
                }
            });
        } else {
            launchButton.setVisible(false);
        }

        historyButton.addClickHandler(new JobHistoryClickHandler(service, tenant, schedule));

        final List<AlertDefinition> alerts = schedule.getAlerts();
        final Anchor expandAlertsAnchor = new Anchor(alerts.size() + " alert(s)");
        if (alerts.isEmpty()) {
            expandAlertsAnchor.addStyleName("discrete");
        }
        expandAlertsAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final FlowPanel alertListPanel = new FlowPanel();
                alertListPanel.setStyleName("AlertListPanel");

                if (alerts.isEmpty()) {
                    Label label = new Label("(no alerts)");
                    label.setStylePrimaryName("AlertPanel");
                    alertListPanel.add(label);
                }
                for (AlertDefinition alert : alerts) {
                    AlertPanel alertPanel = new AlertPanel(service, schedule, alert);
                    alertListPanel.add(alertPanel);
                }

                alertsPanel.clear();
                alertsPanel.add(new CreateAlertAnchor(service, schedule));
                alertsPanel.add(alertListPanel);
            }
        });
        alertsPanel.add(expandAlertsAnchor);
    }

    public ScheduleDefinition getSchedule() {
        return _schedule;
    }

    public void updateScheduleWidgets() {
        final JobIdentifier job = _schedule.getJob();
        jobLabel.setText(job.getName());

        final TriggerType triggerType = _schedule.getTriggerType();
        switch (triggerType) {
        case PERIODIC:
            scheduleAnchor.setText(_schedule.getCronExpression());
            scheduleAnchor.removeStyleName("discrete");
            break;
        case DEPENDENT:
            scheduleAnchor.setText(_schedule.getDependentJob().getName());
            scheduleAnchor.removeStyleName("discrete");
            break;
        case MANUAL:
            scheduleAnchor.setText("Manually triggered");
            scheduleAnchor.addStyleName("discrete");
            break;
        }

        scheduleAnchor.setStyleName("TriggerAnchor-" + triggerType);
    }

}
