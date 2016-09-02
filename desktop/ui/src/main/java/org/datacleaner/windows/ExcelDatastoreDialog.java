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

import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;

import org.apache.metamodel.util.FileResource;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.guice.Nullable;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImmutableEntry;
import org.datacleaner.widgets.AbstractResourceTextField;
import org.datacleaner.widgets.CustomColumnNamesWidget;

public final class ExcelDatastoreDialog extends AbstractFileBasedDatastoreDialog<ExcelDatastore> {

	private static final long serialVersionUID = 1L;

    private final CustomColumnNamesWidget _columnNamesWidget;

	@Inject
	protected ExcelDatastoreDialog(@Nullable ExcelDatastore originalDatastore,
			MutableDatastoreCatalog mutableDatastoreCatalog, WindowContext windowContext, UserPreferences userPreferences) {
		super(originalDatastore, mutableDatastoreCatalog, windowContext, userPreferences);

        if (originalDatastore != null) {
            _columnNamesWidget = new CustomColumnNamesWidget(originalDatastore.getCustomColumnNames());
        } else {
            _columnNamesWidget = new CustomColumnNamesWidget(null);
        }
	}

    protected List<Entry<String, JComponent>> getFormElements() {
        List<Entry<String, JComponent>> res = super.getFormElements();
        res.add(new ImmutableEntry<>("Column Names", _columnNamesWidget.getPanel()));
        return res;
    }

	@Override
	protected void setFileFilters(AbstractResourceTextField<?> filenameField) {
		FileFilter combinedFilter = FileFilters.combined("Any Excel Spreadsheet (.xls, .xlsx)", FileFilters.XLS,
				FileFilters.XLSX);
		filenameField.addChoosableFileFilter(combinedFilter);
		filenameField.addChoosableFileFilter(FileFilters.XLS);
		filenameField.addChoosableFileFilter(FileFilters.XLSX);
		filenameField.addChoosableFileFilter(FileFilters.ALL);
		filenameField.setSelectedFileFilter(combinedFilter);
	}

	@Override
	protected String getBannerTitle() {
		return "MS Excel spreadsheet";
	}

	@Override
	public String getWindowTitle() {
		return "Excel spreadsheet | Datastore";
	}

	@Override
	protected ExcelDatastore createDatastore(String name, String filename) {
		return new ExcelDatastore(name, new FileResource(filename), filename, _columnNamesWidget.getColumnNames());
	}

	@Override
	protected String getDatastoreIconPath() {
		return IconUtils.EXCEL_IMAGEPATH;
	}
}
