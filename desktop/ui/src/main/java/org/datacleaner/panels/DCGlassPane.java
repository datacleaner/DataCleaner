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
package org.datacleaner.panels;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.Timer;

import org.datacleaner.util.WidgetUtils;

/**
 * Encapsulated Swing glass pane, ensures correct access to it.
 */
public class DCGlassPane {
    private final JRootPane _rootPane;
    private final Container _glassPane;

    public DCGlassPane(final JFrame frame) {
        this(frame.getRootPane());
    }

    public DCGlassPane(final JDialog dialog) {
        this(dialog.getRootPane());
    }

    public DCGlassPane(final JRootPane rootpane) {
        _rootPane = rootpane;
        _glassPane = (Container) rootpane.getGlassPane();
        _glassPane.setLayout(null);
        _glassPane.setBackground(WidgetUtils.BG_COLOR_DARKEST);
    }

    public void showTooltip(final JComponent comp, final int timeoutMillis) {
        add(comp);

        new Timer(timeoutMillis, e -> remove(comp)).start();
    }

    public void add(final JComponent comp) {
        _glassPane.add(comp);
        _glassPane.setVisible(true);
    }

    public void remove(final JComponent comp) {
        final Component[] components = _glassPane.getComponents();
        for (int i = 0; i < components.length; i++) {
            final Component component = components[i];
            if (component.equals(comp)) {
                _glassPane.remove(i);
                break;
            }
        }
        if (_glassPane.getComponentCount() == 0) {
            _glassPane.setVisible(false);
        }
    }

    public Dimension getSize() {
        return getContentPaneInternal().getSize();
    }

    public void addCentered(final JComponent comp) {
        final Dimension compSize = comp.getSize();
        final Dimension totalSize = getSize();
        final int x = (totalSize.width - compSize.width) / 2;
        final int y = (totalSize.height - compSize.height) / 2;
        comp.setLocation(x, y);
        add(comp);
    }

    private Container getContentPaneInternal() {
        return _rootPane.getContentPane();
    }

    public boolean isEmpty() {
        return _glassPane.getComponentCount() == 0;
    }

    public Point getLocationOnScreen() {
        final Point contentPaneLocation = getContentPaneInternal().getLocationOnScreen();
        final int x = contentPaneLocation.x;
        final int y = contentPaneLocation.y;
        return new Point(x, y);
    }
}
