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
import org.eobjects.datacleaner.monitor.shared.WizardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.LoadingIndicator;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;
import org.eobjects.datacleaner.monitor.wizard.callbacks.JavaScriptCallbacks;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class JobWizardController extends AbstractWizardController<WizardServiceAsync> {

    private final SchedulingServiceAsync schedulingServiceAsync = GWT.create(SchedulingService.class);

    private final ClientConfig clientConfig = new DictionaryClientConfig();
    private final DatastoreIdentifier _datastoreIdentifier;
    private ScheduleDefinition scheduleDefinitionForJob = null;

    public JobWizardController(WizardPanel wizardPanel, TenantIdentifier tenant, WizardIdentifier wizardIdentifier,
            DatastoreIdentifier datastoreIdentifier, WizardServiceAsync wizardService) {
        super(wizardPanel, tenant, wizardIdentifier, wizardService);

        wizardPanel.setHeader("Build job: " + wizardIdentifier.getDisplayName());

        _datastoreIdentifier = datastoreIdentifier;
    }

    /**
     * Starts the wizard
     */
    public void startWizard() {
        setLoading();
        WizardServiceAsync wizardService = getWizardService();
        wizardService.startJobWizard(getTenant(), getWizardIdentifier(), _datastoreIdentifier, getLocaleName(),
                createNextPageCallback());
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
            	JavaScriptCallbacks.onWizardFinished();
            }
        });

        final FlowPanel contentPanel = new FlowPanel();
        contentPanel.addStyleName("WizardFinishedPanel");

        final LoadingIndicator loadingIndicator = new LoadingIndicator();
        contentPanel.add(loadingIndicator);
        if (jobName == null) {
            contentPanel.add(new Label("Job created! Wizard finished."));
        } else {
            contentPanel.add(new Label("Job '" + jobName + "' created! Wizard finished."));
        }
        contentPanel.add(new Label(
                "Close the dialog to return, or click one of the links below to start using the job."));

        if (jobName != null) {

            if (clientConfig.isScheduleEditor()) {
                getSchedule(new Runnable() {

                    @Override
                    public void run() {
                        final Anchor triggerAnchor = new Anchor("Run this job now");
                        triggerAnchor.addStyleName("TriggerJob");
                        ClickHandler triggerJobClickHandler = new TriggerJobClickHandler(schedulingServiceAsync,
                                getTenant(), scheduleDefinitionForJob);
                        ClickHandler removeWizardClickHandlerForTriggerJob = new RemoveWizardClickHandler(
                                triggerJobClickHandler, JobWizardController.this);
                        triggerAnchor.addClickHandler(removeWizardClickHandlerForTriggerJob);
                        contentPanel.add(triggerAnchor);

                        final Anchor schedulingAnchor = new Anchor("Set up a job schedule");
                        schedulingAnchor.addStyleName("ScheduleJob");
                        ClickHandler customizeScheduleClickHandler = new CustomizeScheduleClickHandler(null,
                                schedulingServiceAsync, getTenant(), scheduleDefinitionForJob);
                        ClickHandler removeWizardClickHandlerForCustomizeSchedule = new RemoveWizardClickHandler(
                                customizeScheduleClickHandler, JobWizardController.this);
                        schedulingAnchor.addClickHandler(removeWizardClickHandlerForCustomizeSchedule);

                        contentPanel.add(schedulingAnchor);
                        contentPanel.remove(loadingIndicator);
                    }
                }, jobName);
            }

            setContent(contentPanel);
            getWizardPanel().getButtonPanel().clear();
            getWizardPanel().getButtonPanel().addButton(button);
            // getWizardPanel().center();
        }

    }

    private void getSchedule(final Runnable runnable, final String jobName) {
        schedulingServiceAsync.getSchedules(clientConfig.getTenant(), new DCAsyncCallback<List<ScheduleDefinition>>() {
            @Override
            public void onSuccess(List<ScheduleDefinition> result) {
                for (ScheduleDefinition scheduleDefinition : result) {
                    if (scheduleDefinition.getJob().getName().equals(jobName)) {
                        scheduleDefinitionForJob = scheduleDefinition;
                        runnable.run();
                    }
                }
            }
        });

    }
}