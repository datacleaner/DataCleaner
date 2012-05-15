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
package org.eobjects.datacleaner.monitor.timeline;

import java.util.List;

import org.eobjects.datacleaner.monitor.timeline.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineIdentifier;
import org.eobjects.datacleaner.monitor.timeline.widgets.TimelineListPanel;
import org.eobjects.datacleaner.monitor.timeline.widgets.TimelinePanel;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;
import org.eobjects.datacleaner.monitor.util.ErrorHandler;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TimelineEntryPoint implements EntryPoint {

    public void onModuleLoad() {
        GWT.setUncaughtExceptionHandler(ErrorHandler.getUncaughtExceptionHandler());

        final TimelineServiceAsync service = GWT.create(TimelineService.class);
        final TenantIdentifier tenant = new TenantIdentifier("DC");

        final TimelineListPanel timelineListPanel = new TimelineListPanel(service, tenant);

        final TabPanel tabPanel = new TabPanel();
        tabPanel.addStyleName("MainTabPanel");
        tabPanel.add(timelineListPanel, "Timelines");
        tabPanel.add(new Label("TODO"), "Repository");
        tabPanel.add(new Label("TODO"), "Datastores");
        tabPanel.selectTab(0);
        
        final RootPanel rootPanel = RootPanel.get("RootPanelTarget");
        rootPanel.add(tabPanel);

        service.getSavedTimelines(tenant, new DCAsyncCallback<List<TimelineIdentifier>>() {
            @Override
            public void onSuccess(List<TimelineIdentifier> result) {
                for (TimelineIdentifier identifier : result) {
                    showTimeline(tenant, identifier, service, timelineListPanel);
                }
            }
        });
    }

    private void showTimeline(TenantIdentifier tenant, TimelineIdentifier identifier, TimelineServiceAsync service,
            TimelineListPanel timelineListPanel) {
        final TimelinePanel timelinePanel = new TimelinePanel(tenant, service, identifier, timelineListPanel);
        timelineListPanel.addTimelinePanel(timelinePanel);
    }
}
