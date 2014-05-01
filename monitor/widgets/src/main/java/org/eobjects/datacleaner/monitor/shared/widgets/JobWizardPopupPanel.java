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
package org.eobjects.datacleaner.monitor.shared.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.datacleaner.monitor.shared.DatastoreService;
import org.eobjects.datacleaner.monitor.shared.DatastoreServiceAsync;
import org.eobjects.datacleaner.monitor.shared.WizardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;
import org.eobjects.datacleaner.monitor.util.Urls;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;

/**
 * A popup for a job wizard
 */
public class JobWizardPopupPanel extends AbstractWizardPopupPanel<WizardServiceAsync> {

    private final DatastoreServiceAsync datastoreService = GWT.create(DatastoreService.class);

    private List<DatastoreIdentifier> _datastores;
    private List<WizardIdentifier> _wizards;
    private int _stepsBeforeWizardPages;

    public JobWizardPopupPanel(WizardServiceAsync service, TenantIdentifier tenant) {
        this(service, tenant, null);
    }

    public JobWizardPopupPanel(WizardServiceAsync service, TenantIdentifier tenant, DatastoreIdentifier datastore) {
        super("Build job", service, tenant);
        addStyleName("JobWizardPopupPanel");

        if (datastore == null) {
            _stepsBeforeWizardPages = 2;
            final FlowPanel outerPanel = new FlowPanel();
            outerPanel.setStyleName("InitialSelectionOuterPanel");
            final List<RadioButton> datastoreRadios = new ArrayList<RadioButton>();
            showDatastoreSelection(outerPanel, datastoreRadios);

            final List<RadioButton> wizardRadios = new ArrayList<RadioButton>();
            showNonDatastoreConsumingWizardSelection(outerPanel, wizardRadios);

            setNextClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    for (int i = 0; i < datastoreRadios.size(); i++) {
                        final RadioButton radio = datastoreRadios.get(i);
                        if (radio.getValue().booleanValue()) {
                            final DatastoreIdentifier datastore = _datastores.get(i);
                            showWizardSelection(datastore);
                            return;
                        }
                    }

                    for (int i = 0; i < wizardRadios.size(); i++) {
                        final RadioButton radio = wizardRadios.get(i);
                        if (radio.getValue().booleanValue()) {
                            final WizardIdentifier wizard = _wizards.get(i);
                            startWizard(wizard, null);
                            return;
                        }
                    }
                }
            });

            setContent(outerPanel);
        } else {
            _stepsBeforeWizardPages = 1;
            showWizardSelection(datastore);
        }
    }

    @Override
    protected int getStepsBeforeWizardPages() {
        return _stepsBeforeWizardPages;
    }

    private void showDatastoreSelection(final Panel outerPanel, final List<RadioButton> radios) {
        final FlowPanel datastoreSelectionPanel = new FlowPanel();
        outerPanel.add(datastoreSelectionPanel);

        datastoreService.getAvailableDatastores(_tenant, new DCAsyncCallback<List<DatastoreIdentifier>>() {
            @Override
            public void onSuccess(List<DatastoreIdentifier> datastores) {
                _datastores = datastores;
                showDatastoreSelection(datastoreSelectionPanel, datastores, radios);
            }
        });
    }

    private void showNonDatastoreConsumingWizardSelection(final Panel outerPanel, final List<RadioButton> radios) {
        final FlowPanel wizardSelectionPanel = new FlowPanel();
        outerPanel.add(wizardSelectionPanel);

        _service.getNonDatastoreConsumingJobWizardIdentifiers(_tenant, getLocaleName(),
                new DCAsyncCallback<List<WizardIdentifier>>() {
                    @Override
                    public void onSuccess(List<WizardIdentifier> wizards) {
                        _wizards = wizards;
                        showNonDatastoreConsumingWizardSelection(wizardSelectionPanel, wizards, radios);
                    }
                });
    }

    private void showNonDatastoreConsumingWizardSelection(final FlowPanel panel, final List<WizardIdentifier> wizards,
            final List<RadioButton> radios) {
        if (wizards == null || wizards.isEmpty()) {
            // do nothing
            return;
        }

        panel.add(new Label("Or select a different job type ..."));
        for (final WizardIdentifier wizard : wizards) {
            final RadioButton radio = new RadioButton("initialSelection", wizard.getDisplayName());
            radio.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    GWT.log("Clicked: " + wizard + " - expected " + wizard.getExpectedPageCount() + " pages");
                    _stepsBeforeWizardPages = 1;
                    setSteps(wizard.getExpectedPageCount() + getStepsBeforeWizardPages(), false);
                    setProgress(0);
                }
            });
            panel.add(radio);
            radios.add(radio);
        }

        center();
    }

    private void showDatastoreSelection(final FlowPanel panel, final List<DatastoreIdentifier> datastores,
            final List<RadioButton> radios) {
        panel.add(new Label("Please select the source datastore of your job ..."));

        for (final DatastoreIdentifier datastore : datastores) {
            final RadioButton radio = new RadioButton("initialSelection", datastore.getName());
            radio.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    GWT.log("Clicked: " + datastore);
                    _stepsBeforeWizardPages = 2;
                    setSteps(getStepsBeforeWizardPages(), true);
                    setProgress(0);
                }
            });
            radios.add(radio);
            panel.add(radio);
        }
        center();
    }

    protected void showWizardSelection(final DatastoreIdentifier datastore) {
        setLoading();
        setHeader("Build job: " + datastore.getName());

        _service.getJobWizardIdentifiers(_tenant, datastore, getLocaleName(),
                new DCAsyncCallback<List<WizardIdentifier>>() {
                    @Override
                    public void onSuccess(List<WizardIdentifier> wizards) {
                        showWizardSelection(datastore, wizards);
                    }
                });
    }

    protected void showWizardSelection(final DatastoreIdentifier datastore, final List<WizardIdentifier> wizards) {
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
                        startWizard(wizard, datastore);
                        return;
                    }
                }

                // no job wizard is selected if we reach this point
                throw new DCUserInputException("Please select a job type to create");
            }
        });

        setProgress(progress);
        setContent(panel);
        center();
    }

    private void startWizard(WizardIdentifier wizard, DatastoreIdentifier datastore) {
        setLoading();
        setHeader("Build job: " + wizard.getDisplayName());
        _service.startJobWizard(_tenant, wizard, datastore, getLocaleName(), createNextPageCallback());
        return;
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
        contentPanel.add(new Label(
                "Close the dialog to return, or click one of the links below to start using the job."));

        if (jobName != null) {
            final String encodedJobName = URL.encodeQueryString(jobName);

            final Anchor triggerAnchor = new Anchor("Run this job now");
            triggerAnchor.addStyleName("TriggerJob");
            triggerAnchor.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    final String url = Urls.createRelativeUrl("scheduling.jsf#trigger_" + encodedJobName);
                    Urls.assign(url);
                }
            });

            final Anchor monitorAnchor = new Anchor("Monitor this job's metrics on the dashboard");
            monitorAnchor.addStyleName("MonitorJob");
            monitorAnchor.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    final String url = Urls.createRelativeUrl("dashboard.jsf#new_timeline_" + encodedJobName);
                    Urls.assign(url);
                }
            });

            final Anchor schedulingAnchor = new Anchor("Set up a job schedule");
            schedulingAnchor.addStyleName("ScheduleJob");
            schedulingAnchor.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    final String url = Urls.createRelativeUrl("scheduling.jsf#schedule_" + encodedJobName);
                    Urls.assign(url);
                }
            });

            contentPanel.add(triggerAnchor);
            contentPanel.add(monitorAnchor);
            contentPanel.add(schedulingAnchor);
        }

        setContent(contentPanel);
        getButtonPanel().clear();
        addButton(button);
        center();
    }

}
