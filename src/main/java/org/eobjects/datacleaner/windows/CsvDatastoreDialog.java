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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import org.eobjects.analyzer.connection.CsvDatastore;
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
import org.eobjects.datacleaner.widgets.CharSetEncodingComboBox;
import org.eobjects.datacleaner.widgets.FileSelectionListener;
import org.eobjects.datacleaner.widgets.FilenameTextField;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.eobjects.metamodel.CsvDataContextStrategy;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DefaultDataContext;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.FileHelper;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

public class CsvDatastoreDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(CsvDatastoreDialog.class);

	/**
	 * Amount of bytes to read for autodetection of encoding, separator and
	 * quotes
	 */
	private static final int SAMPLE_BUFFER_SIZE = 2048;

	/**
	 * Amount of rows to display in the preview table
	 */
	private static final int PREVIEW_ROWS = 7;

	private static final String SEPARATOR_TAB = "Tab (\\t)";
	private static final String SEPARATOR_COMMA = "Comma (,)";
	private static final String SEPARATOR_SEMICOLON = "Semicolon (;)";
	private static final String SEPARATOR_PIPE = "Pipe (|)";

	private static final String QUOTE_DOUBLE_QUOTE = "Double quote (\")";
	private static final String QUOTE_SINGLE_QUOTE = "Single quote (')";
	private static final String QUOTE_NONE = "(None)";

	private final UserPreferences userPreferences = UserPreferences.getInstance();
	private final MutableDatastoreCatalog _mutableDatastoreCatalog;
	private final JXTextField _datastoreNameField;
	private final FilenameTextField _filenameField;
	private final JComboBox _separatorCharField;
	private final JComboBox _quoteCharField;
	private final JComboBox _encodingComboBox;
	private final JLabel _statusLabel;
	private final DCTable _previewTable = new DCTable(new DefaultTableModel(PREVIEW_ROWS, 10));
	private final DCPanel _outerPanel = new DCPanel();
	private final JButton _addDatastoreButton;
	private final CsvDatastore _originalDatastore;

	public CsvDatastoreDialog(MutableDatastoreCatalog mutableDatastoreCatalog) {
		this(null, mutableDatastoreCatalog);
	}

	public CsvDatastoreDialog(CsvDatastore datastore, MutableDatastoreCatalog mutableDatastoreCatalog) {
		super();
		_originalDatastore = datastore;
		_mutableDatastoreCatalog = mutableDatastoreCatalog;
		_datastoreNameField = WidgetFactory.createTextField("Datastore name");

		_filenameField = new FilenameTextField(userPreferences.getDatastoreDirectory(), true);
		_filenameField.getTextField().getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent e) {
				onSettingsUpdated(true, true);
			}
		});

		_separatorCharField = new JComboBox(new String[] { SEPARATOR_COMMA, SEPARATOR_TAB, SEPARATOR_SEMICOLON,
				SEPARATOR_PIPE });
		_separatorCharField.setEditable(true);
		_separatorCharField.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				onSettingsUpdated(false, false);
			}
		});

		_quoteCharField = new JComboBox(new String[] { QUOTE_NONE, QUOTE_DOUBLE_QUOTE, QUOTE_SINGLE_QUOTE });
		_quoteCharField.setEditable(true);
		_quoteCharField.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				onSettingsUpdated(false, false);
			}
		});

		_encodingComboBox = new CharSetEncodingComboBox();
		_encodingComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				onSettingsUpdated(true, false);
			}
		});

		_statusLabel = new JLabel("Please select file");

		FileFilter combinedFilter = FileFilters.combined("Any raw data file (.csv, .tsv, .dat, .txt)", FileFilters.CSV,
				FileFilters.TSV, FileFilters.DAT, FileFilters.TXT);
		_filenameField.addChoosableFileFilter(combinedFilter);
		_filenameField.addChoosableFileFilter(FileFilters.CSV);
		_filenameField.addChoosableFileFilter(FileFilters.TSV);
		_filenameField.addChoosableFileFilter(FileFilters.DAT);
		_filenameField.addChoosableFileFilter(FileFilters.TXT);
		_filenameField.addChoosableFileFilter(FileFilters.ALL);
		_filenameField.setSelectedFileFilter(combinedFilter);
		_filenameField.addFileSelectionListener(new FileSelectionListener() {

			@Override
			public void onSelected(FilenameTextField filenameTextField, File file) {
				File dir = file.getParentFile();
				userPreferences.setDatastoreDirectory(dir);

				if (StringUtils.isNullOrEmpty(_datastoreNameField.getText())) {
					_datastoreNameField.setText(file.getName());
				}

				if (FileFilters.TSV.accept(file)) {
					_separatorCharField.setSelectedItem(SEPARATOR_TAB);
				}
			}
		});

		_addDatastoreButton = WidgetFactory.createButton("Save datastore", "images/datastore-types/csv.png");
		_addDatastoreButton.setEnabled(false);

		if (_originalDatastore != null) {
			_datastoreNameField.setText(_originalDatastore.getName());
			_filenameField.setFilename(_originalDatastore.getFilename());
			_encodingComboBox.setSelectedItem(_originalDatastore.getEncoding());

			Character separatorChar = _originalDatastore.getSeparatorChar();
			String separator = null;
			if (separatorChar != null) {
				if (separatorChar.charValue() == ',') {
					separator = SEPARATOR_COMMA;
				} else if (separatorChar.charValue() == ';') {
					separator = SEPARATOR_SEMICOLON;
				} else if (separatorChar.charValue() == '|') {
					separator = SEPARATOR_PIPE;
				} else if (separatorChar.charValue() == '\t') {
					separator = SEPARATOR_TAB;
				} else {
					separator = separatorChar.toString();
				}
			}
			_separatorCharField.setSelectedItem(separator);

			Character quoteChar = _originalDatastore.getQuoteChar();
			final String quote;
			if (quoteChar == null) {
				quote = QUOTE_NONE;
			} else {
				if (quoteChar.charValue() == CsvDatastore.NOT_A_CHAR) {
					quote = QUOTE_NONE;
				} else if (quoteChar.charValue() == '"') {
					quote = QUOTE_DOUBLE_QUOTE;
				} else if (quoteChar.charValue() == '\'') {
					quote = QUOTE_SINGLE_QUOTE;
				} else {
					quote = quoteChar.toString();
				}
			}
			_quoteCharField.setSelectedItem(quote);
		}
	}

	@Override
	protected String getBannerTitle() {
		return "Comma-separated\nfile";
	}

	private void onSettingsUpdated(boolean autoDetectSeparatorAndQuote, boolean autoDetectEncoding) {
		List<String> warnings = new ArrayList<String>();
		boolean showPreview = true;
		ImageManager imageManager = ImageManager.getInstance();

		File file = new File(_filenameField.getFilename());
		if (file.exists()) {
			if (!file.isFile()) {
				_statusLabel.setText("Not a valid file!");
				_statusLabel.setIcon(imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL));
				_addDatastoreButton.setEnabled(false);
				return;
			}
		} else {
			_statusLabel.setText("The file does not exist!");
			_statusLabel.setIcon(imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL));
			_addDatastoreButton.setEnabled(false);
			return;
		}
		_addDatastoreButton.setEnabled(true);

		byte[] bytes = new byte[SAMPLE_BUFFER_SIZE];

		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			int bufferSize = fileInputStream.read(bytes, 0, SAMPLE_BUFFER_SIZE);
			if (bufferSize != -1 && bufferSize != SAMPLE_BUFFER_SIZE) {
				bytes = Arrays.copyOf(bytes, bufferSize);
			}
		} catch (Exception e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Error reading from file: " + e.getMessage(), e);
			}
			_statusLabel.setText("Error reading from file: " + e.getMessage());
			_statusLabel.setIcon(imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL));
			return;
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException ioe) {
					logger.debug("Could not close reader", ioe);
				}
			}
		}

		final String charSet;
		if (autoDetectEncoding) {
			CharsetDetector cd = new CharsetDetector();
			cd.setText(bytes);
			CharsetMatch charsetMatch = cd.detect();
			charSet = charsetMatch.getName();
			logger.info("CharsetMatch: {} ({}% confidence)", charSet, charsetMatch.getConfidence());
			_encodingComboBox.setSelectedItem(charSet);
		} else {
			charSet = _encodingComboBox.getSelectedItem().toString();
		}

		char[] buffer = new char[SAMPLE_BUFFER_SIZE];
		Reader reader = null;
		try {
			reader = new InputStreamReader(new ByteArrayInputStream(bytes), charSet);

			// read a sample of the file auto-detect quotes and separators
			int bufferSize = reader.read(buffer);
			if (bufferSize != -1) {
				buffer = Arrays.copyOf(buffer, bufferSize);
			}
		} catch (Exception e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Error reading from file: " + e.getMessage(), e);
			}
			_statusLabel.setText("Error reading from file: " + e.getMessage());
			_statusLabel.setIcon(imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL));
			return;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
					logger.debug("Could not close reader", ioe);
				}
			}
		}

		if (indexOf('\n', buffer) == -1 && buffer.length == SAMPLE_BUFFER_SIZE) {
			warnings.add("No newline in first " + buffer.length + " chars");
			// don't show the preview if no newlines where found (it may try
			// to treat the whole file as a single row)
			showPreview = false;
		}

		if (autoDetectSeparatorAndQuote) {
			int newlines = 0;
			int tabs = 0;
			int commas = 0;
			int semicolons = 0;
			int pipes = 0;
			int singleQuotes = 0;
			int doubleQuotes = 0;
			for (int i = 0; i < buffer.length; i++) {
				char c = buffer[i];
				if (c == '\n') {
					newlines++;
				} else if (c == '\t') {
					tabs++;
				} else if (c == ',') {
					commas++;
				} else if (c == ';') {
					semicolons++;
				} else if (c == '\'') {
					singleQuotes++;
				} else if (c == '|') {
					pipes++;
				} else if (c == '"') {
					doubleQuotes++;
				}
			}

			int detectedSeparator = Math.max(tabs, Math.max(commas, Math.max(semicolons, pipes)));
			if (detectedSeparator == 0 || detectedSeparator < newlines) {
				warnings.add("Could not autodetect separator char");
			} else {
				// set the separator
				if (detectedSeparator == commas) {
					_separatorCharField.setSelectedItem(SEPARATOR_COMMA);
				} else if (detectedSeparator == semicolons) {
					_separatorCharField.setSelectedItem(SEPARATOR_SEMICOLON);
				} else if (detectedSeparator == tabs) {
					_separatorCharField.setSelectedItem(SEPARATOR_TAB);
				} else if (detectedSeparator == pipes) {
					_separatorCharField.setSelectedItem(SEPARATOR_PIPE);
				}
			}

			int detectedQuote = Math.max(singleQuotes, doubleQuotes);
			if (detectedQuote == 0 || detectedQuote < newlines) {
				_quoteCharField.setSelectedItem(QUOTE_NONE);
			} else {
				// set the quote
				if (detectedQuote == singleQuotes) {
					_quoteCharField.setSelectedItem(QUOTE_SINGLE_QUOTE);
				} else if (detectedQuote == doubleQuotes) {
					_quoteCharField.setSelectedItem(QUOTE_DOUBLE_QUOTE);
				}
			}
		}

		if (showPreview) {
			updatePreviewTable();
		}

		if (warnings.isEmpty()) {
			_statusLabel.setText("File read - separator and quote chars have been autodetected!");
			_statusLabel.setIcon(imageManager.getImageIcon("images/status/valid.png", IconUtils.ICON_SIZE_SMALL));
		} else {
			StringBuilder sb = new StringBuilder();
			for (String warning : warnings) {
				sb.append(warning);
				sb.append(". ");
			}
			_statusLabel.setText(sb.toString());
			_statusLabel.setIcon(imageManager.getImageIcon("images/status/warning.png", IconUtils.ICON_SIZE_SMALL));
		}
	}

	/**
	 * Finds the index of a char in a char-array, similar to String.indexOf(...)
	 * 
	 * @param c
	 * @param arr
	 * @return
	 */
	private int indexOf(char c, char[] arr) {
		for (int i = 0; i < arr.length; i++) {
			if (c == arr[i]) {
				return i;
			}
		}
		return -1;
	}

	private void updatePreviewTable() {
		try {
			File file = new File(_filenameField.getFilename());

			char separatorChar = getSeparatorChar();
			char quoteChar = getQuoteChar();

			CsvDataContextStrategy dcStrategy = new CsvDataContextStrategy(file, separatorChar, quoteChar, getEncoding());
			DataContext dc = new DefaultDataContext(dcStrategy);
			Schema schema = dc.getDefaultSchema();
			Table table = schema.getTables()[0];
			Column[] columns = table.getColumns();

			Query q = dc.query().from(table).select(columns).toQuery();
			q.setMaxRows(PREVIEW_ROWS);

			DataSet dataSet = dc.executeQuery(q);

			_previewTable.setModel(dataSet.toTableModel());
			_outerPanel.updateUI();
		} catch (Exception e) {
			logger.error("Unexpected error when updating preview table", e);
		}
	}

	@Override
	protected int getDialogWidth() {
		return 550;
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

		row++;
		WidgetUtils.addToGridBag(new JLabel("Character encoding:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_encodingComboBox, formPanel, 1, row);

		row++;
		WidgetUtils.addToGridBag(new JLabel("Separator:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_separatorCharField, formPanel, 1, row);

		row++;
		WidgetUtils.addToGridBag(new JLabel("Quote char:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_quoteCharField, formPanel, 1, row);

		row++;
		WidgetUtils.addToGridBag(_previewTable.toPanel(), formPanel, 0, row, 2, 1);

		_addDatastoreButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (_originalDatastore != null) {
					_mutableDatastoreCatalog.removeDatastore(_originalDatastore);
				}
				CsvDatastore datastore = new CsvDatastore(_datastoreNameField.getText(), _filenameField.getFilename(),
						getQuoteChar(), getSeparatorChar(), getEncoding());
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
		centerPanel.add(_previewTable.toPanel());
		centerPanel.add(buttonPanel);

		JXStatusBar statusBar = new JXStatusBar();
		JXStatusBar.Constraint c1 = new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FILL);
		statusBar.add(_statusLabel, c1);

		_outerPanel.setLayout(new BorderLayout());
		_outerPanel.add(centerPanel, BorderLayout.CENTER);
		_outerPanel.add(statusBar, BorderLayout.SOUTH);

		return _outerPanel;
	}

	public String getEncoding() {
		String encoding = _encodingComboBox.getSelectedItem().toString();
		if (StringUtils.isNullOrEmpty(encoding)) {
			encoding = FileHelper.UTF_8_ENCODING;
		}
		return encoding;
	}

	public Character getSeparatorChar() {
		Object separatorItem = _separatorCharField.getSelectedItem();
		if (SEPARATOR_COMMA.equals(separatorItem)) {
			return ',';
		} else if (SEPARATOR_SEMICOLON.equals(separatorItem)) {
			return ';';
		} else if (SEPARATOR_TAB.equals(separatorItem)) {
			return '\t';
		} else if (SEPARATOR_PIPE.equals(separatorItem)) {
			return '|';
		} else {
			return separatorItem.toString().charAt(0);
		}
	}

	public Character getQuoteChar() {
		Object quoteItem = _quoteCharField.getSelectedItem();
		if (QUOTE_NONE.equals(quoteItem)) {
			return CsvDatastore.NOT_A_CHAR;
		} else if (QUOTE_DOUBLE_QUOTE.equals(quoteItem)) {
			return '"';
		} else if (QUOTE_SINGLE_QUOTE.equals(quoteItem)) {
			return '\'';
		} else {
			return quoteItem.toString().charAt(0);
		}
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected String getWindowTitle() {
		return "CSV file datastore";
	}

}
