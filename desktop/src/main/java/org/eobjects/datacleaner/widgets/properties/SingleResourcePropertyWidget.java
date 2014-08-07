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
import org.eobjects.analyzer.util.VfsResource;
import org.eobjects.analyzer.util.convert.ResourceConverter;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.ExtensionFilter;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.Alignment;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.FileSelectionListener;
import org.eobjects.datacleaner.widgets.FilenameTextField;
import org.apache.metamodel.util.ClasspathResource;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.apache.metamodel.util.UrlResource;
import org.jdesktop.swingx.JXTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Property widget for a single {@link Resource} field.
 */
public final class SingleResourcePropertyWidget extends AbstractPropertyWidget<Resource> {

    private static final Logger logger = LoggerFactory.getLogger(SingleResourcePropertyWidget.class);

    private final FilenameTextField _filenameField;
    private final UserPreferences _userPreferences;
    private final DCComboBox<String> _resourceTypeComboBox;
    private final JXTextField _otherPathTextField;

    // if the resource is of a type which is not catered for by the widget, then
    // this field will hold it's value and the widget will be read only.
    private final Resource _immutableValue;

    @Inject
    public SingleResourcePropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
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

        _resourceTypeComboBox = new DCComboBox<String>(new String[] { "file", "url", "classpath", "vfs" });
        _filenameField = new FilenameTextField(_userPreferences.getConfiguredFileDirectory(), openFileDialog);
        _otherPathTextField = WidgetFactory.createTextField();

        if (extensions != null && extensions.length > 0) {
            List<FileFilter> filters = new ArrayList<FileFilter>(extensions.length);
            for (String extension : extensions) {
                String extensionWithDot;
                if (extension.startsWith(".")) {
                    extensionWithDot = extension;
                } else {
                    extensionWithDot = "." + extension;
                }
                FileFilter filter = new ExtensionFilter(extension.toUpperCase() + " file", extensionWithDot);
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

        final Resource currentValue = getCurrentValue();
        if (currentValue == null) {
            _otherPathTextField.setVisible(false);
            _immutableValue = null;
        } else if (currentValue instanceof FileResource) {
            _otherPathTextField.setVisible(false);
            _filenameField.setFile(((FileResource) currentValue).getFile());
            _immutableValue = null;
        } else if (currentValue instanceof UrlResource || currentValue instanceof VfsResource
                || currentValue instanceof ClasspathResource) {
            _filenameField.setVisible(false);
            _immutableValue = null;
        } else {
            _filenameField.setVisible(false);
            _immutableValue = currentValue;
        }

        if (_immutableValue == null) {
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

            _otherPathTextField.getDocument().addDocumentListener(new DCDocumentListener() {
                @Override
                protected void onChange(DocumentEvent event) {
                    fireValueChanged();
                }
            });
            _resourceTypeComboBox.addListener(new Listener<String>() {
                @Override
                public void onItemSelected(String item) {
                    boolean isFileMode = "file".equals(item);
                    _filenameField.setVisible(isFileMode);
                    _otherPathTextField.setVisible(!isFileMode);

                    fireValueChanged();
                }
            });

            final DCPanel panel = DCPanel.flow(Alignment.LEFT, 0, 0, _resourceTypeComboBox, _filenameField,
                    _otherPathTextField);
            add(panel);
        } else {
            add(DCLabel.dark("- Resource: " + _immutableValue.getName() + " -"));
        }
    }

    /**
     * Gets the resource converter to use - can be overridden by subclasses if
     * needed.
     * 
     * @return
     */
    protected ResourceConverter getResourceConverter() {
        return new ResourceConverter();
    }

    @Override
    public boolean isSet() {
        return getValue() != null;
    }

    public FilenameTextField getFilenameField() {
        return _filenameField;
    }

    @Override
    public Resource getValue() {
        if (_immutableValue != null) {
            return _immutableValue;
        }

        String path;
        String resourceType = _resourceTypeComboBox.getSelectedItem();
        if ("file".equals(resourceType)) {
            path = _filenameField.getFilename();
        } else {
            path = _otherPathTextField.getText();
        }

        if (StringUtils.isNullOrEmpty(path)) {
            return null;
        }

        final String resourceString = resourceType + "://" + path;
        try {
            Resource resource = getResourceConverter().fromString(Resource.class, resourceString);
            return resource;
        } catch (Exception e) {
            // sometimes an exception can occur because the path is not valid (a
            // URL with a wrong pattern or so).
            if (logger.isDebugEnabled()) {
                logger.debug("Could not create resource from string: " + resourceString, e);
            }
            return null;
        }
    }

    @Override
    protected void setValue(Resource value) {
        if (value == null) {
            _filenameField.setFilename("");
            _otherPathTextField.setText("");
            return;
        }

        if (value instanceof FileResource) {
            _resourceTypeComboBox.setSelectedItem("file");
            File existingFile = _filenameField.getFile();
            File newFile = ((FileResource) value).getFile();
            if (existingFile != null && existingFile.getAbsoluteFile().equals(newFile.getAbsoluteFile())) {
                return;
            }
            _filenameField.setFile(newFile);
        } else if (value instanceof UrlResource) {
            _resourceTypeComboBox.setSelectedItem("url");
            final String url = ((UrlResource) value).getUri().toString();
            _otherPathTextField.setText(url);
        } else if (value instanceof ClasspathResource) {
            _resourceTypeComboBox.setSelectedItem("classpath");
            final String resourcePath = ((ClasspathResource) value).getResourcePath();
            _otherPathTextField.setText(resourcePath);
        } else if (value instanceof VfsResource) {
            _resourceTypeComboBox.setSelectedItem("vfs");
            final String path = ((VfsResource) value).getFileObject().getName().getURI();
            _otherPathTextField.setText(path);
        } else {
            throw new UnsupportedOperationException("Unsupported resource type: " + value);
        }
    }
}
