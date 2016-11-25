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
package org.datacleaner.monitor.shared.widgets;

@SuppressWarnings({ "checkstyle:MemberName", "checkstyle:ParameterName", "checkstyle:LocalVariableName" })
public class Color {

    public static final Color ADDITIONAL_COLOR_GREEN_BRIGHT = new Color(123, 207, 38);
    public static final Color ADDITIONAL_COLOR_RED_BRIGHT = new Color(211, 36, 36);
    public static final Color BG_COLOR_BLUE_BRIGHT = new Color(85, 148, 221);
    public static final Color BG_COLOR_ORANGE_BRIGHT = new Color(255, 168, 0);
    public static final Color ADDITIONAL_COLOR_PURPLE_BRIGHT = new Color(211, 36, 156);
    public static final Color ADDITIONAL_COLOR_CYAN_BRIGHT = new Color(36, 209, 211);

    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color LIGHT_GRAY = new Color(211, 211, 211);
    public static final Color GRAY = new Color(128, 128, 128);
    public static final Color DARK_GRAY = new Color(169, 169, 169);
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color RED = new Color(236, 22, 22);
    public static final Color PINK = new Color(248, 17, 229);
    public static final Color ORANGE = new Color(255, 114, 0);
    public static final Color YELLOW = new Color(254, 229, 21);
    public static final Color GREEN = new Color(12, 176, 31);
    public static final Color MAGENTA = new Color(241, 0, 255);
    public static final Color CYAN = new Color(13, 79, 231);
    public static final Color BLUE = new Color(0, 0, 139);

    private static final double DARKER_BRIGHTER_FACTOR = 0.7;

    private static final double SLIGHT_DARKER_FACTOR = 0.9;
    int value;
    private int r;
    private int g;
    private int b;

    public Color(final int r, final int g, final int b) {
        this(r, g, b, 255);
    }

    public Color(final int r, final int g, final int b, final int a) {
        // @formatter:off
        // @checkstyle:off
        value = ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                ((b & 0xFF) << 0);
        // @checkstyle:on
        // @formatter:on
    }

    public Color(final float h, final float s, final float l) {
        float q = 0;
        if (l < 0.5f) {
            q = l * (s + 1.0f);
        } else {
            q = l + s - (l * s);
        }

        final float p = 2.0f * l - q;
        final float hk = h / 360.0f;

        final float tr = hk + 1.0f / 3;
        final float tb = hk - 1.0f / 3;

        this.r = (int) (getComponent(tr, q, p) * 255f);
        this.g = (int) (getComponent(hk, q, p) * 255f);
        this.b = (int) (getComponent(tb, q, p) * 255f);

    }

    public Color(final float r, final float g, final float b, final float a) {
        this((int) (r * 255 + 0.5), (int) (g * 255 + 0.5), (int) (b * 255 + 0.5), (int) (a * 255 + 0.5));
    }

    public Color(final String hex) {
        final int rgb = Integer.decode(hex);
        r = (rgb & 0xff0000) >> 16;
        g = (rgb & 0x00ff00) >> 8;
        b = rgb & 0x0000ff;
    }

    private float getComponent(float tc, final float q, final float p) {
        if (tc < 0) {
            tc += 1;
        } else if (tc > 1) {
            tc -= 1;
        }

        if (tc < (1f / 6f)) {
            tc = p + ((q - p) * 6f * tc);
        } else if ((1f / 6f) <= tc && tc < 0.5f) {
            tc = q;
        } else if (0.5f <= tc && tc < (2f / 3f)) {
            tc = p + ((q - p) * 6.0f * (2f / 3f - tc));
        } else {
            tc = p;
        }

        return tc;
    }

    public float getHue() {
        final float r = this.r / 255f;
        final float g = this.g / 255f;
        final float b = this.b / 255f;

        final float max = getMax(r, g, b);
        final float min = getMin(r, g, b);

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
        final float r = this.r / 255f;
        final float g = this.g / 255f;
        final float b = this.b / 255f;

        final float l = getLightness();
        final float max = getMax(r, g, b);
        final float min = getMin(r, g, b);

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
        final float r = this.r / 255f;
        final float g = this.g / 255f;
        final float b = this.b / 255f;

        return (getMax(r, g, b) + getMin(r, g, b)) / 2f;
    }

    private float getMax(final float r, final float g, final float b) {
        return r > g ? (r > b ? r : b) : (g > b ? g : b);
    }

    private float getMin(final float r, final float g, final float b) {
        return r < g ? (r < b ? r : b) : (g < b ? g : b);
    }

    public int getAlpha() {
        return (getRGB() >> 24) & 0xff;
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

    public int getRGB() {
        return value;
    }

    public Color brighter() {
        int r = getRed();
        int g = getGreen();
        int b = getBlue();

        final int i = (int) (1.0 / (1.0 - DARKER_BRIGHTER_FACTOR));
        if (r == 0 && g == 0 && b == 0) {
            return new Color(i, i, i);
        }
        if (r > 0 && r < i) {
            r = i;
        }
        if (g > 0 && g < i) {
            g = i;
        }
        if (b > 0 && b < i) {
            b = i;
        }

        return new Color(Math.min((int) (r / DARKER_BRIGHTER_FACTOR), 255),
                Math.min((int) (g / DARKER_BRIGHTER_FACTOR), 255), Math.min((int) (b / DARKER_BRIGHTER_FACTOR), 255));
    }

    public Color darker() {
        return new Color(Math.max((int) (getRed() * DARKER_BRIGHTER_FACTOR), 0),
                Math.max((int) (getGreen() * DARKER_BRIGHTER_FACTOR), 0),
                Math.max((int) (getBlue() * DARKER_BRIGHTER_FACTOR), 0));
    }

    public Color slightlyDarker() {
        return new Color(Math.max((int) (getRed() * SLIGHT_DARKER_FACTOR), 0),
                Math.max((int) (getGreen() * SLIGHT_DARKER_FACTOR), 0),
                Math.max((int) (getBlue() * SLIGHT_DARKER_FACTOR), 0));
    }


    public Color clone() {
        return new Color(r, g, b);
    }

    private String pad(final String in) {
        if (in.length() == 0) {
            return "00";
        }
        if (in.length() == 1) {
            return "0" + in;
        }
        return in;
    }

    public String toString() {
        return "#" + pad(Integer.toHexString(r)) + pad(Integer.toHexString(g)) + pad(Integer.toHexString(b));
    }

    public String toHexString() {
        return "#" + Integer.toHexString(getRGB() | 0xFF000000).substring(2);
    }

}
