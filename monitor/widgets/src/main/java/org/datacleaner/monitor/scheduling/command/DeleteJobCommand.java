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
import org.datacleaner.monitor.util.DCRequestBuilder;
import org.datacleaner.monitor.util.DCRequestCallback;
import org.datacleaner.monitor.util.Urls;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class DeleteJobCommand implements Command {

    private TenantIdentifier _tenant;
    private JobIdentifier _job;
    private DCPopupPanel _morePopup;

    public DeleteJobCommand(final TenantIdentifier tenant, final JobIdentifier jobIdentifier,
            final DCPopupPanel morePopup) {
        _tenant = tenant;
        _job = jobIdentifier;
        _morePopup = morePopup;
    }

    @Override
    public void execute() {
        _morePopup.hide();
        final boolean delete = Window.confirm("Are you sure you want to delete the job '" + _job.getName()
                + "' and related schedule, results and timelines.");
        if (delete) {
            final String url = Urls.createRepositoryUrl(_tenant, "jobs/" + _job.getName() + ".delete");
            final DCRequestBuilder requestBuilder = new DCRequestBuilder(RequestBuilder.POST, url);
            requestBuilder.setHeader("Content-Type", "application/json");
            requestBuilder.send("", new DCRequestCallback() {
                @Override
                protected void onSuccess(final Request request, final Response response) {
                    Window.Location.reload();
                }
            });
        }
    }
}
