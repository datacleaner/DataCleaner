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
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.CancelPopupButton;
import org.eobjects.datacleaner.monitor.shared.widgets.DCPopupPanel;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * The {@link ClickHandler} invoked when the user clicks the 'trigger now'
 * button of a job.
 */
public class TriggerJobClickHandler implements ClickHandler {

    private final SchedulingServiceAsync _service;
    private final TenantIdentifier _tenant;
    private final ScheduleDefinition _schedule;

    public TriggerJobClickHandler(SchedulingServiceAsync service, TenantIdentifier tenant, ScheduleDefinition schedule) {
        _service = service;
        _tenant = tenant;
        _schedule = schedule;
    }

    public void showExecutionPopup() {
        final DCPopupPanel popupPanel = new DCPopupPanel("");

        final ExecutionStatusPanel panel = new ExecutionStatusPanel(_service, _tenant, _schedule, popupPanel);
        
        popupPanel.setAutoHideEnabled(false);
        popupPanel.setWidget(panel);
        popupPanel.addButton(new CancelPopupButton(popupPanel, "Close"));
        popupPanel.center();
        popupPanel.show();

        _service.triggerExecution(_tenant, _schedule.getJob(), new DCAsyncCallback<ExecutionLog>() {
            @Override
            public void onSuccess(final ExecutionLog result) {
                panel.jobStarted(result);
            }
        });
    }

    @Override
    public void onClick(ClickEvent event) {
        showExecutionPopup();
    }

}
