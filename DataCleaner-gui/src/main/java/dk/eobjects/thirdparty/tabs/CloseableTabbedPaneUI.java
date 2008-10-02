/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.thirdparty.tabs;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import dk.eobjects.datacleaner.gui.GuiHelper;

/**
 * This is a slightly rewritten/modified version of swingutil's
 * ClosableTabbedPane
 */
public class CloseableTabbedPaneUI extends BasicTabbedPaneUI {

	private CloseableTabbedPane _pane;
	private ImageIcon _closeIconUp;
	private ImageIcon _closeIconDown;
	private volatile int _closeIdx = -1;
	private volatile int _closeWidth = 22;

	public CloseableTabbedPaneUI(CloseableTabbedPane pane) {
		super();
		_pane = pane;
	}

	public void setCloseIcons(ImageIcon closeIconUp, ImageIcon closeIconDown) {
		_closeIconUp = closeIconUp;
		_closeIconDown = closeIconDown;
	}

	public class CloseableTabbedPaneMouseListener extends MouseAdapter
			implements MouseMotionListener {
		@Override
		public void mouseReleased(MouseEvent e) {
			_closeIdx = -1;
			_pane.repaint();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (!_pane.isEnabled()) {
				return;
			}
			if (e.getButton() != 1) {
				return;
			}
			int tabIndex = tabForCoordinate(_pane, e.getX(), e.getY());
			if (tabIndex == -1) {
				return;
			}
			if (_pane.getUnclosables().contains(tabIndex)) {
				return;
			}

			Rectangle r = closeRectFor(tabIndex);
			// Check for mouse being in close box
			if (r.contains(new Point(e.getX(), e.getY()))) {
				// Send tab closed message
				_pane.closeTab(tabIndex);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (!_pane.isEnabled()) {
				return;
			}
			if (e.getButton() != 1) {
				return;
			}
			int tabIndex = tabForCoordinate(_pane, e.getX(), e.getY());
			if (tabIndex == -1) {
				return;
			}
			if (_pane.getUnclosables().contains(tabIndex)) {
				return;
			}
			Rectangle r = closeRectFor(tabIndex);
			if (r.contains(new Point(e.getX(), e.getY()))) {
				_closeIdx = tabIndex;
			} else {
				_closeIdx = -1;
			}
			_pane.repaint();
		}

		public void mouseDragged(MouseEvent e) {
			mouseMoved(e);
			mousePressed(e);
		}

		public void mouseMoved(MouseEvent e) {
			if (_pane == null || !_pane.isEnabled()) {
				return;
			}
			int tabIndex = tabForCoordinate(_pane, e.getX(), e.getY());
			if (tabIndex == -1) {
				return;
			}
			if (_pane.getUnclosables().contains(tabIndex)) {
				return;
			}

			Rectangle r = closeRectFor(tabIndex);
			if (r.contains(new Point(e.getX(), e.getY()))) {
				_closeIdx = tabIndex;
			} else {
				_closeIdx = -1;
			}

			_pane.repaint();
		}
	}

	private Rectangle closeRectFor(int tabIndex) {
		int cw = closeWidth();
		return new Rectangle(rects[tabIndex].x + rects[tabIndex].width - cw
				- cw / 2, rects[tabIndex].y
				+ (lastTabHeight - (_closeIconUp == null ? cw : _closeIconUp
						.getIconHeight())) / 2, cw, cw + 1);
	}

	/**
	 * Override this to provide extra space on right for close button
	 */
	@Override
	protected int calculateTabWidth(int tabPlacement, int tabIndex,
			FontMetrics metrics) {
		int w = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
		if (_pane.getUnclosables().contains(tabIndex) == false)
			w += closeWidth() + closeWidth() / 2;
		return w;
	}

	volatile int lastTabHeight = 0;

	/**
	 * Override this to provide extra space on right for close button
	 */
	@Override
	protected int calculateTabHeight(int tabPlacement, int tabIndex, int v) {
		int h = super.calculateTabHeight(tabPlacement, tabIndex, v);
		if (_pane.getUnclosables().contains(tabIndex) == false)
			h = Math.max(h, (_closeIconUp != null ? _closeIconUp
					.getIconHeight() : _closeWidth) + 6);
		return lastTabHeight = h;
	}

	public int closeWidth() {
		return _closeIconUp != null ? _closeIconUp.getIconWidth() : _closeWidth;
	}

	int closeDownIdx() {
		return _closeIdx;
	}

	@Override
	protected void paintText(Graphics g, int tabPlacement, Font font,
			FontMetrics metrics, int tabIndex, String title,
			Rectangle textRect, boolean isSelected) {
		Rectangle r = new Rectangle(textRect);
		if (_pane.getUnclosables().contains(tabIndex) == false) {
			r.x -= _closeWidth - (_closeWidth / 2);
		}
		super.paintText(g, tabPlacement, font, metrics, tabIndex, title, r,
				isSelected);
	}

	@Override
	protected void paintIcon(Graphics g, int tabPlacement, int tabIndex,
			Icon icon, Rectangle iconRect, boolean isSelected) {
		Rectangle r = new Rectangle(iconRect);
		if (_pane.getUnclosables().contains(tabIndex) == false) {
			r.x -= _closeWidth - (_closeWidth / 2);
		}
		super.paintIcon(g, tabPlacement, tabIndex, icon, r, isSelected);
	}

	@Override
	protected void paintTabBackground(Graphics g, int tabPlacement,
			int tabIndex, int x, int y, int w, int h, boolean isSelected) {

		if (isSelected) {
			g.setColor(GuiHelper.BG_COLOR_LIGHT);
		} else {
			g.setColor(GuiHelper.BG_COLOR_DARK);
		}
		g.fillRect(x, y, w, h);

		if (!_pane.getUnclosables().contains(tabIndex)) {
			final int closeWidth = closeWidth();
			g.drawImage(closeDownIdx() != tabIndex ? _closeIconUp.getImage()
					: _closeIconDown.getImage(), x + w - closeWidth
					- (closeWidth / 2), y + (h - _closeIconUp.getIconHeight())
					/ 2, _pane);
		}
	}

	@Override
	protected void paintContentBorder(Graphics g, int tabPlacement,
			int selectedIndex) {
		// Modification of BasicTabbedPaneUI's paintContentBorder method
		int width = tabPane.getWidth();
		int height = tabPane.getHeight();
		Insets insets = tabPane.getInsets();
		Insets tabAreaInsets = getTabAreaInsets(tabPlacement);

		int x = insets.left;
		int y = insets.top;
		int w = width - insets.right - insets.left;
		int h = height - insets.top - insets.bottom;

		y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
		y -= tabAreaInsets.bottom;
		h -= (y - insets.top);

		g.setColor(GuiHelper.BG_COLOR_LIGHT);
		g.fillRect(x, y, w, h);
	}

	@Override
	protected void installListeners() {
		super.installListeners();
		CloseableTabbedPaneMouseListener mlis = new CloseableTabbedPaneMouseListener();
		_pane.addMouseListener(mlis);
		_pane.addMouseMotionListener(mlis);
	}

	public void setCloseWidth(int width) {
		_closeWidth = width;
	}
}