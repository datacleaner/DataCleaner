package org.eobjects.datacleaner.actions;

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

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.datacleaner.output.CsvDataSetWriter;
import org.eobjects.datacleaner.output.DatastoreDataSetWriter;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.FileFilters;

public class SaveDataSetActionListener implements ActionListener {

	private final List<InputColumn<?>> _inputColumns;
	private final InputRow[] _rows;

	public SaveDataSetActionListener(List<InputColumn<?>> inputColumns, InputRow[] rows) {
		_inputColumns = inputColumns;
		_rows = rows;
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		JMenuItem saveAsDatastoreItem = new JMenuItem("As datastore");
		saveAsDatastoreItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String datastoreName = JOptionPane.showInputDialog("Datastore name");
				DatastoreDataSetWriter writer = new DatastoreDataSetWriter(datastoreName);
				writer.write(_inputColumns, _rows);
			}
		});

		JMenuItem saveAsCsvItem = new JMenuItem("As CSV file");
		saveAsCsvItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(UserPreferences.getInstance().getSaveFileDirectory());
				fileChooser.addChoosableFileFilter(FileFilters.CSV);
				if (fileChooser.showSaveDialog((Component) event.getSource()) == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					if (selectedFile.getName().indexOf('.') == -1) {
						selectedFile = new File(selectedFile.getPath() + ".csv");
					}

					CsvDataSetWriter writer = new CsvDataSetWriter(selectedFile);
					writer.write(_inputColumns, _rows);

					File dir = selectedFile.getParentFile();
					UserPreferences.getInstance().setSaveFileDirectory(dir);
				}
			}
		});

		JPopupMenu popup = new JPopupMenu();
		popup.add(saveAsDatastoreItem);
		popup.add(saveAsCsvItem);
		JComponent source = (JComponent) event.getSource();
		popup.show(source, 0, source.getHeight());
	}
}
