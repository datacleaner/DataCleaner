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

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.metamodel.schema.Table;

class DatastoreOutputAnalyzerResult implements OutputAnalyzerResult {

	private static final long serialVersionUID = 1L;
	private final int _rowCount;
	private final String _datastoreName;
	private final String _tableName;

	public DatastoreOutputAnalyzerResult(int rowCount, String datastoreName, String tableName) {
		_rowCount = rowCount;
		_datastoreName = datastoreName;
		_tableName = tableName;
	}

	@Override
	public int getWrittenRowCount() {
		return _rowCount;
	}

	@Override
	public Datastore getDatastore(DatastoreCatalog datastoreCatalog) {
		return datastoreCatalog.getDatastore(_datastoreName);
	}

	@Override
	public Table getPreviewTable(Datastore datastore) {
		DataContextProvider dcp = datastore.getDataContextProvider();
		Table table = dcp.getDataContext().getDefaultSchema().getTableByName(_tableName);
		dcp.close();
		return table;
	}
}
