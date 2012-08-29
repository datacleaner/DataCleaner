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
package org.eobjects.datacleaner.monitor.dashboard.util;

import java.util.ArrayList;
import java.util.List;

public class ColorProvider {

    private static final Color[] SLICE_COLORS = new Color[] { Color.ADDITIONAL_COLOR_GREEN_BRIGHT,
            Color.ADDITIONAL_COLOR_RED_BRIGHT, Color.BG_COLOR_BLUE_BRIGHT, Color.BG_COLOR_ORANGE_BRIGHT,
            Color.ADDITIONAL_COLOR_PURPLE_BRIGHT, Color.ADDITIONAL_COLOR_CYAN_BRIGHT };

    public static List<String> getColors(int numberOfColors) {
        List<String> colorsList = new ArrayList<String>();
        int colorIndex = 0;
        for (int i = 0; i < numberOfColors; i++) {
            final Color color;
            Color colorCandidate = SLICE_COLORS[colorIndex];
            int darkAmount = i / SLICE_COLORS.length;
            for (int j = 0; j < darkAmount; j++) {
                colorCandidate = colorCandidate.slightlyDarker();
            }
            color = colorCandidate;
            colorIndex++;
            if (colorIndex >= SLICE_COLORS.length) {
                colorIndex = 0;
            }
            colorsList.add(color.toHexString());
        }
        return colorsList;
    }

}