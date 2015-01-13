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

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A {@link TextBox} which only accepts numbers
 */
public class NumberTextBox extends TextBox {

    /**
     * {@link KeyPressHandler} which makes sure only numbers can be entered into
     * the applied textboxes.
     */
    private static class NumbersOnly implements KeyPressHandler {
        @Override
        public void onKeyPress(KeyPressEvent event) {
            if (!Character.isDigit(event.getCharCode())) {
                ((TextBox) event.getSource()).cancelKey();
            }
        }
    }

    public NumberTextBox() {
        super();
        addKeyPressHandler(new NumbersOnly());
    }

    public void setNumberValue(Number value) {
        if (value == null) {
            setValue("");
        } else {
            setValue(value + "");
        }
    }

    public Integer getNumberValue() {
        final String value = getValue();
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        return Integer.parseInt(value);
    }
}
