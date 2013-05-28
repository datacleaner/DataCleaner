/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;

import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.util.ImmutableEntry;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.Nullable;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.CsvConfigurationDetection;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.CharSetEncodingComboBox;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.FileSelectionListener;
import org.eobjects.datacleaner.widgets.FilenameTextField;
import org.eobjects.datacleaner.widgets.HeaderLineComboBox;
import org.eobjects.metamodel.csv.CsvConfiguration;
import org.eobjects.metamodel.util.FileHelper;
import org.eobjects.metamodel.util.FileResource;
import org.eobjects.metamodel.util.Resource;

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

    private static final String ESCAPE_BACKSLASH = "Backslash (\\)";
    private static final String ESCAPE_NONE = "(None)";

    private final JComboBox _separatorCharField;
    private final JComboBox _quoteCharField;
    private final JComboBox _escapeCharField;
    private final HeaderLineComboBox _headerLineComboBox;
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

        _escapeCharField = new JComboBox(new String[] { ESCAPE_NONE, ESCAPE_BACKSLASH });
        _escapeCharField.setSelectedItem(ESCAPE_BACKSLASH);
        _escapeCharField.setEditable(true);

        _encodingComboBox = new CharSetEncodingComboBox();

        _headerLineComboBox = new HeaderLineComboBox();

        _failOnInconsistenciesCheckBox = new JCheckBox("Fail on inconsistent column count", true);
        _failOnInconsistenciesCheckBox.setOpaque(false);
        _failOnInconsistenciesCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

        _addDatastoreButton.setEnabled(false);
        showPreview = true;

        if (_originalDatastore != null) {
            _failOnInconsistenciesCheckBox.setSelected(_originalDatastore.isFailOnInconsistencies());
            _encodingComboBox.setSelectedItem(_originalDatastore.getEncoding());

            _headerLineComboBox.setSelectedItem(_originalDatastore.getHeaderLineNumber());

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

            Character escapeChar = _originalDatastore.getEscapeChar();
            final String escape;
            if (escapeChar == null) {
                escape = ESCAPE_NONE;
            } else {
                if (escapeChar.charValue() == CsvDatastore.NOT_A_CHAR) {
                    escape = ESCAPE_NONE;
                } else if (escapeChar.charValue() == '\\') {
                    escape = ESCAPE_BACKSLASH;
                } else {
                    escape = escapeChar.toString();
                }
            }
            _escapeCharField.setSelectedItem(escape);

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
        _escapeCharField.addItemListener(new ItemListener() {
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
        _headerLineComboBox.addListener(new DCComboBox.Listener<Integer>() {
            @Override
            public void onItemSelected(Integer item) {
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
        if (!validateForm()) {
            return;
        }

        final File file = new File(getFilename());
        if (file == null || !file.exists()) {
            setStatusError("No file selected, or file does not exist");
            showPreview = false;
            return;
        }

        final CsvConfigurationDetection detection = new CsvConfigurationDetection(file);

        final CsvConfiguration configuration;

        try {
            if (autoDetectEncoding) {
                configuration = detection.suggestCsvConfiguration();
            } else {
                String charSet = _encodingComboBox.getSelectedItem().toString();
                configuration = detection.suggestCsvConfiguration(charSet);
            }
        } catch (IllegalStateException e) {
            logger.debug("Failed to auto detect CSV configuration", e);
            setStatusError(e.getMessage());
            return;
        }

        if (autoDetectEncoding) {
            _encodingComboBox.setSelectedItem(configuration.getEncoding());
        }

        if (autoDetectSeparatorAndQuote) {
            // set the separator
            final char separatorChar = configuration.getSeparatorChar();
            if (separatorChar == ',') {
                _separatorCharField.setSelectedItem(SEPARATOR_COMMA);
            } else if (separatorChar == ';') {
                _separatorCharField.setSelectedItem(SEPARATOR_SEMICOLON);
            } else if (separatorChar == '\t') {
                _separatorCharField.setSelectedItem(SEPARATOR_TAB);
            } else if (separatorChar == '|') {
                _separatorCharField.setSelectedItem(SEPARATOR_PIPE);
            } else {
                _separatorCharField.setSelectedItem(separatorChar + "");
            }

            // set the quote
            final char quoteChar = configuration.getQuoteChar();
            if (quoteChar == CsvConfiguration.NOT_A_CHAR) {
                _quoteCharField.setSelectedItem(QUOTE_NONE);
            } else if (quoteChar == '\'') {
                _quoteCharField.setSelectedItem(QUOTE_SINGLE_QUOTE);
            } else if (quoteChar == '"') {
                _quoteCharField.setSelectedItem(QUOTE_DOUBLE_QUOTE);
            } else {
                _quoteCharField.setSelectedItem(quoteChar + "");
            }

            // set the escape char
            char escapeChar = configuration.getEscapeChar();
            if (escapeChar == '\\') {
                _escapeCharField.setSelectedItem(ESCAPE_BACKSLASH);
            }
        }

        showPreview = true;
        validateAndUpdate();
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
        result.add(new ImmutableEntry<String, JComponent>("Escape char", _escapeCharField));
        result.add(new ImmutableEntry<String, JComponent>("Header line", _headerLineComboBox));
        result.add(new ImmutableEntry<String, JComponent>("", _failOnInconsistenciesCheckBox));
        return result;
    }

    public int getHeaderLine() {
        Number headerLineComboValue = _headerLineComboBox.getSelectedItem();
        if (headerLineComboValue != null) {
            int intComboValue = headerLineComboValue.intValue();
            if (intComboValue < 0) {
                return CsvConfiguration.NO_COLUMN_NAME_LINE;
            } else {
                // MetaModel's headerline number is 0-based
                return intComboValue;
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

    public Character getEscapeChar() {
        Object escapeItem = _escapeCharField.getSelectedItem();
        if (ESCAPE_NONE.equals(escapeItem)) {
            return CsvDatastore.NOT_A_CHAR;
        } else if (ESCAPE_BACKSLASH.equals(escapeItem)) {
            return '\\';
        } else {
            return escapeItem.toString().charAt(0);
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
    protected CsvDatastore getPreviewDatastore(String filename) {
        return createDatastore("Preview", filename, false);
    }

    @Override
    protected CsvDatastore createDatastore(String name, String filename) {
        boolean failOnInconsistentRecords = _failOnInconsistenciesCheckBox.isSelected();
        return createDatastore(name, filename, failOnInconsistentRecords);
    }

    private CsvDatastore createDatastore(String name, String filename, boolean failOnInconsistentRecords) {
        final Resource resource = new FileResource(filename);
        return new CsvDatastore(name, resource, filename, getQuoteChar(), getSeparatorChar(), getEscapeChar(),
                getEncoding(), failOnInconsistentRecords, getHeaderLine());
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
