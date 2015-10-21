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

import org.datacleaner.connection.OdbDatastore;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.guice.Nullable;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.IconUtils;
import org.datacleaner.widgets.AbstractResourceTextField;

public final class OdbDatastoreDialog extends AbstractFileBasedDatastoreDialog<OdbDatastore> {

	private static final long serialVersionUID = 1L;

	@Inject
	protected OdbDatastoreDialog(@Nullable OdbDatastore originalDatastore, MutableDatastoreCatalog mutableDatastoreCatalog,
			WindowContext windowContext, UserPreferences userPreferences) {
		super(originalDatastore, mutableDatastoreCatalog, windowContext, userPreferences);
	}

	@Override
	protected String getBannerTitle() {
		return "OpenOffice.org database";
	}

	@Override
	public String getWindowTitle() {
		return "OpenOffice.org database | Datastore";
	}

	@Override
	protected OdbDatastore createDatastore(String name, String filename) {
		return new OdbDatastore(name, filename);
	}

	@Override
	protected String getDatastoreIconPath() {
		return IconUtils.ODB_IMAGEPATH;
	}

	@Override
	protected void setFileFilters(AbstractResourceTextField<?> filenameField) {
		filenameField.addChoosableFileFilter(FileFilters.ODB);
		filenameField.addChoosableFileFilter(FileFilters.ALL);
		filenameField.setSelectedFileFilter(FileFilters.ODB);
	}
}
