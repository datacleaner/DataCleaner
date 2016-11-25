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
package org.datacleaner.widgets.tooltip;

import java.awt.Component;
import java.awt.MouseInfo;

import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.PopupFactory;

/**
 * Specialized {@link PopupFactory} for {@link DCToolTip}s.
 */
public class DCPopupFactory extends PopupFactory {

    @Override
    public Popup getPopup(final Component owner, final Component contents, int x, final int y)
            throws IllegalArgumentException {
        if (contents instanceof DCToolTip) {
            final DCToolTip toolTip = (DCToolTip) contents;
            final JComponent tooltipComponent = toolTip.getTooltipComponent();

            x = MouseInfo.getPointerInfo().getLocation().x;

            return super.getPopup(owner, tooltipComponent, x, y);
        }
        return super.getPopup(owner, contents, x, y);
    }
}
