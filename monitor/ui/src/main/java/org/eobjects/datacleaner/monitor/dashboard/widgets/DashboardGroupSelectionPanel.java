/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.dashboard.widgets;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.datacleaner.monitor.dashboard.DashboardServiceAsync;
import org.eobjects.datacleaner.monitor.dashboard.model.DashboardGroup;
import org.eobjects.datacleaner.monitor.shared.ClientConfig;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.DCPopupPanel;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel which shows and let's the user select different dashboard
 * groups/pages.
 */
public class DashboardGroupSelectionPanel extends FlowPanel {

    private final TenantIdentifier _tenant;
    private final DashboardServiceAsync _service;
    private final SimplePanel _targetPanel;
    private final Map<String, Anchor> _anchors;
    private final FlowPanel _anchorPanel;
    private final boolean _isDashboardEditor;
    private final boolean _displayDefaultGroup;
    private final boolean _displayInfomercial;
    private DashboardGroupPanel _defaultGroupPanel;

    public DashboardGroupSelectionPanel(ClientConfig clientConfig, DashboardServiceAsync service,
            SimplePanel targetPanel) {
        super();

        _tenant = clientConfig.getTenant();
        _service = service;
        _targetPanel = targetPanel;
        _isDashboardEditor = clientConfig.isDashboardEditor();
        _displayDefaultGroup = clientConfig.isDefaultDashboardGroupDisplayed();
        _displayInfomercial = clientConfig.isInformercialDisplayed();
        _anchors = new HashMap<String, Anchor>();
        _anchorPanel = new FlowPanel();
        _anchorPanel.setStyleName("AnchorPanel");

        addStyleName("DashboardGroupSelectionPanel");

        // need to get history token as very first thing to see what happens
        // before the token is manipulated
        final String historyToken = History.getToken();

        if (_displayDefaultGroup) {
            // add the default/"welcome" group
            addGroup(null);

            // this method sets the default group panel
            assert _defaultGroupPanel != null;
        }

        // load all other groups
        _service.getDashboardGroups(_tenant, new DCAsyncCallback<List<DashboardGroup>>() {
            @Override
            public void onSuccess(List<DashboardGroup> result) {

                sortDashboardGroups(result);
                for (DashboardGroup group : result) {
                    addGroup(group);
                }
                initializeSelectedAnchor();

                // check if a new timeline is requested through history token
                initializeNewTimelineIfNeeded(historyToken);
            }

            private void sortDashboardGroups(List<DashboardGroup> result) {
                Collections.sort(result, new Comparator<DashboardGroup>() {
                    @Override
                    public int compare(DashboardGroup o1, DashboardGroup o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                });
            }
        });

        final Anchor createNewGroupAnchor = new Anchor();
        createNewGroupAnchor.setVisible(_isDashboardEditor);
        createNewGroupAnchor.setStyleName("CreateNewDashboardGroupAnchor");
        createNewGroupAnchor.setText("New group");
        createNewGroupAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String name = Window.prompt("Name of the new group?", "");
                boolean validName = name != null && name.trim().length() > 1;
                if (validName) {
                    _service.addDashboardGroup(_tenant, name, new DCAsyncCallback<DashboardGroup>() {
                        @Override
                        public void onSuccess(DashboardGroup result) {
                            addGroup(result);
                        }
                    });
                } else {
                    Window.alert("Please provide a valid group name of at least 2 characters");
                }
            }
        });

        add(_anchorPanel);
        add(createNewGroupAnchor);
    }

    protected void initializeSelectedAnchor() {
        final Anchor anchor;

        final String historyToken = History.getToken();
        if (historyToken == null || historyToken.length() == 0) {
            if (_displayDefaultGroup) {
                anchor = _anchors.get(DashboardGroup.DEFAULT_GROUP_NAME);
            } else {
                anchor = getFirstAnchor();
            }
        } else if (_anchors.containsKey(historyToken)) {
            anchor = _anchors.get(historyToken);
        } else {
            if (_displayDefaultGroup) {
                anchor = _anchors.get(DashboardGroup.DEFAULT_GROUP_NAME);
            } else {
                anchor = getFirstAnchor();
            }
        }

        if (anchor != null) {
            anchor.fireEvent(new ClickEvent() {
            });
        }
    }

    private void initializeNewTimelineIfNeeded(final String historyToken) {
        final String prefix = "new_timeline_";
        if (historyToken != null && historyToken.startsWith(prefix)) {
            String jobName = historyToken.substring(prefix.length());
            jobName = URL.decodeQueryString(jobName);
            
            GWT.log("Showing new timeline popup for job: " + jobName);

            final CreateTimelineHandler handler = new CreateTimelineHandler(_service, _tenant, _defaultGroupPanel);
            final DCPopupPanel popup = handler.createPopup();
            handler.setJob(popup, new JobIdentifier(jobName));
            popup.show();
        }
    }

    private Anchor getFirstAnchor() {
        final int widgetCount = _anchorPanel.getWidgetCount();
        for (int i = 0; i < widgetCount; i++) {
            Widget widget = _anchorPanel.getWidget(i);
            if (widget instanceof Anchor) {
                return (Anchor) widget;
            }
        }
        return null;
    }

    public Anchor addGroup(final DashboardGroup group) {
        final String groupName;
        final DashboardGroupPanel groupPanel;
        if (group == null) {
            groupName = DashboardGroup.DEFAULT_GROUP_NAME;
            groupPanel = new DashboardGroupPanel(_service, _tenant, group, _isDashboardEditor, _displayInfomercial);
            _defaultGroupPanel = groupPanel;
        } else {
            groupName = group.getName();
            groupPanel = null;
        }

        final Anchor anchor = new Anchor(groupName);
        anchor.addClickHandler(new ClickHandler() {
            private DashboardGroupPanel panel = groupPanel;

            @Override
            public void onClick(ClickEvent event) {
                for (Anchor anchor : _anchors.values()) {
                    anchor.removeStyleName("selected");
                }
                anchor.addStyleName("selected");

                if (panel == null) {
                    panel = new DashboardGroupPanel(_service, _tenant, group, _isDashboardEditor, _displayInfomercial);
                }
                if (_defaultGroupPanel == null) {
                    _defaultGroupPanel = panel;
                }
                _targetPanel.setWidget(panel);
                History.newItem(groupName);
            }
        });
        _anchorPanel.add(anchor);

        _anchors.put(groupName, anchor);
        return anchor;
    }
}
