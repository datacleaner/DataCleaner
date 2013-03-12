/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;

/**
 * Listener used to switch between shown/hidden titles of tabs
 * 
 * @author Kasper Sørensen
 */
public final class HideTabTextActionListener implements ActionListener {

	private final CloseableTabbedPane _tabbedPane;
	private final String _title;

	public HideTabTextActionListener(CloseableTabbedPane tabbedPane, int tabIndex) {
		_tabbedPane = tabbedPane;
		_title = tabbedPane.getTitleAt(tabIndex);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    int tabIndex = e.getID();
		if (_title.equals(_tabbedPane.getTitleAt(tabIndex))) {
			_tabbedPane.setTitleAt(tabIndex, "");
		} else {
			_tabbedPane.setTitleAt(tabIndex, _title);
		}
	}

}
