package org.eobjects.datacleaner.monitor.dashboard.util;

/**
 * eobjects.org DataCleaner Copyright (C) 2010 eobjects.org
 * 
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to: Free Software Foundation,
 * Inc. 51 Franklin Street, Fifth Floor Boston, MA 02110-1301 USA
 */

public class Color {

    public final static Color ADDITIONAL_COLOR_GREEN_BRIGHT = new Color(123, 207, 38);
    public final static Color ADDITIONAL_COLOR_RED_BRIGHT = new Color(211, 36, 36);
    public final static Color BG_COLOR_BLUE_BRIGHT = new Color(85, 148, 221);
    public final static Color BG_COLOR_ORANGE_BRIGHT = new Color(255, 168, 0);
    public final static Color ADDITIONAL_COLOR_PURPLE_BRIGHT = new Color(211, 36, 156);
    public final static Color ADDITIONAL_COLOR_CYAN_BRIGHT = new Color(36, 209, 211);

    int value;

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public Color(int r, int g, int b, int a) {
        value = ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                ((b & 0xFF) << 0);
    }

    public Color(float r, float g, float b) {
        this((int) (r * 255 + 0.5), (int) (g * 255 + 0.5), (int) (b * 255 + 0.5));

    }

    public Color(float r, float g, float b, float a) {
        this((int) (r * 255 + 0.5), (int) (g * 255 + 0.5), (int) (b * 255 + 0.5), (int) (a * 255 + 0.5));
    }

    public int getRed() {
        return (getRGB() >> 16) & 0xFF;
    }

    public int getGreen() {
        return (getRGB() >> 8) & 0xFF;
    }

    public int getBlue() {
        return (getRGB() >> 0) & 0xFF;
    }

    public int getAlpha() {
        return (getRGB() >> 24) & 0xff;
    }

    public int getRGB() {
        return value;
    }

    private static final double DARKER_BRIGHTER_FACTOR = 0.7;

    private static final double SLIGHT_DARKER_FACTOR = 0.9;

    public Color brighter() {
        int r = getRed();
        int g = getGreen();
        int b = getBlue();

        int i = (int) (1.0 / (1.0 - DARKER_BRIGHTER_FACTOR));
        if (r == 0 && g == 0 && b == 0) {
            return new Color(i, i, i);
        }
        if (r > 0 && r < i)
            r = i;
        if (g > 0 && g < i)
            g = i;
        if (b > 0 && b < i)
            b = i;

        return new Color(Math.min((int) (r / DARKER_BRIGHTER_FACTOR), 255),
                         Math.min((int) (g / DARKER_BRIGHTER_FACTOR), 255),
                         Math.min((int) (b / DARKER_BRIGHTER_FACTOR), 255));
    }

    public Color darker() {
        return new Color(Math.max((int) (getRed() * DARKER_BRIGHTER_FACTOR), 0),
                         Math.max((int) (getGreen() * DARKER_BRIGHTER_FACTOR), 0),
                         Math.max((int) (getBlue() * DARKER_BRIGHTER_FACTOR), 0));
    }

    public Color slightlyDarker() {
        return new Color(Math.max((int) (getRed() * SLIGHT_DARKER_FACTOR), 0), Math.max(
                (int) (getGreen() * SLIGHT_DARKER_FACTOR), 0), Math.max(
                        (int) (getBlue() * SLIGHT_DARKER_FACTOR), 0));
    }

    public int hashCode() {
        return value;
    }

    public String toString() {
        return getClass().getName() + "[r=" + getRed() + ",g=" + getGreen() + ",b=" + getBlue() + "]";
    }

    public String toHexString() {
        return "#" + Integer.toHexString(getRGB() | 0xFF000000).substring(2);
    }

}
