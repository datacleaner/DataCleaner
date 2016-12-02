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
package org.datacleaner.widgets.tabs;

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

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

import org.datacleaner.panels.DCBannerPanel;
import org.datacleaner.util.WidgetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a slightly rewritten/modified version of swingutil's
 * ClosableTabbedPane
 */
public final class CloseableTabbedPane extends JTabbedPane {

    public static final Color COLOR_BACKGROUND = WidgetUtils.COLOR_DEFAULT_BACKGROUND;
    public static final Color COLOR_FOREGROUND_SELECTED = WidgetUtils.BG_COLOR_DARKEST;
    public static final Color COLOR_FOREGROUND = WidgetUtils.BG_COLOR_LESS_DARK;
    private static final long serialVersionUID = -411551524171347329L;
    private static Logger _logger = LoggerFactory.getLogger(CloseableTabbedPane.class);
    private final List<TabCloseListener> _closeListeners = new LinkedList<>();
    private final List<Integer> _unclosables = new LinkedList<>();
    private final List<Integer> _separators = new LinkedList<>();
    private final Map<Integer, ActionListener> _doubleClickActionListeners = new HashMap<>();
    private final Map<Integer, ActionListener> _rightClickActionListeners = new HashMap<>();

    /**
     * Create a tabbed pane using defaults
     */
    public CloseableTabbedPane() {
        this(true);
    }

    /**
     * Create a tabbed pane
     *
     * @param addBorder
     *            add a small border around the tabbed pane?
     */
    public CloseableTabbedPane(final boolean addBorder) {
        super(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        setUI(new CloseableTabbedPaneUI(this));
        setForeground(COLOR_FOREGROUND);
        setBackground(COLOR_BACKGROUND);
        setOpaque(true);
        if (addBorder) {
            setBorder(WidgetUtils.BORDER_WIDE_DEFAULT);
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
    public CloseableTabbedPane setUnclosableTab(final int val) {
        if (!_unclosables.contains(val)) {
            _unclosables.add(val);
        }
        return this;
    }

    public void addSeparator() {
        synchronized (this) {
            final int tabCountBefore = getTabCount();
            addTab("SEPARATOR", new JLabel());
            _separators.add(tabCountBefore);
        }
    }

    /**
     * Use this method to reverse the actions of {@link #setUnclosableTab(int)}
     */
    public void setClosableTab(final int val) {
        // cast to Object to ensure the RIGHT remove(...) method is invoked in
        // _unclosables
        final Object value = Integer.valueOf(val);
        _unclosables.remove(value);
    }

    @SuppressWarnings("unchecked")
    public <E extends Component> List<E> getTabsOfClass(final Class<E> clazz) {
        final List<E> list = new ArrayList<>();
        final Component[] components = getComponents();
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
    public void addTabCloseListener(final TabCloseListener lis) {
        _closeListeners.add(lis);
    }

    /** Remove a tab close listener */
    public void removeTabCloseListener(final TabCloseListener lis) {
        _closeListeners.remove(lis);
    }

    public void closeTab(final int tabIndex) {
        final Component component = getComponent(tabIndex);
        remove(tabIndex);

        int selectedIndex = getSelectedIndex();
        while (_separators.contains(selectedIndex) && selectedIndex > 0) {
            // make sure the separator tabs are not "selected"
            selectedIndex = selectedIndex - 1;
            setSelectedIndex(selectedIndex);
        }

        if (!_closeListeners.isEmpty()) {
            final TabCloseEvent ev = new TabCloseEvent(this, tabIndex, component);
            for (final TabCloseListener l : _closeListeners) {
                try {
                    l.tabClosed(ev);
                } catch (final Exception ex) {
                    _logger.error(ex.toString(), ex);
                }
            }
        }
    }

    @Override
    public void remove(final Component component) {
        final int index = indexOfComponent(component);
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
            final Set<Integer> keySet = new TreeSet<>(Collections.reverseOrder());
            keySet.addAll(_rightClickActionListeners.keySet());
            for (final Integer key : keySet) {
                final int curIndex = key.intValue();
                if (curIndex > removedIndex) {
                    final ActionListener actionListener = _rightClickActionListeners.get(curIndex);
                    _rightClickActionListeners.remove(curIndex);
                    _rightClickActionListeners.put(curIndex - 1, actionListener);
                }
            }
        }

        // move all double click listeners for tabs above this index down
        {
            final Set<Integer> keySet = new TreeSet<>(Collections.reverseOrder());
            keySet.addAll(_doubleClickActionListeners.keySet());
            for (final Integer key : keySet) {
                final int curIndex = key.intValue();
                if (curIndex > removedIndex) {
                    final ActionListener actionListener = _doubleClickActionListeners.get(curIndex);
                    _doubleClickActionListeners.remove(curIndex);
                    _doubleClickActionListeners.put(curIndex - 1, actionListener);
                }
            }
        }

        // moved all the uncloseable tabs for tabs above this index down
        {
            for (final ListIterator<Integer> it = _unclosables.listIterator(); it.hasNext(); ) {
                final Integer tabIndex = it.next();
                final int curIndex = tabIndex.intValue();
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

    public void setRightClickActionListener(final int index, final ActionListener actionListener) {
        _rightClickActionListeners.put(index, actionListener);
    }

    public ActionListener getRightClickActionListener(final int index) {
        return _rightClickActionListeners.get(index);
    }

    public void setDoubleClickActionListener(final int index, final ActionListener actionListener) {
        _doubleClickActionListeners.put(index, actionListener);
    }

    public ActionListener getDoubleClickActionListener(final int index) {
        return _doubleClickActionListeners.get(index);
    }

    @Override
    public Color getForegroundAt(final int index) {
        if (getSelectedIndex() == index) {
            return COLOR_FOREGROUND_SELECTED;
        }
        return super.getForegroundAt(index);
    }

    @Override
    public void updateUI() {
        repaint();
    }

    public boolean isCloseable(final int index) {
        return !isUncloseable(index);
    }

    public boolean isUncloseable(final int index) {
        return _unclosables.contains(index);
    }

    public Rectangle getTabBounds(final int tabIndex) {
        return getUI().getTabBounds(this, tabIndex);
    }

    public void bindTabTitleToBanner(final DCBannerPanel bannerPanel) {
        final ChangeListener changeListener = e -> {
            final int selectedIndex = getSelectedIndex();
            if (selectedIndex == -1) {
                return;
            }
            final String title = getTitleAt(selectedIndex);
            bannerPanel.setTitle2(title);
            bannerPanel.updateUI();
        };
        addChangeListener(changeListener);

        // trigger an initial update
        changeListener.stateChanged(null);
    }
}
