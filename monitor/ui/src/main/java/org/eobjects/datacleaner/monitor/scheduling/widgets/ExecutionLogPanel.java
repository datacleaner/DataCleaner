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

import java.util.Date;

import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.eobjects.datacleaner.monitor.scheduling.model.TriggerType;
import org.eobjects.datacleaner.monitor.scheduling.widgets.ExecutionLogPoller.Callback;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.LoadingIndicator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel which presents a {@link ExecutionLog}
 */
public class ExecutionLogPanel extends Composite {

    interface MyUiBinder extends UiBinder<Widget, ExecutionLogPanel> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private final SchedulingServiceAsync _service;
    private final TenantIdentifier _tenant;

    @UiField
    Label statusLabel;

    @UiField
    Label beginTimeLabel;

    @UiField
    Label endTimeLabel;

    @UiField
    Label triggerLabel;

    @UiField
    Label logOutputLabel;

    @UiField(provided = true)
    ResultAnchor resultAnchor;

    @UiField
    Label triggeredByLabel;

    @UiField(provided = true)
    LoadingIndicator loadingIndicator;

    public ExecutionLogPanel(SchedulingServiceAsync service, TenantIdentifier tenant, ExecutionLog executionLog,
            boolean pollForUpdates) {
        super();

        _service = service;
        _tenant = tenant;

        loadingIndicator = new LoadingIndicator();
        resultAnchor = new ResultAnchor(tenant);

        initWidget(uiBinder.createAndBindUi(this));

        updateContent(executionLog);

        if (pollForUpdates) {
            final ExecutionLogPoller poller = new ExecutionLogPoller(_service, _tenant, new Callback() {
                @Override
                public void updateExecutionLog(ExecutionLog executionLog) {
                    updateContent(executionLog);
                }
            });
            poller.start(executionLog);
        }
    }

    public void updateContent(final ExecutionLog executionLog) {
        final ExecutionStatus executionStatus;
        if (executionLog == null) {
            executionStatus = ExecutionStatus.UNKNOWN;
        } else {
            executionStatus = executionLog.getExecutionStatus();
            statusLabel.setText(executionStatus.toString());

            final DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);

            final Date beginDate = executionLog.getJobBeginDate();
            if (beginDate == null) {
                beginTimeLabel.setText("not available");
                beginTimeLabel.addStyleName("discrete");
            } else {
                beginTimeLabel.setText(format.format(beginDate));
            }

            final Date endDate = executionLog.getJobEndDate();
            if (endDate == null) {
                endTimeLabel.setText("not available");
                endTimeLabel.addStyleName("discrete");
            } else {
                endTimeLabel.setText(format.format(endDate));
            }

            final TriggerType triggerType = executionLog.getTriggerType();
            switch (triggerType) {
            case PERIODIC:
                triggerLabel.setText("Scheduled: Periodic '" + executionLog.getSchedule().getCronExpression() + "'");
                break;
            case DEPENDENT:
                triggerLabel.setText("Scheduled: After '" + executionLog.getSchedule().getDependentJob().getName()
                        + "'");
                break;
            case MANUAL:
                triggerLabel.setText("Manually triggered");
                break;
            }

            triggeredByLabel.setText(executionLog.getTriggeredBy());

            logOutputLabel.setText(executionLog.getLogOutput());
            
            resultAnchor.setResult(executionLog);
        }

        if (executionStatus == ExecutionStatus.SUCCESS) {
            resultAnchor.setVisible(true);
        } else {
            resultAnchor.setVisible(false);
        }

        GWT.log("Execution status: " + executionStatus);
        if (executionLog != null && executionLog.isFinished()) {
            GWT.log("Hiding loading indicator. Execution status: " + executionStatus);
            loadingIndicator.setVisible(false);
        }
    }
}
