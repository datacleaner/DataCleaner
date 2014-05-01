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

import java.util.ArrayList;
import java.util.List;

import org.eobjects.datacleaner.monitor.scheduling.SchedulingService;
import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.scheduling.widgets.CustomizeScheduleClickHandler;
import org.eobjects.datacleaner.monitor.scheduling.widgets.TriggerJobClickHandler;
import org.eobjects.datacleaner.monitor.shared.ClientConfig;
import org.eobjects.datacleaner.monitor.shared.DictionaryClientConfig;
import org.eobjects.datacleaner.monitor.shared.WizardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.LoadingIndicator;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;

public class JobWizardController extends AbstractWizardController<WizardServiceAsync> {

    private final SchedulingServiceAsync schedulingServiceAsync = GWT.create(SchedulingService.class);

    private final ClientConfig clientConfig = new DictionaryClientConfig();
    private final DatastoreIdentifier _datastoreIdentifier;
    private ScheduleDefinition scheduleDefinitionForJob = null;
    private int _stepsBeforeWizardPages;

    public JobWizardController(WizardPanel wizardPanel, TenantIdentifier tenant, WizardIdentifier wizardIdentifier,
            DatastoreIdentifier datastoreIdentifier, WizardServiceAsync wizardService) {
        super(wizardPanel, tenant, wizardIdentifier, wizardService);
        _datastoreIdentifier = datastoreIdentifier;

        _stepsBeforeWizardPages = 0;
        if (wizardIdentifier == null) {
            _stepsBeforeWizardPages++;
        }

        if (_datastoreIdentifier == null) {
            _stepsBeforeWizardPages++;
        }
    }

    @Override
    protected void startWizard() {
        final WizardIdentifier wizardIdentifier = getWizardIdentifier();

        if (_datastoreIdentifier == null) {
            if (wizardIdentifier == null || wizardIdentifier.isDatastoreConsumer()) {
                _stepsBeforeWizardPages = 2;
                showDatastoreSelection();
                return;
            }
        }

        if (wizardIdentifier == null) {
            _stepsBeforeWizardPages = 1;
            showWizardSelection();
            return;
        }
        
        _stepsBeforeWizardPages = 0;
        getWizardPanel().setHeader("Build job: " + wizardIdentifier.getDisplayName());

        setLoading();
        WizardServiceAsync wizardService = getWizardService();
        wizardService.startJobWizard(getTenant(), wizardIdentifier, _datastoreIdentifier, getLocaleName(),
                createNextPageCallback());
    }

    @Override
    protected int getStepsBeforeWizardPages() {
        return _stepsBeforeWizardPages;
    }

    @Override
    protected void wizardFinished(final String jobName) {
        final Button button = new Button("Close");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                redirectToAnotherPage();
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
        }

    }

    public static native void redirectToAnotherPage() /*-{
                                                      $doc.redirectToAnotherPage();
                                                      }-*/;

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
    
    protected void showDatastoreSelection() {
        // TODO
    }

    protected void showWizardSelection() {
        setLoading();

        if (_datastoreIdentifier == null) {
            getWizardPanel().setHeader("Build job");
        } else {
            getWizardPanel().setHeader("Build job: " + _datastoreIdentifier.getName());
        }

        getWizardService().getJobWizardIdentifiers(getTenant(), _datastoreIdentifier, getLocaleName(),
                new DCAsyncCallback<List<WizardIdentifier>>() {
                    @Override
                    public void onSuccess(List<WizardIdentifier> wizards) {
                        showWizardSelection(wizards);
                    }
                });
    }

    protected void showWizardSelection(final List<WizardIdentifier> wizards) {
        final int progress = _stepsBeforeWizardPages - 1;

        final FlowPanel panel = new FlowPanel();

        panel.add(new Label("Please select the type of job to build:"));

        final List<RadioButton> radios = new ArrayList<RadioButton>(wizards.size());

        if (wizards == null || wizards.isEmpty()) {
            panel.add(new Label("(no job wizards available)"));
        } else {
            for (final WizardIdentifier wizard : wizards) {
                final RadioButton radio = new RadioButton("wizardIdentifier", wizard.getDisplayName());
                radio.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        setSteps(wizard.getExpectedPageCount() + getStepsBeforeWizardPages());
                        setProgress(progress);
                    }
                });
                panel.add(radio);
                radios.add(radio);
            }

        }

        setNextClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                for (int i = 0; i < radios.size(); i++) {
                    final RadioButton radio = radios.get(i);
                    if (radio.getValue().booleanValue()) {
                        final WizardIdentifier wizard = wizards.get(i);
                        setWizardIdentifier(wizard);
                        startWizard();
                        return;
                    }
                }

                // no job wizard is selected if we reach this point
                throw new DCUserInputException("Please select a job type to create");
            }
        });

        setProgress(progress);
        setContent(panel);
    }
}