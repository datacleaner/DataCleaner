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

import org.eobjects.analyzer.connection.OdbDatastore;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.widgets.FilenameTextField;

public class OdbDatastoreDialog extends AbstractFileBasedDatastoreDialog<OdbDatastore> {

	private static final long serialVersionUID = 1L;

	public OdbDatastoreDialog(OdbDatastore originalDatastore, MutableDatastoreCatalog mutableDatastoreCatalog) {
		super(originalDatastore, mutableDatastoreCatalog);
	}

	public OdbDatastoreDialog(MutableDatastoreCatalog mutableDatastoreCatalog) {
		super(mutableDatastoreCatalog);
	}

	@Override
	protected String getBannerTitle() {
		return "OpenOffice.org\ndatabase";
	}

	@Override
	protected String getWindowTitle() {
		return "OpenOffice.org database | Datastore";
	}

	@Override
	protected String getFilename(OdbDatastore datastore) {
		return datastore.getFilename();
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
	protected void setFileFilters(FilenameTextField filenameField) {
		filenameField.addChoosableFileFilter(FileFilters.ODB);
		filenameField.addChoosableFileFilter(FileFilters.ALL);
		filenameField.setSelectedFileFilter(FileFilters.ODB);
	}
}
