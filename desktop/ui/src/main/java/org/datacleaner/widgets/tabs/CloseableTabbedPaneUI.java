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
package org.datacleaner.widgets.tabs;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;

/**
 * This is a slightly rewritten/modified version of swingutil's
 * ClosableTabbedPane
 */
final class CloseableTabbedPaneUI extends BasicTabbedPaneUI {

    // the close image(s)
    private static final Image CLOSE_IMAGE = ImageManager.get().getImage("images/widgets/tab_close.png");
    private static final Image CLOSE_IMAGE_HOVER = ImageManager.get().getImage("images/widgets/tab_close_hover.png");

    // the width of the close images
    private static final int CLOSE_ICON_WIDTH = IconUtils.ICON_SIZE_MEDIUM;

    // the top-margin of a close icon
    private static final int CLOSE_ICON_RIGHT_MARGIN = 6;

    // the insets for a single tab
    private static final Insets TAB_INSETS = new Insets(0, 2, 1, 3);

    // the width of a separator
    private static final int SEPARATOR_WIDTH = 10;

    private final Color _tabUnselectedBackgroundColor = WidgetUtils.COLOR_WELL_BACKGROUND;
    private final Color _tabSelectedBackgroundColor = WidgetUtils.COLOR_DEFAULT_BACKGROUND;
    private final Color _tabBorderColor = WidgetUtils.BG_COLOR_LESS_BRIGHT;

    private final CloseableTabbedPaneMouseListener _mouseListener;
    private final CloseableTabbedPane _pane;

    public CloseableTabbedPaneUI(CloseableTabbedPane pane) {
        super();
        _pane = pane;
        _mouseListener = new CloseableTabbedPaneMouseListener(this, pane);
    }

    @Override
    protected Insets getTabInsets(int tabPlacement, int tabIndex) {
        return TAB_INSETS;
    }

    @Override
    protected Insets getSelectedTabPadInsets(int tabPlacement) {
        return TAB_INSETS;
    }

    @Override
    protected Insets getContentBorderInsets(int tabPlacement) {
        return new Insets(2, 2, 2, 2);
    }

    @Override
    protected Insets getTabAreaInsets(int tabPlacement) {
        return new Insets(0, 0, 0, 0);
    }

    @Override
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        if (isSelected) {
            return 1;
        }
        return 0;
    };

    // increases the visibility of the getRunForTab(...) method
    public int getRunForTab(int tabCount, int tabIndex) {
        return super.getRunForTab(tabCount, tabIndex);
    };

    private Insets getBorderInsets() {
        final Border border = _pane.getBorder();
        if (border == null) {
            return new Insets(0, 0, 0, 0);
        }
        return border.getBorderInsets(_pane);
    }

    /**
     * Helper-method to get a rectangle definition for the close-icon
     * 
     * @param tabIndex
     * @return
     */
    public Rectangle closeRectFor(int tabIndex) {
        final Rectangle rect = rects[tabIndex];
        final int x = rect.x + rect.width - CLOSE_ICON_WIDTH - CLOSE_ICON_RIGHT_MARGIN;
        final int y = rect.y + (rect.height - CLOSE_ICON_WIDTH) / 2;
        final int width = CLOSE_ICON_WIDTH;
        final int height = width;
        return new Rectangle(x, y, width, height);
    }

    /**
     * Override this to provide extra space on right for close button
     */
    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        if (_pane.getSeparators().contains(tabIndex)) {
            return SEPARATOR_WIDTH;
        }
        int width = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
        if (!_pane.getUnclosables().contains(tabIndex)) {
            width += CLOSE_ICON_WIDTH;
        }
        return width;
    }

    @Override
    protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
        final Insets insets = getBorderInsets();
        final int result = maxTabHeight * horizRunCount + insets.top;
        return result;
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight);
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title,
            Rectangle textRect, boolean isSelected) {
        final Rectangle r = new Rectangle(textRect);
        if (!_pane.getUnclosables().contains(tabIndex)) {
            r.x -= CLOSE_ICON_WIDTH - (CLOSE_ICON_WIDTH / 2);
        }
        super.paintText(g, tabPlacement, font, metrics, tabIndex, title, r, isSelected);
    }

    @Override
    protected void paintIcon(Graphics g, int tabPlacement, int tabIndex, Icon icon, Rectangle iconRect,
            boolean isSelected) {
        final Rectangle r = new Rectangle(iconRect);
        if (!_pane.getUnclosables().contains(tabIndex)) {
            r.x -= CLOSE_ICON_WIDTH - (CLOSE_ICON_WIDTH / 2);
        }
        super.paintIcon(g, tabPlacement, tabIndex, icon, r, isSelected);
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
            boolean isSelected) {
        g.setColor(_tabBorderColor);

        // the top line
        g.drawLine(x, y, x + w, y);

        // left line
        g.drawLine(x, y, x, y + h);

        // right line
        g.drawLine(x + w, y, x + w, y + h);

        // the left arc
        g.drawLine(x, y, x, y);

        // the right arc
        g.drawLine(x + w, y, x + w, y);
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
            boolean isSelected) {

        final Color backgroundColor;
        if (isSelected) {
            backgroundColor = _tabSelectedBackgroundColor;
        } else {
            backgroundColor = _tabUnselectedBackgroundColor;
        }

        g.setColor(backgroundColor);

        // height of filled rectangle needs to fill many tab runs and the
        // potential "empty space" generated by separators
        final int height = h * 4;

        g.fillRect(x, y, w, height);

        if (!_pane.getUnclosables().contains(tabIndex)) {
            final Rectangle closeRect = closeRectFor(tabIndex);
            final Image image = _mouseListener.getClosedIndex() != tabIndex ? CLOSE_IMAGE : CLOSE_IMAGE_HOVER;
            g.drawImage(image, closeRect.x, closeRect.y, _pane);
        }
    }

    /**
     * Paints the border for the tab's content, ie. the area below the tabs
     */
    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int tabIndex) {
        final int w = tabPane.getWidth();
        final int h = tabPane.getHeight();
        final Insets tabAreaInsets = getTabAreaInsets(tabPlacement);

        final int x = 0;
        final int y = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight) + tabAreaInsets.bottom;

        g.setColor(_tabSelectedBackgroundColor);
        g.fillRect(x, y, w, h);

        g.setColor(_tabBorderColor);

        // top line, except below selected tab
        final Rectangle selectTabBounds = getTabBounds(tabPane, tabIndex);
        g.drawLine(x, y, selectTabBounds.x, y);
        g.drawLine(selectTabBounds.x + selectTabBounds.width, y, x + w, y);
    }

    @Override
    protected int getTabRunIndent(int tabPlacement, int run) {
        return 4;
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        _pane.addMouseListener(_mouseListener);
        _pane.addMouseMotionListener(_mouseListener);
    }

    @Override
    protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect,
            Rectangle textRect) {
        if (_pane.getSeparators().contains(tabIndex)) {
            return;
        }
        super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
            Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    }
}
