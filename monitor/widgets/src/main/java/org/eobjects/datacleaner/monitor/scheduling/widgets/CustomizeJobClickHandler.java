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

import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.command.AddAlertCommand;
import org.eobjects.datacleaner.monitor.scheduling.command.CopyJobCommand;
import org.eobjects.datacleaner.monitor.scheduling.command.DeleteJobCommand;
import org.eobjects.datacleaner.monitor.scheduling.command.HistoryCommand;
import org.eobjects.datacleaner.monitor.scheduling.command.RenameJobCommand;
import org.eobjects.datacleaner.monitor.scheduling.command.ShowLatestResultCommand;
import org.eobjects.datacleaner.monitor.scheduling.command.ViewJobDefinitionCommand;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.DCPopupPanel;

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
    private DCPopupPanel _popup;

    public CustomizeJobClickHandler(SchedulePanel schedulePanel, TenantIdentifier tenant,ScheduleDefinition schedule,final SchedulingServiceAsync service) {
        _schedulePanel = schedulePanel;
        _tenant = tenant;
        _schedule = schedule;
        _service = service;
        
        _popup = new DCPopupPanel(null);
        _popup.setGlassEnabled(false);
        _popup.setAutoHideEnabled(true);
        _popup.getButtonPanel().setVisible(false);
    }
    
    @Override
    public void onClick(ClickEvent event) {
        final JobIdentifier job = _schedulePanel.getSchedule().getJob();
        final MenuBar menuBar = new MenuBar(true);
        menuBar.addItem("Execution History" ,new HistoryCommand(_schedule, _service, _tenant));
        
        menuBar.addItem("Rename job", new RenameJobCommand(_tenant, job));
           
        menuBar.addItem("Copy job", new CopyJobCommand(_tenant, job));
        
        menuBar.addItem("Delete job", new DeleteJobCommand(_tenant, job));
        
        menuBar.addItem("Add Alert",new AddAlertCommand(_schedule, _service));

        final boolean analysisJob = JobIdentifier.JOB_TYPE_ANALYSIS_JOB.equals(job.getType());
        
        if (analysisJob) {
            menuBar.addSeparator();
            
            menuBar.addItem("Job definition (xml)", new ViewJobDefinitionCommand(_tenant, job, _popup));
                
            menuBar.addItem("Show latest result", new ShowLatestResultCommand(_tenant, job, _popup));
            
        }
        
        _popup.setWidget(menuBar);
        _popup.showRelativeTo((UIObject) event.getSource());
    }

}