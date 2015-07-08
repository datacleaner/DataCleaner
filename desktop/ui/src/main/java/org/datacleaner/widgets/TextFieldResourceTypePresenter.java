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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;

import org.apache.metamodel.util.Resource;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.ExtensionFilter;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;
import org.jdesktop.swingx.JXTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * Generic {@link ResourceTypePresenter} that uses a simple text field to
 * represent any {@link Resource} as per it's {@link ResourceTypeHandler}'s
 * capability to create and parse paths.
 */
public class TextFieldResourceTypePresenter implements ResourceTypePresenter<Resource> {

    private static final Logger logger = LoggerFactory.getLogger(TextFieldResourceTypePresenter.class);

    private final ResourceTypeHandler<?> _resourceTypeHandler;
    private final JXTextField _pathTextField;
    private final List<Listener> _resourceSelectionListeners = new ArrayList<>();
    private final List<FileFilter> _fileFilters = new ArrayList<>();

    public TextFieldResourceTypePresenter(ResourceTypeHandler<?> resourceTypeHandler) {
        _resourceTypeHandler = resourceTypeHandler;
        _pathTextField = WidgetFactory.createTextField("Path");
        _pathTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                onPathChanged();
            }
        });
    }

    private void onPathChanged() {
        final String path = _pathTextField.getText();
        if (Strings.isNullOrEmpty(path)) {
            // do something?
            return;
        }

        final Resource resource = getResource();
        if (resource == null) {
            // TODO: do something?
            return;
        }

        if (!_fileFilters.isEmpty()) {
            final FileFilter selectedFileFilter = _fileFilters.get(0);
            if (selectedFileFilter instanceof ExtensionFilter) {
                final ExtensionFilter extensionFilter = (ExtensionFilter) selectedFileFilter;
                if (!extensionFilter.accept(resource)) {
                    // TODO: do something?
                }
            }
        }

        // do the remaining in a swing worker to avoid blocking the UI since it
        // will often time require I/O which may take time
        final SwingWorker<Resource, Void> worker = new SwingWorker<Resource, Void>() {
            @Override
            protected Resource doInBackground() throws Exception {
                if (!resource.isExists()) {
                    // TODO: do something?
                    return null;
                }

                return resource;
            }
            
            @Override
            protected void done() {
                final Resource resource;
                try {
                    resource = get();
                } catch (Exception e) {
                    // TODO: do something?
                    return;
                }
                
                if (resource == null) {
                    // TODO: do something
                    return;
                }
                
                for (Listener resourceSelectionListener : _resourceSelectionListeners) {
                    resourceSelectionListener.onResourceSelected(TextFieldResourceTypePresenter.this, resource);
                }
            }
        };
        worker.execute();
    }

    @Override
    public JComponent getWidget() {
        return _pathTextField;
    }

    @Override
    public Resource getResource() {
        final String path = _pathTextField.getText();
        try {
            return _resourceTypeHandler.parsePath(path);
        } catch (Exception e) {
            logger.debug("Failed to parse path '{}', returning null", path, e);
            return null;
        }
    }

    @Override
    public void setResource(Resource resource) {
        final String path = _resourceTypeHandler.createPath(resource);
        _pathTextField.setText(path);
    }

    @Override
    public void addListener(Listener listener) {
        _resourceSelectionListeners.add(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        _resourceSelectionListeners.remove(listener);
    }

    @Override
    public void addChoosableFileFilter(FileFilter fileFilter) {
        _fileFilters.add(fileFilter);
    }

    @Override
    public void removeChoosableFileFilter(FileFilter fileFilter) {
        _fileFilters.remove(fileFilter);
    }

    @Override
    public void setSelectedFileFilter(FileFilter fileFilter) {
        _fileFilters.remove(fileFilter);
        _fileFilters.add(0, fileFilter);
    }

}
