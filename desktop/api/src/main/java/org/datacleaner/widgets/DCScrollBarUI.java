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
package org.datacleaner.widgets;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * Custom scrollbar UI for DataCleaner
 */
public class DCScrollBarUI extends BasicScrollBarUI {

    /**
     * Factory method used by Swing to instantiate the {@link DCScrollBarUI}
     * 
     * @param c
     *            the scrollbar component
     * @return
     */
    public static ComponentUI createUI(JComponent c) {
        return new DCScrollBarUI();
    }

    @Override
    protected void installComponents() {
        switch (scrollbar.getOrientation()) {
        case JScrollBar.VERTICAL:
            incrButton = createIncreaseButton(SOUTH);
            decrButton = createDecreaseButton(NORTH);
            break;

        case JScrollBar.HORIZONTAL:
            if (scrollbar.getComponentOrientation().isLeftToRight()) {
                incrButton = createIncreaseButton(EAST);
                decrButton = createDecreaseButton(WEST);
            } else {
                incrButton = createIncreaseButton(WEST);
                decrButton = createDecreaseButton(EAST);
            }
            break;
        }

        incrGap = -1 * scrollBarWidth;
        decrGap = -1 * scrollBarWidth;

        incrButton.setVisible(false);
        decrButton.setVisible(false);

        // Force the children's enabled state to be updated.
        scrollbar.setEnabled(scrollbar.isEnabled());
    }

}
