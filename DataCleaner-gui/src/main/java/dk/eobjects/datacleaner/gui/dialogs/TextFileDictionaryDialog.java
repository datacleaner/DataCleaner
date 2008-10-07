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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import dk.eobjects.datacleaner.catalog.TextFileDictionary;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;

public class TextFileDictionaryDialog extends BanneredDialog {

	private static final long serialVersionUID = -4903413130380918278L;
	private static final Dimension BUTTON_DIMENSION = new Dimension(84, 18);
	private TextFileDictionary _dictionary;
	private JTextField _nameField;
	private JTextField _filenameField;

	public TextFileDictionaryDialog(TextFileDictionary dictionary) {
		super(400, 320);

		JTextArea aboutTextFileDictionaries = GuiHelper.createLabelTextArea()
				.toComponent();
		aboutTextFileDictionaries
				.setText("Text-file dictionaries are dictionaries based on flat files. Register a flat file here and all the words (seperated by whitespace or line-breaks) within the file will be used to populate the dictionary.");
		add(aboutTextFileDictionaries, BorderLayout.SOUTH);

		_dictionary = dictionary;
		updateDialog();
	}

	private void updateDialog() {
		if (_dictionary != null) {
			_nameField.setText(_dictionary.getName());
			_filenameField.setText(_dictionary.getDictionaryFile()
					.getAbsolutePath());
		}
	}

	@Override
	protected Component getContent() {
		final JPanel panel = GuiHelper.createPanel().toComponent();

		JLabel header = new JLabel("Text-file dictionary");
		header.setFont(GuiHelper.FONT_HEADER);
		GuiHelper.addToGridBag(header, panel, 0, 0, 3, 1);

		GuiHelper.addToGridBag(new JLabel("Dictionary name:"), panel, 0, 1);
		_nameField = new JTextField();
		GuiHelper.addToGridBag(_nameField, panel, 1, 1, 2, 1);
		_nameField.setPreferredSize(new Dimension(200, 20));
		GuiHelper.addToGridBag(new JLabel("Filename:"), panel, 0, 2);
		_filenameField = new JTextField();
		GuiHelper.addToGridBag(_filenameField, panel, 1, 2, 1, 1);
		JButton browseButton = new JButton("Browse");
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(new File(
						GuiSettings.DICTIONARIES_SAMPLES));
				GuiHelper.centerOnScreen(fileChooser);
				if (fileChooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					TextFileDictionaryDialog.this._filenameField
							.setText(selectedFile.getAbsolutePath());
				}
			}
		});
		browseButton.setPreferredSize(BUTTON_DIMENSION);
		GuiHelper.addToGridBag(browseButton, panel, 2, 2);

		JButton saveButton = new JButton("Save dictionary", GuiHelper
				.getImageIcon("images/dictionaries.png"));
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = _nameField.getText();
				String filename = _filenameField.getText();
				GuiSettings settings = GuiSettings.getSettings();
				File file = new File(filename);

				if (file.exists() && file.isFile()) {
					name = name.trim();
					if (name.length() > 2) {
						if (_dictionary == null) {
							_dictionary = new TextFileDictionary(name, file);
							settings.getDictionaries().add(_dictionary);
						} else {
							// We can assume that the dictionary is already
							// existing in
							// the settings
							_dictionary.setName(name);
							_dictionary.setDictionaryFile(file);
						}
						GuiSettings.saveSettings(settings);
						dispose();
					} else {
						GuiHelper
								.showErrorMessage(
										"Dictionary name required",
										"Please provide a name of minimum 3 characters for your dictionary.",
										new IllegalArgumentException(name));
					}
				} else {
					GuiHelper
							.showErrorMessage(
									"File not found",
									"The file ["
											+ filename
											+ "] does not exist or is not a valid file. Please select an existing file for your new text-file dictionary.",
									new IllegalArgumentException(filename));
				}
			}
		});
		GuiHelper.addToGridBag(saveButton, panel, 1, 3, 2, 1);

		GridBagLayout layout = (GridBagLayout) panel.getLayout();
		layout.columnWidths = new int[] { 70, 220, 70 };

		return panel;
	}

	@Override
	protected String getDialogTitle() {
		return "Text-file dictionary";
	}
}