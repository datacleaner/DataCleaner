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

import java.util.EventObject;
import javax.swing.JTabbedPane;

/**
 * This is a slightly rewritten/modified version of swingutil's
 * ClosableTabbedPane
 */
public final class TabCloseEvent extends EventObject {

	private static final long serialVersionUID = -8865377836780462308L;
	/** The index of the closing tab */
	private int tab;

	/**
	 * Construct an event for the indicated tab number associated with the
	 * indicated JTabbedPane
	 */
	public TabCloseEvent(JTabbedPane pane, int tab) {
		super(pane);
		this.tab = tab;
	}

	/**
	 * Get the closed tab index
	 * 
	 * @return the tab that is closed
	 */
	public int getClosedTab() {
		return tab;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TabCloseEvent) {
			TabCloseEvent that = (TabCloseEvent) obj;
			return this.tab == that.tab;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return tab;
	}

	@Override
	public String toString() {
		return "TabCloseEvent[tab=" + tab + "]";
	}
}