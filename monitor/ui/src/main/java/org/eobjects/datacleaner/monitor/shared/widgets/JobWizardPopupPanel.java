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

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A popup for a job wizard
 */
public class JobWizardPopupPanel extends AbstractWizardPopupPanel {

    private final DatastoreServiceAsync datastoreService = GWT.create(DatastoreService.class);
    private final int _stepsBeforeWizardPages;

    public JobWizardPopupPanel(WizardServiceAsync service, TenantIdentifier tenant) {
        this(service, tenant, null);
    }

    public JobWizardPopupPanel(WizardServiceAsync service, TenantIdentifier tenant, DatastoreIdentifier datastore) {
        super("New job", service, tenant);
        addStyleName("JobWizardPopupPanel");

        if (datastore == null) {
            _stepsBeforeWizardPages = 2;
            showDatastoreSelection();
        } else {
            _stepsBeforeWizardPages = 1;
            showWizardSelection(datastore);
        }
    }

    @Override
    protected int getStepsBeforeWizardPages() {
        return _stepsBeforeWizardPages;
    }

    private void showDatastoreSelection() {
        datastoreService.getAvailableDatastores(_tenant, new DCAsyncCallback<List<DatastoreIdentifier>>() {
            @Override
            public void onSuccess(List<DatastoreIdentifier> datastores) {
                showDatastoreSelection(datastores);
            }
        });
    }

    private void showDatastoreSelection(final List<DatastoreIdentifier> datastores) {
        final FlowPanel panel = new FlowPanel();

        panel.add(new Label("Please select the source datastore of the job:"));

        final List<RadioButton> radios = new ArrayList<RadioButton>(datastores.size());

        for (final DatastoreIdentifier datastore : datastores) {
            final RadioButton radio = new RadioButton("datastoreName", datastore.getName());
            panel.add(radio);
            radios.add(radio);
        }

        setNextClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                for (int i = 0; i < radios.size(); i++) {
                    final RadioButton radio = radios.get(i);
                    if (radio.getValue().booleanValue()) {
                        final DatastoreIdentifier datastore = datastores.get(i);

                        showWizardSelection(datastore);
                        return;
                    }
                }
            }
        });

        setContent(panel);
        center();
    }

    protected void showWizardSelection(final DatastoreIdentifier datastore) {
        setLoading();
        setHeader("New job: " + datastore.getName());

        _service.getJobWizardIdentifiers(_tenant, datastore, new DCAsyncCallback<List<WizardIdentifier>>() {
            @Override
            public void onSuccess(List<WizardIdentifier> wizards) {
                showWizardSelection(datastore, wizards);
            }
        });
    }

    protected void showWizardSelection(final DatastoreIdentifier datastore, final List<WizardIdentifier> wizards) {
        final int progress = _stepsBeforeWizardPages - 1;

        final FlowPanel panel = new FlowPanel();

        panel.add(new Label("Please select the job type:"));

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

        final TextBox nameTextBox = new TextBox();
        nameTextBox.addStyleName("NameTextBox");

        final Label nameLabel = new Label("Please name the job you are about to create:");
        nameLabel.addStyleName("NameLabel");

        panel.add(nameLabel);
        panel.add(nameTextBox);

        setNextClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final String jobName = nameTextBox.getText();
                if (jobName == null || jobName.trim().isEmpty()) {
                    throw new DCUserInputException("Please enter a valid job name");
                }
                for (int i = 0; i < radios.size(); i++) {
                    final RadioButton radio = radios.get(i);
                    if (radio.getValue().booleanValue()) {
                        final WizardIdentifier wizard = wizards.get(i);
                        setLoading();
                        setHeader("New job: " + jobName);
                        _service.startJobWizard(_tenant, wizard, datastore, jobName, createNextPageCallback());
                        return;
                    }
                }

                // no job wizard is selected if we reach this point
                Window.alert("Please select a job type to create");
            }
        });

        setProgress(progress);
        setContent(panel);
        center();
    }

    @Override
    protected void wizardFinished(String jobName) {
        final Button button = new Button("Close");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // full page refresh.
                Window.Location.reload();
            }
        });

        setContent(new Label("Job '" + jobName + "' created! Wizard finished."));
        getButtonPanel().clear();
        addButton(button);
        center();
    }

}
