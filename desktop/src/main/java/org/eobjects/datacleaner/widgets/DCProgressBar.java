/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.widgets;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.eobjects.datacleaner.util.ProgressCounter;
import org.eobjects.datacleaner.util.WidgetUtils;

/**
 * A progress bar which behaves properly in multithreaded environments.
 */
public class DCProgressBar extends JProgressBar {

    private static final long serialVersionUID = 1L;
    
    private static final int DEFAULT_HEIGHT = 20;

    private final ProgressCounter _value;

    public DCProgressBar(int min, int max) {
        super(min, max);
        setMinimumSize(new Dimension(10, DEFAULT_HEIGHT));
        
        setOpaque(false);
        _value = new ProgressCounter(0);
    }
    
    @Override
    public Dimension getPreferredSize() {
        int width = super.getPreferredSize().width;
        return new Dimension(width, DEFAULT_HEIGHT);
    }
    
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(1000, DEFAULT_HEIGHT);
    }

    /**
     * Sets the value of the progress bar, if the new value is greater than the
     * previous value.
     * 
     * @param newValue
     * @return whether or not the value was greater, and thus updated
     */
    public boolean setValueIfGreater(final int newValue) {
        boolean greater = _value.setIfSignificantToUser(newValue);
        if (greater) {
            if (SwingUtilities.isEventDispatchThread()) {
                super.setValue(newValue);
            } else {
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        DCProgressBar.super.setValue(newValue);
                    }
                };
                SwingUtilities.invokeLater(runnable);
            }
        }
        return greater;
    }
    
    @Override
    public int getValue() {
        return _value.get();
    }

    /**
     * @deprecated use {@link #setValueIfGreater(int)} instead.
     */
    @Deprecated
    @Override
    public void setValue(int newValue) {
        setValueIfGreater(newValue);
    }

    protected int getBarWidth(final int value) {
        final int minimum = getMinimum();
        if (minimum > value) {
            return 0;
        }

        final int width = getWidth();
        final int maximum = getMaximum();
        if (value > maximum) {
            return width;
        }

        final int adjustedMax = maximum - minimum;
        final int adjustedValue = value - minimum;
        final double completenessRatio = 1.0 * adjustedValue / adjustedMax;
        final int barWidth = (int) (width * completenessRatio);
        return barWidth;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        final int width = getWidth();
        final int height = getHeight();
        
        if (isOpaque()) {
            g.setColor(WidgetUtils.BG_COLOR_DARK);
            g.fillRect(0, 0, width, height);
        }

        final int barWidth = getBarWidth(getValue());

        if (barWidth > 0) {
            g.setColor(WidgetUtils.BG_COLOR_BLUE_BRIGHT);
            g.fillRect(0, 0, barWidth, height / 2);
            g.setColor(WidgetUtils.slightlyDarker(WidgetUtils.BG_COLOR_BLUE_BRIGHT));
            g.fillRect(0, height / 2, barWidth, height / 2);

            g.setColor(WidgetUtils.slightlyBrighter(WidgetUtils.BG_COLOR_BLUE_BRIGHT));
            g.drawRect(0, 0, barWidth, height);
        }
    }
}
