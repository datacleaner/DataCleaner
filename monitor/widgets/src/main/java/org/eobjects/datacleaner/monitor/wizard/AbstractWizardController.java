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

import org.eobjects.datacleaner.monitor.shared.JavaScriptCallbacks;
import org.eobjects.datacleaner.monitor.shared.WizardNavigationServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardPage;
import org.eobjects.datacleaner.monitor.shared.model.WizardSessionIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.FileUploadFunctionHandler;
import org.eobjects.datacleaner.monitor.shared.widgets.FormWizardClientController;
import org.eobjects.datacleaner.monitor.shared.widgets.LoadingIndicator;
import org.eobjects.datacleaner.monitor.shared.widgets.WizardClientController;
import org.eobjects.datacleaner.monitor.shared.widgets.WizardProgressBar;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;
import org.eobjects.datacleaner.monitor.util.Urls;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * It is the abstract implementation of panel builder. This class can create a
 * wizard panel of type {@link WizardPanel} based on user input. It also
 * provides controls for Wizard.
 * 
 * @param <S>
 */
public abstract class AbstractWizardController<S extends WizardNavigationServiceAsync> {

    private final S _wizardService;
    private final TenantIdentifier _tenant;
    private final WizardPanel _wizardPanel;

    protected final LoadingIndicator _loadingIndicator;
    private final Button _nextStepButton;
    private final Button _previousStepButton;

    // always holds the current "click handler registration" of the next step
    // button
    private HandlerRegistration _nextButtonClickRegistration;

    // always holds the current "click handler registration" of the previous
    // step button
    private HandlerRegistration _previousButtonClickRegistration;

    private WizardIdentifier _wizardIdentifier;
    private WizardClientController _currentController;

    public AbstractWizardController(WizardPanel wizardPanel, TenantIdentifier tenant,
            WizardIdentifier wizardIdentifier, S wizardService) {
        _wizardPanel = wizardPanel;
        _wizardIdentifier = wizardIdentifier;
        _wizardService = wizardService;
        _tenant = tenant;

        FileUploadFunctionHandler.exportFileUploadFunction();

        _loadingIndicator = new LoadingIndicator();
        _wizardPanel.setContent(_loadingIndicator);

        _previousStepButton = new Button("‹ Back");
        _previousStepButton.setEnabled(false);
        _wizardPanel.getButtonPanel().addButton(_previousStepButton);

        _nextStepButton = new Button("Next ›");
        _wizardPanel.getButtonPanel().addButton(_nextStepButton);
        Button cancelButton = new Button("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                cancelWizard();
            }
        });
        _wizardPanel.getButtonPanel().addButton(cancelButton);
    }

    /**
     * Cancels/stops the wizard, removing it from the UI.
     */
    public void cancelWizard() {
        _wizardPanel.hideWizard();

        if (_currentController == null) {
            // wizard never started, or already ended
            return;
        }

        final WizardSessionIdentifier sessionIdentifier = _currentController.getSessionIdentifier();
        if (sessionIdentifier == null) {
            // session not started yet
            return;
        }

        // cancel the wizard on the server
        _wizardService.cancelWizard(_tenant, sessionIdentifier, new DCAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(final Boolean result) {
                assert result.booleanValue();
            }
        });

        JavaScriptCallbacks.onWizardCancelled(getWizardIdentifier().getDisplayName());
    }

    /**
     * Starts the wizard, eventually showing stuff on the UI.
     */
    public abstract void startWizard();

    /**
     * Gets the number of steps to add before the wizard pages' steps in the
     * {@link WizardProgressBar}.
     * 
     * @return
     */
    protected abstract int getStepsBeforeWizardPages();

    /**
     * Invoked when the wizard has finished.
     * 
     * @param resultEntityName
     *            the resulting string object of the wizard. Usually identifies
     *            the name/id of the thing that was built with the wizard.
     */
    protected abstract void wizardFinished(String resultEntityName);

    protected final void setLoading() {
        setContent(_loadingIndicator);
    }

    protected final void setProgress(int stepIndex) {
        _wizardPanel.getProgressBar().setProgress(stepIndex);
    }

    protected final void setSteps(int steps) {
        _wizardPanel.getProgressBar().setSteps(steps);
    }

    protected final void setSteps(int steps, boolean indicateMore) {
        _wizardPanel.getProgressBar().setSteps(steps, indicateMore);
    }

    protected final void setContent(IsWidget w) {
        _wizardPanel.setContent(w);
    }

    protected final void setPreviousClickHandler(ClickHandler clickHandler) {
        if (_previousButtonClickRegistration != null) {
            _previousButtonClickRegistration.removeHandler();
        }
        if (clickHandler == null) {
            _previousStepButton.setEnabled(false);
        } else {
            _previousButtonClickRegistration = _previousStepButton.addClickHandler(clickHandler);
            _previousStepButton.setEnabled(true);
        }
    }

    protected final void setNextClickHandler(ClickHandler clickHandler) {
        if (_nextButtonClickRegistration != null) {
            _nextButtonClickRegistration.removeHandler();
        }
        _nextButtonClickRegistration = _nextStepButton.addClickHandler(clickHandler);
    }

    protected final AsyncCallback<WizardPage> createNextPageCallback() {
        return new DCAsyncCallback<WizardPage>() {
            @Override
            public void onSuccess(final WizardPage page) {
                if (page.isFinished()) {
                    wizardFinished(page.getWizardResult());
                } else {
                    _wizardPanel.getProgressBar().setSteps(page.getExpectedPageCount() + getStepsBeforeWizardPages());
                    _wizardPanel.getProgressBar().setProgress(page.getPageIndex() + getStepsBeforeWizardPages());

                    _currentController = new FormWizardClientController(_wizardService, _tenant, page);

                    setContent(_currentController);

                    addNextClickHandler();

                    if (page.getPageIndex() > 0) {
                        setPreviousClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                setContent(_loadingIndicator);
                                _currentController.requestPreviousPage(createNextPageCallback());
                            }
                        });
                    } else {
                        setPreviousClickHandler(null);
                    }

                }
            }

            private void addNextClickHandler() {
                setNextClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        _nextButtonClickRegistration.removeHandler();
                        setContent(_loadingIndicator);
                        _currentController.requestNextPage(createNextPageCallback());
                    }
                });
            }

            @Override
            public void onFailure(Throwable e) {
                addNextClickHandler();
                if (e instanceof DCUserInputException) {
                    // restore the previous panel view
                    setContent(_currentController);
                }
                super.onFailure(e);
            }
        };
    }

    /**
     * Gets the {@link WizardIdentifier} of the wizard being controlled.
     * 
     * @return
     */
    public WizardIdentifier getWizardIdentifier() {
        return _wizardIdentifier;
    }

    /**
     * Sets the {@link WizardIdentifier} of the wizard being controlled.
     * 
     * @param wizardIdentifier
     */
    public void setWizardIdentifier(WizardIdentifier wizardIdentifier) {
        _wizardIdentifier = wizardIdentifier;
    }

    /**
     * Gets the wizard controlling service
     * 
     * @return
     */
    public S getWizardService() {
        return _wizardService;
    }

    /**
     * Gets the locale name for inclusion in many of the service requests.
     * 
     * @return
     */
    protected String getLocaleName() {
        LocaleInfo locale = LocaleInfo.getCurrentLocale();
        String localeName = locale.getLocaleName();
        return localeName;
    }

    /**
     * Returns the wizard panel instance
     * 
     * @return
     */
    public WizardPanel getWizardPanel() {
        return _wizardPanel;
    }

    /**
     * Gets the current tenant id.
     * 
     * @return
     */
    public TenantIdentifier getTenant() {
        return _tenant;
    }

    /**
     * Closes (and hides) the wizard after finishing. Call this method from any
     * events that should hide the wizard after the job has finished.
     * 
     * @param string
     */
    protected final void closeWizardAfterFinishing(String resultEntityName, String defaultUrlToGoTo) {
        getWizardPanel().hideWizard();
        final String displayName = getWizardIdentifier().getDisplayName();
        boolean callbackExecuted = JavaScriptCallbacks.onWizardFinished(displayName, resultEntityName);

        if (!callbackExecuted && defaultUrlToGoTo != null) {
            String url = Urls.createRelativeUrl(defaultUrlToGoTo);
            Urls.assign(url);
        }
    }
}
