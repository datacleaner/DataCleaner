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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class CloseableTabbedPaneMouseListener extends MouseAdapter implements MouseMotionListener {
	/**
	 * 
	 */
	private final CloseableTabbedPaneUI _closeableTabbedPaneUI;

	/**
	 * @param closeableTabbedPaneUI
	 */
	CloseableTabbedPaneMouseListener(CloseableTabbedPaneUI closeableTabbedPaneUI) {
		_closeableTabbedPaneUI = closeableTabbedPaneUI;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		_closeableTabbedPaneUI._closeIdx = -1;
		_closeableTabbedPaneUI._pane.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!_closeableTabbedPaneUI._pane.isEnabled()) {
			return;
		}
		if (e.getButton() != 1) {
			return;
		}
		
		
		int clickedTabIndex = _closeableTabbedPaneUI.tabForCoordinate(_closeableTabbedPaneUI._pane, e.getX(), e.getY());

		if (clickedTabIndex == -1) {
			return;
		}

		// only allow closing windows on the same run (row of tabs) as the selected tab
		int selectedIndex = _closeableTabbedPaneUI._pane.getSelectedIndex();
		int runIndexOfSelectedTab = _closeableTabbedPaneUI.getRunForTab(_closeableTabbedPaneUI._pane.getTabCount(), selectedIndex);
		int runIndexOfClickedTab = _closeableTabbedPaneUI.getRunForTab(_closeableTabbedPaneUI._pane.getTabCount(), clickedTabIndex);
		if (runIndexOfClickedTab != runIndexOfSelectedTab) {
			return;
		}

		if (_closeableTabbedPaneUI._pane.getUnclosables().contains(clickedTabIndex)) {
			return;
		}
		if (_closeableTabbedPaneUI._pane.getSeparators().contains(clickedTabIndex)) {
			return;
		}

		Rectangle r = _closeableTabbedPaneUI.closeRectFor(clickedTabIndex);
		// Check for mouse being in close box
		if (r.contains(new Point(e.getX(), e.getY()))) {
			// Send tab closed message
			_closeableTabbedPaneUI._pane.closeTab(clickedTabIndex);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (!_closeableTabbedPaneUI._pane.isEnabled()) {
			return;
		}
		if (e.getButton() != 1) {
			return;
		}
		int tabIndex = _closeableTabbedPaneUI.tabForCoordinate(_closeableTabbedPaneUI._pane, e.getX(), e.getY());

		if (tabIndex == -1) {
			return;
		}
		if (_closeableTabbedPaneUI._pane.getUnclosables().contains(tabIndex)) {
			return;
		}
		if (_closeableTabbedPaneUI._pane.getSeparators().contains(tabIndex)) {
			return;
		}

		Rectangle r = _closeableTabbedPaneUI.closeRectFor(tabIndex);
		if (r.contains(new Point(e.getX(), e.getY()))) {
			_closeableTabbedPaneUI._closeIdx = tabIndex;
		} else {
			_closeableTabbedPaneUI._closeIdx = -1;
		}
		_closeableTabbedPaneUI._pane.repaint();
	}

	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
		mousePressed(e);
	}

	public void mouseMoved(MouseEvent e) {
		if (_closeableTabbedPaneUI._pane == null || !_closeableTabbedPaneUI._pane.isEnabled()) {
			return;
		}
		int tabIndex = _closeableTabbedPaneUI.tabForCoordinate(_closeableTabbedPaneUI._pane, e.getX(), e.getY());
		if (tabIndex == -1) {
			return;
		}
		if (_closeableTabbedPaneUI._pane.getUnclosables().contains(tabIndex)) {
			return;
		}
		if (_closeableTabbedPaneUI._pane.getSeparators().contains(tabIndex)) {
			return;
		}

		Rectangle r = _closeableTabbedPaneUI.closeRectFor(tabIndex);
		if (r.contains(new Point(e.getX(), e.getY()))) {
			_closeableTabbedPaneUI._closeIdx = tabIndex;
		} else {
			_closeableTabbedPaneUI._closeIdx = -1;
		}

		_closeableTabbedPaneUI._pane.repaint();
	}
}