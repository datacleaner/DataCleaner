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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;

import org.apache.metamodel.util.HdfsResource;
import org.datacleaner.configuration.ServerInformation;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.server.HadoopClusterInformation;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.HadoopResource;
import org.datacleaner.util.WidgetFactory;
import org.jdesktop.swingx.JXTextField;

/**
 * {@link ResourceTypePresenter} for {@link HdfsResource}s.
 */
public class HdfsResourceTypePresenter implements ResourceTypePresenter<HdfsResource> {
    private final JXTextField _pathTextField;

    private final DCPanel _panel;
    private final List<ResourceTypePresenter.Listener> _listeners = new ArrayList<>(1);
    private final List<FileFilter> _fileFilters = new ArrayList<>();
    private final HadoopClusterInformation _defaultCluster;

    public HdfsResourceTypePresenter(final List<ServerInformation> servers) {
        _defaultCluster = getDefaultEnvironmentCluster(servers);

        _pathTextField = WidgetFactory.createTextField("path", 12);
        _pathTextField.setText("/");

        final DCDocumentListener documentListener = new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                onInputChanged();
            }
        };
        _pathTextField.getDocument().addDocumentListener(documentListener);

        _panel = DCPanel.flow(Alignment.LEFT, 2, 0, _pathTextField);
    }

    private HadoopClusterInformation getDefaultEnvironmentCluster(List<ServerInformation> serverInformationList) {
        for (ServerInformation serverInformation : serverInformationList) {
            if(serverInformation.getName().equals(HadoopResource.DEFAULT_CLUSTERREFERENCE)){
                return (HadoopClusterInformation) serverInformation;
            }
        }

        return null;
    }

    private void onInputChanged() {
        final HdfsResource resource = getResource();

        TextFieldResourceTypePresenter.handleResourceCandidate(resource, this, _listeners, _fileFilters);
    }

    @Override
    public JComponent getWidget() {
        return _panel;
    }

    @Override
    public HdfsResource getResource() {
        final String path = _pathTextField.getText();
        if (path.length() < 2) {
            return null;
        }

        return new HadoopResource(URI.create(path.replace(" ", "%20")), _defaultCluster);
    }

    @Override
    public void setResource(HdfsResource resource) {
        if (resource == null) {
            return;
        }

        final String qualifiedPath = resource.getQualifiedPath();
        _pathTextField.setText(URI.create(qualifiedPath.replace(" ", "%20")).getPath());
    }

    @Override
    public void addListener(ResourceTypePresenter.Listener listener) {
        _listeners.add(listener);
    }

    @Override
    public void removeListener(ResourceTypePresenter.Listener listener) {
        _listeners.remove(listener);
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
