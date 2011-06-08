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
package org.eobjects.datacleaner.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.actions.ExitActions;
import org.eobjects.datacleaner.windows.AbstractDialog;
import org.eobjects.datacleaner.windows.AbstractWindow;
import org.eobjects.datacleaner.windows.DCWindow;

/**
 * Singleton class that manages events related to opening and closing of windows
 * in DataCleaner.
 * 
 * @see DCWindow
 * @see AbstractWindow
 * @see AbstractDialog
 * 
 * @author Kasper Sørensen
 */
public final class WindowManager {

	private static final WindowManager instance = new WindowManager();

	private final List<DCWindow> _windows = new ArrayList<DCWindow>();
	private final List<ActionListener> _listeners = new ArrayList<ActionListener>();

	public static WindowManager getInstance() {
		return instance;
	}

	private WindowManager() {
	}

	public List<DCWindow> getWindows() {
		return Collections.unmodifiableList(_windows);
	}

	public void addListener(ActionListener listener) {
		_listeners.add(listener);
	}

	public void removeListener(ActionListener listener) {
		_listeners.remove(listener);
	}

	public void onDispose(DCWindow window) {
		_windows.remove(window);
		notifyListeners();

		if (_windows.isEmpty()) {
			ExitActions.exit();
		}
	}

	private void notifyListeners() {
		ActionEvent event = new ActionEvent(this, _windows.size(), null);
		for (ActionListener listener : _listeners) {
			listener.actionPerformed(event);
		}
	}

	public void onShow(DCWindow window) {
		_windows.add(window);

		notifyListeners();
	}

	public int getWindowCount(Class<? extends DCWindow> windowClass) {
		int count = 0;
		for (DCWindow window : _windows) {
			if (ReflectionUtils.is(window.getClass(), windowClass)) {
				count++;
			}
		}
		return count;
	}
}
