/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.windows;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.gui.GuiHelper;

public abstract class AbstractWindow implements WindowListener {

	protected final Log _log = LogFactory.getLog(getClass());
	protected JPanel _panel = GuiHelper.createPanel().toComponent();
	private List<JFrame> _windowList;
	private JMenu _windowsMenu;
	private JFrame _frame;
	private JMenuItem _menuItem;

	public AbstractWindow() {
		_panel.setPreferredSize(new Dimension(780, 500));
	}

	public abstract ImageIcon getFrameIcon();

	public abstract String getTitle();

	/**
	 * Overwrite this method to implement special clean-up on frame disposal
	 */
	public void disposeInternal() {
	}

	public JFrame toFrame(List<JFrame> windowList, JMenu windowMenu) {
		_windowList = windowList;
		_windowsMenu = windowMenu;

		String title = getTitle();
		ImageIcon icon = getFrameIcon();

		_frame = new JFrame(title);
		_frame.setIconImage(icon.getImage());
		_frame.addWindowListener(this);
		_frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		if (_windowList != null) {
			windowList.add(_frame);
		}
		if (_windowsMenu != null) {
			_menuItem = new JMenuItem(title, icon);
			_menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					_frame.toFront();
				}
			});
			_windowsMenu.add(_menuItem);
		}

		_frame.getContentPane().add(_panel);
		_frame.setPreferredSize(_panel.getPreferredSize());
		_frame.pack();
		GuiHelper.centerOnScreen(_frame);
		return _frame;
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
		_log.debug("windowClosed()");

		if (_windowList != null) {
			_windowList.remove(_frame);
			_windowList = null;
		}
		if (_windowsMenu != null) {
			_windowsMenu.remove(_menuItem);
			_windowsMenu = null;
		}

		_panel = null;
		_menuItem = null;
		_frame = null;

		// Perform garbage collection as a lot of resources will have been set
		// free when windows are closed.
		_log.debug("System.gc()");
		System.gc();
	}

	public void windowClosing(WindowEvent e) {
		_frame.setVisible(false);
		_frame.dispose();
		disposeInternal();
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
}