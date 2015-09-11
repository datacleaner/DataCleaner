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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.datacleaner.connection.FixedWidthDatastore;
import org.datacleaner.util.ImmutableEntry;
import org.datacleaner.util.StringUtils;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.guice.Nullable;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.NumberDocument;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.AbstractFilenameTextField;
import org.datacleaner.widgets.CharSetEncodingComboBox;
import org.datacleaner.widgets.DCComboBox.Listener;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.HeaderLineComboBox;
import org.apache.metamodel.fixedwidth.FixedWidthConfiguration;
import org.jdesktop.swingx.JXTextField;

public final class FixedWidthDatastoreDialog extends AbstractFileBasedDatastoreDialog<FixedWidthDatastore> {

	private static final long serialVersionUID = 1L;

	private final CharSetEncodingComboBox _encodingComboBox;
	private final JCheckBox _failOnInconsistenciesCheckBox;
	private final List<JXTextField> _valueWidthTextFields;
	private final DCPanel _valueWidthsPanel;
	private final DCLabel _lineWidthLabel;
	private final HeaderLineComboBox _headerLineComboBox;
	private final JButton _addValueWidthButton;
	private final JButton _removeValueWidthButton;
	private final DocumentListener _updatePreviewTableDocumentListener;

	private volatile boolean showPreview = true;

	@Inject
	protected FixedWidthDatastoreDialog(@Nullable FixedWidthDatastore originalDatastore,
			MutableDatastoreCatalog mutableDatastoreCatalog, WindowContext windowContext, UserPreferences userPreferences) {
		super(originalDatastore, mutableDatastoreCatalog, windowContext, userPreferences);
		_updatePreviewTableDocumentListener = new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent event) {
				onSettingsUpdated(false);
			}
		};
		_lineWidthLabel = DCLabel.bright("");
		_valueWidthsPanel = new DCPanel();
		_valueWidthsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
		_valueWidthTextFields = new ArrayList<JXTextField>();
		_encodingComboBox = new CharSetEncodingComboBox();
		_addValueWidthButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD);
		_removeValueWidthButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE);

		_headerLineComboBox = new HeaderLineComboBox();

		_failOnInconsistenciesCheckBox = new JCheckBox("Fail on inconsistent line length", true);
		_failOnInconsistenciesCheckBox.setOpaque(false);
		_failOnInconsistenciesCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

		if (originalDatastore != null) {
			_encodingComboBox.setSelectedItem(originalDatastore.getEncoding());
			_failOnInconsistenciesCheckBox.setSelected(originalDatastore.isFailOnInconsistencies());

			int[] valueWidths = originalDatastore.getValueWidths();
			for (int valueWidth : valueWidths) {
				addValueWidthTextField(valueWidth);
			}

			_headerLineComboBox.setSelectedIndex(originalDatastore.getHeaderLineNumber());

			onSettingsUpdated(false);
		} else {
			addValueWidthTextField();
			addValueWidthTextField();
			addValueWidthTextField();
		}

		_addValueWidthButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addValueWidthTextField();
			}
		});

		_removeValueWidthButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeValueWidthTextField();
			}
		});
		_encodingComboBox.addListener(new Listener<String>() {
			@Override
			public void onItemSelected(String item) {
				onSettingsUpdated(false);
			}
		});
		_headerLineComboBox.addListener(new Listener<Integer>() {
			@Override
			public void onItemSelected(Integer item) {
				onSettingsUpdated(false);
			}
		});
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
	protected void onFileSelected(File file) {
		onSettingsUpdated(true);
	}

	private void onSettingsUpdated(boolean autoDetectEncoding) {
		if (!validateForm()) {
			return;
		}

		byte[] sampleBuffer = getSampleBuffer();
		if (sampleBuffer == null || sampleBuffer.length == 0) {
			logger.debug("No bytes read to autodetect settings");
			return;
		}

		final String charSet;
		if (autoDetectEncoding) {
			charSet = _encodingComboBox.autoDetectEncoding(sampleBuffer);
		} else {
			charSet = _encodingComboBox.getSelectedItem().toString();
		}
		char[] sampleChars = readSampleBuffer(sampleBuffer, charSet);

		int lineLength = StringUtils.indexOf('\n', sampleChars);
		if (lineLength == -1) {
			setStatusWarning("No newline in first " + sampleChars.length + " chars");
			// don't show the preview if no newlines where found (it may try
			// to treat the whole file as a single row)
			showPreview = false;
		} else {
			int[] valueWidths = getValueWidths(false);
			int totalMappedWidth = 0;
			for (int valueWidth : valueWidths) {
				totalMappedWidth += valueWidth;
			}
			_lineWidthLabel.setText(lineLength + " chars in first line. " + totalMappedWidth + " mapped.");
			_lineWidthLabel.updateUI();
			showPreview = true;
			validateAndUpdate();
		}
	}

	@Override
	protected FixedWidthDatastore getPreviewDatastore(String filename) {
		return createDatastore("Preview", filename, false);
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

	private JXTextField addValueWidthTextField(int valueWidth) {
		JXTextField textField = WidgetFactory.createTextField();
		textField.setColumns(2);
		NumberDocument document = new NumberDocument();
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
		JXTextField textField = _valueWidthTextFields.get(_valueWidthTextFields.size() - 1);
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
		result.add(new ImmutableEntry<String, JComponent>("Character encoding", _encodingComboBox));
		result.add(new ImmutableEntry<String, JComponent>("Column widths", valueWidthConfigurationPanel));
		result.add(new ImmutableEntry<String, JComponent>("Header line", _headerLineComboBox));
		result.add(new ImmutableEntry<String, JComponent>("", _failOnInconsistenciesCheckBox));
		return result;
	}

	@Override
	protected void setFileFilters(AbstractFilenameTextField filenameField) {
		FileFilter combinedFilter = FileFilters.combined("Any text or data file (.txt, .dat)", FileFilters.TXT,
				FileFilters.DAT);
		filenameField.addChoosableFileFilter(combinedFilter);
		filenameField.addChoosableFileFilter(FileFilters.TXT);
		filenameField.addChoosableFileFilter(FileFilters.DAT);
		filenameField.setSelectedFileFilter(combinedFilter);
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
	protected FixedWidthDatastore createDatastore(String name, String filename) {
		boolean failOnInconsistencies = _failOnInconsistenciesCheckBox.isSelected();
		return createDatastore(name, filename, failOnInconsistencies);
	}

	private FixedWidthDatastore createDatastore(String name, String filename, boolean failOnInconsistencies) {
		int[] valueWidths = getValueWidths(true);
		try {
			return new FixedWidthDatastore(name, filename, _encodingComboBox.getSelectedItem().toString(), valueWidths,
					failOnInconsistencies, getHeaderLine());
		} catch (NumberFormatException e) {
			throw new IllegalStateException("Value width must be a valid number.");
		}
	}

	private int[] getValueWidths(boolean failOnMissingValue) {
		int[] valueWidths = new int[_valueWidthTextFields.size()];
		for (int i = 0; i < valueWidths.length; i++) {
			String text = _valueWidthTextFields.get(i).getText();
			if (StringUtils.isNullOrEmpty(text)) {
				if (failOnMissingValue) {
					throw new IllegalStateException("Please fill out all column widths");
				} else {
					text = "0";
				}
			}
			valueWidths[i] = Integer.parseInt(text);
		}
		return valueWidths;
	}

	@Override
	protected String getDatastoreIconPath() {
		return IconUtils.FIXEDWIDTH_IMAGEPATH;
	}

	public int getHeaderLine() {
		Number headerLineComboValue = _headerLineComboBox.getSelectedItem();
		if (headerLineComboValue != null) {
			int intComboValue = headerLineComboValue.intValue();
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
}
