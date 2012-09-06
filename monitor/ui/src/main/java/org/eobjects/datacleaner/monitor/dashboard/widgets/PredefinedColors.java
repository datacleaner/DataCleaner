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
package org.eobjects.datacleaner.monitor.dashboard.widgets;

import org.eobjects.datacleaner.monitor.shared.widgets.Color;

public enum PredefinedColors {

    LIGHT_GRAY("Light gray", Color.LIGHT_GRAY), GRAY("Gray", Color.GRAY), DARK_GRAY("Dark gray", Color.DARK_GRAY), BLACK("Black", Color.BLACK),
    RED("Red", Color.RED), PINK("Pink", Color.PINK), ORANGE("Orange", Color.ORANGE), YELLOW("Yellow", Color.YELLOW), GREEN("Green", Color.GREEN),
    MAGENTA("Magenta", Color.MAGENTA), CYAN("Cyan", Color.CYAN), BLUE("Blue", Color.BLUE);

    private String _name;
    private Color _color;

    PredefinedColors(String name, Color color) {
        _name = name;
        _color = color;
    }

    public Color getColor() {
        return _color;
    }

    public String getName() {
        return _name;
    }

}
