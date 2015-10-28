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
package org.datacleaner.windows;

import javax.inject.Inject;

import org.apache.metamodel.util.Resource;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.JsonDatastore;
import org.datacleaner.guice.Nullable;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.IconUtils;
import org.datacleaner.widgets.ResourceSelector;

/**
 * Dialog for {@link JsonDatastore}s
 */
public final class JsonDatastoreDialog extends AbstractResourceBasedDatastoreDialog<JsonDatastore> {

    private static final long serialVersionUID = 1L;

    @Inject
    protected JsonDatastoreDialog(@Nullable JsonDatastore originalDatastore,
            MutableDatastoreCatalog mutableDatastoreCatalog, WindowContext windowContext,
            DataCleanerConfiguration configuration, UserPreferences userPreferences) {
        super(originalDatastore, mutableDatastoreCatalog, windowContext, configuration, userPreferences);
    }

    @Override
    protected String getBannerTitle() {
        return "JSON file";
    }

    @Override
    public String getWindowTitle() {
        return "JSON file | Datastore";
    }

    @Override
    protected String getDatastoreIconPath() {
        return IconUtils.JSON_IMAGEPATH;
    }

    @Override
    protected JsonDatastore createDatastore(String name, Resource resource) {
        return new JsonDatastore(name, resource);
    }

    @Override
    protected void initializeFileFilters(ResourceSelector resourceSelector) {
        resourceSelector.addChoosableFileFilter(FileFilters.JSON);
        resourceSelector.addChoosableFileFilter(FileFilters.ALL);
        resourceSelector.setSelectedFileFilter(FileFilters.JSON);
    }

    @Override
    protected boolean isPreviewTableEnabled() {
        return true;
    }

    @Override
    protected boolean isPreviewDataAvailable() {
        return true;
    }

}
