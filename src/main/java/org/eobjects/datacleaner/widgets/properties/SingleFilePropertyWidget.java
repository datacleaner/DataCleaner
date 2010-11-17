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

import javax.swing.event.DocumentEvent;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.widgets.FileSelectionListener;
import org.eobjects.datacleaner.widgets.FilenameTextField;

public final class SingleFilePropertyWidget extends AbstractPropertyWidget<File> {

	private static final long serialVersionUID = 1L;

	private final FilenameTextField _filenameField;

	public SingleFilePropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(beanJobBuilder, propertyDescriptor);
		
		_filenameField = new FilenameTextField(UserPreferences.getInstance().getConfiguredFileDirectory());

		File currentValue = (File) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		if (currentValue != null) {
			_filenameField.setFilename(currentValue.getAbsolutePath());
		}

		_filenameField.getTextField().getDocument().addDocumentListener(new DCDocumentListener() {

			@Override
			protected void onChange(DocumentEvent e) {
				fireValueChanged();
			}
		});
		
		_filenameField.addChoosableFileFilter(FileFilters.ALL);
		_filenameField.setSelectedFileFilter(FileFilters.ALL);
		_filenameField.addFileSelectionListener(new FileSelectionListener() {
			@Override
			public void onSelected(FilenameTextField filenameTextField, File file) {
				File dir = file.getParentFile();
				UserPreferences.getInstance().setConfiguredFileDirectory(dir);
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
