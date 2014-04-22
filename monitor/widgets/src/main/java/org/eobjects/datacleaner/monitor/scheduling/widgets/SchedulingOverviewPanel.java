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
package org.eobjects.datacleaner.monitor.scheduling.widgets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.shared.ClientConfig;
import org.eobjects.datacleaner.monitor.shared.widgets.ButtonPanel;
import org.eobjects.datacleaner.monitor.shared.widgets.CreateJobButton;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

/**
 * Presents an overview of all scheduled activity in the DCmonitor.
 */
public class SchedulingOverviewPanel extends Composite {

    private final ClientConfig _clientConfig;
    private final SchedulingServiceAsync _service;
    private final Map<String, ScheduleGroupPanel> _scheduleGroupPanels;
    private final FlowPanel _panel;

    public SchedulingOverviewPanel(ClientConfig clientConfig, SchedulingServiceAsync service) {
        _clientConfig = clientConfig;
        _service = service;
        _scheduleGroupPanels = new HashMap<String, ScheduleGroupPanel>();

        _panel = new FlowPanel();

        _panel.add(createButtonPanel());

        _panel.add(createHeaderPanel());

        _panel.addStyleName("SchedulingOverviewPanel");
        initWidget(_panel);
    }

    private Panel createButtonPanel() {
        final ButtonPanel buttonPanel = new ButtonPanel();
        
        if (_clientConfig.isJobEditor()) {
            final CreateJobButton newJobButton = new CreateJobButton(_clientConfig.getTenant());
            buttonPanel.add(newJobButton);
            
            final String token = History.getToken();
            if ("startWizard".equals(token)) {
                History.newItem("");
                newJobButton.startWizard();
            }
        }
        
        return buttonPanel;
    }

    public void initialize(final Runnable listener) {
        _service.getSchedules(_clientConfig.getTenant(), new DCAsyncCallback<List<ScheduleDefinition>>() {
            @Override
            public void onSuccess(List<ScheduleDefinition> result) {
                for (ScheduleDefinition scheduleDefinition : result) {
                    addSchedule(scheduleDefinition);
                }
                listener.run();
            }
        });
    }

    public void addSchedule(ScheduleDefinition schedule) {
        String groupName = schedule.getGroupName();
        if (groupName == null || groupName.trim().length() == 0) {
            groupName = "(other)";
        }
        final ScheduleGroupPanel scheduleGroupPanel;
        if (_scheduleGroupPanels.containsKey(groupName)) {
            scheduleGroupPanel = _scheduleGroupPanels.get(groupName);
        } else {
            scheduleGroupPanel = new ScheduleGroupPanel(groupName, _clientConfig, _service);
            _panel.add(scheduleGroupPanel);
            _scheduleGroupPanels.put(groupName, scheduleGroupPanel);
        }

        scheduleGroupPanel.addSchedule(schedule);
    }

    private Panel createHeaderPanel() {
        final FlowPanel panel = new FlowPanel();
        panel.addStyleName("ColumnHeaders");

        panel.add(createLabel("", "EmptyColumn"));
        panel.add(createLabel("Job name", "JobColumn"));
        panel.add(createLabel("Schedule", "ScheduleColumn"));
        panel.add(createLabel("Alerts", "AlertsColumn"));
        panel.add(createLabel("Actions", "ActionsColumn"));

        return panel;
    }

    private Label createLabel(String text, String styleName) {
        final Label label = new Label();
        label.setText(text);
        label.setStyleName(styleName);
        return label;
    }
}
