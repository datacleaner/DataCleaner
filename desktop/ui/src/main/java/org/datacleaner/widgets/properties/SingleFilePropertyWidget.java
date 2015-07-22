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
package org.datacleaner.widgets.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.swing.filechooser.FileFilter;

import org.datacleaner.api.FileProperty;
import org.datacleaner.api.FileProperty.FileAccessMode;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.ExtensionFilter;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.FileResolver;
import org.datacleaner.util.StringUtils;
import org.datacleaner.widgets.FileSelectionListener;
import org.datacleaner.widgets.FilenameTextField;

/**
 * Property widget for a single {@link File} field.
 */
public final class SingleFilePropertyWidget extends AbstractPropertyWidget<File> {

    private final FilenameTextField _filenameField;
    private final UserPreferences _userPreferences;
    private final FileResolver _fileResolver;
    private final FileAccessMode _accessMode;
    private final String[] _extensions;

    @Inject
    public SingleFilePropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor, ComponentBuilder componentBuilder,
            UserPreferences userPreferences) {
        super(componentBuilder, propertyDescriptor);
        _userPreferences = userPreferences;
        _fileResolver = new FileResolver(getAnalysisJobBuilder().getConfiguration());

        boolean openFileDialog = true;

        FileProperty fileProperty = propertyDescriptor.getAnnotation(FileProperty.class);
        if (fileProperty != null) {
            _accessMode = fileProperty.accessMode();
            _extensions = fileProperty.extension();

            openFileDialog = (_accessMode == FileAccessMode.OPEN);
        } else {
            _extensions = null;
            _accessMode = FileAccessMode.OPEN;
        }

        _filenameField = new FilenameTextField(_userPreferences.getConfiguredFileDirectory(), openFileDialog);

        if (_extensions != null && _extensions.length > 0) {
            List<FileFilter> filters = new ArrayList<FileFilter>(_extensions.length);
            for (String extension : _extensions) {
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

        File currentValue = getCurrentValue();
        if (currentValue != null) {
            _filenameField.setFile(currentValue);
        }

        _filenameField.addFileSelectionListener(new FileSelectionListener() {
            @Override
            public void onSelected(FilenameTextField filenameTextField, File file) {
                if (file != null) {
                    final File dir = file.getParentFile();
                    _userPreferences.setConfiguredFileDirectory(dir);
                }
                fireValueChanged();
            }
        });

        add(_filenameField);
    }

    @Override
    public boolean isSet() {
        return _filenameField.getFile() != null;
    }

    public FilenameTextField getFilenameField() {
        return _filenameField;
    }

    @Override
    public File getValue() {
        String filename = _filenameField.getFilename();
        if (StringUtils.isNullOrEmpty(filename)) {
            return null;
        }

        if (_accessMode == FileAccessMode.SAVE && _extensions != null && _extensions.length > 0) {
            if (filename.indexOf('.') == -1) {
                filename = filename + '.' + _extensions[0];
            }
        }

        return _fileResolver.toFile(filename);
    }

    @Override
    protected void setValue(File value) {
        if (value == null) {
            _filenameField.setFilename("");
            return;
        }

        File existingFile = _filenameField.getFile();
        if (existingFile != null && existingFile.getAbsoluteFile().equals(value.getAbsoluteFile())) {
            return;
        }

        final String filename = _fileResolver.toPath(value);
        _filenameField.setFilename(filename);
    }
}
