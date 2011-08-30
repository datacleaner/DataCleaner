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
package org.eobjects.datacleaner.widgets.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;

import org.eobjects.analyzer.beans.api.FileProperty;
import org.eobjects.analyzer.beans.api.FileProperty.FileAccessMode;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.ExtensionFilter;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.widgets.FileSelectionListener;
import org.eobjects.datacleaner.widgets.FilenameTextField;

public final class SingleFilePropertyWidget extends AbstractPropertyWidget<File> {

	private static final long serialVersionUID = 1L;

	private final FilenameTextField _filenameField;
	private final UserPreferences _userPreferences;

	@Inject
	public SingleFilePropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder, UserPreferences userPreferences) {
		super(beanJobBuilder, propertyDescriptor);
		_userPreferences = userPreferences;

		boolean openFileDialog = true;
		String[] extensions = null;

		FileProperty fileProperty = propertyDescriptor.getAnnotation(FileProperty.class);
		if (fileProperty != null) {
			openFileDialog = fileProperty.accessMode() == FileAccessMode.OPEN;

			extensions = fileProperty.extension();
		}

		_filenameField = new FilenameTextField(_userPreferences.getConfiguredFileDirectory(), openFileDialog);

		if (extensions != null && extensions.length > 0) {
			List<FileFilter> filters = new ArrayList<FileFilter>(extensions.length);
			for (String extension : extensions) {
				FileFilter filter = new ExtensionFilter(extension.toUpperCase() + " file", "." + extension);
				filters.add(filter);
				_filenameField.addChoosableFileFilter(filter);
			}
			if (filters.size() == 1) {
				_filenameField.setSelectedFileFilter(filters.get(0));
			} else {
				FileFilter filter = FileFilters.combined("All suggested file formats",
						filters.toArray(new FileFilter[filters.size()]));
				_filenameField.setSelectedFileFilter(filter);
			}
		} else {
			_filenameField.setSelectedFileFilter(FileFilters.ALL);
		}

		File currentValue = (File) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		if (currentValue != null) {
			_filenameField.setFile(currentValue);
		}

		_filenameField.getTextField().getDocument().addDocumentListener(new DCDocumentListener() {

			@Override
			protected void onChange(DocumentEvent e) {
				fireValueChanged();
			}
		});

		_filenameField.addFileSelectionListener(new FileSelectionListener() {
			@Override
			public void onSelected(FilenameTextField filenameTextField, File file) {
				File dir = file.getParentFile();
				_userPreferences.setConfiguredFileDirectory(dir);
				fireValueChanged();
			}
		});

		add(_filenameField);
	}

	@Override
	public File getValue() {
		String text = _filenameField.getFilename();
		if (StringUtils.isNullOrEmpty(text)) {
			return null;
		}
		File file = new File(text);
		return file;
	}

	@Override
	protected void setValue(File value) {
		if (value == null) {
			_filenameField.setFilename("");
			return;
		}
		_filenameField.setFilename(value.getAbsolutePath());
	}
}
