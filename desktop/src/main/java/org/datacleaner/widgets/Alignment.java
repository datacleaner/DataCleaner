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

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Compensates for differences in integer constants defined in various parts of
 * the Swing API.
 * 
 * @author Kasper Sørensen
 * 
 */
public enum Alignment {

    CENTER, LEFT, RIGHT;

    public int getFlowLayoutAlignment() {
        if (this == LEFT) {
            return FlowLayout.LEFT;
        } else if (this == CENTER) {
            return FlowLayout.CENTER;
        }
        return FlowLayout.RIGHT;
    }

    public int getLabelAlignment() {
        if (this == LEFT) {
            return JLabel.LEFT;
        } else if (this == CENTER) {
            return JLabel.CENTER;
        }
        return JLabel.RIGHT;
    }

    public int getSwingContstantsAlignment() {
        if (this == LEFT) {
            return SwingConstants.LEFT;
        } else if (this == CENTER) {
            return SwingConstants.CENTER;
        }
        return SwingConstants.RIGHT;
    }
}
