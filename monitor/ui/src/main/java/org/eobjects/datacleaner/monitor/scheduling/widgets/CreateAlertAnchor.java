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
import org.eobjects.datacleaner.monitor.scheduling.model.AlertDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.CancelPopupButton;
import org.eobjects.datacleaner.monitor.shared.widgets.DCPopupPanel;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;

/**
 * Anchor for creating a new alert.
 */
public class CreateAlertAnchor extends Anchor implements ClickHandler {

    private final SchedulingServiceAsync _service;
    private final ScheduleDefinition _schedule;

    public CreateAlertAnchor(SchedulingServiceAsync service, ScheduleDefinition schedule) {
        super("Create alert");
        _service = service;
        _schedule = schedule;
        addStyleName("CreateAlertAnchor");
        addClickHandler(this);
    }

    @Override
    public void onClick(ClickEvent event) {
        final JobIdentifier job = _schedule.getJob();
        final TenantIdentifier tenant = _schedule.getTenant();

        final CustomizeAlertPanel panel = new CustomizeAlertPanel(tenant, job, new AlertDefinition());

        final DCPopupPanel popup = new DCPopupPanel("Create alert");

        final Button button = new Button("Save alert");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AlertDefinition alert = panel.updateAlert();
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

        popup.setWidget(panel);
        popup.addButton(button);
        popup.addButton(new CancelPopupButton(popup));
        popup.center();
        popup.show();
    }
}
