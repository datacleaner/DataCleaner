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

import java.util.Date;

import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.eobjects.datacleaner.monitor.scheduling.model.TriggerType;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.LoadingIndicator;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;
import org.eobjects.datacleaner.monitor.util.Urls;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
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

    @UiField
    Anchor resultAnchor;

    @UiField
    Label triggeredByLabel;

    @UiField(provided = true)
    LoadingIndicator loadingIndicator;

    public ExecutionLogPanel(SchedulingServiceAsync service, TenantIdentifier tenant, ExecutionLog executionLog) {
        super();

        _service = service;
        _tenant = tenant;
        loadingIndicator = new LoadingIndicator();

        initWidget(uiBinder.createAndBindUi(this));

        updateContent(executionLog);
    }

    private void updateContent(final ExecutionLog executionLog) {
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

            final String resultId = executionLog.getResultId();
            final String resultFilename = resultId + ".analysis.result.dat";
            final String url = Urls.createRelativeUrl("repository/" + _tenant.getId() + "/results/" + resultFilename);
            resultAnchor.setHref(url);
            resultAnchor.setTarget("_blank");
            resultAnchor.setText(resultId);
        }

        if (executionStatus == ExecutionStatus.SUCCESS) {
            resultAnchor.setVisible(true);
        } else {
            resultAnchor.setVisible(false);
        }

        GWT.log("Execution status: " + executionStatus);
        if (executionLog != null && !executionLog.isFinished()) {
            new Timer() {
                @Override
                public void run() {
                    _service.getExecution(_tenant, executionLog, new DCAsyncCallback<ExecutionLog>() {
                        @Override
                        public void onSuccess(ExecutionLog result) {
                            updateContent(result);
                        }
                    });
                }
            }.schedule(1000);
        } else {
            GWT.log("Hiding loading indicator. Execution status: " + executionStatus);
            loadingIndicator.setVisible(false);
        }
    }
}
