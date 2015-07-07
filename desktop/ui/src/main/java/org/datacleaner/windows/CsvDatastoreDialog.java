/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.windows;

import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Resource;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.guice.Nullable;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.CsvConfigurationDetection;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImmutableEntry;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.CharSetEncodingComboBox;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.DCComboBox.Listener;
import org.datacleaner.widgets.HeaderLineComboBox;
import org.datacleaner.widgets.ResourceSelector;
import org.datacleaner.widgets.ResourceTypePresenter;

/**
 * Dialog for setting up CSV datastores.
 */
public final class CsvDatastoreDialog extends AbstractResourceBasedDatastoreDialog<CsvDatastore> {

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

    private final JComboBox<String> _separatorCharField;
    private final JComboBox<String> _quoteCharField;
    private final JComboBox<String> _escapeCharField;
    private final HeaderLineComboBox _headerLineComboBox;
    private final CharSetEncodingComboBox _encodingComboBox;
    private final JCheckBox _failOnInconsistenciesCheckBox;
    private final JCheckBox _multilineValuesCheckBox;

    private volatile boolean showPreview = true;

    @Inject
    public CsvDatastoreDialog(@Nullable CsvDatastore originalDatastore,
            MutableDatastoreCatalog mutableDatastoreCatalog, WindowContext windowContext,
            DataCleanerConfiguration configuration, UserPreferences userPreferences) {
        super(originalDatastore, mutableDatastoreCatalog, windowContext, configuration, userPreferences);
        _separatorCharField = new JComboBox<String>(new String[] { SEPARATOR_COMMA, SEPARATOR_TAB, SEPARATOR_SEMICOLON,
                SEPARATOR_PIPE });
        _separatorCharField.setEditable(true);

        _quoteCharField = new JComboBox<String>(new String[] { QUOTE_NONE, QUOTE_DOUBLE_QUOTE, QUOTE_SINGLE_QUOTE });
        _quoteCharField.setEditable(true);

        _escapeCharField = new JComboBox<String>(new String[] { ESCAPE_NONE, ESCAPE_BACKSLASH });
        _escapeCharField.setSelectedItem(ESCAPE_BACKSLASH);
        _escapeCharField.setEditable(true);

        _encodingComboBox = new CharSetEncodingComboBox();

        _headerLineComboBox = new HeaderLineComboBox();

        _failOnInconsistenciesCheckBox = new JCheckBox("Fail on inconsistent column count", true);
        _failOnInconsistenciesCheckBox.setOpaque(false);
        _failOnInconsistenciesCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        _failOnInconsistenciesCheckBox
                .setToolTipText("Check this checkbox to fail fast in case of inconsistent record lengths found in the CSV file. If not checked, missing fields will be represented by <null> values.");

        _multilineValuesCheckBox = new JCheckBox("Enable multi-line values?", false);
        _multilineValuesCheckBox.setOpaque(false);
        _multilineValuesCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        _multilineValuesCheckBox
                .setToolTipText("Check this checkbox if you want to allow CSV values to span multiple lines. Since this is rare, and comes at a performance penalty, we recommend turning multi-line values off.");

        setSaveButtonEnabled(false);
        showPreview = true;

        if (originalDatastore != null) {
            _failOnInconsistenciesCheckBox.setSelected(originalDatastore.isFailOnInconsistencies());
            _multilineValuesCheckBox.setSelected(originalDatastore.isMultilineValues());
            _encodingComboBox.setSelectedItem(originalDatastore.getEncoding());

            _headerLineComboBox.setSelectedItem(originalDatastore.getHeaderLineNumber());

            Character separatorChar = originalDatastore.getSeparatorChar();
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

            Character quoteChar = originalDatastore.getQuoteChar();
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

            Character escapeChar = originalDatastore.getEscapeChar();
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
    protected void onSelected(Resource resource) {
        onSettingsUpdated(true, true);
    }

    private void onSettingsUpdated(final boolean autoDetectSeparatorAndQuote, final boolean autoDetectEncoding) {
        if (!validateForm()) {
            return;
        }

        final Resource resource = getResource();
        if (resource == null || !resource.isExists()) {
            setStatusError("No source selected, or source does not exist");
            showPreview = false;
            return;
        }

        final CsvConfigurationDetection detection = new CsvConfigurationDetection(resource);

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
        result.add(new ImmutableEntry<String, JComponent>("", _multilineValuesCheckBox));
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
    public Image getWindowIcon() {
        return imageManager.getImage(IconUtils.CSV_IMAGEPATH);
    }

    @Override
    public String getWindowTitle() {
        return "CSV file datastore";
    }

    @Override
    protected CsvDatastore getPreviewDatastore(Resource resource) {
        return createDatastore("Preview", resource, false);
    }

    @Override
    protected CsvDatastore createDatastore(String name, Resource resource) {
        boolean failOnInconsistentRecords = _failOnInconsistenciesCheckBox.isSelected();
        return createDatastore(name, resource, failOnInconsistentRecords);
    }

    private CsvDatastore createDatastore(String name, Resource resource, boolean failOnInconsistentRecords) {
        return new CsvDatastore(name, resource, resource.getQualifiedPath(), getQuoteChar(), getSeparatorChar(),
                getEscapeChar(), getEncoding(), failOnInconsistentRecords, isMultilineValues(), getHeaderLine());
    }

    public boolean isMultilineValues() {
        return _multilineValuesCheckBox.isSelected();
    }

    @Override
    protected String getDatastoreIconPath() {
        return IconUtils.CSV_IMAGEPATH;
    }

    @Override
    protected void initializeFileFilters(ResourceSelector resourceSelector) {
        final FileFilter combinedFilter = FileFilters.combined("Any raw data file (.csv, .tsv, .dat, .txt)",
                FileFilters.CSV, FileFilters.TSV, FileFilters.DAT, FileFilters.TXT);
        resourceSelector.addChoosableFileFilter(combinedFilter);
        resourceSelector.addChoosableFileFilter(FileFilters.CSV);
        resourceSelector.addChoosableFileFilter(FileFilters.TSV);
        resourceSelector.addChoosableFileFilter(FileFilters.DAT);
        resourceSelector.addChoosableFileFilter(FileFilters.TXT);
        resourceSelector.addChoosableFileFilter(FileFilters.ALL);
        resourceSelector.setSelectedFileFilter(combinedFilter);

        resourceSelector.addListener(new ResourceTypePresenter.Listener() {
            @Override
            public void onResourceSelected(ResourceTypePresenter<?> presenter, Resource resource) {
                if (FileFilters.TSV.accept(resource)) {
                    _separatorCharField.setSelectedItem(SEPARATOR_TAB);
                }

                onSettingsUpdated(true, true);
            }

            @Override
            public void onPathEntered(ResourceTypePresenter<?> presenter, String path) {
            }
        });
    }
}
