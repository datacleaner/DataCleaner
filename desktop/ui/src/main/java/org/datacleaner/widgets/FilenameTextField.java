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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXTextField;

/**
 * A widget for selecting a file(name). It will be represented as a textfield
 * with a browse button.
 */
public final class FilenameTextField extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final JXTextField _textField = WidgetFactory.createTextField("Filename");
    private final JButton _browseButton = WidgetFactory.createDefaultButton("Browse", IconUtils.ACTION_BROWSE);
    private final List<FileSelectionListener> _listeners = new ArrayList<FileSelectionListener>();
    private final List<FileFilter> _chooseableFileFilters = new ArrayList<FileFilter>();
    private volatile FileFilter _selectedFileFilter;
    private volatile File _directory;
    private int _fileSelectionMode = JFileChooser.FILES_ONLY;

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
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        add(_textField);
        add(Box.createHorizontalStrut(4));
        add(_browseButton);

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

                for (FileFilter filter : _chooseableFileFilters) {
                    fileChooser.addChoosableFileFilter(filter);
                }

                fileChooser.setFileSelectionMode(_fileSelectionMode);

                if (_selectedFileFilter != null) {
                    if (!_chooseableFileFilters.contains(_selectedFileFilter)) {
                        _chooseableFileFilters.add(_selectedFileFilter);
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
                        for (FileSelectionListener listener : _listeners) {
                            listener.onSelected(FilenameTextField.this, file);
                        }
                    }
                }
            }
        });
    }

    public File getDirectory() {
        return _directory;
    }

    public JButton getBrowseButton() {
        return _browseButton;
    }

    public JXTextField getTextField() {
        return _textField;
    }

    public String getFilename() {
        return _textField.getText();
    }

    public void setFilename(String filename) {
        _textField.setText(filename);
    }

    public void setFile(File file) {
        try {
            setFilename(file.getCanonicalPath());
        } catch (IOException e1) {
            // ignore
            setFilename(file.getAbsolutePath());
        }
    }

    public File getFile() {
        String filename = getFilename();
        if (StringUtils.isNullOrEmpty(filename)) {
            return null;
        }
        return new File(filename);
    }

    public void addFileSelectionListener(FileSelectionListener listener) {
        _listeners.add(listener);
    }

    public void removeFileSelectionListener(FileSelectionListener listener) {
        _listeners.remove(listener);
    }

    public void addChoosableFileFilter(FileFilter filter) {
        _chooseableFileFilters.add(filter);
    }

    public void setSelectedFileFilter(FileFilter filter) {
        _selectedFileFilter = filter;
    }

    public void setFileSelectionMode(int fileSelectionMode) {
        _fileSelectionMode = fileSelectionMode;
    }

    public int getFileSelectionMode() {
        return _fileSelectionMode;
    }
}
