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

import java.util.List;
import java.util.Map.Entry;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.eobjects.analyzer.connection.FixedWidthDatastore;
import org.eobjects.analyzer.util.ImmutableEntry;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.bootstrap.WindowManager;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.NumberDocument;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.CharSetEncodingComboBox;
import org.eobjects.datacleaner.widgets.FilenameTextField;

public class FixedWidthDatastoreDialog extends AbstractFileBasedDatastoreDialog<FixedWidthDatastore> {

	private static final long serialVersionUID = 1L;

	private final CharSetEncodingComboBox _encodingComboBox;
	private final JTextField _valueWidthTextField;
	private final JCheckBox _failOnInconsistenciesCheckBox;

	public FixedWidthDatastoreDialog(MutableDatastoreCatalog mutableDatastoreCatalog, WindowManager windowManager) {
		this(null, mutableDatastoreCatalog, windowManager);
	}

	public FixedWidthDatastoreDialog(FixedWidthDatastore originalDatastore, MutableDatastoreCatalog mutableDatastoreCatalog,
			WindowManager windowManager) {
		super(originalDatastore, mutableDatastoreCatalog, windowManager);

		_encodingComboBox = new CharSetEncodingComboBox();
		_valueWidthTextField = WidgetFactory.createTextField("No. characters");
		_valueWidthTextField.setDocument(new NumberDocument());

		_failOnInconsistenciesCheckBox = new JCheckBox("Fail on inconsistent line length", true);
		_failOnInconsistenciesCheckBox.setOpaque(false);
		_failOnInconsistenciesCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

		if (originalDatastore != null) {
			_encodingComboBox.setSelectedItem(originalDatastore.getEncoding());
			_valueWidthTextField.setText("" + originalDatastore.getFixedValueWidth());
			_failOnInconsistenciesCheckBox.setSelected(originalDatastore.isFailOnInconsistencies());
		} else {
			_valueWidthTextField.setText("10");
		}
	}

	@Override
	protected List<Entry<String, JComponent>> getFormElements() {
		List<Entry<String, JComponent>> result = super.getFormElements();
		result.add(new ImmutableEntry<String, JComponent>("Encoding", _encodingComboBox));
		result.add(new ImmutableEntry<String, JComponent>("Value width", _valueWidthTextField));
		result.add(new ImmutableEntry<String, JComponent>("", _failOnInconsistenciesCheckBox));
		return result;
	}

	@Override
	protected void setFileFilters(FilenameTextField filenameField) {
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
	protected String getFilename(FixedWidthDatastore datastore) {
		return datastore.getFilename();
	}

	@Override
	protected FixedWidthDatastore createDatastore(String name, String filename) {
		final String valueWidthText = _valueWidthTextField.getText();
		if (StringUtils.isNullOrEmpty(valueWidthText)) {
			throw new IllegalStateException("Please specify a value width.");
		}
		try {
			final int valueWidth = Integer.parseInt(valueWidthText);
			return new FixedWidthDatastore(name, filename, _encodingComboBox.getSelectedItem().toString(), valueWidth,
					_failOnInconsistenciesCheckBox.isSelected());
		} catch (NumberFormatException e) {
			throw new IllegalStateException("Value width must be a valid number.");
		}
	}

	@Override
	protected String getDatastoreIconPath() {
		return IconUtils.FIXEDWIDTH_IMAGEPATH;
	}
}
