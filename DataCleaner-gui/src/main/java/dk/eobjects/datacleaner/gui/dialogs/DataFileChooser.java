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
package dk.eobjects.datacleaner.gui.dialogs;

import java.io.File;

import javax.swing.JFileChooser;

import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.model.CombinationFilter;
import dk.eobjects.datacleaner.gui.model.ExtensionFilter;

public class DataFileChooser extends JFileChooser {

	private static final long serialVersionUID = 3798179789539560724L;

	private DataContextSelection _dataContextSelection;

	public DataFileChooser(DataContextSelection dataContextSelection) {
		super();
		_dataContextSelection = dataContextSelection;

		// Each supported file type is represented by a file filter
		ExtensionFilter csvFilter = new ExtensionFilter(
				"Comma-separated file (.csv)",
				DataContextSelection.EXTENSION_COMMA_SEPARATED);
		ExtensionFilter tsvFilter = new ExtensionFilter(
				"Tab-separated file (.tsv)",
				DataContextSelection.EXTENSION_TAB_SEPARATED);
		ExtensionFilter excelFilter = new ExtensionFilter("Excel File (.xls)",
				DataContextSelection.EXTENSION_XLS);
		ExtensionFilter odbFilter = new ExtensionFilter(
				"OpenOffice.org database (.odb)",
				DataContextSelection.EXTENSION_ODB);
		ExtensionFilter xmlFilter = new ExtensionFilter(
				"Extensible Markup Language (.xml)",
				DataContextSelection.EXTENSION_XML);
		ExtensionFilter txtFilter = new ExtensionFilter("Text File (.txt)",
				DataContextSelection.EXTENSION_TEXT);
		ExtensionFilter datFilter = new ExtensionFilter("DAT File (.dat)",
				DataContextSelection.EXTENSION_DAT);

		// A filter that combines all the other filters
		CombinationFilter combinationFilter = new CombinationFilter(csvFilter,
				tsvFilter, excelFilter, odbFilter, xmlFilter, txtFilter,
				datFilter);

		addChoosableFileFilter(combinationFilter);
		addChoosableFileFilter(csvFilter);
		addChoosableFileFilter(tsvFilter);
		addChoosableFileFilter(excelFilter);
		addChoosableFileFilter(odbFilter);
		addChoosableFileFilter(xmlFilter);
		addChoosableFileFilter(datFilter);
		addChoosableFileFilter(txtFilter);
		setFileFilter(combinationFilter);
		setAcceptAllFileFilterUsed(false);
		setMultiSelectionEnabled(false);
		GuiHelper.centerOnScreen(this);
	}

	@Override
	public void approveSelection() {
		super.approveSelection();
		try {
			File file = getSelectedFile();
			String fileExtension = DataContextSelection.getExtention(file);
			if (fileExtension
					.equalsIgnoreCase(DataContextSelection.EXTENSION_COMMA_SEPARATED)
					|| fileExtension.equals(DataContextSelection.EXTENSION_DAT)
					|| fileExtension
							.equals(DataContextSelection.EXTENSION_TEXT)
					|| fileExtension
							.equalsIgnoreCase(DataContextSelection.EXTENSION_TAB_SEPARATED)) {
				CsvConfigurationDialog dialog = new CsvConfigurationDialog(
						_dataContextSelection, file);
				dialog.setVisible(true);
			} else {
				_dataContextSelection.selectFile(file);
			}
		} catch (Exception e) {
			GuiHelper.showErrorMessage("Could not read data from file!",
					"An error occurred while reading from the file.", e);
		} finally {
			_dataContextSelection = null;
		}
	}

	@Override
	public void cancelSelection() {
		super.cancelSelection();
		_dataContextSelection = null;
	}
}