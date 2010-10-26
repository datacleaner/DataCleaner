package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.Reader;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.CsvDataContextStrategy;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DefaultDataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;
import dk.eobjects.metamodel.util.FileHelper;

public class OpenCsvFileDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(OpenCsvFileDialog.class);

	private static final int PREVIEW_ROWS = 7;

	private static final String SEPARATOR_TAB = "Tab (\\t)";
	private static final String SEPARATOR_COMMA = "Comma (,)";
	private static final String SEPARATOR_SEMICOLON = "Semicolon (;)";
	private static final String SEPARATOR_PIPE = "Pipe (|)";

	private static final String QUOTE_DOUBLE_QUOTE = "Double quote (\")";
	private static final String QUOTE_SINGLE_QUOTE = "Single quote (')";

	private final MutableDatastoreCatalog _mutableDatastoreCatalog;;
	private final JXTextField _datastoreNameField;
	private final JXTextField _filenameField;
	private final JComboBox _separatorCharField;
	private final JComboBox _quoteCharField;
	private final JComboBox _encodingComboBox;
	private final JButton _browseButton;
	private final JLabel _statusLabel;
	private final DCTable _previewTable = new DCTable(new DefaultTableModel(PREVIEW_ROWS, 10));
	private final DCPanel _outerPanel = new DCPanel();

	public OpenCsvFileDialog(MutableDatastoreCatalog mutableDatastoreCatalog) {
		super();
		_mutableDatastoreCatalog = mutableDatastoreCatalog;
		_datastoreNameField = WidgetUtils.createTextField("Datastore name");

		_filenameField = WidgetUtils.createTextField("Filename");
		_filenameField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				autoDetectQuoteAndSeparator();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				autoDetectQuoteAndSeparator();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				autoDetectQuoteAndSeparator();
			}
		});

		_separatorCharField = new JComboBox(new String[] { SEPARATOR_COMMA, SEPARATOR_TAB, SEPARATOR_SEMICOLON,
				SEPARATOR_PIPE });
		_separatorCharField.setEditable(true);

		_quoteCharField = new JComboBox(new String[] { QUOTE_DOUBLE_QUOTE, QUOTE_SINGLE_QUOTE });
		_quoteCharField.setEditable(true);

		_encodingComboBox = new JComboBox(new String[] { "UTF-8", "ASCII", "CP1252" });
		_encodingComboBox.setEditable(true);
		_encodingComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				autoDetectQuoteAndSeparator();
			}
		});

		_statusLabel = new JLabel("Please select file");

		_browseButton = new JButton("Browse", ImageManager.getInstance().getImageIcon("images/actions/browse.png",
				IconUtils.ICON_SIZE_SMALL));
		_browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(UserPreferences.getInstance().getOpenFileDirectory());
				FileFilter combinedFilter = FileFilters.combined("Any ", FileFilters.CSV, FileFilters.TSV, FileFilters.DAT,
						FileFilters.TXT);
				fileChooser.addChoosableFileFilter(combinedFilter);
				fileChooser.addChoosableFileFilter(FileFilters.CSV);
				fileChooser.addChoosableFileFilter(FileFilters.TSV);
				fileChooser.addChoosableFileFilter(FileFilters.DAT);
				fileChooser.addChoosableFileFilter(FileFilters.TXT);
				fileChooser.addChoosableFileFilter(FileFilters.ALL);
				fileChooser.setFileFilter(combinedFilter);
				WidgetUtils.centerOnScreen(fileChooser);
				int result = fileChooser.showOpenDialog(OpenCsvFileDialog.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					File dir = selectedFile.getParentFile();
					UserPreferences.getInstance().setOpenFileDirectory(dir);
					_filenameField.setText(selectedFile.getAbsolutePath());

					if (StringUtils.isNullOrEmpty(_datastoreNameField.getText())) {
						_datastoreNameField.setText(selectedFile.getName());
					}

					if (FileFilters.TSV.accept(selectedFile)) {
						_separatorCharField.setSelectedItem(SEPARATOR_TAB);
					}

					autoDetectQuoteAndSeparator();
				}
			}
		});
	}

	private void autoDetectQuoteAndSeparator() {
		ImageManager imageManager = ImageManager.getInstance();

		File file = new File(_filenameField.getText());
		if (file.exists()) {
			if (!file.isFile()) {
				_statusLabel.setText("Not a valid file!");
				_statusLabel.setIcon(imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL));
				return;
			}
		} else {
			_statusLabel.setText("The file does not exist!");
			_statusLabel.setIcon(imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL));
			return;
		}

		try {
			Reader reader = FileHelper.getReader(file, _encodingComboBox.getSelectedItem().toString());

			// take 1024 sample characters to auto-detect quotes and separators
			char[] buffer = new char[1024];
			int bufferSize = reader.read(buffer);
			if (bufferSize != -1) {
				buffer = Arrays.copyOf(buffer, bufferSize);
			}
			reader.close();
			int tabs = 0;
			int commas = 0;
			int semicolons = 0;
			int pipes = 0;
			int singleQuotes = 0;
			int doubleQuotes = 0;
			for (int i = 0; i < buffer.length; i++) {
				char c = buffer[i];
				if (c == '\t') {
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
			if (detectedSeparator == 0) {
				throw new IllegalStateException("Could not autodetect separator char");
			}

			if (detectedSeparator == commas) {
				_separatorCharField.setSelectedItem(SEPARATOR_COMMA);
			} else if (detectedSeparator == semicolons) {
				_separatorCharField.setSelectedItem(SEPARATOR_SEMICOLON);
			} else if (detectedSeparator == tabs) {
				_separatorCharField.setSelectedItem(SEPARATOR_TAB);
			} else if (detectedSeparator == pipes) {
				_separatorCharField.setSelectedItem(SEPARATOR_PIPE);
			}

			int detectedQuote = Math.max(singleQuotes, doubleQuotes);
			if (detectedQuote == 0) {
				throw new IllegalStateException("Could not autodetect quote char");
			}

			if (detectedQuote == singleQuotes) {
				_quoteCharField.setSelectedItem(QUOTE_SINGLE_QUOTE);
			} else if (detectedQuote == doubleQuotes) {
				_quoteCharField.setSelectedItem(QUOTE_DOUBLE_QUOTE);
			}

			updatePreviewTable();

			_statusLabel.setText("File read - separator and quote chars have been autodetected!");
			_statusLabel.setIcon(imageManager.getImageIcon("images/status/valid.png", IconUtils.ICON_SIZE_SMALL));
		} catch (Exception e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Error reading from file: " + e.getMessage(), e);
			}
			_statusLabel.setText("Error reading from file: " + e.getMessage());
			_statusLabel.setIcon(imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL));
		}
	}

	private void updatePreviewTable() {
		try {
			File file = new File(_filenameField.getText());

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
		return 400;
	}

	@Override
	protected JComponent getDialogContent() {
		DCPanel formPanel = new DCPanel();

		// temporary variable to make it easier to refactor the layout
		int row = 0;
		WidgetUtils.addToGridBag(new JLabel("Datastore name"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_datastoreNameField, formPanel, 1, row);

		row++;
		WidgetUtils.addToGridBag(new JLabel("Filename"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_filenameField, formPanel, 1, row);
		WidgetUtils.addToGridBag(_browseButton, formPanel, 2, row);

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
		WidgetUtils.addToGridBag(_previewTable.toPanel(), formPanel, 0, row, 3, 1);

		JButton addDatastoreButton = new JButton("Create datastore");
		addDatastoreButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CsvDatastore datastore = new CsvDatastore(_datastoreNameField.getText(), _filenameField.getText(),
						getQuoteChar(), getSeparatorChar(), getEncoding());
				_mutableDatastoreCatalog.addDatastore(datastore);
				dispose();
			}
		});

		DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(addDatastoreButton);

		DCPanel centerPanel = new DCPanel();
		centerPanel.setLayout(new VerticalLayout(4));
		centerPanel.add(formPanel);
		centerPanel.add(_previewTable.toPanel());
		centerPanel.add(buttonPanel);

		centerPanel.setPreferredSize(getDialogWidth(), 380);

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
		if (QUOTE_DOUBLE_QUOTE.equals(quoteItem)) {
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
		return "Open CSV file";
	}

}
