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
package org.datacleaner.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;
import org.datacleaner.output.OutputRow;
import org.datacleaner.output.OutputWriter;
import org.datacleaner.output.csv.CsvOutputWriterFactory;
import org.datacleaner.output.datastore.DatastoreCreationDelegate;
import org.datacleaner.output.datastore.DatastoreCreationDelegateImpl;
import org.datacleaner.output.datastore.DatastoreOutputWriterFactory;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.widgets.DCFileChooser;

public class SaveDataSetActionListener implements ActionListener {

	private final List<InputColumn<?>> _inputColumns;
	private final InputRow[] _rows;
	private final UserPreferences _userPreferences;
	private final DatastoreCatalog _datastoreCatalog;

	public SaveDataSetActionListener(List<InputColumn<?>> inputColumns, InputRow[] rows, UserPreferences userPreferences,
			DatastoreCatalog datastoreCatalog) {
		_inputColumns = inputColumns;
		_rows = rows;
		_userPreferences = userPreferences;
		_datastoreCatalog = datastoreCatalog;
	}

	private void performWrite(OutputWriter writer) {
		for (InputRow row : _rows) {
			OutputRow outputRow = writer.createRow();
			outputRow.setValues(row);
			outputRow.write();
		}
		writer.close();
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		final JMenuItem saveAsDatastoreItem = new JMenuItem("As datastore");
		saveAsDatastoreItem.setIcon(IconUtils.getDatastoreIcon(null));
		saveAsDatastoreItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String datastoreName = JOptionPane.showInputDialog("Datastore name");
				final DatastoreCreationDelegate creationDelegate = new DatastoreCreationDelegateImpl(
						(MutableDatastoreCatalog) _datastoreCatalog);

				final OutputWriter writer = DatastoreOutputWriterFactory.getWriter(
						_userPreferences.getSaveDatastoreDirectory(), creationDelegate, datastoreName, "DATASET",
						_inputColumns.toArray(new InputColumn[0]));
				performWrite(writer);
			}
		});

		final JMenuItem saveAsCsvItem = new JMenuItem("As CSV file");
		saveAsCsvItem.setIcon(ImageManager.get().getImageIcon(IconUtils.CSV_IMAGEPATH));
		saveAsCsvItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DCFileChooser fileChooser = new DCFileChooser(_userPreferences.getAnalysisJobDirectory());
				fileChooser.addChoosableFileFilter(FileFilters.CSV);
				if (fileChooser.showSaveDialog((Component) event.getSource()) == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					if (selectedFile.getName().indexOf('.') == -1) {
						selectedFile = new File(selectedFile.getPath() + ".csv");
					}

					OutputWriter writer = CsvOutputWriterFactory.getWriter(selectedFile.getAbsolutePath(), _inputColumns);
					performWrite(writer);

					File dir = selectedFile.getParentFile();
					_userPreferences.setAnalysisJobDirectory(dir);
				}
			}
		});

		// TODO: Save as excel

		JPopupMenu popup = new JPopupMenu();
		popup.add(saveAsDatastoreItem);
		popup.add(saveAsCsvItem);
		JComponent source = (JComponent) event.getSource();
		popup.show(source, 0, source.getHeight());
	}
}
