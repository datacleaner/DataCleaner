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

import java.util.EventObject;
import javax.swing.JTabbedPane;

/**
 * This is a slightly rewritten/modified version of swingutil's
 * ClosableTabbedPane
 */
public class TabCloseEvent extends EventObject {

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