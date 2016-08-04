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

import java.awt.CardLayout;
import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.datacleaner.actions.MoveComponentTimerActionListener;
import org.datacleaner.util.WidgetUtils;

public class RightInformationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int WIDTH = 360;
    private static final int HEIGHT = 500;
    private static final int POSITION_Y = 130;

    private final DCGlassPane _glassPane;
    private final CardLayout _carLayout = new CardLayout();
    private final Color _background = WidgetUtils.BG_COLOR_BRIGHTEST;
    private final Color _foreground = WidgetUtils.BG_COLOR_DARKEST;
    private final Color _borderColor = WidgetUtils.BG_COLOR_MEDIUM;

    private String openedCard = "";

    public RightInformationPanel(DCGlassPane glassPane) {
        super();
        _glassPane = glassPane;

        setBorder(new CompoundBorder(new LineBorder(_borderColor, 1), new EmptyBorder(20, 20, 20, 30)));
        setSize(WIDTH, HEIGHT);
        setLocation(getXWhenOut(), POSITION_Y);
        setLayout(_carLayout);
    }

    public void addTabToPane(String panelTitle, JComponent panel) {
        add(panel, panelTitle);
    }

    public  void toggleWindow(String tabTitle) {
        if (openedCard.equals(tabTitle)) {
            closeWindow();
        } else {
            openWindow(tabTitle);
        }
    }

    private void openWindow(String tabTitle) {
        if(!openedCard.equals(tabTitle)) {
            moveOut();
        }
        _carLayout.show(this, tabTitle);
        moveIn();
        openedCard = tabTitle;
    }

    private void closeWindow() {
        moveOut();
        openedCard = "";
    }

    public String getOpenedCard() {
        return openedCard;
    }

    private int getXWhenOut() {
        return _glassPane.getSize().width + WIDTH + 10;
    }

    private int getXWhenIn() {
        return _glassPane.getSize().width - WIDTH + 10;
    }

    private void moveIn() {
        setLocation(getXWhenOut(), POSITION_Y);
        _glassPane.add(this);
        final Timer timer = new Timer(10, new MoveComponentTimerActionListener(this, getXWhenIn(), POSITION_Y, 40) {
            @Override
            protected void done() {
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    private void moveOut() {
        final Timer timer = new Timer(10, new MoveComponentTimerActionListener(this, getXWhenOut(), POSITION_Y, 40) {
            @Override
            protected void done() {
                RightInformationPanel me = RightInformationPanel.this;
                _glassPane.remove(me);
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    @Override
    public Color getBackground() {
        return _background;
    }

    @Override
    public Color getForeground() {
        return _foreground;
    }
}
