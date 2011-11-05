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
package org.eobjects.datacleaner.output.beans;

import java.io.File;

import org.eobjects.analyzer.beans.writers.WriteDataResult;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.ExcelDatastore;
import org.eobjects.metamodel.schema.Table;

class ExcelOutputAnalyzerResult implements WriteDataResult {

	private static final long serialVersionUID = 1L;

	private final File _file;
	private final String _sheetName;
	private final int _rowCount;

	public ExcelOutputAnalyzerResult(File file, String sheetName, int rowCount) {
		_file = file;
		_sheetName = sheetName;
		_rowCount = rowCount;
	}

	@Override
	public int getWrittenRowCount() {
		return _rowCount;
	}

	@Override
	public Datastore getDatastore(DatastoreCatalog datastoreCatalog) {
		return new ExcelDatastore(_file.getName(), _file.getPath());
	}

	@Override
	public Table getPreviewTable(Datastore datastore) {
		DatastoreConnection con = datastore.openConnection();
		Table table = con.getDataContext().getDefaultSchema().getTableByName(_sheetName);
		con.close();
		return table;
	}

}
