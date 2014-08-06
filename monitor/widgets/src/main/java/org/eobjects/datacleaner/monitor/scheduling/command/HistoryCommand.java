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
package org.eobjects.datacleaner.monitor.scheduling.command;

import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.scheduling.widgets.JobHistoryPanel;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.CancelPopupButton;
import org.eobjects.datacleaner.monitor.shared.widgets.DCPopupPanel;

import com.google.gwt.user.client.Command;

public class HistoryCommand implements Command {
 
	private ScheduleDefinition _schedule;
	private SchedulingServiceAsync _service;
	private TenantIdentifier _tenant;
	
	public HistoryCommand(ScheduleDefinition schedule,SchedulingServiceAsync service , TenantIdentifier tenant){
		_schedule = schedule;
		_service = service;
		_tenant = tenant;
	}
	@Override
	public void execute() {
		final DCPopupPanel popup = new DCPopupPanel("Execution history: '" + _schedule.getJob().getName() + "'");

        popup.setWidget(new JobHistoryPanel(_schedule.getJob(), _service, _tenant));
        popup.addButton(new CancelPopupButton(popup, "Close"));
        popup.center();
        popup.show();
    }
}
