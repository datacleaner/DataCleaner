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
package org.eobjects.datacleaner.monitor.dashboard.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The panel which welcomes the user the first time he sees the timeline list
 * panel.
 */
public class WelcomePanel extends Composite {

    interface MyUiBinder extends UiBinder<Widget, WelcomePanel> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField
    Button newTimelineButton;

    @UiField
    Button toggleWelcomeTextButton;

    @UiField
    HTMLPanel welcomeTextPanel;

    public WelcomePanel() {
        super();

        initWidget(uiBinder.createAndBindUi(this));

        toggleWelcomeTextButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                welcomeTextPanel.setVisible(!welcomeTextPanel.isVisible());
                updateToggleButtonStyle();
            }
        });
        updateToggleButtonStyle();
    }

    public void setWelcomeTextVisible(boolean visible) {
        welcomeTextPanel.setVisible(visible);
        updateToggleButtonStyle();
    }

    private void updateToggleButtonStyle() {
        if (welcomeTextPanel.isVisible()) {
            toggleWelcomeTextButton.removeStyleName("collapsed");
            toggleWelcomeTextButton.addStyleName("expanded");
        } else {
            toggleWelcomeTextButton.removeStyleName("expanded");
            toggleWelcomeTextButton.addStyleName("collapsed");
        }
    }

    public Button getNewTimelineButton() {
        return newTimelineButton;
    }
}
