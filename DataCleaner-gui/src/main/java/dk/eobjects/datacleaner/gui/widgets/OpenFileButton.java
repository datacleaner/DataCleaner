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

import javax.swing.JButton;

import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.dialogs.DataFileChooser;

public class OpenFileButton extends JButton implements ActionListener {

	private static final long serialVersionUID = -4356493954658130331L;
	private DataContextSelection _dataContextSelection;

	@Override
	public void removeNotify() {
		super.removeNotify();
		_dataContextSelection = null;
	}

	public OpenFileButton(DataContextSelection dataContextSelection) {
		super("Open file", GuiHelper.getImageIcon("images/toolbar_file.png"));
		_dataContextSelection = dataContextSelection;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		DataFileChooser fc = new DataFileChooser(_dataContextSelection);
		GuiHelper.centerOnScreen(fc);
		fc.showOpenDialog(null);
	}
}