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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eobjects.datacleaner.windows.AbstractWindow;
import org.eobjects.datacleaner.windows.MainWindow;

public final class WindowManager {

	private static WindowManager instance = new WindowManager();

	private List<AbstractWindow> windows = new ArrayList<AbstractWindow>();

	public static WindowManager getInstance() {
		return instance;
	}

	private WindowManager() {
	}

	public List<AbstractWindow> getWindows() {
		return Collections.unmodifiableList(windows);
	}

	public MainWindow getMainWindow() {
		for (AbstractWindow window : windows) {
			if (window instanceof MainWindow) {
				return (MainWindow) window;
			}
		}
		throw new IllegalStateException("The main window appears to be missing!");
	}

	public void onDispose(AbstractWindow window) {
		windows.remove(window);
	}

	public void onShow(AbstractWindow window) {
		windows.add(window);
	}
}
