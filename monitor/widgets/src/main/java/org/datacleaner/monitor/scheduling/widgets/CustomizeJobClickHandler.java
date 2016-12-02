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
import org.datacleaner.monitor.scheduling.command.AddAlertCommand;
import org.datacleaner.monitor.scheduling.command.CopyJobCommand;
import org.datacleaner.monitor.scheduling.command.DeleteJobCommand;
import org.datacleaner.monitor.scheduling.command.EditJobCommand;
import org.datacleaner.monitor.scheduling.command.HistoryCommand;
import org.datacleaner.monitor.scheduling.command.RenameJobCommand;
import org.datacleaner.monitor.scheduling.command.ViewJobDefinitionCommand;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.shared.ClientConfig;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.monitor.shared.widgets.DCPopupPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.UIObject;

/**
 * ClickHandler with the responsibility of acting when a user clicks to
 * customize a job's properties.
 */
public class CustomizeJobClickHandler implements ClickHandler {

    private final SchedulePanel _schedulePanel;
    private final ScheduleDefinition _schedule;
    private final TenantIdentifier _tenant;
    private final SchedulingServiceAsync _service;
    private final ClientConfig _clientConfig;
    private final DCPopupPanel _popup;

    public CustomizeJobClickHandler(final SchedulePanel schedulePanel, final TenantIdentifier tenant,
            final ScheduleDefinition schedule, final SchedulingServiceAsync service, final ClientConfig clientConfig) {
        _schedulePanel = schedulePanel;
        _tenant = tenant;
        _schedule = schedule;
        _service = service;
        _clientConfig = clientConfig;

        _popup = new DCPopupPanel(null);
        _popup.setGlassEnabled(false);
        _popup.setAutoHideEnabled(true);
        _popup.getButtonPanel().setVisible(false);
    }

    @Override
    public void onClick(final ClickEvent event) {
        final JobIdentifier job = _schedulePanel.getSchedule().getJob();
        final MenuBar menuBar = new MenuBar(true);

        menuBar.addItem("Execution History", new HistoryCommand(_schedule, _service, _tenant, _popup));

        final boolean analysisJob = JobIdentifier.JOB_TYPE_ANALYSIS_JOB.equals(job.getType());
        if (analysisJob && _clientConfig.isWebstartAvailable()) {
            menuBar.addItem("Edit job", new EditJobCommand(_tenant, _schedule, _popup));
        }

        menuBar.addItem("Rename job", new RenameJobCommand(_tenant, job, _popup));
        menuBar.addItem("Copy job", new CopyJobCommand(_tenant, job, _popup));
        menuBar.addItem("Delete job", new DeleteJobCommand(_tenant, job, _popup));
        menuBar.addItem("Add Alert", new AddAlertCommand(_schedule, _service, _popup));

        if (analysisJob) {
            menuBar.addSeparator();
            menuBar.addItem("View Job Definition", new ViewJobDefinitionCommand(_tenant, job, _popup));
        }

        _popup.setWidget(menuBar);
        _popup.showRelativeTo((UIObject) event.getSource());
    }

}
