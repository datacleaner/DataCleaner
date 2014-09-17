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
package org.eobjects.datacleaner.widgets.tabs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eobjects.datacleaner.panels.DCBannerPanel;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a slightly rewritten/modified version of swingutil's
 * ClosableTabbedPane
 */
public final class CloseableTabbedPane extends JTabbedPane {

    private static final long serialVersionUID = -411551524171347329L;
    private static Logger _logger = LoggerFactory.getLogger(CloseableTabbedPane.class);

    private final List<TabCloseListener> _closeListeners = new LinkedList<TabCloseListener>();
    private final List<Integer> _unclosables = new LinkedList<Integer>();
    private final List<Integer> _separators = new LinkedList<Integer>();
    private final Map<Integer, ActionListener> _doubleClickActionListeners = new HashMap<Integer, ActionListener>();
    private final Map<Integer, ActionListener> _rightClickActionListeners = new HashMap<Integer, ActionListener>();

    private Color unselectedTabTopColor = WidgetUtils.BG_COLOR_DARKEST;
    private Color unselectedTabBottomColor = WidgetUtils.BG_COLOR_DARKEST;
    private Color selectedTabTopColor = WidgetUtils.BG_COLOR_BRIGHTEST;
    private Color selectedTabBottomColor = WidgetUtils.BG_COLOR_BRIGHT;
    private Color _tabBorderColor = WidgetUtils.BG_COLOR_LESS_DARK;

    /**
     * Create a tabbed pane using defaults
     */
    public CloseableTabbedPane() {
        //
        this(true);
    }

    /**
     * Create a tabbed pane
     * 
     * @param addBorder
     *            add a small border around the tabbed pane?
     */
    public CloseableTabbedPane(boolean addBorder) {
        super(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        setUI(new CloseableTabbedPaneUI(this));
        setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        setBackground(WidgetUtils.BG_COLOR_DARK);
        setOpaque(true);
        if (addBorder) {
            setBorder(WidgetUtils.BORDER_WIDE);
        }
    }

    /**
     * Tyically, the last tab open is not closable. So, call this method with a
     * tab number and that tab will not contain a close icon. If you monitor
     * with a {@link TabCloseListener}, you can use {@link #getTabCount()} and
     * when it is 1, you can call {@link #setUncloseableTab(int)}(0) to make the
     * last tab unclosable.
     * 
     * @return
     * 
     * @see #setCloseableTab(int);
     */
    public CloseableTabbedPane setUnclosableTab(int val) {
        if (!_unclosables.contains(val)) {
            _unclosables.add(val);
        }
        return this;
    }

    public void addSeparator() {
        synchronized (this) {
            int tabCountBefore = getTabCount();
            addTab("SEPARATOR", new JLabel());
            _separators.add(tabCountBefore);
        }
    }

    public void addTab(Tab tab) {
        final Icon icon = tab.getIcon();
        if (icon == null) {
            addTab(tab.getTitle(), tab.getContents());
        } else {
            addTab(tab.getTitle(), icon, tab.getContents());
        }

        if (!tab.isCloseable()) {
            final int index = getTabCount() - 1;
            setUnclosableTab(index);
        }
    }

    /**
     * Use this method to reverse the actions of {@link #setUnclosableTab(int)}
     */
    public void setClosableTab(int val) {
        // cast to Object to ensure the RIGHT remove(...) method is invoked in
        // _unclosables
        Object value = Integer.valueOf(val);
        _unclosables.remove(value);
    }

    @SuppressWarnings("unchecked")
    public <E extends Component> List<E> getTabsOfClass(Class<E> clazz) {
        List<E> list = new ArrayList<E>();
        Component[] components = getComponents();
        for (Component component : components) {
            if (component instanceof JScrollPane) {
                component = ((JScrollPane) component).getViewport().getComponent(0);
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
        _closeListeners.add(lis);
    }

    /** Remove a tab close listener */
    public void removeTabCloseListener(TabCloseListener lis) {
        _closeListeners.remove(lis);
    }

    public void closeTab(int tabIndex) {
        Component component = getComponent(tabIndex);
        remove(tabIndex);

        int selectedIndex = getSelectedIndex();
        while (_separators.contains(selectedIndex) && selectedIndex > 0) {
            // make sure the separator tabs are not "selected"
            selectedIndex = selectedIndex - 1;
            setSelectedIndex(selectedIndex);
        }

        if (!_closeListeners.isEmpty()) {
            TabCloseEvent ev = new TabCloseEvent(this, tabIndex, component);
            for (TabCloseListener l : _closeListeners) {
                try {
                    l.tabClosed(ev);
                } catch (Exception ex) {
                    _logger.error(ex.toString(), ex);
                }
            }
        }
    }

    @Override
    public void remove(final Component component) {
        int index = indexOfComponent(component);
        remove(index);
    }

    @Override
    public void remove(final int removedIndex) {
        if (removedIndex < 0) {
            return;
        }
        super.remove(removedIndex);
        _rightClickActionListeners.remove(removedIndex);
        _doubleClickActionListeners.remove(removedIndex);

        // move all right click listeners for tabs above this index down
        {
            Set<Integer> keySet = new TreeSet<Integer>(Collections.reverseOrder());
            keySet.addAll(_rightClickActionListeners.keySet());
            for (Integer key : keySet) {
                int curIndex = key.intValue();
                if (curIndex > removedIndex) {
                    ActionListener actionListener = _rightClickActionListeners.get(curIndex);
                    _rightClickActionListeners.remove(curIndex);
                    _rightClickActionListeners.put(curIndex - 1, actionListener);
                }
            }
        }

        // move all double click listeners for tabs above this index down
        {
            Set<Integer> keySet = new TreeSet<Integer>(Collections.reverseOrder());
            keySet.addAll(_doubleClickActionListeners.keySet());
            for (Integer key : keySet) {
                int curIndex = key.intValue();
                if (curIndex > removedIndex) {
                    ActionListener actionListener = _doubleClickActionListeners.get(curIndex);
                    _doubleClickActionListeners.remove(curIndex);
                    _doubleClickActionListeners.put(curIndex - 1, actionListener);
                }
            }
        }

        // moved all the uncloseable tabs for tabs above this index down
        {
            for (ListIterator<Integer> it = _unclosables.listIterator(); it.hasNext();) {
                Integer tabIndex = it.next();
                int curIndex = tabIndex.intValue();
                if (curIndex == removedIndex) {
                    it.remove();
                } else if (curIndex > removedIndex) {
                    it.set(curIndex - 1);
                }
            }
        }
    }

    @Override
    public void setSelectedIndex(int index) {
        if (getSeparators().contains(index)) {
            index--;
            setSelectedIndex(index);
        } else {
            super.setSelectedIndex(index);
        }
    }

    public void closeAllTabs() {
        for (int i = getTabCount() - 1; i >= 0; i--) {
            closeTab(i);
        }
        removeAll();
    }

    protected List<Integer> getUnclosables() {
        return _unclosables;
    }

    protected List<Integer> getSeparators() {
        return _separators;
    }

    public void setRightClickActionListener(int index, ActionListener actionListener) {
        _rightClickActionListeners.put(index, actionListener);
    }

    public ActionListener getRightClickActionListener(int index) {
        return _rightClickActionListeners.get(index);
    }

    public void setDoubleClickActionListener(int index, ActionListener actionListener) {
        _doubleClickActionListeners.put(index, actionListener);
    }

    public ActionListener getDoubleClickActionListener(int index) {
        return _doubleClickActionListeners.get(index);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        closeAllTabs();
    }

    @Override
    public Color getForegroundAt(int index) {
        if (getSelectedIndex() == index) {
            return WidgetUtils.BG_COLOR_DARKEST;
        }
        return super.getForegroundAt(index);
    }

    @Override
    public void updateUI() {
        repaint();
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
    }

    public Color getUnselectedTabTopColor() {
        return unselectedTabTopColor;
    }

    public void setUnselectedTabTopColor(Color unselectedTabTopColor) {
        this.unselectedTabTopColor = unselectedTabTopColor;
    }

    public Color getUnselectedTabBottomColor() {
        return unselectedTabBottomColor;
    }

    public void setUnselectedTabBottomColor(Color unselectedTabBottomColor) {
        this.unselectedTabBottomColor = unselectedTabBottomColor;
    }

    public Color getSelectedTabTopColor() {
        return selectedTabTopColor;
    }

    public void setSelectedTabTopColor(Color selectedTabTopColor) {
        this.selectedTabTopColor = selectedTabTopColor;
    }

    public Color getSelectedTabBottomColor() {
        return selectedTabBottomColor;
    }

    public void setSelectedTabBottomColor(Color selectedTabBottomColor) {
        this.selectedTabBottomColor = selectedTabBottomColor;
    }

    public Color getTabBorderColor() {
        return _tabBorderColor;
    }

    public void setTabBorderColor(Color tabBorderColor) {
        _tabBorderColor = tabBorderColor;
    }

    public Tab getTab(int i) {
        Component contents = getTabComponentAt(i);
        String title = getTitleAt(i);
        Icon icon = getIconAt(i);
        boolean closeable = isCloseable(i);
        return new Tab(title, icon, contents, closeable);
    }

    public boolean isCloseable(int i) {
        return !isUncloseable(i);
    }

    public boolean isUncloseable(int i) {
        return _unclosables.contains(i);
    }

    public Rectangle getTabBounds(int tabIndex) {
        return getUI().getTabBounds(this, tabIndex);
    }

    public void bindTabTitleToBanner(final DCBannerPanel bannerPanel) {
        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int selectedIndex = getSelectedIndex();
                if (selectedIndex == -1) {
                    return;
                }
                String title = getTitleAt(selectedIndex);
                bannerPanel.setTitle2(title);
                bannerPanel.updateUI();
            }
        };
        addChangeListener(changeListener);

        // trigger an initial update
        changeListener.stateChanged(null);
    }
}