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
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.CancelPopupButton;
import org.eobjects.datacleaner.monitor.shared.widgets.DCPopupPanel;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

/**
 * The {@link ClickHandler} invoked when user clicks on a schedule expression.
 */
public class CustomizeScheduleClickHandler implements ClickHandler {

    private final SchedulingServiceAsync _service;
    private final TenantIdentifier _tenant;
    private final ScheduleDefinition _schedule;
    private final SchedulePanel _schedulePanel;

    public CustomizeScheduleClickHandler(SchedulePanel schedulePanel, SchedulingServiceAsync service,
            TenantIdentifier tenant, ScheduleDefinition schedule) {
        _schedulePanel = schedulePanel;
        _service = service;
        _tenant = tenant;
        _schedule = schedule;
    }
    
    public void showSchedulingPopup() {
        final DCPopupPanel popup = new DCPopupPanel("Customize schedule");

        final CustomizeSchedulePanel customizeSchedulePanel = new CustomizeSchedulePanel(_service, _tenant, _schedule);

        final Button saveButton = new Button("Save schedule");
        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ScheduleDefinition updatedSchedule = customizeSchedulePanel.getUpdatedSchedule();
                _service.updateSchedule(_tenant, updatedSchedule, new DCAsyncCallback<ScheduleDefinition>() {
                    @Override
                    public void onSuccess(ScheduleDefinition result) {
                    	if(_schedulePanel!=null) {
                    		_schedulePanel.updateScheduleWidgets();
                    	}
                        popup.hide();
                    }
                });
            }
        });

        popup.setWidget(customizeSchedulePanel);

        popup.addButton(saveButton);
        popup.addButton(new CancelPopupButton(popup));

        popup.center();
        popup.show();
    }

    @Override
    public void onClick(ClickEvent event) {
        showSchedulingPopup();
    }
}
