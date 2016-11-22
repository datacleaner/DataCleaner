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
package org.datacleaner.monitor.dashboard.widgets;

import java.util.List;

import org.datacleaner.monitor.dashboard.DashboardServiceAsync;
import org.datacleaner.monitor.dashboard.model.DashboardGroup;
import org.datacleaner.monitor.dashboard.model.TimelineIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.monitor.shared.widgets.ButtonPanel;
import org.datacleaner.monitor.shared.widgets.DCButtons;
import org.datacleaner.monitor.shared.widgets.HeadingLabel;
import org.datacleaner.monitor.util.DCAsyncCallback;

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
public class DashboardGroupPanel extends FlowPanel {

    private final TenantIdentifier _tenant;
    private final DashboardServiceAsync _service;
    private final WelcomePanel _welcomePanel;
    private final DashboardGroup _group;
    private final Button _removeGroupButton;
    private final boolean _isDashboardEditor;
    private int _dashboardWidgetCount;

    public DashboardGroupPanel(final DashboardServiceAsync service, final TenantIdentifier tenant, DashboardGroup group,
            final boolean isDashboardEditor, final boolean displayInfomercial) {
        super();
        _tenant = tenant;
        _service = service;
        _group = group;
        _isDashboardEditor = isDashboardEditor;
        _dashboardWidgetCount = 0;

        addStyleName("DashboardGroupPanel");

        _removeGroupButton = DCButtons.dangerButton("glyphicon-minus", "Remove this group");
        _removeGroupButton.setVisible(false);
        _removeGroupButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                final boolean confirmation = Window.confirm("Are you sure you wish to remove this group?");
                if (confirmation) {
                    _service.removeDashboardGroup(_tenant, _group, new DCAsyncCallback<Boolean>() {
                        @Override
                        public void onSuccess(final Boolean result) {
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
        if (displayInfomercial && group == null) {
            // this is the "default" group
            add(new HeadingLabel("Welcome"));
            _welcomePanel = new WelcomePanel();
            newTimelineButton = _welcomePanel.getNewTimelineButton();
            add(_welcomePanel);
        } else {
            if (group == null) {
                group = new DashboardGroup(null);
            }
            add(new HeadingLabel(group.getName()));

            if (group.getDescription() != null) {
                add(new Label(group.getDescription()));
            }

            _welcomePanel = null;
            newTimelineButton = DCButtons.defaultButton("glyphicon-plus", "New timeline chart");

            final ButtonPanel buttonPanel = new ButtonPanel(false);
            buttonPanel.add(newTimelineButton);
            buttonPanel.add(_removeGroupButton);
            add(buttonPanel);
        }

        newTimelineButton.setVisible(_isDashboardEditor);
        newTimelineButton.addClickHandler(new CreateTimelineHandler(_service, _tenant, this));

        _service.getTimelines(_tenant, _group, new DCAsyncCallback<List<TimelineIdentifier>>() {
            @Override
            public void onSuccess(final List<TimelineIdentifier> result) {
                for (final TimelineIdentifier identifier : result) {
                    addTimelinePanel(identifier);
                }

                if (_dashboardWidgetCount == 0 && _group != null && !_group.isDefaultGroup()) {
                    _removeGroupButton.setVisible(true);
                }
            }
        });
    }

    public void addTimelinePanel(final TimelineIdentifier identifier) {
        final TimelinePanel timelinePanel = new TimelinePanel(_tenant, _service, identifier, this, _isDashboardEditor);
        addTimelinePanel(timelinePanel);
    }

    public void addTimelinePanel(final TimelinePanel timelinePanel) {
        add(timelinePanel);
        if (_welcomePanel != null) {
            _welcomePanel.setWelcomeTextVisible(false);
        }
        _dashboardWidgetCount++;
        _removeGroupButton.setVisible(false);
    }

    public void removeTimelinePanel(final TimelinePanel timelinePanel) {
        remove(timelinePanel);
        _dashboardWidgetCount--;
        if (_dashboardWidgetCount == 0) {
            _removeGroupButton.setVisible(true);
        }
    }

    public DashboardGroup getTimelineGroup() {
        return _group;
    }
}
