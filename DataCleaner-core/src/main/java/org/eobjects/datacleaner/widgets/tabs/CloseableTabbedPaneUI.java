/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.widgets.tabs;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;

/**
 * This is a slightly rewritten/modified version of swingutil's
 * ClosableTabbedPane
 */
final class CloseableTabbedPaneUI extends BasicTabbedPaneUI {

	private static final int HACK_HEIGHT_OF_TABS_AREA = 500;

	// the close image(s)
	private static final Image CLOSE_IMAGE = ImageManager.getInstance().getImage("images/widgets/tab_close.png");
	private static final Image CLOSE_IMAGE_HOVER = ImageManager.getInstance().getImage("images/widgets/tab_close_hover.png");

	// the width of the close images
	private static final int CLOSE_ICON_WIDTH = IconUtils.ICON_SIZE_LARGE;

	// the top-margin of a close icon
	private static final int CLOSE_ICON_TOP_MARGIN = 8;
	private static final int CLOSE_ICON_RIGHT_MARGIN = 4;

	private static final int TAB_CORNER_RADIUS = 4;

	// the insets for a single tab
	private static final Insets TAB_INSETS = new Insets(0, 1, 0, 5);

	// the width of a separator
	private static final int SEPARATOR_WIDTH = 10;

	private final CloseableTabbedPaneMouseListener _mouseListener;
	private final CloseableTabbedPane _pane;

	public CloseableTabbedPaneUI(CloseableTabbedPane pane) {
		super();
		_pane = pane;
		_mouseListener = new CloseableTabbedPaneMouseListener(this, pane);
	}

	public CloseableTabbedPane getPane() {
		return _pane;
	}

	@Override
	protected Insets getTabInsets(int tabPlacement, int tabIndex) {
		return TAB_INSETS;
	}

	// increases the visibility of the getRunForTab(...) method
	public int getRunForTab(int tabCount, int tabIndex) {
		return super.getRunForTab(tabCount, tabIndex);
	};

	/**
	 * Helper-method to get a rectangle definition for the close-icon
	 * 
	 * @param tabIndex
	 * @return
	 */
	Rectangle closeRectFor(int tabIndex) {
		Rectangle rect = rects[tabIndex];
		final int x = rect.x + rect.width - CLOSE_ICON_WIDTH - CLOSE_ICON_RIGHT_MARGIN;
		final int y = rect.y + CLOSE_ICON_TOP_MARGIN;
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
		return CLOSE_ICON_TOP_MARGIN + 4 + _pane.getMarginSize() + (horizRunCount * CLOSE_ICON_WIDTH);
	}

	@Override
	protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title,
			Rectangle textRect, boolean isSelected) {
		Rectangle r = new Rectangle(textRect);
		if (!_pane.getUnclosables().contains(tabIndex)) {
			r.x -= CLOSE_ICON_WIDTH - (CLOSE_ICON_WIDTH / 2);
		}
		super.paintText(g, tabPlacement, font, metrics, tabIndex, title, r, isSelected);
	}

	@Override
	protected void paintIcon(Graphics g, int tabPlacement, int tabIndex, Icon icon, Rectangle iconRect, boolean isSelected) {
		Rectangle r = new Rectangle(iconRect);
		if (!_pane.getUnclosables().contains(tabIndex)) {
			r.x -= CLOSE_ICON_WIDTH - (CLOSE_ICON_WIDTH / 2);
		}
		super.paintIcon(g, tabPlacement, tabIndex, icon, r, isSelected);
	}

	@Override
	protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
		if (!isSelected) {
			g.setColor(_pane.getTabBorderColor());

			// the top line
			g.drawLine(x + TAB_CORNER_RADIUS, y, x + w - TAB_CORNER_RADIUS, y);

			// left line
			g.drawLine(x, y + TAB_CORNER_RADIUS, x, y + h + HACK_HEIGHT_OF_TABS_AREA);

			// right line
			g.drawLine(x + w, y + TAB_CORNER_RADIUS, x + w, y + h + HACK_HEIGHT_OF_TABS_AREA);

			// the left arc
			g.drawLine(x, y + TAB_CORNER_RADIUS, x + TAB_CORNER_RADIUS, y);

			// the right arc
			g.drawLine(x + w - TAB_CORNER_RADIUS, y, x + w, y + TAB_CORNER_RADIUS);
		}
	}

	@Override
	protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
			boolean isSelected) {
		final Color topColor;
		final Color bottomColor;

		if (isSelected) {
			topColor = _pane.getSelectedTabTopColor();
			bottomColor = _pane.getSelectedTabBottomColor();
		} else {
			topColor = _pane.getUnselectedTabTopColor();
			bottomColor = _pane.getUnselectedTabBottomColor();
		}
		final GradientPaint gradient = new GradientPaint(x, y, topColor, x, y + h, bottomColor);

		if (g instanceof Graphics2D) {
			((Graphics2D) g).setPaint(gradient);
		} else {
			g.setColor(bottomColor);
		}

		g.fillRoundRect(x, y, w, TAB_CORNER_RADIUS * 2, TAB_CORNER_RADIUS, TAB_CORNER_RADIUS);
		g.fillRect(x, y + TAB_CORNER_RADIUS, w, h);

		if (!isSelected) {
			// The selected tab is larger than the other ones. This little hack
			// will make the unselected tabs fill out the space below them.
			g.setColor(bottomColor);
			g.fillRect(x, y + h, w, HACK_HEIGHT_OF_TABS_AREA);
		}

		if (!_pane.getUnclosables().contains(tabIndex)) {
			Image image = _mouseListener.getClosedIndex() != tabIndex ? CLOSE_IMAGE : CLOSE_IMAGE_HOVER;
			int imageX = x + w - CLOSE_ICON_WIDTH - CLOSE_ICON_RIGHT_MARGIN;
			int imageY = y + CLOSE_ICON_TOP_MARGIN;
			g.drawImage(image, imageX, imageY, _pane);
		}
	}

	/**
	 * Paints the border for the tab's content, ie. the area below the tabs
	 */
	@Override
	protected void paintContentBorder(Graphics g, int tabPlacement, int tabIndex) {
		// Modification of BasicTabbedPaneUI's paintContentBorder method
		final int width = tabPane.getWidth();
		final int height = tabPane.getHeight();
		Insets tabAreaInsets = getTabAreaInsets(tabPlacement);

		final int x = 0;
		final int y = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight) + tabAreaInsets.bottom;
		final int w = width;
		final int h = height;

		// y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
		// y -= tabAreaInsets.bottom;

		g.setColor(_pane.getSelectedTabBottomColor());
		g.fillRect(x, y, w, h);
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
	protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect,
			Rectangle textRect, boolean isSelected) {
	}
}