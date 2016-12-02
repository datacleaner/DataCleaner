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

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.AbstractButton;
import javax.swing.JToggleButton;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.metal.MetalToggleButtonUI;

import org.datacleaner.util.WidgetUtils;

/***
 * A {@link ButtonUI} for default {@link JToggleButton}s in the DataCleaner user
 * interface.
 */
public class DefaultToggleButtonUI extends MetalToggleButtonUI {

    private static final DefaultToggleButtonUI INSTANCE = new DefaultToggleButtonUI();

    private DefaultToggleButtonUI() {
    }

    public static DefaultToggleButtonUI get() {
        return INSTANCE;
    }

    @Override
    public void installDefaults(final AbstractButton b) {
        super.installDefaults(b);
        b.setFocusPainted(false);
        b.setFont(WidgetUtils.FONT_BUTTON);
        b.setBackground(WidgetUtils.BG_COLOR_BRIGHT);
        b.setForeground(WidgetUtils.BG_COLOR_DARK);
        b.setBorder(WidgetUtils.BORDER_BUTTON_DEFAULT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected Color getDisabledTextColor() {
        return WidgetUtils.BG_COLOR_MEDIUM;
    }

    @Override
    protected Color getSelectColor() {
        return WidgetUtils.BG_COLOR_LESS_BRIGHT;
    }

}
