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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.windows.AbstractDialog;
import org.eobjects.datacleaner.windows.AbstractWindow;
import org.eobjects.datacleaner.windows.DCWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the contexts of the GUI windows in DataCleaner.
 * 
 * @see DCWindow
 * @see AbstractWindow
 * @see AbstractDialog
 * 
 * @author Kasper Sørensen
 */
public final class DCWindowContext implements WindowContext {

	private static final Logger logger = LoggerFactory.getLogger(DCWindowContext.class);

	private static final List<WeakReference<DCWindowContext>> _allWindowContexts = new ArrayList<WeakReference<DCWindowContext>>();

	private final List<DCWindow> _windows = new ArrayList<DCWindow>();
	private final List<ActionListener> _windowListeners = new ArrayList<ActionListener>();
	private final List<ExitActionListener> _exitActionListeners = new ArrayList<ExitActionListener>();
	private final AnalyzerBeansConfiguration _configuration;
	private final UserPreferences _userPreferences;
	private boolean _exiting;

	private UsageLogger _usageLogger;

	/**
	 * Helper method to get any window of the application. This can be
	 * convenient if eg. displaying {@link JOptionPane}s from arbitrary places
	 * in the code.
	 * 
	 * @return
	 */
	public static DCWindow getAnyWindow() {
		for (WeakReference<DCWindowContext> ref : _allWindowContexts) {
			DCWindowContext windowContext = ref.get();
			if (windowContext != null) {
				List<DCWindow> windows = windowContext.getWindows();
				for (DCWindow window : windows) {
					if (window != null) {
						return window;
					}
				}
			}
		}
		return null;
	}

	public DCWindowContext(AnalyzerBeansConfiguration configuration, UserPreferences userPreferences, UsageLogger usageLogger) {
		_configuration = configuration;
		_userPreferences = userPreferences;
		_usageLogger = usageLogger;
		_allWindowContexts.add(new WeakReference<DCWindowContext>(this));
		_exiting = false;
	}

	@Override
	public List<DCWindow> getWindows() {
		return Collections.unmodifiableList(_windows);
	}

	@Override
	public void addWindowListener(ActionListener listener) {
		_windowListeners.add(listener);
	}

	@Override
	public void removeWindowListener(ActionListener listener) {
		_windowListeners.remove(listener);
	}

	@Override
	public void onDispose(DCWindow window) {
		_windows.remove(window);
		notifyListeners();

		if (!_exiting) {
			if (_windows.isEmpty()) {
				logger.info("All DataCleaner windows closed");
				exit();
			}
		}
	}

	private void notifyListeners() {
		ActionEvent event = new ActionEvent(this, _windows.size(), null);
		for (ActionListener listener : _windowListeners) {
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
		// ensure that exit actions only occur once.
		if (_exiting) {
			return;
		}
		_exiting = true;
		
		_userPreferences.save();
		_usageLogger.logApplicationShutdown();
		if (_configuration != null) {
			_configuration.getTaskRunner().shutdown();
		}
		for (ExitActionListener actionListener : _exitActionListeners) {
			actionListener.exit(0);
		}
		for (DCWindow window : new ArrayList<DCWindow>(_windows)) {
			window.close();
		}
	}

	@Override
	public void addExitActionListener(ExitActionListener exitActionListener) {
		_exitActionListeners.add(exitActionListener);
	}

	@Override
	public void removeExitActionListener(ExitActionListener exitActionListener) {
		_exitActionListeners.remove(exitActionListener);
	}
}
