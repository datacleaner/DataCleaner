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
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardPage;
import org.eobjects.datacleaner.monitor.shared.model.WizardSessionIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.FileUploadFunctionHandler;
import org.eobjects.datacleaner.monitor.shared.widgets.FormWizardClientController;
import org.eobjects.datacleaner.monitor.shared.widgets.LoadingIndicator;
import org.eobjects.datacleaner.monitor.shared.widgets.WizardClientController;
import org.eobjects.datacleaner.monitor.shared.widgets.WizardProgressBar;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * It is the abstract implementation of panel builder. This class can create a
 * wizard panel of type {@link WizardPanel} based on user input. It also
 * provides controls for Wizard.
 * 
 * @param <S>
 */
public abstract class AbstractWizardController<S extends WizardNavigationServiceAsync> {

	protected final S _service;
	protected final TenantIdentifier _tenant;
	private final LoadingIndicator _loadingIndicator;
	private final WizardProgressBar _progressBar;
	private final SimplePanel _targetPanel;
	private final Button _nextStepButton;
	private final Button _previousStepButton;
	private final WizardPanel _wizardPanel;

	// always holds the current "click handler registration" of the next step
	// button
	private HandlerRegistration _nextButtonClickRegistration;

	// always holds the current "click handler registration" of the previous
	// step
	// button
	private HandlerRegistration _previousButtonClickRegistration;
	private WizardClientController _currentController;

	public AbstractWizardController(String heading, S service,
			TenantIdentifier tenant, String panelType) {

		_wizardPanel = WizardPanelFactory.getWizardPanel(panelType);
		// _wizardPanelwidget.setAutoHideEnabled(false);
		_wizardPanel.setHeader(heading);

		FileUploadFunctionHandler.exportFileUploadFunction();

		_service = service;
		_tenant = tenant;

		_loadingIndicator = new LoadingIndicator();
		_progressBar = new WizardProgressBar();

		_targetPanel = new SimplePanel();
		_targetPanel.setWidget(_loadingIndicator);

		final FlowPanel popupContent = new FlowPanel();
		popupContent.add(_progressBar);
		popupContent.add(_targetPanel);
		_wizardPanel.setWidget(popupContent);

		_previousStepButton = new Button("‹ Back");
		_previousStepButton.setEnabled(false);
		_wizardPanel.addButton(_previousStepButton);

		_nextStepButton = new Button("Next ›");
		_wizardPanel.addButton(_nextStepButton);
		Button cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				_wizardPanel.setVisible(false);
			}
		});
		_wizardPanel.addButton(cancelButton);

		_wizardPanel.addWizardCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				if (_currentController == null) {
					// wizard ended
					return;
				}
				WizardSessionIdentifier sessionIdentifier = _currentController
						.getSessionIdentifier();
				if (sessionIdentifier == null) {
					// session not started yet
					return;
				}
				// cancel the wizard
				_service.cancelWizard(_tenant, sessionIdentifier,
						new DCAsyncCallback<Boolean>() {
							@Override
							public void onSuccess(Boolean result) {
								assert result.booleanValue();
							}
						});
			}
		});
	}

	protected abstract int getStepsBeforeWizardPages();

	protected abstract void wizardFinished(String entityName);

	protected final void setLoading() {
		setContent(_loadingIndicator);
	}

	protected final void setProgress(int stepIndex) {
		_progressBar.setProgress(stepIndex);
	}

	protected final void setSteps(int steps) {
		_progressBar.setSteps(steps);
	}

	protected final void setSteps(int steps, boolean indicateMore) {
		_progressBar.setSteps(steps, indicateMore);
	}

	protected final void setContent(IsWidget w) {
		_targetPanel.setWidget(w);
	}

	protected final void setPreviousClickHandler(ClickHandler clickHandler) {
		if (_previousButtonClickRegistration != null) {
			_previousButtonClickRegistration.removeHandler();
		}
		if (clickHandler == null) {
			_previousStepButton.setEnabled(false);
		} else {
			_previousButtonClickRegistration = _previousStepButton
					.addClickHandler(clickHandler);
			_previousStepButton.setEnabled(true);
		}
	}

	protected final void setNextClickHandler(ClickHandler clickHandler) {
		if (_nextButtonClickRegistration != null) {
			_nextButtonClickRegistration.removeHandler();
		}
		_nextButtonClickRegistration = _nextStepButton
				.addClickHandler(clickHandler);
	}

	protected final AsyncCallback<WizardPage> createNextPageCallback() {
		return new DCAsyncCallback<WizardPage>() {
			@Override
			public void onSuccess(final WizardPage page) {
				if (page.isFinished()) {
					wizardFinished(page.getWizardResult());
				} else {
					_progressBar.setSteps(page.getExpectedPageCount()
							+ getStepsBeforeWizardPages());
					_progressBar.setProgress(page.getPageIndex()
							+ getStepsBeforeWizardPages());

					_currentController = new FormWizardClientController(
							_service, _tenant, page);

					_targetPanel.setWidget(_currentController);

					setNextClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							_nextButtonClickRegistration.removeHandler();
							_targetPanel.setWidget(_loadingIndicator);
							_currentController
									.requestNextPage(createNextPageCallback());
						}
					});

					if (page.getPageIndex() > 0) {
						setPreviousClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								_targetPanel.setWidget(_loadingIndicator);
								_currentController
										.requestPreviousPage(createNextPageCallback());
							}
						});
					} else {
						setPreviousClickHandler(null);
					}

				}
			}

			@Override
			public void onFailure(Throwable e) {
				if (e instanceof DCUserInputException) {
					// restore the previous panel view
					_targetPanel.setWidget(_currentController);
				}
				super.onFailure(e);
			}
		};
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
}
