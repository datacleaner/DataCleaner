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

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel for buttons, also known as a "button group" in Bootstrap terms.
 */
public class ButtonPanel extends FlowPanel {

    private final boolean _block;

    public ButtonPanel() {
        this(true);
    }

    /**
     * 
     * @param block
     *            whether or not to use "block"/"justify" mode where the buttons
     *            take up a complete block's space
     */
    public ButtonPanel(boolean block) {
        super();
        _block = block;
        addStyleName("btn-group");
        if (block) {
            addStyleName("btn-group-justified");
        }
    }

    /**
     * Adds a button to this {@link ButtonPanel}
     * 
     * @param button
     */
    public void addButton(Button button) {
        if (_block) {
            // in this mode bootstrap wants all buttons wrapped in another
            // button group
            final ButtonPanel buttonPanel = new ButtonPanel(false);
            buttonPanel.addButton(button);
            add(buttonPanel);
        } else {
            add(button);
        }
    }

    /**
     * Removes a button from this {@link ButtonPanel}
     * 
     * @param button
     */
    public void removeButton(Button button) {
        if (_block) {
            final int widgetCount = getWidgetCount();
            for (int i = 0; i < widgetCount; i++) {
                final Widget widget = getWidget(i);
                if (widget instanceof ButtonPanel) {
                    final ButtonPanel childButtonPanel = (ButtonPanel) widget;
                    if (childButtonPanel.hasButton(button)) {
                        remove(childButtonPanel);
                        return;
                    }
                }
            }
        } else {
            remove(button);
        }
    }

    private boolean hasButton(Button button) {
        // only checks direct children
        final int index = getWidgetIndex(button);
        return index != -1;
    }

    /**
     * Removes all buttons from this {@link ButtonPanel}.
     */
    public void removeAllButtons() {
        clear();
    }
}
