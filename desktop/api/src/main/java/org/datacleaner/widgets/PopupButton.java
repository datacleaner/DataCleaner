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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LookAndFeelManager;

/**
 * A toggle button that when selected shows a popup menu.
 * 
 * To use this button, access the popup menu via {@link #getMenu()} and add
 * items to it.
 */
public class PopupButton extends JToggleButton {

    private static final long serialVersionUID = 1L;

    public static enum MenuPosition {
        TOP, BOTTOM, LEFT, RIGHT;
    }

    private final JPopupMenu popupMenu = new JPopupMenu();
    private MenuPosition menuPosition;

    public PopupButton(String text) {
        this(text, null);
    }

    public PopupButton(String text, Icon icon) {
        this(text, icon, MenuPosition.BOTTOM);
    }

    public PopupButton(String text, Icon icon, MenuPosition position) {
        super(text, icon);
        this.menuPosition = position;

        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isSelected()) {
                    showPopup(popupMenu);
                }
            }
        });

        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent pme) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
                if (UIManager.getBoolean("PopupMenu.consumeEventOnClose")) {
                    setSelected(false);
                } else {
                    Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
                    Point componentLoc = getLocationOnScreen();
                    mouseLoc.x -= componentLoc.x;
                    mouseLoc.y -= componentLoc.y;
                    if (!contains(mouseLoc)) {
                        setSelected(false);
                    }
                }
            }
        });
    }

    public void setMenuPosition(MenuPosition menuPosition) {
        this.menuPosition = menuPosition;
    }

    public MenuPosition getMenuPosition() {
        return menuPosition;
    }

    protected void showPopup(JPopupMenu menu) {
        if (menuPosition == null) {
            menuPosition = MenuPosition.BOTTOM;
        }
        final int x;
        final int y;
        switch (menuPosition) {
        case BOTTOM:
            x = 0;
            y = getHeight();
            break;
        case TOP:
            x = 0;
            y = menu.getHeight() * -1;
            break;
        case LEFT:
            x = menu.getWidth() * -1;
            y = 0;
            break;
        case RIGHT:
            x = getWidth();
            y = 0;
            break;
        default:
            throw new UnsupportedOperationException("Unsupported position: " + menuPosition);
        }
        menu.show(this, x, y);
    }

    public JPopupMenu getMenu() {
        return popupMenu;
    }

    public static void main(String[] args) {
        LookAndFeelManager.get().init();
        PopupButton popupButton = new PopupButton("More", ImageManager.get().getImageIcon("images/menu/more.png"));
        JPanel toolBarPanel = new JPanel();
        toolBarPanel.add(popupButton);
        JToolBar toolBar = new JToolBar();
        toolBar.add(toolBarPanel);

        JPanel popupPanel = new JPanel();
        popupPanel.setLayout(new BorderLayout());
        popupPanel.add(new JLabel("This popup has components"), BorderLayout.NORTH);
        popupPanel.add(new JTextArea("Some text", 15, 20), BorderLayout.CENTER);
        popupPanel.add(new JSlider(), BorderLayout.SOUTH);
        popupButton.getMenu().add(popupPanel);

        final JFrame frame = new JFrame("test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(640, 480));
        frame.add(toolBar, BorderLayout.PAGE_START);

        frame.pack();
        frame.setVisible(true);
    }
}
