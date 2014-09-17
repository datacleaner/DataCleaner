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

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * A panel for buttons
 */
public class ButtonPanel extends FlowPanel {

    public ButtonPanel() {
        super();
        addStyleName("ButtonPanel");
    }

    /**
     * Adds a button to this {@link ButtonPanel}
     * 
     * @param button
     */
    public void addButton(Button button) {
        add(button);
    }

    /**
     * Removes a button from this {@link ButtonPanel}
     * 
     * @param button
     */
    public void removeButton(Button button) {
        remove(button);
    }

    /**
     * Removes all buttons from this {@link ButtonPanel}.
     */
    public void removeAllButtons() {
        clear();
    }
}
