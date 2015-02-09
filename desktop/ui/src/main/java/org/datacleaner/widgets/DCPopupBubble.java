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

import java.awt.Image;
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

    public static interface PopupCallback {
        /**
         * Give a last chance to refuse popping up
         * @return true if the popup is okay, false to inhibit
         */
        boolean onBeforeShow();
    }
    
    public static enum Position {
        TOP, BOTTOM
    }
    
    private static final Image BACKGROUND_IMAGE_BOTTOM = ImageManager.get().getImage("images/window/popup-bubble-bottom.png");
    private static final Image BACKGROUND_IMAGE_TOP = ImageManager.get().getImage("images/window/popup-bubble-top.png");
    
    private final DCGlassPane _glassPane;
    private final DCPanel _panel;
    private int _xOnScreen;
    private int _yOnScreen;
    private Position _position;

    public DCPopupBubble(DCGlassPane glassPane, String text, int xOnScreen, int yOnScreen) {
        this(glassPane, text, xOnScreen, yOnScreen, (Icon) null);
    }
    
    public DCPopupBubble(DCGlassPane glassPane, String text, int xOnScreen, int yOnScreen, String iconPath) {
        this(glassPane, text, xOnScreen, yOnScreen, ImageManager.get().getImageIcon(iconPath));
    }
    
    public DCPopupBubble(DCGlassPane glassPane, String text, int xOnScreen, int yOnScreen, Icon icon) {
        this(glassPane, text, xOnScreen, yOnScreen, icon, Position.BOTTOM);
    }

    public DCPopupBubble(DCGlassPane glassPane, String text, int xOnScreen, int yOnScreen, Icon icon, Position position) {
        _glassPane = glassPane;
        _position = position;
        
        if (_position == Position.BOTTOM) {
            _panel = new DCPanel(BACKGROUND_IMAGE_BOTTOM, 0, 0);
        } else {
            _panel = new DCPanel(BACKGROUND_IMAGE_TOP, 0, 0);
        }
        
        _xOnScreen = xOnScreen;
        _yOnScreen = yOnScreen;
        final DCLabel label = DCLabel.bright(text);
        if (icon != null) {
            label.setIcon(icon);
        }
        label.setFont(WidgetUtils.FONT_SMALL);
        label.setSize(240, 60);
        if (_position == Position.BOTTOM) {
            label.setLocation(5, 20);
        } else {
            label.setLocation(5, 5);
        }
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
        attachTo(component, null);
    }

    public void attachTo(final JComponent component, final PopupCallback popupCallback) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (popupCallback != null && !popupCallback.onBeforeShow()) {
                    return;
                } else if (component.isEnabled()) {
                    final Point locationOnScreen = component.getLocationOnScreen();
                    final int x = locationOnScreen.x + 15;
                    if (_position == Position.BOTTOM) {
                        DCPopupBubble.this.setLocationOnScreen(x,
                                locationOnScreen.y + component.getHeight());
                    } else {
                        DCPopupBubble.this.setLocationOnScreen(x,
                                locationOnScreen.y - 81);
                    }
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
