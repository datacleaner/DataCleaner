/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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

import javax.swing.filechooser.FileFilter;

import org.eobjects.analyzer.connection.AccessDatastore;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.widgets.FilenameTextField;

public class AccessDatastoreDialog extends AbstractFileBasedDatastoreDialog<AccessDatastore> {

	private static final long serialVersionUID = 1L;

	public AccessDatastoreDialog(AccessDatastore originalDatastore, MutableDatastoreCatalog mutableDatastoreCatalog) {
		super(originalDatastore, mutableDatastoreCatalog);
	}

	public AccessDatastoreDialog(MutableDatastoreCatalog mutableDatastoreCatalog) {
		super(mutableDatastoreCatalog);
	}

	@Override
	protected String getBannerTitle() {
		return "MS Access\ndatabase";
	}

	@Override
	public String getWindowTitle() {
		return "MS Access database | Datastore";
	}

	@Override
	protected String getFilename(AccessDatastore datastore) {
		return datastore.getFilename();
	}

	@Override
	protected AccessDatastore createDatastore(String name, String filename) {
		return new AccessDatastore(name, filename);
	}

	@Override
	protected String getDatastoreIconPath() {
		return IconUtils.ACCESS_IMAGEPATH;
	}

	@Override
	protected void setFileFilters(FilenameTextField filenameField) {
		FileFilter combinedFilter = FileFilters.combined("Any Access database (.mdb, .accdb)", FileFilters.MDB,
				FileFilters.ACCDB);
		filenameField.addChoosableFileFilter(FileFilters.MDB);
		filenameField.addChoosableFileFilter(FileFilters.ACCDB);
		filenameField.addChoosableFileFilter(FileFilters.ALL);
		filenameField.setSelectedFileFilter(combinedFilter);
	}
}
