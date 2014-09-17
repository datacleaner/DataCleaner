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
package org.eobjects.datacleaner.monitor.scheduling.command;

import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.DCPopupPanel;
import org.eobjects.datacleaner.monitor.shared.widgets.LoadingIndicator;
import org.eobjects.datacleaner.monitor.util.DCRequestBuilder;
import org.eobjects.datacleaner.monitor.util.DCRequestCallback;
import org.eobjects.datacleaner.monitor.util.Urls;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class CopyJobCommand implements Command {
 
	private TenantIdentifier _tenant;
	private JobIdentifier _job ; 
	private DCPopupPanel _morePopup;
	
	public CopyJobCommand(TenantIdentifier tenant,JobIdentifier jobIdentifier, DCPopupPanel morePopup) {
		_tenant = tenant;
		_job = jobIdentifier; 
		_morePopup = morePopup;
	}
	
	@Override
	public void execute() {
		_morePopup.hide();
		final String newJobName = Window.prompt("Enter new job name", _job.getName() + " (Copy)");

        if (newJobName == null || newJobName.trim().length() == 0 || newJobName.equals(_job.getName())) {
            return;
        }

        final DCPopupPanel popup = new DCPopupPanel("Copying...");
        popup.setWidget(new LoadingIndicator());
        popup.center();
        popup.show();

        final String url = Urls.createRepositoryUrl(_tenant, "jobs/" + _job.getName() + ".copy");

        final JSONObject payload = new JSONObject();
        payload.put("name", new JSONString(newJobName));

        final DCRequestBuilder requestBuilder = new DCRequestBuilder(RequestBuilder.POST, url);
        requestBuilder.setHeader("Content-Type", "application/json");
        requestBuilder.send(payload.toString(), new DCRequestCallback() {
            @Override
            protected void onSuccess(Request request, Response response) {
                Window.Location.reload();
            }
        });
    }
}