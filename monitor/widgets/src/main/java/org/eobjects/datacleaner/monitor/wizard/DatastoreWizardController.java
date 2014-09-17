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
package org.eobjects.datacleaner.monitor.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.datacleaner.monitor.shared.JavaScriptCallbacks;
import org.eobjects.datacleaner.monitor.shared.WizardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;
import org.eobjects.datacleaner.monitor.util.Urls;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;

/**
 * Wizard controller for Datastore wizards
 */
public class DatastoreWizardController extends AbstractWizardController<WizardServiceAsync> {

    private int _stepsBeforeWizardPages;

    public DatastoreWizardController(WizardPanel wizardPanel, TenantIdentifier tenant,
            WizardIdentifier wizardIdentifier, WizardServiceAsync wizardService) {
        super(wizardPanel, tenant, wizardIdentifier, wizardService);

        _stepsBeforeWizardPages = 0;
        if (wizardIdentifier == null) {
            _stepsBeforeWizardPages++;
        }
    }

    @Override
    public void startWizard() {
        getWizardPanel().addStyleClass("JobWizardPanel");
        getWizardPanel().showWizard();

        final WizardIdentifier wizardIdentifier = getWizardIdentifier();

        if (wizardIdentifier == null) {
            showWizardSelection();
            return;
        }

        getWizardPanel().setHeader("Register datastore: " + wizardIdentifier.getDisplayName());
        setLoading();

        WizardServiceAsync wizardService = getWizardService();
        wizardService.startDatastoreWizard(getTenant(), wizardIdentifier, getLocaleName(), createNextPageCallback());
    }

    private void showWizardSelection() {
        setLoading();
        getWizardPanel().setHeader("Register datastore");

        getWizardService().getDatastoreWizardIdentifiers(getTenant(), getLocaleName(),
                new DCAsyncCallback<List<WizardIdentifier>>() {
                    @Override
                    public void onSuccess(List<WizardIdentifier> wizards) {
                        showWizardSelection(wizards);
                    }
                });
    }

    private void showWizardSelection(final List<WizardIdentifier> wizards) {
        final FlowPanel panel = new FlowPanel();

        panel.add(new Label("Please select the type of datastore to register:"));

        final List<RadioButton> radios = new ArrayList<RadioButton>(wizards.size());

        if (wizards == null || wizards.isEmpty()) {
            panel.add(new Label("(no datastore wizards available)"));
        } else {
            for (final WizardIdentifier wizard : wizards) {
                final RadioButton radio = new RadioButton("wizardIdentifier", wizard.getDisplayName());
                radio.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        setSteps(wizard.getExpectedPageCount() + getStepsBeforeWizardPages());
                        setProgress(0);
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
            }
        });

        setContent(panel);
        getWizardPanel().refreshUI();
    }

    @Override
    protected int getStepsBeforeWizardPages() {
        return _stepsBeforeWizardPages;
    }

    @Override
    protected void wizardFinished(final String datastoreName) {
        final String encodedDatastoreName = URL.encodeQueryString(datastoreName);

        final Button button = new Button("Close");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // full page refresh.
                closeWizardAfterFinishing(datastoreName, "datastores.jsf");
            }
        });

        final Anchor jobWizardAnchor = new Anchor("Build a job for this datastore");
        jobWizardAnchor.addStyleName("BuildJob");
        jobWizardAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final String htmlDivId = getWizardPanel().getCustomHtmlDivId();
                closeWizardAfterFinishing(datastoreName, null);

                JavaScriptCallbacks.startJobWizard(datastoreName, null, htmlDivId);
            }
        });

        final Anchor queryAnchor = new Anchor("Explore / query this datastore");
        queryAnchor.addStyleName("QueryDatastore");
        queryAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final String url = Urls.createRelativeUrl("query.jsf?ds=" + encodedDatastoreName);
                Window.open(url, "_blank", "location=no,width=770,height=400,toolbar=no,menubar=no");
            }
        });

        final FlowPanel contentPanel = new FlowPanel();
        contentPanel.addStyleName("WizardFinishedPanel");
        contentPanel.add(new Label("Datastore '" + datastoreName + "' created! Wizard finished."));

        contentPanel.add(new Label(
                "Click 'Close' to return, or click one of the links below to start using the datastore."));
        contentPanel.add(jobWizardAnchor);
        contentPanel.add(queryAnchor);

        setContent(contentPanel);
        getWizardPanel().getButtonPanel().clear();
        getWizardPanel().getButtonPanel().addButton(button);
        getWizardPanel().refreshUI();
    }

}
