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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;

import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.FileDatastore;
import org.eobjects.analyzer.util.ImmutableEntry;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.bootstrap.WindowContext;
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
import org.eobjects.datacleaner.widgets.LoadingIcon;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.FileHelper;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for rather simple file-based datastores such as Excel-datastores,
 * Access-datastores, dBase-datastores etc.
 * 
 * @author Kasper Sørensen
 * 
 * @param <D>
 */
public abstract class AbstractFileBasedDatastoreDialog<D extends FileDatastore> extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Amount of bytes to read for autodetection of encoding, separator and
	 * quotes
	 */
	private static final int SAMPLE_BUFFER_SIZE = 128 * 1024;

	/**
	 * Max amount of columns to display in the preview table
	 */
	private static final int PREVIEW_COLUMNS = 10;

	protected static final ImageManager imageManager = ImageManager.getInstance();
	protected final MutableDatastoreCatalog _mutableDatastoreCatalog;
	protected final D _originalDatastore;
	protected final JButton _addDatastoreButton;
	private final JLabel _statusLabel;
	private final JXTextField _datastoreNameField;
	private final FilenameTextField _filenameField;
	private final DCPanel _outerPanel = new DCPanel();
	private final DCPanel _previewTablePanel;
	private final DCTable _previewTable;
	private final LoadingIcon _loadingIcon;
	private final UserPreferences _userPreferences;

	protected AbstractFileBasedDatastoreDialog(D originalDatastore, MutableDatastoreCatalog mutableDatastoreCatalog,
			WindowContext windowContext, UserPreferences userPreferences) {
		super(windowContext, imageManager.getImage("images/window/banner-datastores.png"));
		_originalDatastore = originalDatastore;
		_mutableDatastoreCatalog = mutableDatastoreCatalog;
		_userPreferences = userPreferences;
		_datastoreNameField = WidgetFactory.createTextField("Datastore name");
		_statusLabel = DCLabel.bright("Please select file");

		_filenameField = new FilenameTextField(_userPreferences.getOpenDatastoreDirectory(), true);

		_addDatastoreButton = WidgetFactory.createButton("Save datastore", getDatastoreIconPath());
		_addDatastoreButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Datastore datastore = createDatastore(getDatastoreName(), getFilename());

				if (_originalDatastore != null) {
					_mutableDatastoreCatalog.removeDatastore(_originalDatastore);
				}

				_mutableDatastoreCatalog.addDatastore(datastore);
				dispose();
			}
		});

		if (_originalDatastore != null) {
			_datastoreNameField.setText(_originalDatastore.getName());
			_datastoreNameField.setEnabled(false);
			_filenameField.setFilename(_originalDatastore.getFilename());
		}

		// add listeners after setting initial values.
		_datastoreNameField.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent event) {
				validateAndUpdate();
			}
		});
		setFileFilters(_filenameField);
		_filenameField.getTextField().getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent e) {
				validateAndUpdate();
			}
		});
		_filenameField.addFileSelectionListener(new FileSelectionListener() {
			@Override
			public void onSelected(FilenameTextField filenameTextField, File file) {
				final File dir;
				if (file.isDirectory()) {
					dir = file;
				} else {
					dir = file.getParentFile();
				}
				_userPreferences.setOpenDatastoreDirectory(dir);

				if (StringUtils.isNullOrEmpty(_datastoreNameField.getText())) {
					_datastoreNameField.setText(file.getName());
				}

				validateAndUpdate();

				onFileSelected(file);
			}
		});

		if (isDirectoryBased()) {
			_filenameField.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}

		if (isPreviewTableEnabled()) {
			_previewTable = new DCTable(new DefaultTableModel(7, 10));
			_previewTablePanel = _previewTable.toPanel();
			_previewTablePanel.setBorder(new EmptyBorder(0, 10, 0, 10));
			_loadingIcon = new LoadingIcon();
			_loadingIcon.setVisible(false);
			_loadingIcon.setPreferredSize(_previewTablePanel.getPreferredSize());
		} else {
			_previewTable = null;
			_previewTablePanel = null;
			_loadingIcon = null;
		}
	}

	/**
	 * Can be overridden by subclasses in order to react to file selection
	 * events.
	 * 
	 * @param file
	 */
	protected void onFileSelected(File file) {
	}

	protected abstract D createDatastore(String name, String filename);

	protected abstract String getDatastoreIconPath();

	protected abstract void setFileFilters(FilenameTextField filenameField);

	protected final void validateAndUpdate() {
		boolean valid = validateForm();
		_addDatastoreButton.setEnabled(valid);
		if (valid) {
			updatePreviewTable();
		}
	}

	protected void setStatusValid() {
		_statusLabel.setText("Datastore ready");
		_statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_VALID, IconUtils.ICON_SIZE_SMALL));
	}

	protected void setStatusWarning(String text) {
		_statusLabel.setText(text);
		_statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_WARNING, IconUtils.ICON_SIZE_SMALL));
	}

	protected void setStatusError(String text) {
		_statusLabel.setText(text);
		_statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_ERROR, IconUtils.ICON_SIZE_SMALL));
	}

	protected boolean validateForm() {
		final String filename = _filenameField.getFilename();
		if (StringUtils.isNullOrEmpty(filename)) {
			setStatusError("Please enter or select a filename");
			return false;
		}

		final File file = new File(filename);
		if (!file.exists()) {
			setStatusError("The file does not exist!");
			return false;
		}

		if (isDirectoryBased()) {
			if (!file.isDirectory()) {
				setStatusError("Not a valid directory!");
				return false;
			}
		} else {
			if (!file.isFile()) {
				setStatusError("Not a valid file!");
				return false;
			}
		}

		final String datastoreName = _datastoreNameField.getText();
		if (StringUtils.isNullOrEmpty(datastoreName)) {
			setStatusError("Please enter a datastore name");
			return false;
		}

		setStatusValid();
		return true;
	}

	protected boolean isPreviewTableEnabled() {
		return false;
	}

	@Override
	protected int getDialogWidth() {
		if (isPreviewTableEnabled()) {
			return 550;
		}
		return 400;
	}

	protected List<Entry<String, JComponent>> getFormElements() {
		ArrayList<Entry<String, JComponent>> res = new ArrayList<Entry<String, JComponent>>();
		res.add(new ImmutableEntry<String, JComponent>("Datastore name", _datastoreNameField));
		if (isDirectoryBased()) {
			res.add(new ImmutableEntry<String, JComponent>("Directory", _filenameField));
		} else {
			res.add(new ImmutableEntry<String, JComponent>("Filename", _filenameField));
		}
		return res;
	}

	public String getDatastoreName() {
		return _datastoreNameField.getText();
	}

	public String getFilename() {
		return _filenameField.getFilename();
	}

	@Override
	protected final JComponent getDialogContent() {
		DCPanel formPanel = new DCPanel();

		List<Entry<String, JComponent>> formElements = getFormElements();
		// temporary variable to make it easier to refactor the layout
		int row = 0;
		for (Entry<String, JComponent> entry : formElements) {
			String key = entry.getKey();
			if (StringUtils.isNullOrEmpty(key)) {
				WidgetUtils.addToGridBag(entry.getValue(), formPanel, 0, row, 2, 1);
			} else {
				WidgetUtils.addToGridBag(DCLabel.bright(key + ":"), formPanel, 0, row);
				WidgetUtils.addToGridBag(entry.getValue(), formPanel, 1, row);
			}
			row++;
		}

		if (isPreviewTableEnabled()) {
			WidgetUtils.addToGridBag(_loadingIcon, formPanel, 0, row, 2, 1);
			row++;
		}

		DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttonPanel.add(_addDatastoreButton);

		DCPanel centerPanel = new DCPanel();
		centerPanel.setLayout(new VerticalLayout(4));
		centerPanel.add(formPanel);
		if (isPreviewTableEnabled()) {
			centerPanel.add(_previewTablePanel);
		}
		centerPanel.add(buttonPanel);

		JXStatusBar statusBar = WidgetFactory.createStatusBar(_statusLabel);

		_outerPanel.setLayout(new BorderLayout());
		_outerPanel.add(centerPanel, BorderLayout.CENTER);
		_outerPanel.add(statusBar, BorderLayout.SOUTH);

		validateAndUpdate();

		return _outerPanel;
	}

	private void updatePreviewTable() {
		if (!isPreviewTableEnabled()) {
			return;
		}

		// show loading indicator
		_addDatastoreButton.setEnabled(false);
		_previewTable.setVisible(false);
		_loadingIcon.setVisible(true);

		// read file in background, it may take time if eg. it's located on a
		// network drive
		new SwingWorker<DataSet, Void>() {

			@Override
			protected DataSet doInBackground() throws Exception {
				return getPreviewData(getFilename());
			}

			@Override
			protected void done() {
				try {
					DataSet dataSet = get();
					if (dataSet != null) {
						_previewTable.setModel(dataSet.toTableModel());
					}
				} catch (Throwable e) {
					if (e instanceof ExecutionException) {
						// get the cause of the execution exception (it's a
						// wrapper around the throwable)
						e = e.getCause();
					}
					if (logger.isWarnEnabled()) {
						logger.warn("Error creating preview data: " + e.getMessage(), e);
					}

					setStatusError("Error create preview data: " + e.getMessage());
				}

				// show table
				_previewTable.setVisible(true);
				_loadingIcon.setVisible(false);
				_addDatastoreButton.setEnabled(true);
			}
		}.execute();
	}

	private final DataSet getPreviewData(String filename) {
		if (!isPreviewDataAvailable()) {
			logger.info("Not displaying preview table because isPreviewDataAvailable() returned false");
			return null;
		}
		D datastore = getPreviewDatastore(filename);
		DatastoreConnection con = datastore.openConnection();
		DataContext dc = con.getDataContext();
		Table table = getPreviewTable(dc);
		Column[] columns = table.getColumns();
		if (columns.length > getPreviewColumns()) {
			// include max 10 columns
			columns = Arrays.copyOf(columns, getPreviewColumns());
		}
		Query q = dc.query().from(table).select(columns).toQuery();
		q.setMaxRows(7);

		DataSet dataSet = dc.executeQuery(q);

		con.close();

		return dataSet;
	}

	protected boolean isPreviewDataAvailable() {
		return true;
	}

	protected Table getPreviewTable(DataContext dc) {
		return dc.getDefaultSchema().getTables()[0];
	}

	protected int getPreviewColumns() {
		return PREVIEW_COLUMNS;
	}

	protected D getPreviewDatastore(String filename) {
		D datastore = createDatastore("Preview", filename);
		return datastore;
	}

	protected byte[] getSampleBuffer() {
		final File file = new File(getFilename());
		byte[] bytes = new byte[SAMPLE_BUFFER_SIZE];
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			int bufferSize = fileInputStream.read(bytes, 0, SAMPLE_BUFFER_SIZE);
			if (bufferSize != -1 && bufferSize != SAMPLE_BUFFER_SIZE) {
				bytes = Arrays.copyOf(bytes, bufferSize);
			}
			return bytes;
		} catch (IOException e) {
			logger.error("IOException occurred while reading sample buffer", e);
			return new byte[0];
		} finally {
			FileHelper.safeClose(fileInputStream);
		}
	}

	protected char[] readSampleBuffer(byte[] bytes, final String charSet) {
		char[] buffer = new char[bytes.length];
		Reader reader = null;
		try {
			reader = new InputStreamReader(new ByteArrayInputStream(bytes), charSet);

			// read a sample of the file to auto-detect quotes and separators
			int bufferSize = reader.read(buffer);
			if (bufferSize != -1) {
				buffer = Arrays.copyOf(buffer, bufferSize);
			}
		} catch (Exception e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Error reading from file: " + e.getMessage(), e);
			}
			setStatusError("Error reading from file: " + e.getMessage());
			return new char[0];
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
					logger.debug("Could not close reader", ioe);
				}
			}
		}
		return buffer;
	}

	protected DCTable getPreviewTable() {
		return _previewTable;
	}

	@Override
	public Image getWindowIcon() {
		return imageManager.getImage(getDatastoreIconPath());
	}

	protected boolean isDirectoryBased() {
		return false;
	}
}
