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
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;

import org.apache.metamodel.util.Resource;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.jdesktop.swingx.JXTextField;


public abstract class AbstractResourceTextField<R extends Resource> extends DCPanel
        implements ResourceTypePresenter<R> {
    
    private static final long serialVersionUID = 1L;
    
    protected final JXTextField _textField = WidgetFactory.createTextField("Filename");
    protected final JButton _browseButton = WidgetFactory.createDefaultButton("Browse", IconUtils.ACTION_BROWSE);
    protected final List<Listener> _resourceListeners = new ArrayList<>();
    protected final List<FileFilter> _choosableFileFilters = new ArrayList<>();
    protected volatile FileFilter _selectedFileFilter;
    protected boolean _textFieldUpdating = false;
    protected int _fileSelectionMode = JFileChooser.FILES_ONLY;

    public AbstractResourceTextField() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        add(_textField);
        add(Box.createHorizontalStrut(4));
        add(_browseButton);

        _textField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                _textFieldUpdating = true;
                _textField.setToolTipText(_textField.getText().isEmpty() ? "Filename" : _textField.getText());
                try {
                    final String text = _textField.getText();
                    notifyListeners(text);
                } finally {
                    _textFieldUpdating = false;
                }
            }
        });

    }

    protected void notifyListeners(String text) {
        final Resource resource = getResource();
        for (Listener listener : _resourceListeners) {
            listener.onPathEntered(this, text);
            if(resource != null) {
                listener.onResourceSelected(this, resource);
            }
        }
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
        if (_textFieldUpdating) {
            // ignore this event - it's a call back from listeners that reacted
            // to a text field change.
            return;
        }
        _textField.setText(filename);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        _browseButton.setEnabled(enabled);
        _textField.setEnabled(enabled);
    }

    @Override
    public void addChoosableFileFilter(FileFilter filter) {
        _choosableFileFilters.add(filter);
    }

    @Override
    public void setSelectedFileFilter(FileFilter filter) {
        _selectedFileFilter = filter;
    }

    @Override
    public void removeChoosableFileFilter(FileFilter filter) {
        _choosableFileFilters.remove(filter);
    }

    public int getFileSelectionMode() {
        return _fileSelectionMode;
    }

    public void setFileSelectionMode(int fileSelectionMode) {
        _fileSelectionMode = fileSelectionMode;
    }

    @Override
    public JComponent getWidget() {
        return this;
    }

    @Override
    public void addListener(Listener listener) {
        _resourceListeners.add(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        _resourceListeners.remove(listener);
    }

    @Override
    public abstract R getResource();

    @Override
    public abstract void setResource(R resource);

}
