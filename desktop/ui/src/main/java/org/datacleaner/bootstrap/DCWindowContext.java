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
package org.datacleaner.bootstrap;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.user.UsageLogger;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.windows.AbstractDialog;
import org.datacleaner.windows.AbstractWindow;
import org.datacleaner.windows.DCWindow;
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
public final class DCWindowContext extends SimpleWindowContext implements WindowContext {

	private static final Logger logger = LoggerFactory.getLogger(DCWindowContext.class);

	private static final List<WeakReference<DCWindowContext>> _allWindowContexts = new ArrayList<WeakReference<DCWindowContext>>();

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
	public void addWindowListener(ActionListener listener) {
		_windowListeners.add(listener);
	}

	@Override
	public void removeWindowListener(ActionListener listener) {
		_windowListeners.remove(listener);
	}

	@Override
	public void onDispose(DCWindow window) {
		super.onDispose(window);
		notifyListeners();

		if (!_exiting) {
			if (getWindows().isEmpty()) {
				logger.info("All DataCleaner windows closed");
				exit();
			}
		}
	}

	private void notifyListeners() {
		ActionEvent event = new ActionEvent(this, getWindows().size(), null);
		for (ActionListener listener : _windowListeners) {
			listener.actionPerformed(event);
		}
	}

	@Override
	public void onShow(DCWindow window) {
	    super.onShow(window);

		notifyListeners();
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
		
		if (_userPreferences != null) {
		    _userPreferences.save();
		}
		if (_usageLogger != null) {
		    _usageLogger.logApplicationShutdown();
		}
		if (_configuration != null) {
			_configuration.getTaskRunner().shutdown();
		}
		for (ExitActionListener actionListener : _exitActionListeners) {
			actionListener.exit(0);
		}
		
		final List<DCWindow> windowsCopy = new ArrayList<DCWindow>(getWindows());
        for (DCWindow window : windowsCopy) {
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
