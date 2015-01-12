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
import java.util.List;

import org.datacleaner.windows.DCWindow;

/**
 * Interface for component that manages the context of the UI in DataCleaner.
 * 
 * @author Kasper Sørensen
 */
public interface WindowContext {

	/**
	 * Adds an action listener for exit actions
	 * 
	 * @param exitActionListener
	 */
	public void addExitActionListener(ExitActionListener exitActionListener);

	/**
	 * Removes an action listener for exit actions
	 * 
	 * @param exitActionListener
	 */
	public void removeExitActionListener(ExitActionListener exitActionListener);

	/**
	 * Gets all active windows in the application
	 * 
	 * @return
	 */
	public List<DCWindow> getWindows();

	/**
	 * Method which should be invoked when a window is closed/disposed.
	 * 
	 * @param window
	 *            the window that was closed.
	 */
	public void onDispose(DCWindow window);

	/**
	 * Method which should be invoked when a window is opened/shown.
	 * 
	 * @param window
	 *            the window that is shown.
	 */
	public void onShow(DCWindow window);

	/**
	 * Gets the count of windows of a particular type.
	 * 
	 * @param windowClass
	 *            the window type
	 * @return an integer representing the count of the specified window type.
	 */
	public int getWindowCount(Class<? extends DCWindow> windowClass);

	/**
	 * Adds a window listener which will be invoked when windows are shown and
	 * disposed.
	 * 
	 * @param listener
	 */
	public void addWindowListener(ActionListener listener);

	/**
	 * Removes a window listener that has previously been added using
	 * {@link #addWindowListener(ActionListener)}
	 * 
	 * @param listener
	 */
	public void removeWindowListener(ActionListener listener);

	/**
	 * Requests that an "exit application" dialog is shown.
	 * 
	 * @return true if the user decides to exit.
	 */
	public boolean showExitDialog();

	/**
	 * Requests the application to exit.
	 */
	public void exit();
}
