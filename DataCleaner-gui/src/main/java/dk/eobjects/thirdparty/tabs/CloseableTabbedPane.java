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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.gui.GuiHelper;

/**
 * This is a slightly rewritten/modified version of swingutil's
 * ClosableTabbedPane
 */
public class CloseableTabbedPane extends JTabbedPane {

	private static final long serialVersionUID = -411551524171347329L;
	private static Log _log = LogFactory.getLog(CloseableTabbedPane.class);
	private Vector<TabCloseListener> _closeListener = new Vector<TabCloseListener>();
	private volatile CloseableTabbedPaneUI _ui;
	private Vector<Integer> _unclosables = new Vector<Integer>();

	/**
	 * Create a tabbed pane
	 */
	public CloseableTabbedPane() {
		super(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		_ui = new CloseableTabbedPaneUI(this);
		setUI(_ui);
		setBorder(new LineBorder(GuiHelper.BG_COLOR_DARKBLUE, 4));
		ImageIcon closeIcon = GuiHelper.getImageIcon("images/tab_close.png");
		ImageIcon closeHoverIcon = GuiHelper
				.getImageIcon("images/tab_close_hover.png");
		setIcons(closeIcon, closeHoverIcon);
	}

	/**
	 * Tyically, the last tab open is not closable. So, call this method with a
	 * tab number and that tab will not contain a close icon. If you monitor
	 * with a {@link TabCloseListener}, you can use {@link #getTabCount()} and
	 * when it is 1, you can call {@link #setUncloseableTab(int)}(0) to make the
	 * last tab unclosable.
	 * @return 
	 * 
	 * @see #setCloseableTab(int);
	 */
	public CloseableTabbedPane setUnclosableTab(int val) {
		if (!_unclosables.contains(val)) {
			_unclosables.addElement(val);
		}
		return this;
	}

	/**
	 * Use this method to reverse the actions of {@link #setUnclosableTab(int)}
	 */
	public void setClosableTab(int val) {
		_unclosables.removeElement(val);
	}

	@SuppressWarnings("unchecked")
	public <E extends Component> List<E> getTabsOfClass(Class<E> clazz) {
		List<E> list = new ArrayList<E>();
		Component[] components = getComponents();
		for (Component component : components) {
			if (component instanceof JScrollPane) {
				component = ((JScrollPane) component).getViewport()
						.getComponent(0);
			}
			if (clazz.isAssignableFrom(component.getClass())) {
				list.add((E) component);
			}
		}
		return list;
	}

	/**
	 * Add a tab close listener. On close events, the listener is responsible
	 * for deleting the tab or otherwise reacting to the event.
	 */
	public void addTabCloseListener(TabCloseListener lis) {
		_closeListener.addElement(lis);
	}

	/** Remove a tab close listener */
	public void removeTabCloseListener(TabCloseListener lis) {
		_closeListener.removeElement(lis);
	}

	public void setIcons(ImageIcon normalIcon, ImageIcon hoverIcon) {
		_ui.setCloseIcons(normalIcon, hoverIcon);
	}

	public void closeTab(int tab) {
		if (_log.isDebugEnabled()) {
			_log
					.debug("Closing tab: " + tab + ", listeners: "
							+ _closeListener);
		}
		if (_closeListener.size() == 0) {
			return;
		}

		TabCloseEvent ev = new TabCloseEvent(this, tab);
		for (TabCloseListener l : _closeListener) {
			try {
				if (_log.isDebugEnabled()) {
					_log.debug("Sending close to: " + l);
				}
				l.tabClosed(ev);
			} catch (Exception ex) {
				_log.error(ex.toString(), ex);
			}
		}
	}

	public void closeAllTabs() {
		for (int i = getTabCount() - 1; i >= 0; i--) {
			closeTab(i);
		}
		removeAll();
	}

	public void setCloseWidth(int width) {
		_ui.setCloseWidth(width);
		repaint();
	}

	public Vector<Integer> getUnclosables() {
		return _unclosables;
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		_log.debug("removeNotify()");
		closeAllTabs();
	}
}