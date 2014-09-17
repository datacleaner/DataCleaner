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
package org.eobjects.datacleaner.monitor.dashboard;

import org.eobjects.datacleaner.monitor.dashboard.widgets.DashboardGroupSelectionPanel;
import org.eobjects.datacleaner.monitor.shared.ClientConfig;
import org.eobjects.datacleaner.monitor.shared.DictionaryClientConfig;
import org.eobjects.datacleaner.monitor.util.ErrorHandler;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * GWT Entry point for the Dashboard module
 */
public class DashboardEntryPoint implements EntryPoint {

    public void onModuleLoad() {
        GWT.setUncaughtExceptionHandler(ErrorHandler.getUncaughtExceptionHandler());

        final ClientConfig clientConfig = new DictionaryClientConfig();

        final DashboardServiceAsync service = GWT.create(DashboardService.class);
        render(service, clientConfig);
    }

    protected void render(DashboardServiceAsync service, ClientConfig clientConfig) {
        final FlowPanel timelinesSplitPanel = new FlowPanel();
        timelinesSplitPanel.setStyleName("TimelinesSplitPanel");
        {
            final SimplePanel targetPanel = new SimplePanel();
            final DashboardGroupSelectionPanel selectionPanel = new DashboardGroupSelectionPanel(clientConfig, service,
                    targetPanel);

            timelinesSplitPanel.add(selectionPanel);
            timelinesSplitPanel.add(targetPanel);
        }

        final RootPanel rootPanel = RootPanel.get("RootPanelTarget");
        rootPanel.add(timelinesSplitPanel);
    }
}
