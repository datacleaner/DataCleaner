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

/**
 * Class responsible for adjusting/scaling widgets according to the screen resolution.
 *
 * NOTE: Historically this class scaled hardcoded pixel sizes based on the screen
 * width. Since the move to Java 17 + FlatLaf, the JVM handles HiDPI scaling
 * natively (including per-monitor scale factors), so this class is now a no-op
 * compatibility shim. It is scheduled for removal - do not use it in new code.
 */
public class WidgetScreenResolutionAdjuster {

    private static final WidgetScreenResolutionAdjuster INSTANCE = new WidgetScreenResolutionAdjuster();

    public static WidgetScreenResolutionAdjuster get() {
        return INSTANCE;
    }

    private WidgetScreenResolutionAdjuster() {
    }

    public int adjust(int size) {
        return size;
    }

    public float adjust(float size) {
        return size;
    }

    public double adjust(double size) {
        return size;
    }

    public float getSizeAdjustment() {
        return 1;
    }

    public Image scale(Image image) {
        return image;
    }
}