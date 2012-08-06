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
package org.eobjects.datacleaner.monitor.shared.widgets;

import org.eobjects.datacleaner.monitor.shared.JobWizardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardPage;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardSessionIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Command that starts a job wizard. Used by the {@link CreateJobButton}'s drop
 * down menu.
 */
final class StartWizardCommand implements Command {

    private final JobWizardServiceAsync _service;
    private final TenantIdentifier _tenant;
    private final JobWizardIdentifier _wizard;
    private final LoadingIndicator _loadingIndicator;

    private WizardPanel _currentPanel;

    public StartWizardCommand(JobWizardServiceAsync service, TenantIdentifier tenant, JobWizardIdentifier wizard) {
        _service = service;
        _tenant = tenant;
        _wizard = wizard;
        _loadingIndicator = new LoadingIndicator();
    }

    @Override
    public void execute() {
        final StartWizardPanel startWizardPanel = new StartWizardPanel(_service, _tenant, _wizard);
        _currentPanel = startWizardPanel;

        final Button nextStepButton = new Button("Next");

        final DCPopupPanel popup = new DCPopupPanel("New job: " + _wizard.getDisplayName());
        popup.addStyleName("JobWizardPopupPanel");
        popup.setAutoHideEnabled(false);
        popup.setWidget(startWizardPanel);
        popup.addButton(nextStepButton);
        popup.addButton(new CancelPopupButton(popup));

        nextStepButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popup.setWidget(_loadingIndicator);
                _currentPanel.requestNextPage(new DCAsyncCallback<JobWizardPage>() {
                    @Override
                    public void onSuccess(JobWizardPage result) {
                        if (result == null) {
                            _currentPanel = null;
                            wizardFinished(popup);
                        } else {
                            _currentPanel = new FormWizardPanel(_service, _tenant, result);
                            popup.setWidget(_currentPanel);
                            popup.center();
                        }
                    }
                });
            }
        });

        popup.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                if (_currentPanel == null) {
                    // wizard ended
                    return;
                }
                JobWizardSessionIdentifier sessionIdentifier = _currentPanel.getSessionIdentifier();
                if (sessionIdentifier == null) {
                    // session not started yet
                    return;
                }
                // cancel the wizard
                _service.cancelWizard(_tenant, startWizardPanel.getSessionIdentifier(), new DCAsyncCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        assert result.booleanValue();
                    }
                });
            }
        });

        popup.center();
        popup.show();
    }

    private void wizardFinished(DCPopupPanel popup) {
        popup.setWidget(new Label("Job created! Wizard finished."));
        popup.getButtonPanel().clear();
        popup.addButton(new CancelPopupButton(popup, "Close"));
        popup.center();
    }

}
