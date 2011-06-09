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
package org.eobjects.datacleaner.windows;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;

import org.eobjects.analyzer.reference.TextFileDictionary;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.util.WindowManager;
import org.eobjects.datacleaner.widgets.CharSetEncodingComboBox;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.FileSelectionListener;
import org.eobjects.datacleaner.widgets.FilenameTextField;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

public final class TextFileDictionaryDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	private final UserPreferences _userPreferences = UserPreferences.getInstance();
	private final TextFileDictionary _originalDictionary;
	private final MutableReferenceDataCatalog _catalog;
	private final JXTextField _nameTextField;
	private final FilenameTextField _filenameTextField;
	private final JComboBox _encodingComboBox;
	private volatile boolean _nameAutomaticallySet = true;

	public TextFileDictionaryDialog(MutableReferenceDataCatalog catalog, WindowManager windowManager) {
		this(null, catalog, windowManager);
	}

	public TextFileDictionaryDialog(TextFileDictionary dictionary, MutableReferenceDataCatalog catalog,
			WindowManager windowManager) {
		super(windowManager, ImageManager.getInstance().getImage("images/window/banner-dictionaries.png"));
		_originalDictionary = dictionary;
		_catalog = catalog;

		_nameTextField = WidgetFactory.createTextField("Dictionary name");
		_nameTextField.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent e) {
				_nameAutomaticallySet = false;
			}
		});

		_filenameTextField = new FilenameTextField(_userPreferences.getOpenDatastoreDirectory(), true);
		_filenameTextField.addFileSelectionListener(new FileSelectionListener() {
			@Override
			public void onSelected(FilenameTextField filenameTextField, File file) {
				if (_nameAutomaticallySet || StringUtils.isNullOrEmpty(_nameTextField.getText())) {
					_nameTextField.setText(file.getName());
					_nameAutomaticallySet = true;
				}
				File dir = file.getParentFile();
				_userPreferences.setOpenDatastoreDirectory(dir);
			}
		});

		_encodingComboBox = new CharSetEncodingComboBox();

		if (dictionary != null) {
			_nameTextField.setText(dictionary.getName());
			_filenameTextField.setFilename(dictionary.getFilename());
			_encodingComboBox.setSelectedItem(dictionary.getEncoding());
		}
	}

	@Override
	protected String getBannerTitle() {
		return "Text file dictionary";
	}

	@Override
	protected int getDialogWidth() {
		return 465;
	}

	@Override
	protected JComponent getDialogContent() {
		final DCPanel formPanel = new DCPanel();

		int row = 0;
		WidgetUtils.addToGridBag(DCLabel.bright("Dictionary name:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_nameTextField, formPanel, 1, row);

		row++;
		WidgetUtils.addToGridBag(DCLabel.bright("Filename:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_filenameTextField, formPanel, 1, row);

		row++;
		WidgetUtils.addToGridBag(DCLabel.bright("Character encoding:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_encodingComboBox, formPanel, 1, row);

		row++;
		final JButton saveButton = WidgetFactory.createButton("Save dictionary", "images/model/dictionary.png");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = _nameTextField.getText();
				if (StringUtils.isNullOrEmpty(name)) {
					JOptionPane.showMessageDialog(TextFileDictionaryDialog.this,
							"Please fill out the name of the dictionary");
					return;
				}

				String filename = _filenameTextField.getFilename();
				if (StringUtils.isNullOrEmpty(filename)) {
					JOptionPane.showMessageDialog(TextFileDictionaryDialog.this,
							"Please fill out the filename or select a file using the 'Browse' button");
					return;
				}

				String encoding = (String) _encodingComboBox.getSelectedItem();
				if (StringUtils.isNullOrEmpty(filename)) {
					JOptionPane.showMessageDialog(TextFileDictionaryDialog.this, "Please select a character encoding");
					return;
				}

				TextFileDictionary dict = new TextFileDictionary(name, filename, encoding);

				if (_originalDictionary != null) {
					_catalog.removeDictionary(_originalDictionary);
				}
				_catalog.addDictionary(dict);
				TextFileDictionaryDialog.this.dispose();
			}
		});

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttonPanel.add(saveButton);
		WidgetUtils.addToGridBag(buttonPanel, formPanel, 0, row, 2, 1);

		final DCLabel descriptionLabel = DCLabel
				.brightMultiLine("A text file dictionary is a dictionary based on a text file containing values separated by linebreaks.");
		descriptionLabel.setBorder(new EmptyBorder(10, 10, 10, 20));
		descriptionLabel.setPreferredSize(new Dimension(300, 100));

		final DCPanel mainPanel = new DCPanel();
		mainPanel.setLayout(new VerticalLayout(4));
		mainPanel.add(descriptionLabel);
		mainPanel.add(formPanel);

		return mainPanel;
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	public String getWindowTitle() {
		return "Text file dictionary";
	}

}
