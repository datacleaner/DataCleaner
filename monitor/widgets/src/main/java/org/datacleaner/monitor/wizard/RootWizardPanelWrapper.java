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
package org.datacleaner.monitor.wizard;

import org.datacleaner.monitor.shared.widgets.ButtonPanel;
import org.datacleaner.monitor.shared.widgets.WizardProgressBar;

import com.google.gwt.core.client.GWT;
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
        rootPanel.clear();
        GWT.log("RootWizardPanelWrapper.show(): Found div by id '" + _htmlDivId + "': " + rootPanel);
        rootPanel.add(getWizardWidget());
    }

    @Override
    public void hideWizard() {
        _childWizardPanel.hideWizard();
        RootPanel rootPanel = RootPanel.get(_htmlDivId);
        rootPanel.remove(getWizardWidget());
    }

    @Override
    public WizardProgressBar getProgressBar() {
        return _childWizardPanel.getProgressBar();
    }

    @Override
    public void refreshUI() {
        _childWizardPanel.refreshUI();
    }

    @Override
    public void addStyleClass(String styleClass) {
        _childWizardPanel.addStyleClass(styleClass);
    }

    @Override
    public String getCustomHtmlDivId() {
        return _childWizardPanel.getCustomHtmlDivId();
    }
}
