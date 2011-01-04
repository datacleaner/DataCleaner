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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

class CloseableTabbedPaneMouseListener extends MouseAdapter implements MouseMotionListener {

	private final CloseableTabbedPaneUI _tabbedPaneUI;
	private final CloseableTabbedPane _pane;
	private volatile int _closedIndex = -1;

	/**
	 * @param closeableTabbedPaneUI
	 */
	public CloseableTabbedPaneMouseListener(CloseableTabbedPaneUI closeableTabbedPaneUI, CloseableTabbedPane pane) {
		_tabbedPaneUI = closeableTabbedPaneUI;
		_pane = pane;
	}

	public int getClosedIndex() {
		return _closedIndex;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		_closedIndex = -1;
		_pane.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		final int button = e.getButton();
		if (button == MouseEvent.NOBUTTON) {
			return;
		}

		if (!_pane.isEnabled()) {
			return;
		}

		int clickedTabIndex = _tabbedPaneUI.tabForCoordinate(_pane, e.getX(), e.getY());

		if (clickedTabIndex == -1) {
			return;
		}

		// regular left click
		if (button == MouseEvent.BUTTON1) {

			// only allow closing windows on the same run (row of tabs) as the
			// selected tab
			int selectedIndex = _pane.getSelectedIndex();
			int runIndexOfSelectedTab = _tabbedPaneUI.getRunForTab(_pane.getTabCount(), selectedIndex);
			int runIndexOfClickedTab = _tabbedPaneUI.getRunForTab(_pane.getTabCount(), clickedTabIndex);
			if (runIndexOfClickedTab != runIndexOfSelectedTab) {
				return;
			}

			if (_pane.getUnclosables().contains(clickedTabIndex)) {
				return;
			}
			if (_pane.getSeparators().contains(clickedTabIndex)) {
				return;
			}

			Rectangle r = _tabbedPaneUI.closeRectFor(clickedTabIndex);
			// Check for mouse being in close box
			if (r.contains(new Point(e.getX(), e.getY()))) {
				// Send tab closed message
				_pane.closeTab(clickedTabIndex);
			}
		}

		// right click
		if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
			ActionListener actionListener = _pane.getRightClickActionListener(clickedTabIndex);
			if (actionListener != null) {
				actionListener.actionPerformed(new ActionEvent(e, clickedTabIndex, "right-click"));
			}
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
		int tabIndex = _tabbedPaneUI.tabForCoordinate(_pane, e.getX(), e.getY());

		if (tabIndex == -1) {
			return;
		}
		if (_pane.getUnclosables().contains(tabIndex)) {
			return;
		}
		if (_pane.getSeparators().contains(tabIndex)) {
			return;
		}

		Rectangle r = _tabbedPaneUI.closeRectFor(tabIndex);
		if (r.contains(new Point(e.getX(), e.getY()))) {
			_closedIndex = tabIndex;
		} else {
			_closedIndex = -1;
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
		int tabIndex = _tabbedPaneUI.tabForCoordinate(_pane, e.getX(), e.getY());
		if (tabIndex == -1) {
			return;
		}
		if (_pane.getUnclosables().contains(tabIndex)) {
			return;
		}
		if (_pane.getSeparators().contains(tabIndex)) {
			return;
		}

		Rectangle r = _tabbedPaneUI.closeRectFor(tabIndex);
		if (r.contains(new Point(e.getX(), e.getY()))) {
			_closedIndex = tabIndex;
		} else {
			_closedIndex = -1;
		}

		_pane.repaint();
	}
}