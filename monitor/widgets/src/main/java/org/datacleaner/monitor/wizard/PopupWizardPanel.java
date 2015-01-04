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
import org.datacleaner.monitor.shared.widgets.DCPopupPanel;
import org.datacleaner.monitor.shared.widgets.WizardProgressBar;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Popup style panel used by wizards. It creates a popup panel on which elements
 * can be added and controlled through wizard framework
 * 
 */
public class PopupWizardPanel extends DCPopupPanel implements WizardPanel {

    private final SimplePanel _contentPanel;
    private final ButtonPanel _buttonPanel;
    private final FlowPanel _wizardFlowPanel;
    private final WizardProgressBar _progressBar;

    public PopupWizardPanel() {
        super("");
        _buttonPanel = new ButtonPanel();
        _progressBar = new WizardProgressBar();
        _contentPanel = getContentPanel();
        _wizardFlowPanel = getWizardFlowPanel();
        super.setWidget(_wizardFlowPanel);
        setAppearance();
    }

    private FlowPanel getWizardFlowPanel() {
        FlowPanel wizardFlowPanel = new FlowPanel();
        wizardFlowPanel.add(_progressBar);
        wizardFlowPanel.add(_contentPanel);
        wizardFlowPanel.add(_buttonPanel);
        return wizardFlowPanel;
    }

    private SimplePanel getContentPanel() {
        int clientHeight = Window.getClientHeight();
        int heightMargin = 100;
        int maxHeight = (int) ((clientHeight - heightMargin) * 0.90);
        SimplePanel contentPanel = new ScrollPanel();
        contentPanel.getElement().getStyle().setProperty("maxHeight", maxHeight + "px");
        contentPanel.setStyleName("PopupWizardPanelContent");
        return contentPanel;
    }

    private void setAppearance() {
        setGlassEnabled(true);
        setAutoHideEnabled(false);
        setModal(false);
        addStyleName("WizardPanel");
        addStyleName("PopupWizardPanel");
    }

    @Override
    public void setHeader(String header) {
        super.setHeader(header);
    }

    @Override
    public ButtonPanel getButtonPanel() {
        return _buttonPanel;
    }

    @Override
    public void setContent(IsWidget w) {
        _contentPanel.setWidget(w);
        center();
    }

    @Override
    public void hideWizard() {
        hide();
    }

    @Override
    public void showWizard() {
        center();
        show();
    }

    @Override
    public Widget getWizardWidget() {
        return this;
    }

    @Override
    public WizardProgressBar getProgressBar() {
        return _progressBar;
    }

    @Override
    public void refreshUI() {
        center();
    }

    @Override
    public void addStyleClass(String styleClass) {
        super.addStyleName(styleClass);
    }

    @Override
    public String getCustomHtmlDivId() {
        return null;
    }
}
