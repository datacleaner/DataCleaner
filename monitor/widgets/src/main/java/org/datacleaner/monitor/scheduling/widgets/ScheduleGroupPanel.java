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
package org.datacleaner.monitor.scheduling.widgets;

import org.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.shared.ClientConfig;
import org.datacleaner.monitor.shared.widgets.HeadingLabel;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Presents a group of related schedules in a list view
 */
public class ScheduleGroupPanel extends FlowPanel {

    private final SchedulingServiceAsync _service;
    private final ClientConfig _clientConfig;

    public ScheduleGroupPanel(String name, ClientConfig clientConfig, SchedulingServiceAsync service) {
        _clientConfig = clientConfig;
        _service = service;

        addStyleName("ScheduleGroupPanel");
        if (name != null) {
            add(createHeader(name));
        }
    }

    private Widget createHeader(String name) {
        final HeadingLabel label = new HeadingLabel(name);
        return label;
    }

    public void addSchedule(ScheduleDefinition schedule) {
        final SchedulePanel panel = new SchedulePanel(_clientConfig, schedule, _service);
        add(panel);
    }
}
