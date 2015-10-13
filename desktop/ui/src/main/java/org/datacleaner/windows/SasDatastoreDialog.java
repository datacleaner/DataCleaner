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

import java.io.File;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.swing.JComponent;

import org.datacleaner.connection.SasDatastore;
import org.datacleaner.util.ImmutableEntry;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.guice.Nullable;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.widgets.AbstractResourceTextField;
import org.datacleaner.widgets.DCLabel;
import org.eobjects.metamodel.sas.SasFilenameFilter;

public final class SasDatastoreDialog extends AbstractFileBasedDatastoreDialog<SasDatastore> {

	private static final long serialVersionUID = 1L;

	private final DCLabel _tableCountLabel;

	@Inject
	protected SasDatastoreDialog(@Nullable SasDatastore originalDatastore, MutableDatastoreCatalog mutableDatastoreCatalog,
			WindowContext windowContext, UserPreferences userPreferences) {
		super(originalDatastore, mutableDatastoreCatalog, windowContext, userPreferences);
		_tableCountLabel = DCLabel.bright("Please choose the directory to infer the tables");

		if (originalDatastore != null) {
			onFileSelected(new File(originalDatastore.getFilename()));
		}
	}

	@Override
	protected void onFileSelected(File file) {
		if (file.exists() && file.isDirectory()) {
			String[] files = file.list(new SasFilenameFilter());
			_tableCountLabel.setText("Directory contains " + files.length + " SAS table(s).");
			if (files.length == 0) {
				setStatusWarning("No SAS tables in directory");
			} else {
				setStatusValid();
			}
		} else {
			setStatusWarning("Please select a valid directory");
		}
	}

	@Override
	protected List<Entry<String, JComponent>> getFormElements() {
		List<Entry<String, JComponent>> result = super.getFormElements();
		result.add(new ImmutableEntry<String, JComponent>("Tables", _tableCountLabel));
		return result;
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
	protected SasDatastore createDatastore(String name, String filename) {
		return new SasDatastore(name, new File(filename));
	}

	@Override
	protected String getDatastoreIconPath() {
		return IconUtils.SAS_IMAGEPATH;
	}

	@Override
	protected void setFileFilters(AbstractResourceTextField<?> filenameField) {
	}

	@Override
	protected boolean isDirectoryBased() {
		return true;
	}
}
