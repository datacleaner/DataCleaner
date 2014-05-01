package org.eobjects.datacleaner.monitor.wizard;

import org.eobjects.datacleaner.monitor.shared.widgets.ButtonPanel;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A wrapper around a {@link WizardPanel} that places it in a {@link RootPanel}
 * when {@link #showWizard()} is called.
 */
public class RootWizardPanelWrapper implements WizardPanel {

    private final WizardPanel _childWizardPanel;
    private final String _htmlDivId;
    private Widget _childWidget;

    public RootWizardPanelWrapper(WizardPanel wizardPanel, String htmlDivId) {
        _childWizardPanel = wizardPanel;
        _htmlDivId = htmlDivId;
    }

    @Override
    public void setHeader(String header) {
        _childWizardPanel.setHeader(header);
    }

    @Override
    public ButtonPanel getButtonPanel() {
        return _childWizardPanel.getButtonPanel();
    }

    @Override
    public void setContent(IsWidget w) {
        _childWizardPanel.setContent(w);
    }

    @Override
    public Widget getWizardWidget() {
        if (_childWidget == null) {
            _childWidget = _childWizardPanel.getWizardWidget();
        }
        return _childWidget;
    }

    @Override
    public void showWizard() {
        _childWizardPanel.showWizard();
        RootPanel rootPanel = RootPanel.get(_htmlDivId);
        rootPanel.add(getWizardWidget());
    }

    @Override
    public void addWizardCloseHandler(WizardCloseHandler closeHandler) {
        _childWizardPanel.addWizardCloseHandler(closeHandler);
    }

    @Override
    public void hideWizard() {
        _childWizardPanel.hideWizard();
        RootPanel rootPanel = RootPanel.get(_htmlDivId);
        rootPanel.remove(getWizardWidget());
    }
}
