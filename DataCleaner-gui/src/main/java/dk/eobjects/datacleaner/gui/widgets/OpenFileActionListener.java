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
import java.io.File;

import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import dk.eobjects.datacleaner.gui.DataCleanerGui;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.model.CombinationFilter;
import dk.eobjects.datacleaner.gui.windows.ProfilerWindow;
import dk.eobjects.datacleaner.gui.windows.ValidatorWindow;
import dk.eobjects.datacleaner.util.DomHelper;

public class OpenFileActionListener implements ActionListener {
	private Log _log = LogFactory.getLog(OpenFileActionListener.class);

	public void actionPerformed(ActionEvent event) {
		JFileChooser fileChooser = new JFileChooser();
		CombinationFilter combinationFilter = new CombinationFilter(
				ProfilerWindow.EXTENSION_FILTER,
				ValidatorWindow.EXTENSION_FILTER);
		fileChooser.addChoosableFileFilter(combinationFilter);
		fileChooser.addChoosableFileFilter(ProfilerWindow.EXTENSION_FILTER);
		fileChooser.addChoosableFileFilter(ValidatorWindow.EXTENSION_FILTER);
		fileChooser.setFileFilter(combinationFilter);
		GuiHelper.centerOnScreen(fileChooser);
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {

				DocumentBuilder documentBuilder = DomHelper
						.getDocumentBuilder();
				Document document = documentBuilder.parse(file);
				Node rootNode = document.getDocumentElement();
				if (ProfilerWindow.NODE_NAME.equals(rootNode.getNodeName())) {
					ProfilerWindow window = ProfilerWindow
							.deserialize(rootNode);
					DataCleanerGui.getMainWindow().addWindow(window);
				} else if (ValidatorWindow.NODE_NAME.equals(rootNode
						.getNodeName())) {
					ValidatorWindow window = ValidatorWindow
							.deserialize(rootNode);
					DataCleanerGui.getMainWindow().addWindow(window);
				} else {
					throw new Exception(
							"Could not deserialize node with name '"
									+ rootNode.getNodeName() + "'");
				}
			} catch (Exception e) {
				_log.warn(e);
				GuiHelper.showErrorMessage("Error opening file",
						"An error occurred when opening the file '"
								+ file.getName() + "'", e);
			}
		}
	}
}