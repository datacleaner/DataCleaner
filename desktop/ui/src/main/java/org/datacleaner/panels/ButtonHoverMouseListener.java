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
package org.datacleaner.panels;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.datacleaner.util.WidgetUtils;

public class ButtonHoverMouseListener extends MouseAdapter {

    private final Border _hoverBorder;
    private volatile Border _previousBorder;

    public ButtonHoverMouseListener() {
        final MatteBorder outerBorder = new MatteBorder(1, 1, 1, 1, WidgetUtils.BG_COLOR_BRIGHT);
        _hoverBorder = new CompoundBorder(outerBorder, new EmptyBorder(2, 4, 2, 4));
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        JComponent component = (JComponent) e.getComponent();
        Border previousBorder = component.getBorder();
        if (previousBorder != _hoverBorder) {
            _previousBorder = previousBorder;
        }
        component.setBorder(_hoverBorder);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        JComponent component = (JComponent) e.getComponent();
        component.setBorder(_previousBorder);
    }
}
