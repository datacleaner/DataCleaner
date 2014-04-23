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

package org.eobjects.datacleaner.monitor.wizard;

import java.util.List;

import org.eobjects.datacleaner.monitor.scheduling.SchedulingService;
import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.scheduling.widgets.CustomizeScheduleClickHandler;
import org.eobjects.datacleaner.monitor.scheduling.widgets.TriggerJobClickHandler;
import org.eobjects.datacleaner.monitor.shared.ClientConfig;
import org.eobjects.datacleaner.monitor.shared.DictionaryClientConfig;
import org.eobjects.datacleaner.monitor.shared.WizardNavigationServiceAsync;
import org.eobjects.datacleaner.monitor.shared.WizardService;
import org.eobjects.datacleaner.monitor.shared.WizardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;
import org.eobjects.datacleaner.monitor.util.Urls;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class JobWizardPanel extends AbstractWizardController<WizardNavigationServiceAsync> {

    private final static WizardServiceAsync service = GWT.create(WizardService.class);
    final SchedulingServiceAsync schedulingServiceAsync = GWT.create(SchedulingService.class);
	private TenantIdentifier tenant;
	private ScheduleDefinition scheduleDefinitionForJob = null ;
	private ClientConfig clientConfig = new DictionaryClientConfig();
	private JobWizardPanel jobWizardPanel ;


    public JobWizardPanel(String tenantName, String panelType, String datastoreName, String wizardDisplayName, String htmlDivNameToShowWizardIn) {
        super("Build job: " + wizardDisplayName, service, tenantName, panelType, htmlDivNameToShowWizardIn);
        this.tenant = new TenantIdentifier(tenantName) ;

        WizardIdentifier wizardIdentifier = createAndReturnWizardIdentifier(wizardDisplayName);
        DatastoreIdentifier datastoreIdentifier = createAndReturnDatastoreIdentifier(datastoreName);
        startWizard(wizardIdentifier, datastoreIdentifier);
    }

    private DatastoreIdentifier createAndReturnDatastoreIdentifier(String datastoreName) {
        DatastoreIdentifier datastoreIdentifier = new DatastoreIdentifier();
        datastoreIdentifier.setName(datastoreName);
        return datastoreIdentifier;
    }

    private WizardIdentifier createAndReturnWizardIdentifier(String wizardDisplayName) {
        WizardIdentifier wizardIdentifier = new WizardIdentifier();
        wizardIdentifier.setDisplayName(wizardDisplayName);
        return wizardIdentifier;
    }

    private void startWizard(WizardIdentifier wizard, DatastoreIdentifier datastore) {
        setLoading();
        service.startJobWizard(_tenant, wizard, datastore, getLocaleName(), createNextPageCallback());
        return;
    }

    @Override
    protected int getStepsBeforeWizardPages() {
        return 0;
    }

    @Override
    protected void wizardFinished(final String jobName) {
        final Button button = new Button("Close");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // full page refresh.
                final String url = Urls.createRelativeUrl("scheduling.jsf");
                Urls.assign(url);
            }
        });

        final FlowPanel contentPanel = new FlowPanel();
        contentPanel.addStyleName("WizardFinishedPanel");
        if (jobName == null) {
            contentPanel.add(new Label("Job created! Wizard finished."));
        } else {
            contentPanel.add(new Label("Job '" + jobName + "' created! Wizard finished."));
        }
        contentPanel.add(new Label("Close the dialog to return, or click one of the links below to start using the job."));
        
        if (jobName != null) {

        	jobWizardPanel = this ;
            if (clientConfig.isScheduleEditor()) {
            	getSchedule(new Runnable() {
					

					@Override
					public void run() {
						final Anchor triggerAnchor = new Anchor("Run this job now");
		            	triggerAnchor.addStyleName("TriggerJob");
                		ClickHandler triggerJobClickHandler = new TriggerJobClickHandler(schedulingServiceAsync, tenant, scheduleDefinitionForJob);
                		ClickHandler removeWizardClickHandlerForTriggerJob = new RemoveWizardClickHandler(triggerJobClickHandler, jobWizardPanel);
                		triggerAnchor.addClickHandler(removeWizardClickHandlerForTriggerJob);
                		contentPanel.add(triggerAnchor);

                		 final Anchor schedulingAnchor = new Anchor("Set up a job schedule");
                         schedulingAnchor.addStyleName("ScheduleJob");
                         ClickHandler customizeScheduleClickHandler = new CustomizeScheduleClickHandler(null, schedulingServiceAsync, tenant, scheduleDefinitionForJob) ;
                         ClickHandler removeWizardClickHandlerForCustomizeSchedule = new RemoveWizardClickHandler(customizeScheduleClickHandler, jobWizardPanel);
                         schedulingAnchor.addClickHandler(removeWizardClickHandlerForCustomizeSchedule);
                		
                         contentPanel.add(schedulingAnchor);

					}
				}, jobName);
        }


        setContent(contentPanel);
        getWizardPanel().getButtonPanel().clear();
        getWizardPanel().addButton(button);
        getWizardPanel().center();
        }
        
    }
    
    private void getSchedule(final Runnable runnable, final String jobName) {
        schedulingServiceAsync.getSchedules(clientConfig.getTenant(), new DCAsyncCallback<List<ScheduleDefinition>>() {
            @Override
            public void onSuccess(List<ScheduleDefinition> result) {
                for (ScheduleDefinition scheduleDefinition : result) {
                	if(scheduleDefinition.getJob().getName().equals(jobName)) {
                		scheduleDefinitionForJob = scheduleDefinition ;
                		runnable.run();
                	}
                }
            }
        });
    	
    }
	
}
