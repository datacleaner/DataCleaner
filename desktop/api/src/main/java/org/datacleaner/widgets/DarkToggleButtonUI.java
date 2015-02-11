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
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.metal.MetalToggleButtonUI;

import org.datacleaner.util.WidgetUtils;

/***
 * A {@link ButtonUI} for dark buttons in the DataCleaner user interface.
 */
public class DarkToggleButtonUI extends MetalToggleButtonUI {

    public static final Color COLOR_BG_SELECT = DarkButtonUI.COLOR_BG_SELECT;
    public static final Color COLOR_BG_DEFAULT = DarkButtonUI.COLOR_BG_DEFAULT;

    private static final DarkToggleButtonUI INSTANCE = new DarkToggleButtonUI();

    public static DarkToggleButtonUI get() {
        return INSTANCE;
    }

    private DarkToggleButtonUI() {
    }

    @Override
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        b.setBackground(COLOR_BG_DEFAULT);
        b.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        b.setFocusPainted(false);
        b.setBorder(WidgetUtils.BORDER_BUTTON_DARK);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    @Override
    protected Color getSelectColor() {
        return COLOR_BG_SELECT;
    }
}
