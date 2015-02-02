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
package org.datacleaner.widgets;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.datacleaner.panels.DCGlassPane;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;

public class DCPopupBubble {

    private static final ImageManager imageManager = ImageManager.get();

    private final DCGlassPane _glassPane;
    private final DCPanel _panel;
    private int _xOnScreen;
    private int _yOnScreen;

    public DCPopupBubble(DCGlassPane glassPane, String text, int xOnScreen, int yOnScreen) {
        this(glassPane, text, xOnScreen, yOnScreen, (Icon) null);
    }

    public DCPopupBubble(DCGlassPane glassPane, String text, int xOnScreen, int yOnScreen, String iconPath) {
        this(glassPane, text, xOnScreen, yOnScreen, (iconPath == null ? null : imageManager.getImageIcon(iconPath)));
    }

    public DCPopupBubble(DCGlassPane glassPane, String text, int xOnScreen, int yOnScreen, Icon icon) {
        _glassPane = glassPane;
        _panel = new DCPanel(imageManager.getImage("images/window/popup-bubble.png"), 0, 0);
        _xOnScreen = xOnScreen;
        _yOnScreen = yOnScreen;
        final DCLabel label = DCLabel.bright(text);
        if (icon != null) {
            label.setIcon(icon);
        }
        label.setFont(WidgetUtils.FONT_SMALL);
        label.setSize(240, 60);
        label.setLocation(5, 20);
        label.setVerticalAlignment(JLabel.CENTER);

        _panel.setLayout(null);
        _panel.setSize(250, 81);
        _panel.add(label);
    }

    private void initLocation() {
        Point locationOnScreen = _glassPane.getLocationOnScreen();
        int x = _xOnScreen - locationOnScreen.x - 40;
        if (x < 0) {
            x = 0;
        }
        int y = _yOnScreen - locationOnScreen.y;
        _panel.setLocation(x, y);
    }

    public void showTooltip(int timeoutMillis) {
        initLocation();
        _glassPane.showTooltip(_panel, timeoutMillis);
    }

    public void show() {
        initLocation();
        _glassPane.add(_panel);
    }

    public void hide() {
        _glassPane.remove(_panel);
    }

    public void setLocationOnScreen(int x, int y) {
        _xOnScreen = x;
        _yOnScreen = y;
    }

    public void attachTo(final JComponent component) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (component.isEnabled()) {
                    Point locationOnScreen = component.getLocationOnScreen();
                    DCPopupBubble.this.setLocationOnScreen(locationOnScreen.x + 15,
                            locationOnScreen.y + component.getHeight());
                    DCPopupBubble.this.show();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                DCPopupBubble.this.hide();
            }
            
             @Override
            public void mouseClicked(MouseEvent e) {
                 DCPopupBubble.this.hide();
            }
        });
    }
}
