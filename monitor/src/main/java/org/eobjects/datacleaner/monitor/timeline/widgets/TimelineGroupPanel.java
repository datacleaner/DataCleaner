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

import java.util.List;

import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.HeadingLabel;
import org.eobjects.datacleaner.monitor.timeline.TimelineServiceAsync;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineGroup;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Container panel for a group of timelines. Each timeline will be presented in
 * a {@link TimelinePanel}.
 */
public class TimelineGroupPanel extends FlowPanel {

    private final TenantIdentifier _tenant;
    private final TimelineServiceAsync _service;
    private final WelcomePanel _welcomePanel;
    private final TimelineGroup _group;
    private final Button _removeGroupButton;
    private int _dashboardWidgetCount;

    public TimelineGroupPanel(TimelineServiceAsync service, TenantIdentifier tenant, TimelineGroup group) {
        super();
        _tenant = tenant;
        _service = service;
        _group = group;
        _dashboardWidgetCount = 0;

        addStyleName("TimelineGroupPanel");

        _removeGroupButton = new Button("Remove this group");
        _removeGroupButton.addStyleDependentName("ImageButton");
        _removeGroupButton.addStyleName("RemoveButton");
        _removeGroupButton.setVisible(false);
        _removeGroupButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final boolean confirmation = Window.confirm("Are you sure you wish to remove this group?");
                if (confirmation) {
                    _service.removeTimelineGroup(_tenant, _group, new DCAsyncCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            if (result != null && result.booleanValue()) {
                                Window.Location.reload();
                            } else {
                                Window.alert("Failed to remove group. Please check server logs for details.");
                            }
                        }
                    });
                }
            }
        });

        final Button newTimelineButton;
        if (group == null) {
            // this is the "default" group
            add(new HeadingLabel("Welcome"));
            _welcomePanel = new WelcomePanel();
            newTimelineButton = _welcomePanel.getNewTimelineButton();
            add(_welcomePanel);
        } else {
            add(new HeadingLabel(group.getName()));

            if (group.getDescription() != null) {
                add(new Label(group.getDescription()));
            }

            _welcomePanel = null;
            newTimelineButton = new Button("New timeline");
            add(newTimelineButton);
            add(_removeGroupButton);
        }
        
        newTimelineButton.addStyleDependentName("ImageButton");
        newTimelineButton.addStyleName("NewDashboardWidgetButton");
        newTimelineButton.addClickHandler(new CreateTimelineHandler(_service, _tenant, this));

        _service.getTimelines(_tenant, _group, new DCAsyncCallback<List<TimelineIdentifier>>() {
            @Override
            public void onSuccess(List<TimelineIdentifier> result) {
                for (TimelineIdentifier identifier : result) {
                    addTimelinePanel(identifier);
                }

                if (_dashboardWidgetCount == 0) {
                    _removeGroupButton.setVisible(true);
                }
            }
        });
    }

    public void addTimelinePanel(TimelineIdentifier identifier) {
        final TimelinePanel timelinePanel = new TimelinePanel(_tenant, _service, identifier, this);
        addTimelinePanel(timelinePanel);
    }

    public void addTimelinePanel(TimelinePanel timelinePanel) {
        add(timelinePanel);
        if (_welcomePanel != null) {
            _welcomePanel.setWelcomeTextVisible(false);
        }
        _dashboardWidgetCount++;
        _removeGroupButton.setVisible(false);
    }

    public void removeTimelinePanel(TimelinePanel timelinePanel) {
        remove(timelinePanel);
        _dashboardWidgetCount--;
        if (_dashboardWidgetCount == 0) {
            _removeGroupButton.setVisible(true);
        }
    }

    public TimelineGroup getTimelineGroup() {
        return _group;
    }
}
