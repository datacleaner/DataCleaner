package org.datacleaner.util;

import java.awt.GraphicsEnvironment;
import java.awt.Image;

public class WidgetScreenResolutionAdjuster {

    private static final float MAX_ADJUSTMENT = 3f;

    public static WidgetScreenResolutionAdjuster get() {
        return new WidgetScreenResolutionAdjuster();
    }

    // threshold in screen width above which we'll start scaling the UI
    private final int highDpiThreshold = SystemProperties.getInt("datacleaner.highdpi.threshold", 1200);
    private final int width;

    private WidgetScreenResolutionAdjuster() {
        final GraphicsEnvironment graphicsEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        width = (int) graphicsEnv.getMaximumWindowBounds().getWidth();
    }

    public int adjust(int size) {
        return (int) (size * getSizeAdjustment());
    }

    public float adjust(float size) {
        return size * getSizeAdjustment();
    }

    public double adjust(double size) {
        return size * getSizeAdjustment();
    }

    public float getSizeAdjustment() {

        if (width < highDpiThreshold) {
            return 1;
        }

        return Math.max(MAX_ADJUSTMENT, 1.0f * width / highDpiThreshold);
    }

    public Image scale(Image image) {
        if (image == null || getSizeAdjustment() == 1f) {
            return image;
        }
        final int w = image.getWidth(null);
        final int h = image.getHeight(null);

        return image.getScaledInstance(adjust(w), adjust(h), Image.SCALE_SMOOTH);
    }
}
