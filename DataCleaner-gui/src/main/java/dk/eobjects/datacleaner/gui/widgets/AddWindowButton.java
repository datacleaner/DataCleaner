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
package dk.eobjects.datacleaner.gui.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;

import dk.eobjects.datacleaner.gui.DataCleanerGui;
import dk.eobjects.datacleaner.gui.windows.AbstractWindow;

public class AddWindowButton extends JButton implements ActionListener {

	private static final long serialVersionUID = -657579054404807834L;
	private JDialog _dialog;
	private Class<? extends AbstractWindow> _windowClass;

	public AddWindowButton(final Class<? extends AbstractWindow> windowClass,
			Icon icon, final JDialog dialog, String title) {
		super(icon);
		setToolTipText(title);
		addActionListener(this);
		setName(title);
		_dialog = dialog;
		_windowClass = windowClass;
	}

	public void actionPerformed(ActionEvent event) {
		try {
			AbstractWindow window = _windowClass.newInstance();
			DataCleanerGui.getMainWindow().addWindow(window);
			_dialog.dispose();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}