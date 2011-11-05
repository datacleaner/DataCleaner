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

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.util.ImmutableEntry;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.Nullable;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.NumberDocument;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.CharSetEncodingComboBox;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.FileSelectionListener;
import org.eobjects.datacleaner.widgets.FilenameTextField;
import org.eobjects.metamodel.csv.CsvConfiguration;
import org.eobjects.metamodel.util.FileHelper;
import org.eobjects.metamodel.util.NumberComparator;

/**
 * Dialog for setting up CSV datastores.
 * 
 * @author Kasper Sørensen
 */
public final class CsvDatastoreDialog extends AbstractFileBasedDatastoreDialog<CsvDatastore> {

	private static final long serialVersionUID = 1L;

	private static final String SEPARATOR_TAB = "Tab (\\t)";
	private static final String SEPARATOR_COMMA = "Comma (,)";
	private static final String SEPARATOR_SEMICOLON = "Semicolon (;)";
	private static final String SEPARATOR_PIPE = "Pipe (|)";

	private static final String QUOTE_DOUBLE_QUOTE = "Double quote (\")";
	private static final String QUOTE_SINGLE_QUOTE = "Single quote (')";
	private static final String QUOTE_NONE = "(None)";

	private final JComboBox _separatorCharField;
	private final JComboBox _quoteCharField;
	private final JComboBox _headerLineComboBox;
	private final CharSetEncodingComboBox _encodingComboBox;
	private final JCheckBox _failOnInconsistenciesCheckBox;

	private volatile boolean showPreview = true;

	@Inject
	public CsvDatastoreDialog(@Nullable CsvDatastore datastore, MutableDatastoreCatalog mutableDatastoreCatalog,
			WindowContext windowContext, UserPreferences userPreferences) {
		super(datastore, mutableDatastoreCatalog, windowContext, userPreferences);
		_separatorCharField = new JComboBox(new String[] { SEPARATOR_COMMA, SEPARATOR_TAB, SEPARATOR_SEMICOLON,
				SEPARATOR_PIPE });
		_separatorCharField.setEditable(true);

		_quoteCharField = new JComboBox(new String[] { QUOTE_NONE, QUOTE_DOUBLE_QUOTE, QUOTE_SINGLE_QUOTE });
		_quoteCharField.setEditable(true);

		_encodingComboBox = new CharSetEncodingComboBox();

		_headerLineComboBox = new JComboBox();
		JTextComponent headerLineNumberText = (JTextComponent) _headerLineComboBox.getEditor().getEditorComponent();
		headerLineNumberText.setDocument(new NumberDocument());
		_headerLineComboBox.setEditable(true);
		_headerLineComboBox.setModel(new DefaultComboBoxModel(new Integer[] { 0, 1 }));
		_headerLineComboBox.setSelectedItem(1);
		_headerLineComboBox.setRenderer(new DefaultListCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				if (value instanceof Integer) {
					Integer i = (Integer) value;
					if (i <= 0) {
						value = "No header";
					}
				}
				return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}

		});

		_failOnInconsistenciesCheckBox = new JCheckBox("Fail on inconsistent column count", true);
		_failOnInconsistenciesCheckBox.setOpaque(false);
		_failOnInconsistenciesCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

		_addDatastoreButton.setEnabled(false);
		showPreview = true;

		if (_originalDatastore != null) {
			_failOnInconsistenciesCheckBox.setSelected(_originalDatastore.isFailOnInconsistencies());
			_encodingComboBox.setSelectedItem(_originalDatastore.getEncoding());

			_headerLineComboBox.setSelectedItem(_originalDatastore.getHeaderLineNumber() + 1);

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

			onSettingsUpdated(false, false);
		}

		// add listeners
		_separatorCharField.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				onSettingsUpdated(false, false);
			}
		});
		_quoteCharField.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				onSettingsUpdated(false, false);
			}
		});
		_encodingComboBox.addListener(new Listener<String>() {
			@Override
			public void onItemSelected(String item) {
				onSettingsUpdated(true, false);
			}
		});
		_headerLineComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				onSettingsUpdated(false, false);
			}
		});
	}

	@Override
	protected String getBannerTitle() {
		return "Comma-separated file";
	}

	@Override
	protected void onFileSelected(File file) {
		onSettingsUpdated(true, true);
	}

	private void onSettingsUpdated(final boolean autoDetectSeparatorAndQuote, final boolean autoDetectEncoding) {
		onSettingsUpdated(autoDetectSeparatorAndQuote, autoDetectEncoding, getSampleBuffer());
	}

	private void onSettingsUpdated(boolean autoDetectSeparatorAndQuote, boolean autoDetectEncoding, byte[] sampleBuffer) {
		if (!validateForm()) {
			return;
		}

		if (sampleBuffer == null || sampleBuffer.length == 0) {
			logger.debug("No bytes read to autodetect settings");
			return;
		}

		final List<String> warnings = new ArrayList<String>();
		showPreview = true;

		final String charSet;
		if (autoDetectEncoding) {
			charSet = _encodingComboBox.autoDetectEncoding(sampleBuffer);
		} else {
			charSet = _encodingComboBox.getSelectedItem().toString();
		}

		char[] sampleChars = readSampleBuffer(sampleBuffer, charSet);

		if (StringUtils.indexOf('\n', sampleChars) == -1) {
			warnings.add("No newline in first " + sampleChars.length + " chars");
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
			for (int i = 0; i < sampleChars.length; i++) {
				char c = sampleChars[i];
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
			validateAndUpdate();
		}

		if (warnings.isEmpty()) {
			setStatusValid();
		} else {
			StringBuilder sb = new StringBuilder();
			for (String warning : warnings) {
				sb.append(warning);
				sb.append(". ");
			}
			setStatusWarning(sb.toString());
		}
	}

	@Override
	protected boolean validateForm() {
		Object selectedEncoding = _encodingComboBox.getSelectedItem();
		if (selectedEncoding == null || selectedEncoding.toString().length() == 0) {
			setStatusError("Please select a character encoding!");
			return false;
		}
		return super.validateForm();
	}

	@Override
	protected boolean isPreviewTableEnabled() {
		return true;
	}

	@Override
	protected boolean isPreviewDataAvailable() {
		return showPreview;
	}

	@Override
	protected List<Entry<String, JComponent>> getFormElements() {
		List<Entry<String, JComponent>> result = super.getFormElements();
		result.add(new ImmutableEntry<String, JComponent>("Character encoding", _encodingComboBox));
		result.add(new ImmutableEntry<String, JComponent>("Separator", _separatorCharField));
		result.add(new ImmutableEntry<String, JComponent>("Quote char", _quoteCharField));
		result.add(new ImmutableEntry<String, JComponent>("Header line", _headerLineComboBox));
		result.add(new ImmutableEntry<String, JComponent>("", _failOnInconsistenciesCheckBox));
		return result;
	}

	public int getHeaderLine() {
		Number headerLineComboValue = NumberComparator.toNumber(_headerLineComboBox.getSelectedItem());
		if (headerLineComboValue != null) {
			int intComboValue = headerLineComboValue.intValue();
			if (intComboValue <= 0) {
				return CsvConfiguration.NO_COLUMN_NAME_LINE;
			} else {
				// MetaModel's headerline number is 0-based
				return intComboValue - 1;
			}
		} else {
			return CsvConfiguration.DEFAULT_COLUMN_NAME_LINE;
		}
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
	public Image getWindowIcon() {
		return imageManager.getImage(IconUtils.CSV_IMAGEPATH);
	}

	@Override
	public String getWindowTitle() {
		return "CSV file datastore";
	}

	@Override
	protected CsvDatastore createDatastore(String name, String filename) {
		return new CsvDatastore(name, filename, getQuoteChar(), getSeparatorChar(), getEncoding(),
				_failOnInconsistenciesCheckBox.isSelected(), getHeaderLine());
	}

	@Override
	protected String getDatastoreIconPath() {
		return IconUtils.CSV_IMAGEPATH;
	}

	@Override
	protected void setFileFilters(final FilenameTextField filenameField) {
		FileFilter combinedFilter = FileFilters.combined("Any raw data file (.csv, .tsv, .dat, .txt)", FileFilters.CSV,
				FileFilters.TSV, FileFilters.DAT, FileFilters.TXT);
		filenameField.addChoosableFileFilter(combinedFilter);
		filenameField.addChoosableFileFilter(FileFilters.CSV);
		filenameField.addChoosableFileFilter(FileFilters.TSV);
		filenameField.addChoosableFileFilter(FileFilters.DAT);
		filenameField.addChoosableFileFilter(FileFilters.TXT);
		filenameField.addChoosableFileFilter(FileFilters.ALL);
		filenameField.setSelectedFileFilter(combinedFilter);

		filenameField.addFileSelectionListener(new FileSelectionListener() {

			@Override
			public void onSelected(FilenameTextField filenameTextField, File file) {
				if (FileFilters.TSV.accept(file)) {
					_separatorCharField.setSelectedItem(SEPARATOR_TAB);
				}
			}
		});

		filenameField.getTextField().getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent e) {
				onSettingsUpdated(true, true);
			}
		});
	}
}
