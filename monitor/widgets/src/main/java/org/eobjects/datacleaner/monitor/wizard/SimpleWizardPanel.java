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

import org.eobjects.datacleaner.monitor.shared.widgets.ButtonPanel;
import org.eobjects.datacleaner.monitor.shared.widgets.HeadingLabel;

import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Inline panel used by wizards. It creates {@link SimplePanel} on which
 * elements can be added and controlled through wizard framework
 * 
 */
public class SimpleWizardPanel extends SimplePanel implements WizardPanel {

	private final SimplePanel _contentPanel;
	private final ButtonPanel _buttonPanel;
	private FlowPanel _wizardFlowPanel;

	public SimpleWizardPanel() {
		super();
		_buttonPanel = new ButtonPanel();
		setAppearance();
		_contentPanel = getContentPanel();
		_wizardFlowPanel = getWizardFlowPanel();
		super.setWidget(_wizardFlowPanel);
	}

	private FlowPanel getWizardFlowPanel() {
		FlowPanel wizardFlowPanel = new FlowPanel();
		wizardFlowPanel.add(_contentPanel);
		wizardFlowPanel.add(_buttonPanel);
		wizardFlowPanel.setVisible(true);
		return wizardFlowPanel;
	}

	private SimplePanel getContentPanel() {
		int clientHeight = Window.getClientHeight();
		int heightMargin = 100;
		int maxHeight = (int) ((clientHeight - heightMargin) * 0.90);
		SimplePanel contentPanel = new ScrollPanel();
		contentPanel.getElement().getStyle()
				.setProperty("maxHeight", maxHeight + "px");
		contentPanel.setStyleName("PopupWizardPanelContent");
		return contentPanel;
	}

	private void setAppearance() {
		addStyleName("SimpleWizardPanel");
	}

	public void setHeader(String header) {
		final Widget firstWidget = _wizardFlowPanel.getWidget(0);
		if (firstWidget instanceof HeadingLabel) {
			HeadingLabel headingLabel = (HeadingLabel) firstWidget;
			headingLabel.setText(header);
		} else {
			HeadingLabel headingLabel = new HeadingLabel(header);
			_wizardFlowPanel.insert(headingLabel, 0);
		}
	}

	public void addButton(Button button) {
		getButtonPanel().addButton(button);
	}

	public void removeButton(Button button) {
		getButtonPanel().removeButton(button);
	}

	public ButtonPanel getButtonPanel() {
		return _buttonPanel;
	}

	public void removeButtons() {
		getButtonPanel().clear();
	}

	@Override
	public void setWidget(Widget w) {
		_contentPanel.setWidget(w);
	}

	@Override
	public void setWidget(IsWidget w) {
		_contentPanel.setWidget(w);
	}

	@Override
	public void addWizardCloseHandler(
			CloseHandler<? extends Widget> closeHandler) {
		// TODO Auto-generated method stub

	}

	@Override
	public SimplePanel getInstance() {
		return this;
	}

}
