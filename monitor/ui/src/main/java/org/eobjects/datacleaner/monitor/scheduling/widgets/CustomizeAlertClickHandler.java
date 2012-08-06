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
package org.eobjects.datacleaner.monitor.scheduling.widgets;

import org.eobjects.datacleaner.monitor.shared.widgets.CancelPopupButton;
import org.eobjects.datacleaner.monitor.shared.widgets.DCPopupPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

/**
 * Clickhandler invoked when an alert is clicked. The user will be presented
 * with a dialog to customize/edit the alert.
 */
public class CustomizeAlertClickHandler implements ClickHandler {

    private final AlertPanel _alertPanel;

    public CustomizeAlertClickHandler(AlertPanel alertPanel) {
        _alertPanel = alertPanel;
    }

    @Override
    public void onClick(ClickEvent event) {
        final DCPopupPanel popup = new DCPopupPanel("Alert");

        final CustomizeAlertPanel customizeAlertPanel = new CustomizeAlertPanel(_alertPanel.getAlert());
        final Button button = new Button("Save alert");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                customizeAlertPanel.updateAlert();
                _alertPanel.updateAlert();
                popup.hide();
            }
        });

        popup.setWidget(customizeAlertPanel);
        popup.addButton(button);
        popup.addButton(new CancelPopupButton(popup));
        popup.center();
        popup.show();
    }

}
