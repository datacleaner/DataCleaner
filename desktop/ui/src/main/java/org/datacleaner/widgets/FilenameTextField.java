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
package org.datacleaner.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetUtils;

/**
 * A widget for selecting a file(name). It will be represented as a textfield
 * with a browse button.
 *
 * It is preferred to use this widget's "big brother" implementation,
 * {@link ResourceSelector} which will work with any type of {@link Resource},
 * not just files (e.g. {@link FileResource} and others).
 */
public final class FilenameTextField extends AbstractFilenameTextField<FileResource, FileSelectionListener> {

    private static final long serialVersionUID = 1L;

    private volatile File _directory;

    /**
     *
     * @param directory
     * @param fileOpenDialog
     *            true if browse dialog should be an "open file" dialog or false
     *            if it should be a "save file" dialog.
     */
    public FilenameTextField(File directory, final boolean fileOpenDialog) {
        super();
        _directory = directory;

        _browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final DCFileChooser fileChooser;
                if (_directory == null) {
                    fileChooser = new DCFileChooser();
                } else {
                    fileChooser = new DCFileChooser(_directory);
                }

                WidgetUtils.centerOnScreen(fileChooser);

                for (FileFilter filter : _choosableFileFilters) {
                    fileChooser.addChoosableFileFilter(filter);
                }

                fileChooser.setFileSelectionMode(_fileSelectionMode);

                if (_selectedFileFilter != null) {
                    if (!_choosableFileFilters.contains(_selectedFileFilter)) {
                        _choosableFileFilters.add(_selectedFileFilter);
                    }
                    fileChooser.setFileFilter(_selectedFileFilter);
                }

                int result;
                if (fileOpenDialog) {
                    result = fileChooser.showOpenDialog(FilenameTextField.this);
                } else {
                    result = fileChooser.showSaveDialog(FilenameTextField.this);
                }
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();

                    boolean accepted = true;
                    if (fileOpenDialog) {
                        accepted = file.exists();
                    }

                    if (accepted) {
                        setFile(file);
                        if (file.isDirectory()) {
                            _directory = file;
                        } else {
                            _directory = file.getParentFile();
                        }
                        notifyListeners();
                    }
                }
            }
        });
    }

    @Override
    protected void notifyListeners() {
        final File file = getFile();
        for (FileSelectionListener listener : _selectionListeners) {
            listener.onSelected(this, file);
        }
        final Resource fileResource = new FileResource(file);
        for (Listener listener : _resourceListeners) {
            listener.onResourceSelected(this, fileResource);
        }
    }

    @Override
    public FileResource getResource() {
        final File file = getFile();
        if (file == null) {
            return null;
        }
        return new FileResource(file);
    }

    @Override
    public void setResource(FileResource resource) {
        setFile(resource.getFile());
    }

    public File getDirectory() {
        return _directory;
    }

    public File getFile() {
        String filename = getFilename();
        if (StringUtils.isNullOrEmpty(filename)) {
            return null;
        }
        return new File(filename);
    }

    public void setFile(File file) {
        try {
            setFilename(file.getCanonicalPath());
        } catch (IOException e1) {
            // ignore
            setFilename(file.getAbsolutePath());
        }
    }

    @Override
    protected boolean isSelectionOkay() {
        return !StringUtils.isNullOrEmpty(getFilename());
    }
}
