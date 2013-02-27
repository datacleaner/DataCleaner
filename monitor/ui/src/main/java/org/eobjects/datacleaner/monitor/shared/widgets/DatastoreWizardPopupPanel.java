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

import java.util.ArrayList;
import java.util.List;

import org.eobjects.datacleaner.monitor.shared.WizardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardPage;
import org.eobjects.datacleaner.monitor.shared.model.WizardSessionIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A popup for a datastore wizard.
 */
public class DatastoreWizardPopupPanel extends DCPopupPanel {

    private final WizardServiceAsync _service;
    private final TenantIdentifier _tenant;
    private final LoadingIndicator _loadingIndicator;
    private final WizardProgressBar _progressBar;
    private final SimplePanel _targetPanel;
    private final Button _nextStepButton;

    // always holds the current "click handler registration" of the next step
    // button
    private HandlerRegistration _clickRegistration;
    private WizardPanel _currentPanel;

    public DatastoreWizardPopupPanel(WizardServiceAsync service, TenantIdentifier tenant) {
        super("New datastore");

        addStyleName("DatastoreWizardPopupPanel");

        _service = service;
        _tenant = tenant;
        _loadingIndicator = new LoadingIndicator();
        _progressBar = new WizardProgressBar();
        _progressBar.setSteps(1, true);
        _progressBar.setProgress(0);

        _targetPanel = new SimplePanel();
        _targetPanel.setWidget(_loadingIndicator);

        _service.getDatastoreWizardIdentifiers(_tenant, new DCAsyncCallback<List<WizardIdentifier>>() {
            @Override
            public void onSuccess(List<WizardIdentifier> wizards) {
                showWizardSelection(wizards);
            }
        });

        final FlowPanel popupContent = new FlowPanel();
        popupContent.add(_progressBar);
        popupContent.add(_targetPanel);
        setWidget(popupContent);

        _nextStepButton = new Button("Next");
        addButton(_nextStepButton);
        addButton(new CancelPopupButton(this));

        addCloseHandler(new CloseHandler<PopupPanel>() {
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
    }

    protected void showWizardSelection(final List<WizardIdentifier> wizards) {
        final FlowPanel panel = new FlowPanel();

        final TextBox nameTextBox = new TextBox();

        panel.add(new Label("Please select the datastore type:"));

        final List<RadioButton> radios = new ArrayList<RadioButton>(wizards.size());

        for (final WizardIdentifier wizard : wizards) {
            final RadioButton radio = new RadioButton("wizardIdentifier", wizard.getDisplayName());
            radio.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    _progressBar.setSteps(wizard.getExpectedPageCount() + 1);
                    _progressBar.setProgress(0);
                }
            });
            panel.add(radio);
            radios.add(radio);
        }

        panel.add(new Label("Please name the datastore you are about to create:"));
        panel.add(nameTextBox);

        _clickRegistration = _nextStepButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final String datastoreName = nameTextBox.getText();
                if (datastoreName == null || datastoreName.trim().isEmpty()) {
                    throw new DCUserInputException("Please enter a valid datastore name");
                }
                for (int i = 0; i < radios.size(); i++) {
                    final RadioButton radio = radios.get(i);
                    if (radio.getValue().booleanValue()) {
                        final WizardIdentifier wizard = wizards.get(i);
                        _targetPanel.setWidget(_loadingIndicator);
                        setHeader("New datastore: " + datastoreName);
                        _service.startDatastoreWizard(_tenant, wizard, datastoreName, createNextPageCallback());
                        return;
                    }
                }
            }
        });

        _targetPanel.setWidget(panel);
        center();
    }

    protected AsyncCallback<WizardPage> createNextPageCallback() {
        return new DCAsyncCallback<WizardPage>() {
            @Override
            public void onSuccess(final WizardPage page) {
                if (page == null) {
                    wizardFinished();
                } else {
                    _progressBar.setSteps(page.getExpectedPageCount() + 1);
                    _progressBar.setProgress(page.getPageIndex() + 1);

                    _currentPanel = new FormWizardPanel(_service, _tenant, page);

                    _targetPanel.setWidget(_currentPanel);

                    _clickRegistration.removeHandler();
                    _clickRegistration = _nextStepButton.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            _targetPanel.setWidget(_loadingIndicator);
                            _currentPanel.requestNextPage(createNextPageCallback());
                        }
                    });

                    DatastoreWizardPopupPanel.this.center();
                }
            }
        };
    }

    private void wizardFinished() {
        final Button button = new Button("Close");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // full page refresh.
                Window.Location.reload();
            }
        });

        _targetPanel.setWidget(new Label("Datastore created! Wizard finished."));
        getButtonPanel().clear();
        addButton(button);
        center();
    }
}
