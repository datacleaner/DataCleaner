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
import org.datacleaner.monitor.shared.widgets.HeadingLabel;
import org.datacleaner.monitor.shared.widgets.WizardProgressBar;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Inline panel used by wizards. It creates {@link SimplePanel} on which
 * elements can be added and controlled through wizard framework
 */
public class SimpleWizardPanel implements WizardPanel {

    private final String _htmlDivId;
    private final SimplePanel _contentPanel;
    private final ButtonPanel _buttonPanel;
    private final WizardProgressBar _progressBar;
    private FlowPanel _wizardFlowPanel;

    public SimpleWizardPanel(final String htmlDivId) {
        super();
        _htmlDivId = htmlDivId;
        _buttonPanel = new ButtonPanel();
        _progressBar = new WizardProgressBar();
        _contentPanel = getContentPanel();
        _wizardFlowPanel = getWizardFlowPanel();
    }

    private FlowPanel getWizardFlowPanel() {
        final FlowPanel wizardFlowPanel = new FlowPanel();
        wizardFlowPanel.add(_progressBar);
        wizardFlowPanel.add(_contentPanel);
        wizardFlowPanel.add(_buttonPanel);
        wizardFlowPanel.addStyleName("SimpleWizardPanel");
        wizardFlowPanel.addStyleName("WizardPanel");
        return wizardFlowPanel;
    }

    private SimplePanel getContentPanel() {
        final SimplePanel contentPanel = new ScrollPanel();
        contentPanel.setStyleName("SimpleWizardPanelContent");
        return contentPanel;
    }

    public void setHeader(final String header) {
        final Widget firstWidget = _wizardFlowPanel.getWidget(0);
        if (firstWidget instanceof HeadingLabel) {
            final HeadingLabel headingLabel = (HeadingLabel) firstWidget;
            headingLabel.setText(header);
        } else {
            final HeadingLabel headingLabel = new HeadingLabel(header);
            _wizardFlowPanel.insert(headingLabel, 0);
        }
    }

    @Override
    public ButtonPanel getButtonPanel() {
        return _buttonPanel;
    }

    @Override
    public void setContent(final IsWidget w) {
        _contentPanel.setWidget(w);
    }

    @Override
    public void hideWizard() {
        _wizardFlowPanel.setVisible(false);
    }

    @Override
    public void showWizard() {
        _wizardFlowPanel.setVisible(true);
    }

    @Override
    public Widget getWizardWidget() {
        return _wizardFlowPanel;
    }

    @Override
    public WizardProgressBar getProgressBar() {
        return _progressBar;
    }

    @Override
    public void refreshUI() {
        // do nothing
    }

    @Override
    public void addStyleClass(final String styleClass) {
        getWizardWidget().addStyleName(styleClass);
    }

    @Override
    public String getCustomHtmlDivId() {
        return _htmlDivId;
    }
}
