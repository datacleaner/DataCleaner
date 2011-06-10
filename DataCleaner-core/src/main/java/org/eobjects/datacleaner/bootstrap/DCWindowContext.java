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
package org.eobjects.datacleaner.bootstrap;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.windows.AbstractDialog;
import org.eobjects.datacleaner.windows.AbstractWindow;
import org.eobjects.datacleaner.windows.DCWindow;

/**
 * Represents the contexts of the GUI windows in DataCleaner.
 * 
 * @see DCWindow
 * @see AbstractWindow
 * @see AbstractDialog
 * 
 * @author Kasper Sørensen
 */
public final class DCWindowContext implements WindowManager {

	private final List<DCWindow> _windows = new ArrayList<DCWindow>();
	private final List<ActionListener> _listeners = new ArrayList<ActionListener>();
	private final ExitActionListener _exitActionListener;

	public DCWindowContext() {
		this(new DCExitActionListener());
	}

	public DCWindowContext(ExitActionListener exitActionListener) {
		_exitActionListener = exitActionListener;
	}

	@Override
	public List<DCWindow> getWindows() {
		return Collections.unmodifiableList(_windows);
	}

	@Override
	public void addWindowListener(ActionListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeWindowListener(ActionListener listener) {
		_listeners.remove(listener);
	}

	@Override
	public void onDispose(DCWindow window) {
		_windows.remove(window);
		notifyListeners();

		if (_windows.isEmpty()) {
			exit();
		}
	}

	private void notifyListeners() {
		ActionEvent event = new ActionEvent(this, _windows.size(), null);
		for (ActionListener listener : _listeners) {
			listener.actionPerformed(event);
		}
	}

	@Override
	public void onShow(DCWindow window) {
		_windows.add(window);

		notifyListeners();
	}

	@Override
	public int getWindowCount(Class<? extends DCWindow> windowClass) {
		int count = 0;
		for (DCWindow window : _windows) {
			if (ReflectionUtils.is(window.getClass(), windowClass)) {
				count++;
			}
		}
		return count;
	}

	@Override
	public boolean showExitDialog() {
		int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit DataCleaner?", "Exit",
				JOptionPane.OK_CANCEL_OPTION);

		if (confirmation == JOptionPane.OK_OPTION) {
			return true;
		}
		return false;
	}

	@Override
	public void exit() {
		UserPreferences.getInstance().save();
		UsageLogger.getInstance().logApplicationShutdown();
		_exitActionListener.exit(0);
	}
}
