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

import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.timeline.widgets.TimelineGroupSelectionPanel;
import org.eobjects.datacleaner.monitor.util.ErrorHandler;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * GWT Entry point for the Timeline module
 */
public class TimelineEntryPoint implements EntryPoint {

    public void onModuleLoad() {
        GWT.setUncaughtExceptionHandler(ErrorHandler.getUncaughtExceptionHandler());

        final TimelineServiceAsync service = GWT.create(TimelineService.class);
        final TenantIdentifier tenant = new TenantIdentifier("DC");

        final FlowPanel timelinesSplitPanel = new FlowPanel();
        timelinesSplitPanel.setStyleName("TimelinesSplitPanel");
        {
            final SimplePanel targetPanel = new SimplePanel();
            final TimelineGroupSelectionPanel selectionPanel = new TimelineGroupSelectionPanel(tenant, service,
                    targetPanel);

            timelinesSplitPanel.add(selectionPanel);
            timelinesSplitPanel.add(targetPanel);
        }

        final RootPanel rootPanel = RootPanel.get("RootPanelTarget");
        rootPanel.add(timelinesSplitPanel);
    }
}
