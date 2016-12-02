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
package org.datacleaner.monitor.shared.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

/**
 * A simple cancel button that will hide a popup.
 */
public class CancelPopupButton extends Button implements ClickHandler {

    private final DCPopupPanel _popup;

    public CancelPopupButton(final DCPopupPanel popup) {
        this(popup, "Cancel");
    }

    public CancelPopupButton(final DCPopupPanel popup, final String labelText) {
        this(popup, labelText, false);
    }

    public CancelPopupButton(final DCPopupPanel popup, final String labelText, final boolean primaryButton) {
        super(labelText);
        _popup = popup;
        addClickHandler(this);
        if (primaryButton) {
            DCButtons.applyPrimaryStyle(this);
        } else {
            DCButtons.applyDefaultStyle(this);
        }
    }

    @Override
    public void onClick(final ClickEvent event) {
        _popup.hide();
    }
}
