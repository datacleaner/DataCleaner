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

import java.awt.event.ActionListener;
import java.util.List;

import org.eobjects.datacleaner.windows.DCWindow;

/**
 * Interface for component that manages the context of the UI in DataCleaner.
 * 
 * @author Kasper Sørensen
 */
public interface WindowManager {

	public List<DCWindow> getWindows();

	public void onDispose(DCWindow window);

	public void onShow(DCWindow window);

	public int getWindowCount(Class<? extends DCWindow> windowClass);
	
	public void addWindowListener(ActionListener listener);
	
	public void removeWindowListener(ActionListener listener);

	public boolean showExitDialog();

	public void exit();
}
