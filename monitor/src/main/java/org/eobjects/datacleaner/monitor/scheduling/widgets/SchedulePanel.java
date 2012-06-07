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

import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.ButtonPanel;
import org.eobjects.datacleaner.monitor.shared.widgets.HeadingLabel;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * A panel which presents a schedule
 */
public class SchedulePanel extends FlowPanel {

    public SchedulePanel(TenantIdentifier tenant, ScheduleDefinition schedule, SchedulingServiceAsync service) {
        super();
        addStyleName("SchedulePanel");
        
        final ButtonPanel buttonPanel = new ButtonPanel();
        buttonPanel.add(new Button(""));
        
        add(new HeadingLabel("Job: " + schedule.getJob().getName()));
        if (schedule.isActive()) {
            add(new Label("Schedule: " + schedule.getScheduleExpression()));
        } else {
            add(new Label("Schedule: Not scheduled"));
        }
    }

}
