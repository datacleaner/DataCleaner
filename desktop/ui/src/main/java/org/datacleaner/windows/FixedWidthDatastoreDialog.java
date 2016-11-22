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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.apache.metamodel.fixedwidth.FixedWidthConfiguration;
import org.apache.metamodel.util.Resource;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.FixedWidthDatastore;
import org.datacleaner.guice.Nullable;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImmutableEntry;
import org.datacleaner.util.NumberDocument;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.CharSetEncodingComboBox;
import org.datacleaner.widgets.CustomColumnNamesWidget;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.HeaderLineComboBox;
import org.datacleaner.widgets.ResourceSelector;
import org.datacleaner.widgets.ResourceTypePresenter;
import org.jdesktop.swingx.JXTextField;

public final class FixedWidthDatastoreDialog extends AbstractResourceBasedDatastoreDialog<FixedWidthDatastore> {

    private static final long serialVersionUID = 1L;

    private final CharSetEncodingComboBox _encodingComboBox;
    private final JCheckBox _failOnInconsistenciesCheckBox;
    private final JCheckBox _skipEbcdicHeaderCheckBox;
    private final JCheckBox _eolPresentCheckBox;
    private final List<JXTextField> _valueWidthTextFields;
    private final DCPanel _valueWidthsPanel;
    private final DCLabel _lineWidthLabel;
    private final HeaderLineComboBox _headerLineComboBox;
    private final JButton _addValueWidthButton;
    private final JButton _removeValueWidthButton;
    private final DocumentListener _updatePreviewTableDocumentListener;
    private final CustomColumnNamesWidget _columnNamesWidget;

    private volatile boolean showPreview = true;

    private Set<JTextField> _columnNameFields = new HashSet<>();

    @Inject
    protected FixedWidthDatastoreDialog(@Nullable final FixedWidthDatastore originalDatastore,
            final MutableDatastoreCatalog mutableDatastoreCatalog, final WindowContext windowContext,
            final DataCleanerConfiguration configuration, final UserPreferences userPreferences) {
        super(originalDatastore, mutableDatastoreCatalog, windowContext, configuration, userPreferences);
        _updatePreviewTableDocumentListener = new DCDocumentListener() {
            @Override
            protected void onChange(final DocumentEvent event) {
                onSettingsUpdated(false);
            }
        };
        _lineWidthLabel = DCLabel.bright("");
        _valueWidthsPanel = new DCPanel();
        _valueWidthsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
        _valueWidthTextFields = new ArrayList<>();
        _encodingComboBox = new CharSetEncodingComboBox();
        _addValueWidthButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD_DARK);
        _removeValueWidthButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE_DARK);

        _headerLineComboBox = new HeaderLineComboBox();

        _failOnInconsistenciesCheckBox = createCheckBox("Fail on inconsistent line length", true);
        _skipEbcdicHeaderCheckBox = createCheckBox("Input file contains a header that should be skipped", false);
        _eolPresentCheckBox = createCheckBox("Input file contains new line characters", true);

        if (originalDatastore != null) {
            _columnNamesWidget = new CustomColumnNamesWidget(originalDatastore.getCustomColumnNames());

            _encodingComboBox.setSelectedItem(originalDatastore.getEncoding());
            _failOnInconsistenciesCheckBox.setSelected(originalDatastore.isFailOnInconsistencies());
            _skipEbcdicHeaderCheckBox.setSelected(originalDatastore.isSkipEbcdicHeader());
            _eolPresentCheckBox.setSelected(originalDatastore.isEolPresent());

            final int[] valueWidths = originalDatastore.getValueWidths();
            for (final int valueWidth : valueWidths) {
                addValueWidthTextField(valueWidth);
            }

            _headerLineComboBox.setSelectedIndex(originalDatastore.getHeaderLineNumber());

            onSettingsUpdated(false);
        } else {
            _columnNamesWidget = new CustomColumnNamesWidget(null);

            addValueWidthTextField();
            addValueWidthTextField();
            addValueWidthTextField();
        }

        _addValueWidthButton.addActionListener(e -> addValueWidthTextField());
        _removeValueWidthButton.addActionListener(e -> removeValueWidthTextField());
        _encodingComboBox.addListener(item -> onSettingsUpdated(false));
        _headerLineComboBox.addListener(item -> onSettingsUpdated(false));

        _columnNamesWidget.getButtons().forEach(button -> button.addActionListener(action -> {
            onSettingsUpdated(false);
            SwingUtilities.invokeLater(() -> registerColumnNameFields());
        }));

        registerColumnNameFields();

    }

    private JCheckBox createCheckBox(final String label, final boolean selected) {
        final JCheckBox checkBox = new JCheckBox(label, selected);
        checkBox.setOpaque(false);
        checkBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        checkBox.addItemListener(item -> onSettingsUpdated(false));

        return checkBox;
    }

    @Override
    protected boolean validateForm() {
        final Object selectedEncoding = _encodingComboBox.getSelectedItem();
        if (selectedEncoding == null || selectedEncoding.toString().length() == 0) {
            setStatusError("Please select a character encoding!");
            return false;
        }
        return super.validateForm();
    }

    @Override
    protected void onSelected(final Resource resource) {
        onSettingsUpdated(true);
    }

    private void onSettingsUpdated(final boolean autoDetectEncoding) {
        if (!validateForm()) {
            return;
        }

        final byte[] sampleBuffer = getSampleBuffer();
        if (sampleBuffer == null || sampleBuffer.length == 0) {
            logger.debug("No bytes read to autodetect settings");
            return;
        }

        final String charSet;
        if (autoDetectEncoding) {
            charSet = _encodingComboBox.autoDetectEncoding(sampleBuffer);
        } else {
            charSet = _encodingComboBox.getSelectedItem();
        }

        final char[] sampleChars = readSampleBuffer(sampleBuffer, charSet);

        final int lineLength = StringUtils.indexOf('\n', sampleChars);
        if (_eolPresentCheckBox.isSelected() && lineLength == -1) {
            setStatusWarning("No newline in first " + sampleChars.length + " chars");
            // don't show the preview if no newlines were found (it may try to treat the whole file as a single row)
            showPreview = false;
        } else {
            final int[] valueWidths = getValueWidths(false);
            int totalMappedWidth = 0;
            for (final int valueWidth : valueWidths) {
                totalMappedWidth += valueWidth;
            }
            _lineWidthLabel.setText(lineLength + " chars in first line. " + totalMappedWidth + " mapped.");
            _lineWidthLabel.updateUI();
            showPreview = true;
            validateAndUpdate();
        }
    }

    @Override
    protected byte[] getSampleBuffer() {
        final Resource resource = getResource();
        final int bufferSize = getBufferSize();
        byte[] bytes = new byte[bufferSize];

        try (final InputStream fileInputStream = resource.read()) {
            final int startPosition = getStartPosition();
            fileInputStream.skip(startPosition);
            final int bytesRead = fileInputStream.read(bytes, 0, bufferSize);

            if (bytesRead != -1 && bytesRead <= bufferSize) {
                bytes = Arrays.copyOf(bytes, bytesRead);
            }

            return bytes;
        } catch (final IOException e) {
            logger.error("IOException occurred while reading sample buffer", e);
            return new byte[0];
        }
    }

    private int getStartPosition() {
        return _skipEbcdicHeaderCheckBox.isSelected() ? getRecordDataLength() : 0;
    }

    private int getBufferSize() {
        return _eolPresentCheckBox.isSelected() ? SAMPLE_BUFFER_SIZE : getRecordDataLength();
    }

    private int getRecordDataLength() {
        int length = 0;

        if (_valueWidthTextFields != null && _valueWidthTextFields.size() > 0) {
            for (final JXTextField textField : _valueWidthTextFields) {
                try {
                    final int columnWidth = Integer.parseInt(textField.getText());
                    length += columnWidth;
                } catch (final NumberFormatException e) {
                    throw new IllegalStateException("Value width must be a valid number.");
                }
            }
        }

        return length;
    }

    @Override
    protected FixedWidthDatastore getPreviewDatastore(final Resource resource) {
        return createDatastore("Preview", resource, false, _skipEbcdicHeaderCheckBox.isSelected(),
                _eolPresentCheckBox.isSelected());
    }

    @Override
    protected boolean isPreviewDataAvailable() {
        return showPreview;
    }

    @Override
    protected boolean isPreviewTableEnabled() {
        return true;
    }

    private JXTextField addValueWidthTextField() {
        return addValueWidthTextField(8);
    }

    private JXTextField addValueWidthTextField(final int valueWidth) {
        final JXTextField textField = WidgetFactory.createTextField();
        textField.setColumns(2);
        final NumberDocument document = new NumberDocument();
        document.addDocumentListener(_updatePreviewTableDocumentListener);
        textField.setDocument(document);
        textField.setText(valueWidth + "");
        _valueWidthTextFields.add(textField);
        _valueWidthsPanel.add(textField);
        if (_valueWidthTextFields.size() > 1) {
            _removeValueWidthButton.setEnabled(true);
        }
        _valueWidthsPanel.updateUI();
        onSettingsUpdated(false);
        return textField;
    }

    private JXTextField removeValueWidthTextField() {
        if (_valueWidthTextFields.isEmpty()) {
            return null;
        }
        final JXTextField textField = _valueWidthTextFields.get(_valueWidthTextFields.size() - 1);
        _valueWidthTextFields.remove(textField);
        _valueWidthsPanel.remove(textField);

        if (_valueWidthTextFields.size() == 1) {
            _removeValueWidthButton.setEnabled(false);
        } else {
            _removeValueWidthButton.setEnabled(true);
        }
        _valueWidthsPanel.updateUI();
        onSettingsUpdated(false);
        return textField;
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    protected List<Entry<String, JComponent>> getFormElements() {
        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
        buttonPanel.add(_addValueWidthButton);
        buttonPanel.add(_removeValueWidthButton);

        final DCPanel valueWidthConfigurationPanel = new DCPanel();
        valueWidthConfigurationPanel.setLayout(new BorderLayout());
        valueWidthConfigurationPanel.add(_valueWidthsPanel, BorderLayout.CENTER);
        valueWidthConfigurationPanel.add(buttonPanel, BorderLayout.EAST);
        valueWidthConfigurationPanel.add(_lineWidthLabel, BorderLayout.SOUTH);

        final List<Entry<String, JComponent>> result = super.getFormElements();
        result.add(new ImmutableEntry<>("Character encoding", _encodingComboBox));
        result.add(new ImmutableEntry<>("Column widths", valueWidthConfigurationPanel));
        result.add(new ImmutableEntry<>("Header line", _headerLineComboBox));
        result.add(new ImmutableEntry<>("", _failOnInconsistenciesCheckBox));
        result.add(new ImmutableEntry<>("", _skipEbcdicHeaderCheckBox));
        result.add(new ImmutableEntry<>("", _eolPresentCheckBox));
        // TODO: Uncomment the line about columns names panel after the release of metamodel 4.5.5
        //result.add(new ImmutableEntry<>("Column Names", _columnNamesWidget.getPanel()));
        return result;
    }

    @Override
    protected String getBannerTitle() {
        return "Fixed width file";
    }

    @Override
    public String getWindowTitle() {
        return "Fixed width file | Datastore";
    }

    @Override
    protected FixedWidthDatastore createDatastore(final String name, final Resource resource) {
        final boolean failOnInconsistencies = _failOnInconsistenciesCheckBox.isSelected();
        final boolean skipEbcdicHeader = _skipEbcdicHeaderCheckBox.isSelected();
        final boolean eolPresent = _eolPresentCheckBox.isSelected();

        return createDatastore(name, resource, failOnInconsistencies, skipEbcdicHeader, eolPresent);

    }

    private FixedWidthDatastore createDatastore(final String name, final Resource resource, final boolean failOnInconsistencies,
            final boolean skipEbcdicHeader, final boolean eolPresent) {
        final int[] valueWidths = getValueWidths(true);
        try {
            return new FixedWidthDatastore(name, resource, resource.getQualifiedPath(), _encodingComboBox
                    .getSelectedItem(), valueWidths, failOnInconsistencies, skipEbcdicHeader, eolPresent,
                    getHeaderLine(), _columnNamesWidget.getColumnNames());
        } catch (final NumberFormatException e) {
            throw new IllegalStateException("Value width must be a valid number.");
        }
    }

    private int[] getValueWidths(final boolean failOnMissingValue) {
        final int[] valueWidths = new int[_valueWidthTextFields.size()];

        try {
            for (int i = 0; i < valueWidths.length; i++) {
                String text = _valueWidthTextFields.get(i).getText();

                if (StringUtils.isNullOrEmpty(text)) {
                    if (failOnMissingValue) {
                        throw new IllegalStateException("Please fill out all column widths.");
                    } else {
                        text = "0";
                    }
                }

                valueWidths[i] = Integer.parseInt(text);
            }
        } catch (final NumberFormatException e) {
            throw new IllegalStateException("Please specify all column widths as numbers. ");
        }

        return valueWidths;
    }

    @Override
    protected String getDatastoreIconPath() {
        return IconUtils.FIXEDWIDTH_IMAGEPATH;
    }

    public int getHeaderLine() {
        final Number headerLineComboValue = _headerLineComboBox.getSelectedItem();
        if (headerLineComboValue != null) {
            final int intComboValue = headerLineComboValue.intValue();
            if (intComboValue < 0) {
                return FixedWidthConfiguration.NO_COLUMN_NAME_LINE;
            } else {
                // MetaModel's headerline number is 0-based
                return intComboValue;
            }
        } else {
            return FixedWidthConfiguration.DEFAULT_COLUMN_NAME_LINE;
        }
    }

    private void registerColumnNameFields() {
        _columnNamesWidget.getColumnNameFields().stream().filter(field -> !_columnNameFields.contains(field)).forEach(
                field -> {
                    field.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyTyped(final KeyEvent e) {
                            onSettingsUpdated(false);
                        }
                    });

                    _columnNameFields.add(field);
                });
    }

    @Override
    protected void initializeFileFilters(final ResourceSelector resourceSelector) {
        final FileFilter combinedFilter = FileFilters.combined("Any text, data or EBCDIC files (.txt, .dat, .ebc)",
                FileFilters.TXT, FileFilters.DAT, FileFilters.EBC);
        resourceSelector.addChoosableFileFilter(combinedFilter);
        resourceSelector.addChoosableFileFilter(FileFilters.TXT);
        resourceSelector.addChoosableFileFilter(FileFilters.DAT);
        resourceSelector.addChoosableFileFilter(FileFilters.EBC);
        resourceSelector.setSelectedFileFilter(combinedFilter);
        resourceSelector.addListener(new ResourceTypePresenter.Listener() {
            @Override
            public void onResourceSelected(final ResourceTypePresenter<?> presenter, final Resource resource) {
                onSettingsUpdated(true);
            }

            @Override
            public void onPathEntered(final ResourceTypePresenter<?> presenter, final String path) {
            }
        });
    }
}
