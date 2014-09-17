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
package org.eobjects.datacleaner.windows;

import javax.inject.Inject;

import org.eobjects.analyzer.connection.DbaseDatastore;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.Nullable;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.widgets.FilenameTextField;

public final class DbaseDatastoreDialog extends AbstractFileBasedDatastoreDialog<DbaseDatastore> {

	private static final long serialVersionUID = 1L;

	@Inject
	protected DbaseDatastoreDialog(@Nullable DbaseDatastore originalDatastore,
			MutableDatastoreCatalog mutableDatastoreCatalog, WindowContext windowContext, UserPreferences userPreferences) {
		super(originalDatastore, mutableDatastoreCatalog, windowContext, userPreferences);
	}

	@Override
	protected String getBannerTitle() {
		return "dBase database";
	}

	@Override
	public String getWindowTitle() {
		return "dBase database | Datastore";
	}

	@Override
	protected DbaseDatastore createDatastore(String name, String filename) {
		return new DbaseDatastore(name, filename);
	}

	@Override
	protected String getDatastoreIconPath() {
		return IconUtils.DBASE_IMAGEPATH;
	}

	@Override
	protected void setFileFilters(FilenameTextField filenameField) {
		filenameField.addChoosableFileFilter(FileFilters.DBF);
		filenameField.addChoosableFileFilter(FileFilters.ALL);
		filenameField.setSelectedFileFilter(FileFilters.DBF);
	}
}
