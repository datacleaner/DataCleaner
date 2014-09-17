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
package org.eobjects.datacleaner.monitor.shared.widgets;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A {@link PopupPanel} which has a heading in the top and a button panel in the
 * bottom.
 */
public class DCPopupPanel extends PopupPanel {

    private final SimplePanel _panel;
    private final ButtonPanel _buttonPanel;
    private FlowPanel _outerPanel;

    public DCPopupPanel(String heading) {
        super();
        addStyleName("DCPopupPanel");
        setGlassEnabled(true);
        setAutoHideEnabled(false);
        setModal(false);
        _buttonPanel = new ButtonPanel();
        
        final int clientHeight = Window.getClientHeight();
        final int heightMargin = 100; // represents slack for buttons, border and header
        final int maxHeight = (int) ((clientHeight - heightMargin) * 0.90);
        
        _panel = new ScrollPanel();
        _panel.getElement().getStyle().setProperty("maxHeight", maxHeight + "px");
        _panel.setStyleName("DCPopupPanelContent");

        _outerPanel = new FlowPanel();
        if (heading != null) {
            _outerPanel.add(new HeadingLabel(heading));
        }
        _outerPanel.add(_panel);
        _outerPanel.add(_buttonPanel);

        super.setWidget(_outerPanel);
    }

    public void setHeader(String header) {
        final Widget firstWidget = _outerPanel.getWidget(0);
        if (firstWidget instanceof HeadingLabel) {
            HeadingLabel headingLabel = (HeadingLabel) firstWidget;
            headingLabel.setText(header);
        } else {
            HeadingLabel headingLabel = new HeadingLabel(header);
            _outerPanel.insert(headingLabel, 0);
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
        _panel.setWidget(w);
    }

    @Override
    public void setWidget(IsWidget w) {
        _panel.setWidget(w);
    }
}
