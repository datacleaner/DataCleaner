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

import org.eobjects.datacleaner.monitor.shared.WizardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardPage;
import org.eobjects.datacleaner.monitor.shared.model.WizardSessionIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Command that starts a job wizard. Used by the {@link CreateJobButton}'s drop
 * down menu.
 */
final class StartJobWizardCommand implements Command {

    private final WizardServiceAsync _service;
    private final TenantIdentifier _tenant;
    private final WizardIdentifier _wizard;
    private final LoadingIndicator _loadingIndicator;
    private final WizardProgressBar _progressBar;
    private final SimplePanel _targetPanel;
    private WizardPanel _currentPanel;

    public StartJobWizardCommand(WizardServiceAsync service, TenantIdentifier tenant, WizardIdentifier wizard) {
        _service = service;
        _tenant = tenant;
        _wizard = wizard;
        _loadingIndicator = new LoadingIndicator();
        _progressBar = new WizardProgressBar();
        _progressBar.setSteps(wizard.getExpectedPageCount() + 1);
        _progressBar.setProgress(0);
        
        _targetPanel = new SimplePanel();
    }

    @Override
    public void execute() {
        final StartWizardPanel startWizardPanel = new StartWizardPanel(_service, _tenant, _wizard, _progressBar);
        _currentPanel = startWizardPanel;
        _targetPanel.setWidget(_currentPanel);

        final Button nextStepButton = new Button("Next");
        
        final FlowPanel popupContent = new FlowPanel();
        popupContent.add(_progressBar);
        popupContent.add(_targetPanel);

        final DCPopupPanel popup = new DCPopupPanel("New job: " + _wizard.getDisplayName());
        popup.addStyleName("JobWizardPopupPanel");
        popup.setAutoHideEnabled(false);
        popup.setWidget(popupContent);
        popup.addButton(nextStepButton);
        popup.addButton(new CancelPopupButton(popup));

        nextStepButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                _targetPanel.setWidget(_loadingIndicator);
                _currentPanel.requestNextPage(new DCAsyncCallback<WizardPage>() {
                    @Override
                    public void onSuccess(WizardPage page) {
                        if (page == null) {
                            _currentPanel = null;
                            wizardFinished(popup, _targetPanel);
                        } else {
                            _progressBar.setSteps(page.getExpectedPageCount() + 1);
                            _progressBar.setProgress(page.getPageIndex() + 1);
                            _currentPanel = new FormWizardPanel(_service, _tenant, page);

                            _targetPanel.setWidget(_currentPanel);
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
                WizardSessionIdentifier sessionIdentifier = _currentPanel.getSessionIdentifier();
                if (sessionIdentifier == null) {
                    // session not started yet
                    return;
                }
                // cancel the wizard
                _service.cancelWizard(_tenant, sessionIdentifier, new DCAsyncCallback<Boolean>() {
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

    private void wizardFinished(DCPopupPanel popup, SimplePanel targetPanel) {
        final CancelPopupButton button = new CancelPopupButton(popup, "Close");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // full page refresh.
                Window.Location.reload();
            }
        });

        targetPanel.setWidget(new Label("Job created! Wizard finished."));
        popup.getButtonPanel().clear();
        popup.addButton(button);
        popup.center();
    }

}
