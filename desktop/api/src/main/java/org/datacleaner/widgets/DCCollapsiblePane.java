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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A lightweight replacement for SwingX's {@code JXCollapsiblePane}: a
 * container whose content can be collapsed (with a short animation) towards
 * one of the four directions, removing the need for the SwingX dependency
 * for this feature.
 */
public class DCCollapsiblePane extends JComponent {

    private static final long serialVersionUID = 1L;

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private static final int ANIMATION_MS_PER_STEP = 16;
    private static final int ANIMATION_STEPS = 12;

    private final JPanel _contentPane;
    private final Direction _direction;
    private final Timer _animationTimer;
    private boolean _collapsed = false;
    private boolean _animated = true;
    private int _step = ANIMATION_STEPS;
    private boolean _animatingToCollapsed;

    public DCCollapsiblePane() {
        this(Direction.UP);
    }

    public DCCollapsiblePane(final Direction direction) {
        _direction = direction;
        _contentPane = new JPanel(new BorderLayout());
        setLayout(new BorderLayout());
        super.add(_contentPane, BorderLayout.CENTER);

        _animationTimer = new Timer(ANIMATION_MS_PER_STEP, new CollapseAnimationListener());
        _animationTimer.setRepeats(true);
    }

    /**
     * Gets the container that holds the content of this pane.
     *
     * @return the content pane
     */
    public JPanel getContentPane() {
        return _contentPane;
    }

    @Override
    public Component add(final Component comp) {
        return _contentPane.add(comp);
    }

    /**
     * Sets whether collapsing/expanding should be animated (default true).
     *
     * @param animated true to animate, false to jump directly to the new state
     */
    public void setAnimated(final boolean animated) {
        _animated = animated;
    }

    public boolean isAnimated() {
        return _animated;
    }

    public boolean isCollapsed() {
        return _collapsed;
    }

    public void setCollapsed(final boolean collapsed) {
        if (collapsed == _collapsed) {
            return;
        }

        _collapsed = collapsed;
        _animatingToCollapsed = collapsed;

        final Dimension contentSize = _contentPane.getPreferredSize();
        if (!_animated || !isDisplayable() || contentSize.width == 0 || contentSize.height == 0) {
            // jump directly to the target state
            _step = ANIMATION_STEPS;
            _animationTimer.stop();
        } else {
            _step = 0;
            _animationTimer.start();
        }

        revalidate();
        repaint();
    }

    private class CollapseAnimationListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent e) {
            _step++;
            if (_step >= ANIMATION_STEPS) {
                _animationTimer.stop();
                _step = ANIMATION_STEPS;
            }
            revalidate();
            repaint();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return scale(super.getPreferredSize());
    }

    @Override
    public Dimension getMinimumSize() {
        return scale(super.getMinimumSize());
    }

    @Override
    public Dimension getMaximumSize() {
        return scale(super.getMaximumSize());
    }

    /**
     * Computes the pane size based on the animation progress: the collapse
     * axis size is interpolated between 0 (collapsed) and the full size
     * (expanded).
     */
    private Dimension scale(final Dimension size) {
        if (_animationTimer.isRunning()) {
            final double progress = (double) _step / ANIMATION_STEPS;
            final double fraction = _animatingToCollapsed ? 1d - progress : progress;
            return collapse(size, fraction);
        }
        if (_collapsed) {
            return collapse(size, 0d);
        }
        return size;
    }

    private Dimension collapse(final Dimension size, final double fraction) {
        final Dimension result = new Dimension(size);
        final int scaled;
        if (fraction <= 0d) {
            scaled = 0;
        } else {
            scaled = (int) Math.round(fraction * (isHorizontal() ? size.width : size.height));
        }
        if (isHorizontal()) {
            result.width = scaled;
        } else {
            result.height = scaled;
        }
        return result;
    }

    private boolean isHorizontal() {
        return _direction == Direction.LEFT || _direction == Direction.RIGHT;
    }
}
