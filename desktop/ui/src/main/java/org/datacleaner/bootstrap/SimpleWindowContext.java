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

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.windows.DCWindow;

/**
 * Very simple WindowContext implementation used for initialization purposes,
 * loading indicators etc., before the DataCleaner application is ready to serve
 * "real" windows.
 */
public class SimpleWindowContext implements WindowContext {

    private final List<DCWindow> _windows = new ArrayList<>();

    @Override
    public void addExitActionListener(final ExitActionListener exitActionListener) {
    }

    @Override
    public void removeExitActionListener(final ExitActionListener exitActionListener) {
    }

    @Override
    public final List<DCWindow> getWindows() {
        return Collections.unmodifiableList(_windows);
    }

    @Override
    public void onDispose(final DCWindow window) {
        _windows.remove(window);
    }

    @Override
    public void onShow(final DCWindow window) {
        _windows.add(window);
    }

    @Override
    public final int getWindowCount(final Class<? extends DCWindow> windowClass) {
        int count = 0;
        for (final DCWindow window : _windows) {
            if (ReflectionUtils.is(window.getClass(), windowClass)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void addWindowListener(final ActionListener listener) {
    }

    @Override
    public void removeWindowListener(final ActionListener listener) {
    }

    @Override
    public boolean showExitDialog() {
        return false;
    }

    @Override
    public void exit() {
    }

}
