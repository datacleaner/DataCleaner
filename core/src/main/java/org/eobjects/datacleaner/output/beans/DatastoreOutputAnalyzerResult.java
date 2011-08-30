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

import java.util.Arrays;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DatastoreOutputAnalyzerResult implements OutputAnalyzerResult {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DatastoreOutputAnalyzerResult.class);

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
		final DataContextProvider dcp = datastore.getDataContextProvider();
		final DataContext dc = dcp.getDataContext();

		// It is likely that schemas are cached, and since it is likely a new
		// table, we refresh the schema.
		dc.refreshSchemas();

		final Schema schema = dc.getDefaultSchema();
		final Table table = schema.getTableByName(_tableName);

		if (table == null && logger.isWarnEnabled()) {
			logger.warn("Could not find table '{}', even after refreshing schemas", _tableName);
			logger.warn("Available tables are: {}", Arrays.toString(schema.getTableNames()));
		}

		dcp.close();
		return table;
	}
}
