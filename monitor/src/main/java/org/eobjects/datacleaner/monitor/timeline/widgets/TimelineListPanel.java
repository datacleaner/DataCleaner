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
package org.eobjects.datacleaner.monitor.timeline.widgets;

import org.eobjects.datacleaner.monitor.timeline.TimelineServiceAsync;
import org.eobjects.datacleaner.monitor.timeline.model.TenantIdentifier;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Container panel for timelines. Each timeline will be presented in a
 * {@link TimelinePanel}.
 */
public class TimelineListPanel extends FlowPanel {

    public TimelineListPanel(TimelineServiceAsync service, TenantIdentifier tenant) {
        super();
        addStyleName("TimelineListPanel");
        
        add(new HeadingLabel("Timelines"));
        
        final Button newTimelineButton = new Button("New timeline");
        newTimelineButton.addClickHandler(new CreateTimelineHandler(service, tenant, this));
        add(newTimelineButton);
    }
    
    public void addTimelinePanel(TimelinePanel timelinePanel) {
        add(timelinePanel);
    }
}
