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
package org.datacleaner.actions;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.Timer;

public abstract class MoveComponentTimerActionListener implements ActionListener {

    private final JComponent component;
    private final int _toX;
    private final int _toY;
    private volatile int numSteps;

    public MoveComponentTimerActionListener(final JComponent component, final int x, final int y, final int numSteps) {
        if (numSteps <= 0) {
            throw new IllegalArgumentException("numSteps must be a postive number");
        }
        this.component = component;
        this._toX = x;
        this._toY = y;
        this.numSteps = numSteps;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Timer timer = (Timer) e.getSource();

        final Point location = component.getLocation();
        final int currentX = location.x;
        final int currentY = location.y;
        if (currentX == _toX && currentY == _toY) {
            timer.stop();
        } else if (numSteps <= 1) {
            component.setLocation(_toX, _toY);
            done();
        } else {
            int diffX = _toX - currentX;
            diffX = diffX / numSteps;
            int diffY = _toY - currentY;
            diffY = diffY / numSteps;

            component.setLocation(currentX + diffX, currentY + diffY);
        }

        numSteps--;
    }

    protected abstract void done();
}
