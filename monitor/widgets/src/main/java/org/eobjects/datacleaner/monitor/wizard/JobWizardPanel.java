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

import org.eobjects.datacleaner.monitor.shared.WizardNavigationServiceAsync;
import org.eobjects.datacleaner.monitor.shared.WizardService;
import org.eobjects.datacleaner.monitor.shared.WizardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.util.Urls;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class JobWizardPanel extends AbstractWizardController<WizardNavigationServiceAsync> {

    private final static WizardServiceAsync service = GWT.create(WizardService.class);

    public JobWizardPanel(String tenantName, String panelType, String datastoreName, String wizardDisplayName, String htmlDivNameToShowWizardIn) {
        super("Build job: " + wizardDisplayName, service, tenantName, panelType, htmlDivNameToShowWizardIn);

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
    protected void wizardFinished(String jobName) {
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
        getWizardPanel().getButtonPanel().clear();
        getWizardPanel().addButton(button);
        getWizardPanel().center();
    }

}
