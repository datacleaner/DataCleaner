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

import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.util.Urls;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class EditJobCommand implements Command {
 
	private TenantIdentifier _tenant;
	private ScheduleDefinition _schedule;
	
	public EditJobCommand(TenantIdentifier tenantIdentifier, ScheduleDefinition schedule){
		_tenant = tenantIdentifier;
		_schedule = schedule;
		
	}
	
	@Override
	public void execute() {
		String url = Urls.createRepositoryUrl(_tenant, "jobs/" + _schedule.getJob().getName() + ".launch.jnlp");
        Window.open(url, "_blank", null);
    }
}
