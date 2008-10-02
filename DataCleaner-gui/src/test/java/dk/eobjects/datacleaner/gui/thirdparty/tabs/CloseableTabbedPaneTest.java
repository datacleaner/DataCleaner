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
package dk.eobjects.datacleaner.gui.thirdparty.tabs;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.easymock.EasyMock;

import dk.eobjects.thirdparty.tabs.CloseableTabbedPane;
import dk.eobjects.thirdparty.tabs.TabCloseEvent;
import dk.eobjects.thirdparty.tabs.TabCloseListener;
import junit.framework.TestCase;

public class CloseableTabbedPaneTest extends TestCase {

	public void testGetTabsOfClass() throws Exception {
		CloseableTabbedPane tabbedPane = new CloseableTabbedPane();
		JPanel panel1 = new JPanel();
		tabbedPane.addTab("foo", panel1);
		JPanel panel2 = new JPanel();
		tabbedPane.addTab("bar", new JScrollPane(panel2));
		tabbedPane.addTab("foobar", new JLabel("foobar"));

		assertEquals(1, tabbedPane.getTabsOfClass(JLabel.class).size());
		assertEquals(2, tabbedPane.getTabsOfClass(JPanel.class).size());
		assertEquals(3, tabbedPane.getTabsOfClass(JComponent.class).size());
	}

	public void testCloseAllTabs() throws Exception {

		CloseableTabbedPane tabbedPane = new CloseableTabbedPane();

		TabCloseListener listener = EasyMock.createMock(TabCloseListener.class);

		listener.tabClosed(new TabCloseEvent(tabbedPane, 4));
		listener.tabClosed(new TabCloseEvent(tabbedPane, 3));
		listener.tabClosed(new TabCloseEvent(tabbedPane, 2));
		listener.tabClosed(new TabCloseEvent(tabbedPane, 1));
		listener.tabClosed(new TabCloseEvent(tabbedPane, 0));

		EasyMock.replay(listener);

		tabbedPane.addTabCloseListener(listener);

		tabbedPane.addTab("f", new JPanel());
		tabbedPane.addTab("o", new JPanel());

		assertEquals(2, tabbedPane.getTabCount());

		tabbedPane.addTab("b", new JPanel());
		tabbedPane.addTab("a", new JPanel());
		tabbedPane.addTab("r", new JPanel());

		assertEquals(5, tabbedPane.getTabCount());

		tabbedPane.closeAllTabs();

		assertEquals(0, tabbedPane.getTabCount());

		// Shouldn't yield any listener calls
		tabbedPane.closeAllTabs();

		EasyMock.verify(listener);
	}
}