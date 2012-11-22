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

import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.DCPopupPanel;
import org.eobjects.datacleaner.monitor.shared.widgets.LoadingIndicator;
import org.eobjects.datacleaner.monitor.util.DCRequestBuilder;
import org.eobjects.datacleaner.monitor.util.DCRequestCallback;
import org.eobjects.datacleaner.monitor.util.Urls;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.UIObject;

/**
 * ClickHandler with the responsibility of acting when a user clicks to
 * customize a job's properties.
 */
public class CustomizeJobClickHandler implements ClickHandler {

    private final SchedulePanel _schedulePanel;
    private final TenantIdentifier _tenant;

    public CustomizeJobClickHandler(SchedulePanel schedulePanel, TenantIdentifier tenant) {
        _schedulePanel = schedulePanel;
        _tenant = tenant;
    }

    @Override
    public void onClick(ClickEvent event) {
        final JobIdentifier job = _schedulePanel.getSchedule().getJob();
        final MenuBar menuBar = new MenuBar(true);
        menuBar.addItem("Rename job", new Command() {
            @Override
            public void execute() {
                final String newName = Window.prompt("Enter job name", job.getName());
                if (newName == null || newName.trim().length() == 0 || newName.equals(job.getName())) {
                    return;
                }

                final DCPopupPanel popup = new DCPopupPanel("Renaming...");
                popup.setWidget(new LoadingIndicator());
                popup.center();
                popup.show();

                final String url = Urls.createRepositoryUrl(_tenant, "jobs/" + job.getName() + ".modify");

                final JSONObject payload = new JSONObject();
                payload.put("name", new JSONString(newName));

                final DCRequestBuilder requestBuilder = new DCRequestBuilder(RequestBuilder.POST, url);
                requestBuilder.setHeader("Content-Type", "application/json");
                requestBuilder.send(payload.toString(), new DCRequestCallback() {
                    @Override
                    protected void onSuccess(Request request, Response response) {
                        Window.Location.reload();
                    }
                });
            }
        });
        menuBar.addItem("Copy job", new Command() {
            @Override
            public void execute() {
                final String newJobName = Window.prompt("Enter new job name", job.getName() + " (Copy)");

                if (newJobName == null || newJobName.trim().length() == 0 || newJobName.equals(job.getName())) {
                    return;
                }

                final DCPopupPanel popup = new DCPopupPanel("Copying...");
                popup.setWidget(new LoadingIndicator());
                popup.center();
                popup.show();

                final String url = Urls.createRepositoryUrl(_tenant, "jobs/" + job.getName() + ".copy");

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
        });
        
        menuBar.addItem("Delete job", new Command() {
            @Override
            public void execute() {
                boolean delete = Window.confirm("Are you sure you want to delete the job '" + job.getName()
                        + "' and related schedule, results and timelines.");
                if (delete) {
                    final String url = Urls.createRepositoryUrl(_tenant, "jobs/" + job.getName() + ".delete");
                    final DCRequestBuilder requestBuilder = new DCRequestBuilder(RequestBuilder.POST, url);
                    requestBuilder.setHeader("Content-Type", "application/json");
                    requestBuilder.send("", new DCRequestCallback() {
                        @Override
                        protected void onSuccess(Request request, Response response) {
                            Window.Location.reload();
                        }
                    });
                }
            }
        });
        
        menuBar.addSeparator();

        menuBar.addItem("Job definition (xml)", new Command() {
            @Override
            public void execute() {
                String url = Urls.createRepositoryUrl(_tenant, "jobs/" + job.getName() + ".analysis.xml");
                Window.Location.assign(url);
            }
        });

        final DCPopupPanel popup = new DCPopupPanel(null);
        popup.setGlassEnabled(false);
        popup.setWidget(menuBar);
        popup.setAutoHideEnabled(true);
        popup.getButtonPanel().setVisible(false);
        popup.showRelativeTo((UIObject) event.getSource());
    }

}
