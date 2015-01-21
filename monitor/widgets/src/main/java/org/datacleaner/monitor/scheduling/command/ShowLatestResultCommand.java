/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.monitor.scheduling.command;

import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.monitor.shared.widgets.DCPopupPanel;
import org.datacleaner.monitor.util.Urls;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class ShowLatestResultCommand implements Command {
 
	private TenantIdentifier tenant;
	private JobIdentifier job ;
	private DCPopupPanel popup;
	
	public ShowLatestResultCommand(TenantIdentifier tenantIdentifier,JobIdentifier jobIdentifier,DCPopupPanel popupPanel){
		tenant = tenantIdentifier;
		job = jobIdentifier;
		popup = popupPanel;
	}
	
	@Override
	public void execute() {
		 String url = Urls.createRepositoryUrl(tenant, "results/" + job.getName() + "-latest.analysis.result.dat");
         Window.open(url, "datacleaner_job_details", null);
         popup.hide();
     }
}
 

