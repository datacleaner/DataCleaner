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
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.FileSelectionListener;
import org.eobjects.datacleaner.widgets.FilenameTextField;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Superclass for rather simple file-based datastores such as Excel-datastores,
 * Access-datastores, dBase-datastores etc.
 * 
 * @author Kasper Sørensen
 * 
 * @param <D>
 */
public abstract class AbstractFileBasedDatastoreDialog<D extends Datastore> extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	protected static final ImageManager imageManager = ImageManager.getInstance();
	protected final UserPreferences userPreferences = UserPreferences.getInstance();
	protected final MutableDatastoreCatalog _mutableDatastoreCatalog;;
	protected final JXTextField _datastoreNameField;
	protected final FilenameTextField _filenameField;
	protected final D _originalDatastore;

	private final JLabel _statusLabel;
	private final DCPanel _outerPanel = new DCPanel();
	private final JButton _addDatastoreButton;

	public AbstractFileBasedDatastoreDialog(MutableDatastoreCatalog mutableDatastoreCatalog) {
		this(null, mutableDatastoreCatalog);
	}

	public AbstractFileBasedDatastoreDialog(D originalDatastore, MutableDatastoreCatalog mutableDatastoreCatalog) {
		super(imageManager.getImage("images/window/banner-datastores.png"));
		_originalDatastore = originalDatastore;
		_mutableDatastoreCatalog = mutableDatastoreCatalog;
		_datastoreNameField = WidgetFactory.createTextField("Datastore name");
		_statusLabel = DCLabel.bright("Please select file");

		_filenameField = new FilenameTextField(userPreferences.getOpenDatastoreDirectory(), true);
		_filenameField.getTextField().getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent e) {
				updateStatus();
			}
		});

		setFileFilters(_filenameField);

		_filenameField.addFileSelectionListener(new FileSelectionListener() {
			@Override
			public void onSelected(FilenameTextField filenameTextField, File file) {
				File dir = file.getParentFile();
				userPreferences.setOpenDatastoreDirectory(dir);

				if (StringUtils.isNullOrEmpty(_datastoreNameField.getText())) {
					_datastoreNameField.setText(file.getName());
				}

				updateStatus();
			}
		});

		_addDatastoreButton = WidgetFactory.createButton("Save datastore", getDatastoreIconPath());

		if (_originalDatastore != null) {
			_datastoreNameField.setText(_originalDatastore.getName());
			_datastoreNameField.setEnabled(false);
			_filenameField.setFilename(getFilename(_originalDatastore));
		}

		updateStatus();
	}

	protected abstract String getFilename(D datastore);

	protected abstract D createDatastore(String name, String filename);

	protected abstract String getDatastoreIconPath();

	protected abstract void setFileFilters(FilenameTextField filenameField);

	private void updateStatus() {
		final String filename = _filenameField.getFilename();
		if (StringUtils.isNullOrEmpty(filename)) {
			_statusLabel.setText("Please enter or select a filename");
			_statusLabel.setIcon(imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL));
			_addDatastoreButton.setEnabled(false);
			return;
		}

		final String datastoreName = _datastoreNameField.getText();
		if (StringUtils.isNullOrEmpty(datastoreName)) {
			_statusLabel.setText("Please enter a datastore name");
			_statusLabel.setIcon(imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL));
			_addDatastoreButton.setEnabled(false);
			return;
		}

		final File file = new File(filename);
		if (!file.exists()) {
			_statusLabel.setText("The file does not exist!");
			_statusLabel.setIcon(imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL));
			_addDatastoreButton.setEnabled(false);
		}
		if (!file.isFile()) {
			_statusLabel.setText("Not a valid file!");
			_statusLabel.setIcon(imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL));
			_addDatastoreButton.setEnabled(false);
		}

		_statusLabel.setText("Datastore ready");
		_statusLabel.setIcon(imageManager.getImageIcon("images/status/valid.png", IconUtils.ICON_SIZE_SMALL));
		_addDatastoreButton.setEnabled(true);
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
		WidgetUtils.addToGridBag(DCLabel.bright("Datastore name:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_datastoreNameField, formPanel, 1, row);

		row++;
		WidgetUtils.addToGridBag(DCLabel.bright("Filename:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_filenameField, formPanel, 1, row);

		_addDatastoreButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Datastore datastore = createDatastore(_datastoreNameField.getText(), _filenameField.getFilename());

				if (_originalDatastore != null) {
					_mutableDatastoreCatalog.removeDatastore(_originalDatastore);
				}

				_mutableDatastoreCatalog.addDatastore(datastore);
				dispose();
			}
		});

		DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttonPanel.add(_addDatastoreButton);

		DCPanel centerPanel = new DCPanel();
		centerPanel.setLayout(new VerticalLayout(4));
		centerPanel.add(formPanel);
		centerPanel.add(buttonPanel);

		JXStatusBar statusBar = WidgetFactory.createStatusBar(_statusLabel);

		_outerPanel.setLayout(new BorderLayout());
		_outerPanel.add(centerPanel, BorderLayout.CENTER);
		_outerPanel.add(statusBar, BorderLayout.SOUTH);

		return _outerPanel;
	}
}
