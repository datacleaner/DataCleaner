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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.metamodel.fixedwidth.FixedWidthConfiguration;
import org.apache.metamodel.util.AlphabeticSequence;
import org.apache.metamodel.util.Resource;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.FixedWidthDatastore;
import org.datacleaner.guice.Nullable;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImmutableEntry;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.CharSetEncodingComboBox;
import org.datacleaner.widgets.HeaderLineComboBox;
import org.datacleaner.widgets.ResourceSelector;
import org.datacleaner.widgets.ResourceTypePresenter;
import org.datacleaner.widgets.table.DCEditableTable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

public final class FixedWidthDatastoreDialog extends AbstractResourceBasedDatastoreDialog<FixedWidthDatastore> {

    private static final long serialVersionUID = 1L;

    private final CharSetEncodingComboBox _encodingComboBox;
    private final JCheckBox _failOnInconsistenciesCheckBox;
    private final JCheckBox _skipEbcdicHeaderCheckBox;
    private final JCheckBox _eolPresentCheckBox;
    private final DCEditableTable _columnsTable;
    private final HeaderLineComboBox _headerLineComboBox;
    private volatile boolean showPreview = true;

    @Inject
    protected FixedWidthDatastoreDialog(@Nullable final FixedWidthDatastore originalDatastore,
            final MutableDatastoreCatalog mutableDatastoreCatalog, final WindowContext windowContext,
            final DataCleanerConfiguration configuration, final UserPreferences userPreferences) {
        super(originalDatastore, mutableDatastoreCatalog, windowContext, configuration, userPreferences);
        _columnsTable =
                new DCEditableTable(new String[] { "Name", "Width" }, new Class[] { String.class, Integer.class });

        _encodingComboBox = new CharSetEncodingComboBox();

        _headerLineComboBox = new HeaderLineComboBox(false); // usually fixed width files don't have headers

        _failOnInconsistenciesCheckBox = createCheckBox("Fail on inconsistent line length", true);
        _skipEbcdicHeaderCheckBox = createCheckBox("Input file contains a header that should be skipped", false);
        _eolPresentCheckBox = createCheckBox("Input file contains new line characters", true);

        if (originalDatastore != null) {
            _encodingComboBox.setSelectedItem(originalDatastore.getEncoding());
            _failOnInconsistenciesCheckBox.setSelected(originalDatastore.isFailOnInconsistencies());
            _skipEbcdicHeaderCheckBox.setSelected(originalDatastore.isSkipEbcdicHeader());
            _eolPresentCheckBox.setSelected(originalDatastore.isEolPresent());

            final List<String> customColumnNames = originalDatastore.getCustomColumnNames();
            final int[] valueWidths = originalDatastore.getValueWidths();

            final DefaultTableModel model = (DefaultTableModel) _columnsTable.getModel();
            model.setRowCount(Math.max(valueWidths.length, customColumnNames.size()));

            for (int rowIndex = 0; rowIndex < customColumnNames.size(); rowIndex++) {
                final String columnName = customColumnNames.get(rowIndex);
                _columnsTable.setValueAt(columnName, rowIndex, 0);
            }

            for (int rowIndex = 0; rowIndex < valueWidths.length; rowIndex++) {
                final int valueWidth = valueWidths[rowIndex];
                _columnsTable.setValueAt(valueWidth, rowIndex, 1);
            }

            _headerLineComboBox.setSelectedIndex(originalDatastore.getHeaderLineNumber());

            onSettingsUpdated(false);
        }

        _encodingComboBox.addListener(item -> onSettingsUpdated(false));
        _headerLineComboBox.addListener(item -> onSettingsUpdated(false));
        _columnsTable.getModel().addTableModelListener(event -> onSettingsUpdated(false));
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
            showPreview = true;
            validateAndUpdate();
        }
    }

    @Override
    protected byte[] getSampleBuffer() {
        final Resource resource = getResource();
        final int bufferSize = getBufferSize();
        byte[] bytes = new byte[bufferSize];

        try (InputStream fileInputStream = resource.read()) {
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

        final int[] valueWidths = getValueWidths(false);
        for (int width : valueWidths) {
            length += width;
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

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    protected List<Entry<String, JComponent>> getFormElements() {
        final List<Entry<String, JComponent>> result = super.getFormElements();
        result.add(new ImmutableEntry<>("Character encoding", _encodingComboBox));
        result.add(new ImmutableEntry<>("Columns", _columnsTable.toPanel(false)));
        result.add(new ImmutableEntry<>("Header line", _headerLineComboBox));
        result.add(new ImmutableEntry<>("", _failOnInconsistenciesCheckBox));
        result.add(new ImmutableEntry<>("", _skipEbcdicHeaderCheckBox));
        result.add(new ImmutableEntry<>("", _eolPresentCheckBox));
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

    private FixedWidthDatastore createDatastore(final String name, final Resource resource,
            final boolean failOnInconsistencies, final boolean skipEbcdicHeader, final boolean eolPresent) {
        final int[] valueWidths = getValueWidths(failOnInconsistencies);
        return new FixedWidthDatastore(name, resource, resource.getQualifiedPath(), _encodingComboBox.getSelectedItem(),
                valueWidths, failOnInconsistencies, skipEbcdicHeader, eolPresent, getHeaderLine(), getColumnNames());
    }

    private List<String> getColumnNames() {
        final int rowCount = _columnsTable.getRowCount();
        final List<String> list = new ArrayList<>(rowCount);
        final AlphabeticSequence sequence = new AlphabeticSequence();
        for (int i = 0; i < rowCount; i++) {
            final String nameStr = (String) _columnsTable.getValueAt(i, 0);
            list.add(Objects.firstNonNull(Strings.emptyToNull(nameStr), sequence.next()));
        }
        return list;
    }

    private int[] getValueWidths(final boolean failOnMissingValue) {
        final int rowCount = _columnsTable.getRowCount();
        final int[] valueWidths = new int[rowCount];

        try {
            for (int i = 0; i < valueWidths.length; i++) {
                final Object value = _columnsTable.getValueAt(i, 1);
                final Number number = ConvertToNumberTransformer.transformValue(value);

                if (number == null) {
                    if (failOnMissingValue) {
                        throw new IllegalStateException("Please fill out all column widths.");
                    } else {
                        valueWidths[i] = 1; // Use 1 as the default, although no default is very sensible
                    }
                } else {
                    valueWidths[i] = number.intValue();
                }
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
