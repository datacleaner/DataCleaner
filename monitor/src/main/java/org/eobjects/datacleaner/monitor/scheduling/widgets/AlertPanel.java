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
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Presents an alert to the user.
 */
public class AlertPanel extends FlowPanel {

    private final ScheduleDefinition _schedule;
    private final SchedulingServiceAsync _service;
    private final AlertDefinition _alert;
    private final Anchor _anchor;

    public AlertPanel(SchedulingServiceAsync service, ScheduleDefinition schedule, AlertDefinition alert) {
        super();
        addStyleName("AlertPanel");
        _service = service;
        _schedule = schedule;
        _alert = alert;
        _anchor = new Anchor(_alert.toString());
        _anchor.addClickHandler(new CustomizeAlertClickHandler(this));

        add(_anchor);
    }

    public AlertDefinition getAlert() {
        return _alert;
    }

    public void updateAlert() {
        _anchor.setText(_alert.toString());
        TenantIdentifier tenant = _schedule.getTenant();
        _service.updateSchedule(tenant, _schedule, new DCAsyncCallback<ScheduleDefinition>() {
            @Override
            public void onSuccess(ScheduleDefinition result) {
                GWT.log("Succesfully updated schedule: " + result);
            }
        });
    }
}
