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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;

import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.ServerInformationCatalog;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.convert.ResourceConverter;
import org.datacleaner.util.convert.ResourceConverter.ResourceStructure;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;

/**
 * A widget which allows the user to select/enter a {@link Resource} to use for
 * some file-like operation.
 */
public class ResourceSelector extends DCPanel implements ResourceTypePresenter<Resource> {
    private static final long serialVersionUID = 1L;

    private final DCComboBox<String> _resourceTypeComboBox;
    private final ResourceConverter _resourceConverter;
    private final UserPreferences _userPreferences;
    private final Map<String, ResourceTypePresenter<?>> _resourceTypePresenters;
    private boolean _openMode;
    private ResourceTypePresenter<?> _currentPresenter;
    private ServerInformationCatalog _serverInformationCatalog;

    public ResourceSelector(final DataCleanerConfiguration configuration, final UserPreferences userPreferences,
            final boolean openMode) {
        this(configuration, new ResourceConverter(configuration), userPreferences, openMode);
    }

    public ResourceSelector(final DataCleanerConfiguration configuration, final ResourceConverter resourceConverter,
            final UserPreferences userPreferences, final boolean openMode) {
        _resourceConverter = resourceConverter;
        _userPreferences = userPreferences;
        _openMode = openMode;
        _resourceTypePresenters = new HashMap<>();
        _serverInformationCatalog = configuration.getServerInformationCatalog();

        final List<String> schemes = new ArrayList<>();
        final Collection<ResourceTypeHandler<?>> resourceTypeHandlers = _resourceConverter.getResourceTypeHandlers();
        for (final ResourceTypeHandler<?> resourceTypeHandler : resourceTypeHandlers) {
            final String scheme = resourceTypeHandler.getScheme();
            schemes.add(scheme);

            final ResourceTypePresenter<?> presenter = createResourceTypePresenter(scheme, resourceTypeHandler);
            _resourceTypePresenters.put(scheme, presenter);
        }

        _resourceTypeComboBox = new DCComboBox<>(schemes);
        _resourceTypeComboBox.addListener(this::onSchemeSelected);

        setScheme("file");

        setLayout(new FlowLayout(Alignment.LEFT.getFlowLayoutAlignment(), 0, 0));
        add(_resourceTypeComboBox);
        add(Box.createHorizontalStrut(4));
        for (final ResourceTypePresenter<?> presenter : getResourceTypePresenters()) {
            add(presenter.getWidget());
        }
    }

    protected ResourceTypePresenter<?> createResourceTypePresenter(final String scheme,
            final ResourceTypeHandler<?> resourceTypeHandler) {
        final ResourceTypePresenter<?> presenter;
        switch (scheme) {
        case "file":
            presenter = new FilenameTextField(_userPreferences.getConfiguredFileDirectory(), _openMode);
            break;
        case "hdfs":
            presenter = new HdfsResourceTypePresenter(_serverInformationCatalog);
            break;
        default:
            presenter = new TextFieldResourceTypePresenter(resourceTypeHandler);
            break;
        }
        return presenter;
    }

    public void setScheme(final String scheme) {
        _resourceTypeComboBox.setSelectedItem(scheme);
    }

    private void onSchemeSelected(final String item) {
        final Collection<ResourceTypePresenter<?>> presenters = _resourceTypePresenters.values();
        for (final ResourceTypePresenter<?> presenter : presenters) {
            presenter.getWidget().setVisible(false);
        }
        final ResourceTypePresenter<?> presenter = _resourceTypePresenters.get(item);
        presenter.getWidget().setVisible(true);
        updateUI();
        _currentPresenter = presenter;
    }

    public Resource getResource() {
        if (_currentPresenter == null) {
            return null;
        }
        return _currentPresenter.getResource();
    }

    public void setResource(final Resource resource) {
        final String stringRepresentation = _resourceConverter.toString(resource);
        final ResourceStructure structure = _resourceConverter.parseStructure(stringRepresentation);
        final String scheme = structure.getScheme();
        setScheme(scheme);

        // we need to do this ugly cast in order to call setResource(...) with a
        // generic argument
        @SuppressWarnings("unchecked") final ResourceTypePresenter<Resource> presenter =
                (ResourceTypePresenter<Resource>) _currentPresenter;
        presenter.setResource(resource);
    }

    public ResourceTypePresenter<?> getResourceTypePresenter(final String scheme) {
        return _resourceTypePresenters.get(scheme);
    }

    public Collection<ResourceTypePresenter<?>> getResourceTypePresenters() {
        return _resourceTypePresenters.values();
    }

    @Override
    public JComponent getWidget() {
        return this;
    }

    @Override
    public void addListener(final Listener listener) {
        final Collection<ResourceTypePresenter<?>> presenters = getResourceTypePresenters();
        for (final ResourceTypePresenter<?> presenter : presenters) {
            presenter.addListener(listener);
        }
    }

    @Override
    public void removeListener(final Listener listener) {
        final Collection<ResourceTypePresenter<?>> presenters = getResourceTypePresenters();
        for (final ResourceTypePresenter<?> presenter : presenters) {
            presenter.removeListener(listener);
        }
    }

    @Override
    public void addChoosableFileFilter(final FileFilter fileFilter) {
        final Collection<ResourceTypePresenter<?>> presenters = getResourceTypePresenters();
        for (final ResourceTypePresenter<?> presenter : presenters) {
            presenter.addChoosableFileFilter(fileFilter);
        }
    }

    @Override
    public void removeChoosableFileFilter(final FileFilter fileFilter) {
        final Collection<ResourceTypePresenter<?>> presenters = getResourceTypePresenters();
        for (final ResourceTypePresenter<?> presenter : presenters) {
            presenter.removeChoosableFileFilter(fileFilter);
        }
    }

    @Override
    public void setSelectedFileFilter(final FileFilter fileFilter) {
        final Collection<ResourceTypePresenter<?>> presenters = getResourceTypePresenters();
        for (final ResourceTypePresenter<?> presenter : presenters) {
            presenter.setSelectedFileFilter(fileFilter);
        }
    }

    public String getResourcePath() {
        final Resource resource = getResource();
        if (resource == null) {
            return null;
        }
        return _resourceConverter.toString(resource);
    }

    public void setResourcePath(final String path) {
        final Resource resource = _resourceConverter.fromString(Resource.class, path);
        if (resource != null) {
            setResource(resource);
        }
    }

    public void setCurrentPresenter(final ResourceTypePresenter<?> currentPresenter) {
        _currentPresenter = currentPresenter;
    }

    public void setVisibleResourceTypeCombox(final boolean isVisible) {
        _resourceTypeComboBox.setVisible(isVisible);
    }

}
