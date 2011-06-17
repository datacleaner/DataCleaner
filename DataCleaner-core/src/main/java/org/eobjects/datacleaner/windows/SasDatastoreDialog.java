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

import java.io.File;

import org.eobjects.analyzer.connection.SasDatastore;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.widgets.FilenameTextField;

public class SasDatastoreDialog extends AbstractFileBasedDatastoreDialog<SasDatastore> {

	private static final long serialVersionUID = 1L;

	public SasDatastoreDialog(MutableDatastoreCatalog mutableDatastoreCatalog, WindowContext windowContext) {
		super(mutableDatastoreCatalog, windowContext);
	}

	public SasDatastoreDialog(SasDatastore originalDatastore, MutableDatastoreCatalog mutableDatastoreCatalog,
			WindowContext windowContext) {
		super(originalDatastore, mutableDatastoreCatalog, windowContext);
	}

	@Override
	protected String getBannerTitle() {
		return "SAS library";
	}

	@Override
	public String getWindowTitle() {
		return "SAS library | Datastore";
	}

	@Override
	protected String getFilename(SasDatastore datastore) {
		return datastore.getFilename();
	}

	@Override
	protected SasDatastore createDatastore(String name, String filename) {
		return new SasDatastore(name, new File(filename));
	}

	@Override
	protected String getDatastoreIconPath() {
		return IconUtils.SAS_IMAGEPATH;
	}

	@Override
	protected void setFileFilters(FilenameTextField filenameField) {
	}
	
	@Override
	protected boolean isDirectoryBased() {
		return true;
	}
}
