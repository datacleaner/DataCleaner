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
package org.eobjects.datacleaner.monitor.shared.widgets;

public class Color {

    public final static Color ADDITIONAL_COLOR_GREEN_BRIGHT = new Color(123, 207, 38);
    public final static Color ADDITIONAL_COLOR_RED_BRIGHT = new Color(211, 36, 36);
    public final static Color BG_COLOR_BLUE_BRIGHT = new Color(85, 148, 221);
    public final static Color BG_COLOR_ORANGE_BRIGHT = new Color(255, 168, 0);
    public final static Color ADDITIONAL_COLOR_PURPLE_BRIGHT = new Color(211, 36, 156);
    public final static Color ADDITIONAL_COLOR_CYAN_BRIGHT = new Color(36, 209, 211);

    public final static Color WHITE = new Color(255, 255, 255);
    public final static Color LIGHT_GRAY = new Color(192, 192, 192);
    public final static Color GRAY = new Color(128, 128, 128);
    public final static Color DARK_GRAY = new Color(64, 64, 64);
    public final static Color BLACK = new Color(0, 0, 0);
    public final static Color RED = new Color(236, 22, 22);
    public final static Color PINK = new Color(248, 17, 229);
    public final static Color ORANGE = new Color(255, 114, 0);
    public final static Color YELLOW = new Color(254, 229, 21);
    public final static Color GREEN = new Color(12, 176, 31);
    public final static Color MAGENTA = new Color(241, 3, 139);
    public final static Color CYAN = new Color(13, 79, 231);
    public final static Color BLUE = new Color(0, 0, 255);

    private static final double DARKER_BRIGHTER_FACTOR = 0.7;

    private static final double SLIGHT_DARKER_FACTOR = 0.9;

    private int r, g, b;
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

    public Color(float h, float s, float l) {
        float q = 0;
        if (l < 0.5f)
            q = l * (s + 1.0f);
        else
            q = l + s - (l * s);

        float p = 2.0f * l - q;
        float hk = h / 360.0f;

        float tr = hk + 1.0f / 3;
        float tg = hk;
        float tb = hk - 1.0f / 3;

        this.r = (int) (getComponent(tr, q, p) * 255f);
        this.g = (int) (getComponent(tg, q, p) * 255f);
        this.b = (int) (getComponent(tb, q, p) * 255f);

    }

    private float getComponent(float tc, float q, float p) {
        if (tc < 0)
            tc += 1;
        else if (tc > 1)
            tc -= 1;

        if (tc < (1f / 6f))
            tc = p + ((q - p) * 6f * tc);
        else if ((1f / 6f) <= tc && tc < 0.5f)
            tc = q;
        else if (0.5f <= tc && tc < (2f / 3f))
            tc = p + ((q - p) * 6.0f * (2f / 3f - tc));
        else
            tc = p;

        return tc;
    }

    public Color(float r, float g, float b, float a) {
        this((int) (r * 255 + 0.5), (int) (g * 255 + 0.5), (int) (b * 255 + 0.5), (int) (a * 255 + 0.5));
    }

    public Color(String hex) {
        int rgb = Integer.decode(hex);
        r = (rgb & 0xff0000) >> 16;
        g = (rgb & 0x00ff00) >> 8;
        b = rgb & 0x0000ff;
    }

    public float getHue() {
        float r = this.r / 255f;
        float g = this.g / 255f;
        float b = this.b / 255f;

        float max = getMax(r, g, b);
        float min = getMin(r, g, b);

        if (max == min) {
            return 0;
        } else if (max == r) {
            return (60f * ((float) (g - b) / max - min) + 360) % 360;
        } else if (max == g) {
            return (60f * ((float) (b - r) / max - min) + 120);
        } else if (max == b) {
            return (60f * ((float) (r - g) / max - min) + 240);
        } else {
            return 0;
        }
    }

    public float getSaturation() {
        float r = this.r / 255f;
        float g = this.g / 255f;
        float b = this.b / 255f;

        float l = getLightness();
        float max = getMax(r, g, b);
        float min = getMin(r, g, b);

        if (max == min) {
            return 0;
        } else if (l <= 0.5) {
            return (max - min) / (2f * l);
        } else if (l > 0.5) {
            return (max - min) / (2f - 2f * l);
        } else {
            return 0;
        }
    }

    public float getLightness() {
        float r = this.r / 255f;
        float g = this.g / 255f;
        float b = this.b / 255f;

        return (getMax(r, g, b) + getMin(r, g, b)) / 2f;
    }

    private float getMax(float r, float g, float b) {
        return r > g ? (r > b ? r : b) : (g > b ? g : b);
    }

    private float getMin(float r, float g, float b) {
        return r < g ? (r < b ? r : b) : (g < b ? g : b);
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

    
    public Color clone() {
        return new Color(r, g, b);
    }
    
    private String pad(String in) {
        if (in.length() == 0) {
            return "00";
        }
        if (in.length() == 1) {
            return "0" + in;
        }
        return in;
    }

    public String toString() {
        return "#"
                + pad(Integer.toHexString(r))
                + pad(Integer.toHexString(g))
                + pad(Integer.toHexString(b));
    }

    public String toHexString() {
        return "#" + Integer.toHexString(getRGB() | 0xFF000000).substring(2);
    }

}
