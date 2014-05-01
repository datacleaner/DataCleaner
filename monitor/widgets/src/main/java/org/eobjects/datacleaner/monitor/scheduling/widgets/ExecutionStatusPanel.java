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

import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.scheduling.widgets.ExecutionLogPoller.Callback;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.DCPopupPanel;
import org.eobjects.datacleaner.monitor.shared.widgets.HeadingLabel;
import org.eobjects.datacleaner.monitor.shared.widgets.LoadingIndicator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Simple panel for showing the status of a job, ie. through
 * {@link ExecutionLog} monitoring.
 */
public class ExecutionStatusPanel extends FlowPanel {

    private final LoadingIndicator _loadingIndicator;
    private final TenantIdentifier _tenant;
    private final ScheduleDefinition _schedule;
    private final SchedulingServiceAsync _service;
    private final DCPopupPanel _popupPanel;
    private final HeadingLabel _headerLabel;
    private final Anchor _detailsLink;

    private ExecutionLog _result;
    private ExecutionLogPanel _logPanel;

    public ExecutionStatusPanel(SchedulingServiceAsync service, TenantIdentifier tenant, ScheduleDefinition schedule,
            DCPopupPanel popupPanel) {
        super();
        _service = service;
        _tenant = tenant;
        _schedule = schedule;
        _popupPanel = popupPanel;
        _loadingIndicator = new LoadingIndicator();

        addStyleName("ExecutionStatusPanel");

        _headerLabel = new HeadingLabel("Job '" + schedule.getJob().getName() + "' is executing...");
        add(_headerLabel);
        add(_loadingIndicator);

        _detailsLink = new Anchor("Show execution details");
        _detailsLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showLogPanel();
            }
        });
        add(_detailsLink);
    }

    public void jobStarted(ExecutionLog execution) {
        final ExecutionLogPoller poller = new ExecutionLogPoller(_service, _tenant, new Callback() {
            @Override
            public void updateExecutionLog(ExecutionLog executionLog) {
                jobStatusUpdated(executionLog);
            }
        });
        poller.start(execution);
    }

    public void jobStatusUpdated(ExecutionLog executionLog) {
        _result = executionLog;
        if (_logPanel != null) {
            _logPanel.updateContent(executionLog);
        }

        if (executionLog != null) {
            if (executionLog.isFinished()) {
                if (executionLog.getExecutionStatus() == ExecutionStatus.SUCCESS) {
                    _loadingIndicator.setVisible(false);
                    _detailsLink.setVisible(false);

                    _headerLabel.setText("Job finished!");

                    final ResultAnchor resultAnchor = new ResultAnchor(_tenant);
                    resultAnchor.setResult(executionLog, "Show result");
                    add(resultAnchor);
                } else {
                    // use the log panel to show errors etc.
                    showLogPanel();
                }
            }
        }
    }

    public void showLogPanel() {
        _logPanel = new ExecutionLogPanel(_service, _tenant, _result, false);
        _popupPanel.setHeader("Execution log: '" + _schedule.getJob().getName() + "'");
        _popupPanel.setWidget(_logPanel);
        _popupPanel.center();
    }
}
