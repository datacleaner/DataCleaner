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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.ExcelDatastore;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

public class OpenExcelSpreadsheetDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	private final MutableDatastoreCatalog _mutableDatastoreCatalog;;
	private final JXTextField _datastoreNameField;
	private final JXTextField _filenameField;
	private final JButton _browseButton;
	private final JLabel _statusLabel;
	private final DCPanel _outerPanel = new DCPanel();
	private final JButton _addDatastoreButton;

	public OpenExcelSpreadsheetDialog(MutableDatastoreCatalog mutableDatastoreCatalog) {
		super();
		_mutableDatastoreCatalog = mutableDatastoreCatalog;
		_datastoreNameField = WidgetFactory.createTextField("Datastore name");

		_filenameField = WidgetFactory.createTextField("Filename");
		_filenameField.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent e) {
				autoDetectQuoteAndSeparator();
			}
		});

		_statusLabel = new JLabel("Please select file");

		_browseButton = new JButton("Browse", ImageManager.getInstance().getImageIcon("images/actions/browse.png",
				IconUtils.ICON_SIZE_SMALL));
		_browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(UserPreferences.getInstance().getDatastoreDirectory());
				FileFilter combinedFilter = FileFilters.combined("Any Excel Spreadsheet (.xls, .xlsx)", FileFilters.XLS,
						FileFilters.XLSX);
				fileChooser.addChoosableFileFilter(combinedFilter);
				fileChooser.addChoosableFileFilter(FileFilters.XLS);
				fileChooser.addChoosableFileFilter(FileFilters.XLSX);
				fileChooser.addChoosableFileFilter(FileFilters.ALL);
				fileChooser.setFileFilter(combinedFilter);
				WidgetUtils.centerOnScreen(fileChooser);
				int result = fileChooser.showOpenDialog(OpenExcelSpreadsheetDialog.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					File dir = selectedFile.getParentFile();
					UserPreferences.getInstance().setDatastoreDirectory(dir);
					_filenameField.setText(selectedFile.getAbsolutePath());

					if (StringUtils.isNullOrEmpty(_datastoreNameField.getText())) {
						_datastoreNameField.setText(selectedFile.getName());
					}

					autoDetectQuoteAndSeparator();
				}
			}
		});

		_addDatastoreButton = new JButton("Create datastore");
		_addDatastoreButton.setEnabled(false);
	}

	@Override
	protected String getBannerTitle() {
		return "MS Excel";
	}

	private void autoDetectQuoteAndSeparator() {
		ImageManager imageManager = ImageManager.getInstance();

		File file = new File(_filenameField.getText());
		if (file.exists()) {
			if (file.isFile()) {
				_statusLabel.setText("Excel spreadsheet ready");
				_statusLabel.setIcon(imageManager.getImageIcon("images/status/valid.png", IconUtils.ICON_SIZE_SMALL));
				_addDatastoreButton.setEnabled(true);
			} else {
				_statusLabel.setText("Not a valid file!");
				_statusLabel.setIcon(imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL));
				_addDatastoreButton.setEnabled(false);
			}
		} else {
			_statusLabel.setText("The file does not exist!");
			_statusLabel.setIcon(imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL));
			_addDatastoreButton.setEnabled(false);
		}

	}

	@Override
	protected int getDialogWidth() {
		return 400;
	}

	@Override
	protected JComponent getDialogContent() {
		DCPanel formPanel = new DCPanel();

		// temporary variable to make it easier to refactor the layout
		int row = 0;
		WidgetUtils.addToGridBag(new JLabel("Datastore name:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_datastoreNameField, formPanel, 1, row);

		row++;
		WidgetUtils.addToGridBag(new JLabel("Filename:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_filenameField, formPanel, 1, row);
		WidgetUtils.addToGridBag(_browseButton, formPanel, 2, row);

		_addDatastoreButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Datastore datastore = new ExcelDatastore(_datastoreNameField.getText(), _filenameField.getText());
				_mutableDatastoreCatalog.addDatastore(datastore);
				dispose();
			}
		});

		DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(_addDatastoreButton);

		DCPanel centerPanel = new DCPanel();
		centerPanel.setLayout(new VerticalLayout(4));
		centerPanel.add(formPanel);
		centerPanel.add(buttonPanel);

		centerPanel.setPreferredSize(getDialogWidth(), 190);

		JXStatusBar statusBar = new JXStatusBar();
		JXStatusBar.Constraint c1 = new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FILL);
		statusBar.add(_statusLabel, c1);

		_outerPanel.setLayout(new BorderLayout());
		_outerPanel.add(centerPanel, BorderLayout.CENTER);
		_outerPanel.add(statusBar, BorderLayout.SOUTH);

		return _outerPanel;
	}

	@Override
	protected String getWindowTitle() {
		return "Open Excel spreadsheet";
	}

}
