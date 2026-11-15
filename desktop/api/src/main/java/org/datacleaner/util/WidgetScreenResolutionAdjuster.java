/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.util;

import java.awt.Image;

import com.formdev.flatlaf.util.UIScale;

/**
 * Class responsible for adjusting/scaling widgets according to the UI scale
 * factor. Delegates to FlatLaf's {@link UIScale}, which applies the zoom
 * configured via the {@code datacleaner.ui.scale} / {@code flatlaf.uiScale}
 * system properties (in addition to the JRE's native HiDPI scaling).
 *
 * NOTE: this class is a compatibility shim for the many legacy call sites that
 * use hardcoded pixel sizes. New code should use layout managers and scale
 * from font metrics instead. See UI_MODERNIZATION.md.
 */
public class WidgetScreenResolutionAdjuster {

    private static final WidgetScreenResolutionAdjuster INSTANCE = new WidgetScreenResolutionAdjuster();

    public static WidgetScreenResolutionAdjuster get() {
        return INSTANCE;
    }

    private WidgetScreenResolutionAdjuster() {
    }

    public int adjust(int size) {
        return UIScale.scale(size);
    }

    public float adjust(float size) {
        return UIScale.scale(size);
    }

    public double adjust(double size) {
        return UIScale.scale((float) size);
    }

    public float getSizeAdjustment() {
        return UIScale.getUserScaleFactor();
    }

    public Image scale(Image image) {
        final float factor = getSizeAdjustment();
        if (image == null || factor == 1f) {
            return image;
        }
        final int w = image.getWidth(null);
        final int h = image.getHeight(null);
        return image.getScaledInstance(Math.round(w * factor), Math.round(h * factor), Image.SCALE_SMOOTH);
    }
}