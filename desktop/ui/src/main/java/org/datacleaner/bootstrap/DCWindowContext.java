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

import org.datacleaner.configuration.DataCleanerConfiguration;
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
 */
public final class DCWindowContext extends SimpleWindowContext implements WindowContext {

    private static final Logger logger = LoggerFactory.getLogger(DCWindowContext.class);

    private static final List<WeakReference<DCWindowContext>> _allWindowContexts = new ArrayList<>();

    private final List<ActionListener> _windowListeners = new ArrayList<>();
    private final List<ExitActionListener> _exitActionListeners = new ArrayList<>();
    private final DataCleanerConfiguration _configuration;
    private final UserPreferences _userPreferences;
    private boolean _exiting;

    public DCWindowContext(final DataCleanerConfiguration configuration, final UserPreferences userPreferences) {
        _configuration = configuration;
        _userPreferences = userPreferences;
        _allWindowContexts.add(new WeakReference<>(this));
        _exiting = false;
    }

    /**
     * Helper method to get any window of the application. This can be
     * convenient if eg. displaying {@link JOptionPane}s from arbitrary places
     * in the code.
     *
     * @return
     */
    public static DCWindow getAnyWindow() {
        for (final WeakReference<DCWindowContext> ref : _allWindowContexts) {
            final DCWindowContext windowContext = ref.get();
            if (windowContext != null) {
                final List<DCWindow> windows = windowContext.getWindows();
                for (final DCWindow window : windows) {
                    if (window != null) {
                        return window;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void addWindowListener(final ActionListener listener) {
        _windowListeners.add(listener);
    }

    @Override
    public void removeWindowListener(final ActionListener listener) {
        _windowListeners.remove(listener);
    }

    @Override
    public void onDispose(final DCWindow window) {
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
        final ActionEvent event = new ActionEvent(this, getWindows().size(), null);
        for (final ActionListener listener : _windowListeners) {
            listener.actionPerformed(event);
        }
    }

    @Override
    public void onShow(final DCWindow window) {
        super.onShow(window);

        notifyListeners();
    }

    @Override
    public boolean showExitDialog() {
        final int confirmation = JOptionPane
                .showConfirmDialog(null, "Are you sure you want to exit DataCleaner?", "Exit",
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
        if (_configuration != null) {
            _configuration.getEnvironment().getTaskRunner().shutdown();
        }
        for (final ExitActionListener actionListener : _exitActionListeners) {
            actionListener.exit(0);
        }

        final List<DCWindow> windowsCopy = new ArrayList<>(getWindows());
        for (final DCWindow window : windowsCopy) {
            window.close();
        }
    }

    @Override
    public void addExitActionListener(final ExitActionListener exitActionListener) {
        _exitActionListeners.add(exitActionListener);
    }

    @Override
    public void removeExitActionListener(final ExitActionListener exitActionListener) {
        _exitActionListeners.remove(exitActionListener);
    }
}
